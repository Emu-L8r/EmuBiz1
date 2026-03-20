package com.emul8r.bizap.data.remote.api

import com.emul8r.bizap.domain.model.Customer
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API definition for Customer operations.
 */
interface CustomerApi {
    @POST("customers")
    suspend fun createCustomer(@Body customer: Customer): Response<Customer>

    @PUT("customers/{id}")
    suspend fun updateCustomer(
        @Path("id") id: Long,
        @Body customer: Customer,
        @Header("If-Unmodified-Since") lastUpdated: Long
    ): Response<Customer>

    @DELETE("customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: Long): Response<Unit>

    @GET("customers/{id}")
    suspend fun getCustomer(@Path("id") id: Long): Response<Customer>
}
