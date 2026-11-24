package com.example.yungasdistribuidora.data.remote.realtime

import android.util.Log
import com.example.yungasdistribuidora.data.remote.api.SalesApi
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseRealtimeManager(
    private val salesApi: SalesApi,
    private val onRefreshTriggered: () -> Unit
) {
    companion object {
        private const val TAG = "SupabaseRealtime"
        private const val SUPABASE_WS_URL = "wss://yungasdistribuidora.supabase.co/realtime/v1/websocket"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRunning = false
    private var heartbeatJob: Job? = null
    private var tokenRefreshJob: Job? = null
    private var reconnectJob: Job? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        scope.launch {
            while (isRunning) {
                try {
                    val response = salesApi.getRealtimeAccess()
                    if (response.isSuccessful && response.body() != null) {
                        val access = response.body()!!
                        val token = access.resolvedToken ?: ""
                        val rUrl = access.resolvedUrl
                        val wsUrl = if (!rUrl.isNullOrBlank() && !rUrl.contains("api.yungasdistribuidora.cc")) {
                            rUrl
                        } else {
                            "$SUPABASE_WS_URL?apikey=$SUPABASE_ANON_KEY&vsn=1.0.0"
                        }

                        connectWebSocket(wsUrl, token)
                        
                        val expiresIn = access.expiresInSeconds ?: 300L
                        scheduleTokenRefresh((expiresIn - 60).coerceAtLeast(60L) * 1000L)
                        break
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get realtime access", e)
                }
                delay(10_000)
            }
        }
    }

    private fun connectWebSocket(url: String, token: String) {
        webSocket?.close(1000, "Reconnecting")
        webSocket = null

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket opened")
                startHeartbeat(webSocket)

                val subDelivery = JSONObject().apply {
                    put("topic", "realtime:yungas:delivery")
                    put("event", "phx_join")
                    put("payload", JSONObject().put("access_token", token))
                    put("ref", "1")
                }
                val subInventory = JSONObject().apply {
                    put("topic", "realtime:yungas:inventory")
                    put("event", "phx_join")
                    put("payload", JSONObject().put("access_token", token))
                    put("ref", "2")
                }
                webSocket.send(subDelivery.toString())
                webSocket.send(subInventory.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                try {
                    val json = JSONObject(text)
                    val topic = json.optString("topic")
                    val event = json.optString("event")
                    if (topic.contains("yungas:delivery") || topic.contains("yungas:inventory") || event == "postgres_changes" || event == "broadcast" || event == "changed") {
                        onRefreshTriggered()
                    }
                } catch (e: Exception) {
                    onRefreshTriggered()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (this@SupabaseRealtimeManager.webSocket !== webSocket) return
                Log.e(TAG, "WebSocket failure", t)
                stopHeartbeat()
                reconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (this@SupabaseRealtimeManager.webSocket !== webSocket) return
                Log.d(TAG, "WebSocket closed: $reason")
                stopHeartbeat()
                reconnect()
            }
        })
    }

    private fun startHeartbeat(ws: WebSocket) {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isRunning) {
                delay(30_000)
                try {
                    val hb = JSONObject().apply {
                        put("topic", "phoenix")
                        put("event", "heartbeat")
                        put("payload", JSONObject())
                        put("ref", "hb_${System.currentTimeMillis()}")
                    }
                    ws.send(hb.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending heartbeat", e)
                    break
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun scheduleTokenRefresh(delayMillis: Long) {
        tokenRefreshJob?.cancel()
        tokenRefreshJob = scope.launch {
            delay(delayMillis)
            if (isRunning) {
                Log.d(TAG, "Refreshing realtime token...")
                try {
                    val response = salesApi.getRealtimeAccess()
                    if (response.isSuccessful && response.body() != null) {
                        val access = response.body()!!
                        val newToken = access.resolvedToken ?: ""
                        if (newToken.isNotBlank()) {
                            val ws = webSocket
                            if (ws != null) {
                                listOf("realtime:yungas:delivery", "realtime:yungas:inventory")
                                    .forEachIndexed { index, topic ->
                                        val refreshMsg = JSONObject().apply {
                                            put("topic", topic)
                                            put("event", "access_token")
                                            put("payload", JSONObject().put("access_token", newToken))
                                            put("ref", "refresh_${System.currentTimeMillis()}_$index")
                                        }
                                        ws.send(refreshMsg.toString())
                                    }
                            }
                        }
                        if (newToken.isNotBlank() && isRunning) {
                            val nextDelay = ((access.expiresInSeconds ?: 300L) - 60L).coerceAtLeast(60L) * 1000L
                            scheduleTokenRefresh(nextDelay)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh token", e)
                    if (isRunning) scheduleTokenRefresh(60_000L)
                }
            }
        }
    }

    private fun reconnect() {
        if (!isRunning || reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(5_000)
            while (isRunning) {
                try {
                    val response = salesApi.getRealtimeAccess()
                    val access = response.body()
                    if (response.isSuccessful && access != null) {
                        val token = access.resolvedToken.orEmpty()
                        val url = access.resolvedUrl
                        val wsUrl = if (!url.isNullOrBlank() && !url.contains("api.yungasdistribuidora.cc")) {
                            url
                        } else {
                            "$SUPABASE_WS_URL?apikey=$SUPABASE_ANON_KEY&vsn=1.0.0"
                        }
                        connectWebSocket(wsUrl, token)
                        scheduleTokenRefresh(((access.expiresInSeconds ?: 300L) - 60L).coerceAtLeast(60L) * 1000L)
                        return@launch
                    }
                    Log.w(TAG, "Realtime reconnect access unavailable: HTTP ${response.code()}")
                } catch (e: Exception) {
                    Log.e(TAG, "Realtime reconnect failed", e)
                }
                delay(10_000)
            }
        }
    }

    fun stop() {
        isRunning = false
        stopHeartbeat()
        reconnectJob?.cancel()
        reconnectJob = null
        tokenRefreshJob?.cancel()
        tokenRefreshJob = null
        webSocket?.close(1000, "Client stopped")
        webSocket = null
        Log.d(TAG, "SupabaseRealtimeManager stopped")
    }
}
