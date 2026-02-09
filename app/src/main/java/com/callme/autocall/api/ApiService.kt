package com.callme.autocall.api

import retrofit2.Response
import retrofit2.http.GET

/**
 * API service interface for fetching phone numbers
 */
interface ApiService {
    /**
     * Fetch phone number from configured endpoint
     */
    @GET(".")
    suspend fun getPhoneNumber(): Response<PhoneNumberResponse>
}
