package com.callme.autocall.manager

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager

/**
 * Manager class for handling screen wake and unlock
 */
class ScreenManager(private val context: Context) {
    private val tag = "ScreenManager"
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val WAKE_LOCK_TAG = "AutoCall:ScreenWakeLock"
    }

    /**
     * Wake up the screen and unlock if necessary
     */
    fun wakeUpAndUnlock() {
        try {
            Log.d(tag, "Waking up screen and unlocking")
            
            // Acquire wake lock to turn on screen
            acquireWakeLock()
            
            // Check if screen is locked
            if (isScreenLocked()) {
                Log.d(tag, "Screen is locked, attempting to unlock")
                unlockScreen()
            }
            
        } catch (e: Exception) {
            Log.e(tag, "Error waking up screen: ${e.message}", e)
        }
    }

    /**
     * Acquire wake lock to keep screen on
     */
    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                Log.d(tag, "Wake lock already held")
                return
            }

            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or 
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                WAKE_LOCK_TAG
            )
            
            wakeLock?.acquire(60_000L) // 60 seconds timeout
            Log.d(tag, "Wake lock acquired")
            
        } catch (e: Exception) {
            Log.e(tag, "Error acquiring wake lock: ${e.message}", e)
        }
    }

    /**
     * Release wake lock
     */
    fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                wakeLock = null
                Log.d(tag, "Wake lock released")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error releasing wake lock: ${e.message}", e)
        }
    }

    /**
     * Check if screen is locked
     */
    private fun isScreenLocked(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            keyguardManager.isDeviceLocked
        } else {
            keyguardManager.isKeyguardLocked
        }
    }

    /**
     * Unlock the screen
     * Note: This requires SYSTEM_ALERT_WINDOW permission on Android 6+
     * For full unlock functionality, app might need to be a system app or use accessibility services
     */
    private fun unlockScreen() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                // Android 8.1+: Use KeyguardManager to request dismiss
                keyguardManager.requestDismissKeyguard(null, null)
                Log.d(tag, "Requested keyguard dismiss")
            } else {
                // Older versions: Use deprecated method
                @Suppress("DEPRECATION")
                val keyguardLock = keyguardManager.newKeyguardLock(WAKE_LOCK_TAG)
                @Suppress("DEPRECATION")
                keyguardLock.disableKeyguard()
                Log.d(tag, "Disabled keyguard")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error unlocking screen: ${e.message}", e)
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        releaseWakeLock()
    }
}
