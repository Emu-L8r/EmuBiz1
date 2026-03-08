package com.emul8r.bizap.data.remote.api

import com.emul8r.bizap.domain.model.Invoice
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API definition for Invoice operations.
 */
interface InvoiceApi {
    @POST("invoices")
    suspend fun createInvoice(@Body invoice: Invoice): Response<Invoice>

    @PUT("invoices/{id}")
    suspend fun updateInvoice(
        @Path("id") id: Long,
        @Body invoice: Invoice,
        @Header("If-Unmodified-Since") lastUpdated: Long
    ): Response<Invoice>

    @DELETE("invoices/{id}")
    suspend fun deleteInvoice(@Path("id") id: Long): Response<Unit>

    @GET("invoices/{id}")
    suspend fun getInvoice(@Path("id") id: Long): Response<Invoice>

    @POST("invoices/{id}/payments")
    suspend fun recordPayment(
        @Path("id") id: Long,
        @Query("amount") amount: Long,
        @Query("paymentDate") paymentDate: Long,
        @Query("notes") notes: String?
    ): Response<Unit>
}
