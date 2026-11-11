package com.example.yungasdistribuidora.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeliveryItemDto(
    @SerializedName("productId") val productId: String?,
    @SerializedName("productName") val productName: String?,
    @SerializedName("ordered") val ordered: Int?,
    @SerializedName("solicitado") val solicitado: Int?,
    @SerializedName("delivered") val delivered: Int?,
    @SerializedName("entregado") val entregado: Int?,
    @SerializedName("pending") val pending: Int?,
    @SerializedName("pendiente") val pendiente: Int?,
    @SerializedName("revision") val revision: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("attribution") val attribution: String?
) {
    val quantityOrdered: Int get() = ordered ?: solicitado ?: 0
    val quantityDelivered: Int get() = delivered ?: entregado ?: 0
    val quantityPending: Int get() = pending ?: pendiente ?: maxOf(0, quantityOrdered - quantityDelivered)
    val itemRevision: Int get() = revision ?: 0
}

data class DeliveryEventDto(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("user") val user: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("details") val details: String?,
    @SerializedName("attribution") val attribution: String?
)

data class DeliverySummaryDto(
    @SerializedName("notDeliveredCount") val notDeliveredCount: Int?,
    @SerializedName("noEntregado") val noEntregado: Int?,
    @SerializedName("partialCount") val partialCount: Int?,
    @SerializedName("parcial") val parcial: Int?,
    @SerializedName("completedCount") val completedCount: Int?,
    @SerializedName("completo") val completo: Int?
) {
    val noEntregadoCount: Int get() = notDeliveredCount ?: noEntregado ?: 0
    val parcialCountVal: Int get() = partialCount ?: parcial ?: 0
    val completoCountVal: Int get() = completedCount ?: completo ?: 0
}

data class DeliveryResponseDto(
    @SerializedName("saleId") val saleId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("items") val items: List<DeliveryItemDto>?,
    @SerializedName("events") val events: List<DeliveryEventDto>?,
    @SerializedName("summary") val summary: DeliverySummaryDto?,
    @SerializedName("revision") val revision: Int?,
    @SerializedName("attribution") val attribution: String?
)

data class DeliveryEventRequestDto(
    @SerializedName("type") val type: String?,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("productId") val productId: String?,
    @SerializedName("observations") val observations: String?
)

data class UpdateDeliveryQuantityRequestDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("expectedRevision") val expectedRevision: Int
)

data class RealtimeAccessDto(
    @SerializedName("token") val token: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("websocketUrl") val websocketUrl: String?,
    @SerializedName("apiKey") val apiKey: String?,
    @SerializedName("key") val key: String?
) {
    val resolvedToken: String? get() = token ?: accessToken
    val resolvedUrl: String? get() = url ?: websocketUrl
    val resolvedApiKey: String? get() = apiKey ?: key
}
