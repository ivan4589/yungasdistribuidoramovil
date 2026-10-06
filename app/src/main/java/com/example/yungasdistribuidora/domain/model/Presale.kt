package com.example.yungasdistribuidora.domain.model

enum class SaleModality(val displayName: String) {
    POR_COBRAR("Por Cobrar"),
    CONTADO("Contado"),
    CREDITO("Crédito")
}

data class PresaleItem(
    val product: Product,
    val quantity: Double,
    val unitPrice: Double
) {
    val subtotal: Double
        get() = quantity * unitPrice
}

data class PresaleDraft(
    val client: Client? = null,
    val items: List<PresaleItem> = emptyList(),
    val modality: SaleModality = SaleModality.POR_COBRAR,
    val observations: String = ""
)
