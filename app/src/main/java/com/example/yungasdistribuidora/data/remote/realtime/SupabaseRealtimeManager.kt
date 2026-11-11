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
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        scope.launch {
            while (isRunning) {
                try {
                    val response = salesApi.getRealtimeAccess()
                    if (response.isSuccessful && response.body() != null) {
                        val access = response.body()!!
                        val wsUrl = access.resolvedUrl ?: "wss://api.yungasdistribuidora.cc/realtime/v1/websocket"
                        val token = access.resolvedToken ?: access.resolvedApiKey ?: ""

                        connectWebSocket(wsUrl, token)
                        break
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseRealtime", "Failed to get realtime access", e)
                }
                delay(10_000)
            }
        }
    }

    private fun connectWebSocket(url: String, token: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("SupabaseRealtime", "WebSocket opened")
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
                Log.d("SupabaseRealtime", "Received message: $text")
                try {
                    val json = JSONObject(text)
                    val topic = json.optString("topic")
                    val event = json.optString("event")
                    if (topic.contains("yungas:delivery") || topic.contains("yungas:inventory") || event == "postgres_changes" || event == "broadcast") {
                        onRefreshTriggered()
                    }
                } catch (e: Exception) {
                    onRefreshTriggered()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("SupabaseRealtime", "WebSocket failure", t)
                reconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("SupabaseRealtime", "WebSocket closed: $reason")
                reconnect()
            }
        })
    }

    private fun reconnect() {
        if (!isRunning) return
        scope.launch {
            delay(5_000)
            start()
        }
    }

    fun stop() {
        isRunning = false
        webSocket?.close(1000, "Client stopped")
        webSocket = null
    }
}
