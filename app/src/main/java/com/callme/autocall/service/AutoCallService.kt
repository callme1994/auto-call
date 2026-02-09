package com.callme.autocall.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.callme.autocall.R
import com.callme.autocall.api.ApiService
import com.callme.autocall.api.PhoneNumberResponse
import com.callme.autocall.manager.CallManager
import com.callme.autocall.manager.ScreenManager
import com.callme.autocall.utils.PreferenceHelper
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Foreground service for auto-calling functionality
 * Runs every 30 seconds to fetch phone numbers and make calls
 */
class AutoCallService : LifecycleService() {
    private val tag = "AutoCallService"
    
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var callManager: CallManager
    private lateinit var screenManager: ScreenManager
    
    private var pollingJob: Job? = null
    private var apiService: ApiService? = null

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "auto_call_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POLLING_INTERVAL = 30_000L // 30 seconds

        /**
         * Start the auto call service
         */
        fun start(context: Context) {
            val intent = Intent(context, AutoCallService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stop the auto call service
         */
        fun stop(context: Context) {
            val intent = Intent(context, AutoCallService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service created")
        
        // Initialize components
        preferenceHelper = PreferenceHelper(this)
        callManager = CallManager(this)
        screenManager = ScreenManager(this)
        
        // Create notification channel
        createNotificationChannel()
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification("Service started"))
        
        // Mark service as running
        preferenceHelper.setServiceRunning(true)
        
        // Initialize API service
        initializeApiService()
        
        // Start polling
        startPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(tag, "Service started")
        return START_STICKY // Restart service if killed by system
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Service destroyed")
        
        // Stop polling
        stopPolling()
        
        // Clean up resources
        callManager.cleanup()
        screenManager.cleanup()
        
        // Mark service as stopped
        preferenceHelper.setServiceRunning(false)
    }

    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Auto Call Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Auto call service notification"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification for foreground service
     */
    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, Class.forName("com.callme.autocall.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Auto Call Service")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Update notification with new content
     */
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Initialize Retrofit API service
     */
    private fun initializeApiService() {
        try {
            val apiUrl = preferenceHelper.getApiUrl()
            Log.d(tag, "Initializing API service with URL: $apiUrl")
            
            // Create logging interceptor
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            // Create OkHttp client
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Create Retrofit instance
            val retrofit = Retrofit.Builder()
                .baseUrl(ensureTrailingSlash(apiUrl))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            apiService = retrofit.create(ApiService::class.java)
            
        } catch (e: Exception) {
            Log.e(tag, "Error initializing API service: ${e.message}", e)
        }
    }

    /**
     * Ensure URL has trailing slash for Retrofit
     */
    private fun ensureTrailingSlash(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }

    /**
     * Start polling for phone numbers
     */
    private fun startPolling() {
        stopPolling() // Stop any existing polling
        
        pollingJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    Log.d(tag, "Polling for phone number...")
                    updateNotification("Checking for calls...")
                    
                    // Fetch phone number from API
                    val phoneNumber = fetchPhoneNumber()
                    
                    if (phoneNumber != null && phoneNumber.isNotBlank()) {
                        Log.d(tag, "Phone number received: $phoneNumber")
                        updateNotification("Calling: $phoneNumber")
                        
                        // Make the call
                        makePhoneCall(phoneNumber)
                    } else {
                        Log.d(tag, "No phone number received")
                        updateNotification("Waiting for calls...")
                    }
                    
                } catch (e: Exception) {
                    Log.e(tag, "Error in polling loop: ${e.message}", e)
                    updateNotification("Error: ${e.message}")
                }
                
                // Wait for next polling interval
                delay(POLLING_INTERVAL)
            }
        }
    }

    /**
     * Stop polling
     */
    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Fetch phone number from API
     */
    private suspend fun fetchPhoneNumber(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val service = apiService ?: run {
                    Log.e(tag, "API service not initialized")
                    return@withContext null
                }
                
                val response = service.getPhoneNumber()
                
                if (response.isSuccessful) {
                    val phoneData = response.body()
                    val phoneNumber = phoneData?.getPhoneNumber()
                    Log.d(tag, "API response successful: $phoneNumber")
                    phoneNumber
                } else {
                    Log.e(tag, "API response failed: ${response.code()} - ${response.message()}")
                    null
                }
                
            } catch (e: Exception) {
                Log.e(tag, "Error fetching phone number: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Make a phone call
     */
    private fun makePhoneCall(phoneNumber: String) {
        try {
            // Wake up screen and unlock
            screenManager.wakeUpAndUnlock()
            
            // Wait a bit for screen to wake up
            lifecycleScope.launch {
                delay(1000)
                
                // Make the call
                callManager.makeCall(phoneNumber)
                
                Log.d(tag, "Call initiated to: $phoneNumber")
            }
            
        } catch (e: Exception) {
            Log.e(tag, "Error making phone call: ${e.message}", e)
        }
    }
}
