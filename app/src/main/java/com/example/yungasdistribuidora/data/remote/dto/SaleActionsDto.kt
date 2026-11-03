package com.example.yungasdistribuidora.data.remote.dto

data class UpdateSaleRequestDto(
    val clientId: String,
    val details: List<SaleDetailRequestDto>,
    val bonuses: List<BonusAllocationRequestDto>,
    val discount: Double?,
    val observations: String,
    val saleType: String,
    val dueDate: String?
)

fun CreateSaleRequestDto.forUpdate() = UpdateSaleRequestDto(
    clientId, details, bonuses.orEmpty(), discount, observations.orEmpty(), saleType, dueDate
)

data class WhatsAppContactDto(
    val id: String, val name: String, val phone: String?,
    val isActive: Boolean, val whatsappConsent: Boolean
)
data class WhatsAppClientDto(val phone: String?, val whatsappConsent: Boolean)
data class WhatsAppBatchRequest(val recipients: List<String>, val resend: Boolean = false)
data class WhatsAppRecipientResult(val recipient: String, val status: String?, val error: String?)
data class WhatsAppBatchResponse(val results: List<WhatsAppRecipientResult>?)

data class WhatsAppDeliverySummary(val results: List<WhatsAppRecipientResult>) {
    val acceptedCount get() = results.count { it.status == "SENT" }
    fun message(): String = buildString {
        append("Aceptados por Meta: $acceptedCount de ${results.size}. La entrega se confirma posteriormente.")
        results.filter { it.status != "SENT" }.forEach {
            append("\n${if (it.recipient == "principal") "Principal" else "Contacto ${it.recipient}"}: ${it.error ?: "Resultado no confirmado; revisa el historial antes de reenviar"}")
        }
    }
}

fun eligibleWhatsAppRecipients(client: WhatsAppClientDto, contacts: List<WhatsAppContactDto>): List<String> =
    buildList {
        if (client.whatsappConsent && !client.phone.isNullOrBlank()) add("principal")
        contacts.filter { it.isActive && it.whatsappConsent && !it.phone.isNullOrBlank() }
            .forEach { add(it.id) }
    }.distinct()
