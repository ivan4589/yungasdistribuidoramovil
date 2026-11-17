package com.example.yungasdistribuidora.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeliveryResponseDto(
    @SerializedName("saleId") val saleId: String?,
    @SerializedName("saleNumber") val saleNumber: String?,
    @SerializedName("saleStatus") val saleStatus: String?,
    @SerializedName("deliveryMode") val deliveryMode: Boolean?,
    @SerializedName("revision") val revision: Int?,
    @SerializedName("complete") val complete: Boolean?,
    @SerializedName("products") val products: List<DeliveryProductDto>?,
    @SerializedName("events") val events: List<DeliveryEventDto>?
)

data class DeliveryProductDto(
    @SerializedName("saleDetailId") val saleDetailId: String?,
    @SerializedName("productId") val productId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("ordered") val ordered: Int?,
    @SerializedName("delivered") val delivered: Int?,
    @SerializedName("pending") val pending: Int?
) {
    val quantityOrdered: Int get() = ordered ?: 0
    val quantityDelivered: Int get() = delivered ?: 0
    val quantityPending: Int get() = pending ?: maxOf(0, quantityOrdered - quantityDelivered)
}

data class DeliveryEventDto(
    @SerializedName("id") val id: String?,
    @SerializedName("productId") val productId: String?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("kind") val kind: String?, // "DELIVER" or "RETURN"
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("createdAt") val createdAt: String?
)

data class DeliveryEventRequestDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("kind") val kind: String, // "DELIVER" or "RETURN"
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("expectedRevision") val expectedRevision: Int
)

data class UpdateDeliveryQuantityRequestDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("expectedRevision") val expectedRevision: Int
)

data class RealtimeAccessDto(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("expiresInSeconds") val expiresInSeconds: Long?,
    @SerializedName("channels") val channels: List<String>?,
    @SerializedName("private") val private: Boolean?,
    @SerializedName("url") val url: String?,
    @SerializedName("websocketUrl") val websocketUrl: String?,
    @SerializedName("apiKey") val apiKey: String?,
    @SerializedName("key") val key: String?
) {
    val resolvedToken: String? get() = accessToken ?: apiKey ?: key
    val resolvedUrl: String? get() = url ?: websocketUrl ?: "wss://api.yungasdistribuidora.cc/realtime/v1/websocket"
}
