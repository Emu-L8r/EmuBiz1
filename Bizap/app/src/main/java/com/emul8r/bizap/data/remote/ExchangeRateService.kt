package com.emul8r.bizap.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * DETERMINISTIC CURRENCY API CLIENT
 */
interface ExchangeRateService {
    @GET("latest.json")
    suspend fun fetchRates(
        @Query("app_id") appId: String,
        @Query("base") base: String = "USD", // OpenExchangeRates free tier requires USD base
        @Query("symbols") symbols: String = "AUD,USD,EUR,GBP,JPY"
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val base: String,
    val rates: Map<String, Double>,
    val timestamp: Long
)

