package com.example.yungasdistribuidora.domain.usecase

import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleDetailRequestDto
import com.example.yungasdistribuidora.domain.model.PresaleDraft
import com.example.yungasdistribuidora.domain.model.SaleModality
import java.text.SimpleDateFormat
import java.util.*

class RegisterPresaleUseCase {

    fun validateAndBuildRequest(
        draft: PresaleDraft,
        isAdmin: Boolean,
        discountInput: Double,
        paymentMethod: String?,
        paymentReference: String?,
        dueDateStr: String?
    ): Result<CreateSaleRequestDto> {
        val client = draft.client ?: return Result.failure(Exception("Debe seleccionar un cliente"))
        if (draft.items.isEmpty()) return Result.failure(Exception("Debe agregar al menos un producto"))

        // Check duplicate productIds
        val productIds = draft.items.map { it.product.id }
        if (productIds.size != productIds.toSet().size) {
            return Result.failure(Exception("No se permiten productos repetidos en la preventa"))
        }

        val subtotal = draft.items.sumOf { it.subtotal }
        if (discountInput < 0) return Result.failure(Exception("El descuento no puede ser negativo"))
        if (discountInput > subtotal) return Result.failure(Exception("El descuento no puede superar el subtotal"))

        if (!isAdmin && discountInput > 0) {
            return Result.failure(Exception("Los vendedores no pueden aplicar descuentos"))
        }

        // Validate items quantity and stock
        for (item in draft.items) {
            if (item.quantity < 1) return Result.failure(Exception("La cantidad de '${item.product.name}' debe ser al menos 1"))
            if (item.unitPrice < 0) return Result.failure(Exception("El precio unitario de '${item.product.name}' no puede ser negativo"))
            if (item.quantity > item.product.availableStock) {
                return Result.failure(Exception("Stock insuficiente para '${item.product.name}'. Disponible: ${item.product.availableStock.toInt()}"))
            }
        }

        val saleType: String
        val initialPayment: Double
        val finalDueDate: String?
        val finalPaymentMethod: String?

        when (draft.modality) {
            SaleModality.POR_COBRAR -> {
                saleType = "CASH"
                initialPayment = 0.0
                finalDueDate = null
                finalPaymentMethod = null
            }
            SaleModality.CONTADO -> {
                saleType = "CASH"
                initialPayment = subtotal - discountInput
                finalDueDate = null
                if (paymentMethod.isNullOrBlank()) {
                    return Result.failure(Exception("La modalidad Contado exige un método de pago"))
                }
                finalPaymentMethod = paymentMethod
            }
            SaleModality.CREDITO -> {
                saleType = "CREDIT"
                initialPayment = 0.0
                if (dueDateStr.isNullOrBlank()) {
                    return Result.failure(Exception("La fecha de vencimiento es obligatoria para crédito"))
                }
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
                    val dueDate = sdf.parse(dueDateStr) ?: return Result.failure(Exception("Fecha inválida"))
                    
                    val todayCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val today = todayCal.time

                    val maxCal = Calendar.getInstance().apply {
                        time = today
                        add(Calendar.DAY_OF_YEAR, 7)
                    }
                    val maxDate = maxCal.time

                    if (dueDate.before(today)) {
                        return Result.failure(Exception("La fecha de vencimiento no puede ser anterior a hoy"))
                    }
                    if (dueDate.after(maxDate)) {
                        return Result.failure(Exception("La fecha de crédito no puede superar los 7 días"))
                    }

                    finalDueDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(dueDate)
                } catch (e: Exception) {
                    return Result.failure(Exception("Formato de fecha inválido"))
                }
                finalPaymentMethod = paymentMethod?.takeIf { !it.isBlank() }
            }
        }

        val details = draft.items.map { item ->
            SaleDetailRequestDto(
                productId = item.product.id,
                quantity = item.quantity.toInt(),
                unitPrice = item.unitPrice,
                manualPrice = isAdmin
            )
        }

        val request = CreateSaleRequestDto(
            clientId = client.id,
            details = details,
            discount = discountInput.takeIf { it > 0 } ?: 0.0,
            observations = draft.observations.takeIf { !it.isBlank() },
            saleType = saleType,
            dueDate = finalDueDate,
            initialPayment = initialPayment,
            paymentMethod = finalPaymentMethod,
            paymentReference = paymentReference?.takeIf { !it.isBlank() }
        )

        return Result.success(request)
    }
}
