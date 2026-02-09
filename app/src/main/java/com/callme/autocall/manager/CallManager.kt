package com.callme.autocall.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

/**
 * Manager class for handling phone calls
 */
class CallManager(private val context: Context) {
    private val tag = "CallManager"
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    
    private var hangUpJob: Job? = null
    private var isCallActive = false

    companion object {
        private const val AUTO_HANG_UP_DELAY = 20_000L // 20 seconds
    }

    /**
     * Make a phone call to the specified number
     * @param phoneNumber The phone number to call
     */
    fun makeCall(phoneNumber: String) {
        if (!hasCallPermission()) {
            Log.e(tag, "CALL_PHONE permission not granted")
            return
        }

        try {
            Log.d(tag, "Making call to: $phoneNumber")
            
            // Create call intent
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            isCallActive = true
            
            // Schedule automatic hang up after 20 seconds
            scheduleAutoHangUp()
            
        } catch (e: Exception) {
            Log.e(tag, "Error making call: ${e.message}", e)
        }
    }

    /**
     * Schedule automatic hang up after specified delay
     */
    private fun scheduleAutoHangUp() {
        // Cancel any existing hang up job
        hangUpJob?.cancel()
        
        // Schedule new hang up job
        hangUpJob = CoroutineScope(Dispatchers.Main).launch {
            delay(AUTO_HANG_UP_DELAY)
            hangUpCall()
        }
    }

    /**
     * Hang up the current call
     */
    fun hangUpCall() {
        try {
            if (!isCallActive) {
                Log.d(tag, "No active call to hang up")
                return
            }

            Log.d(tag, "Attempting to hang up call")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ (API 28+)
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ANSWER_PHONE_CALLS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    telecomManager.endCall()
                    Log.d(tag, "Call ended using TelecomManager")
                } else {
                    Log.e(tag, "ANSWER_PHONE_CALLS permission not granted")
                }
            } else {
                // For older versions, try reflection (may not work on all devices)
                try {
                    val telephonyClass = Class.forName(telephonyManager.javaClass.name)
                    val method = telephonyClass.getMethod("endCall")
                    method.invoke(telephonyManager)
                    Log.d(tag, "Call ended using reflection")
                } catch (e: Exception) {
                    Log.e(tag, "Failed to end call using reflection: ${e.message}")
                }
            }
            
            isCallActive = false
            hangUpJob?.cancel()
            
        } catch (e: Exception) {
            Log.e(tag, "Error hanging up call: ${e.message}", e)
        }
    }

    /**
     * Check if CALL_PHONE permission is granted
     */
    private fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Cancel scheduled hang up
     */
    fun cancelAutoHangUp() {
        hangUpJob?.cancel()
        hangUpJob = null
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        cancelAutoHangUp()
        isCallActive = false
    }
}
