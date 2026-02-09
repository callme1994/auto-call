package com.callme.autocall.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing app permissions
 */
class PermissionHelper {
    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
        const val OVERLAY_PERMISSION_REQUEST_CODE = 1002
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003

        /**
         * Get all required permissions based on Android version
         */
        fun getRequiredPermissions(): Array<String> {
            val permissions = mutableListOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.INTERNET
            )

            // Android 9+ (API 28+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
                permissions.add(Manifest.permission.FOREGROUND_SERVICE)
            }

            // Android 10+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.USE_FULL_SCREEN_INTENT)
            }

            // Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            // Android 14+ (API 34+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions.add("android.permission.FOREGROUND_SERVICE_PHONE_CALL")
            }

            return permissions.toTypedArray()
        }

        /**
         * Check if all required permissions are granted
         */
        fun hasAllPermissions(context: Context): Boolean {
            return getRequiredPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * Request all required permissions
         */
        fun requestPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                getRequiredPermissions(),
                PERMISSION_REQUEST_CODE
            )
        }

        /**
         * Check if overlay permission is granted (for Android 6+)
         */
        fun hasOverlayPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        /**
         * Request overlay permission
         */
        fun requestOverlayPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
                activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }

        /**
         * Check if battery optimization is disabled
         */
        fun isBatteryOptimizationDisabled(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        }

        /**
         * Request to disable battery optimization
         */
        fun requestDisableBatteryOptimization(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivity(intent)
            }
        }
    }
}
