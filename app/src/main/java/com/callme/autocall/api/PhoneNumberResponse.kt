package com.callme.autocall.api

import com.google.gson.annotations.SerializedName

/**
 * API response model for phone number data
 * Supports multiple JSON formats:
 * {"phoneNumber": "13800138000", "timestamp": 1234567890}
 * {"phone": "13800138000"}
 */
data class PhoneNumberResponse(
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long? = null
) {
    /**
     * Get the phone number from either field
     */
    fun getPhoneNumber(): String? {
        return phoneNumber ?: phone
    }
}
