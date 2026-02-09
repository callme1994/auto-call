package com.callme.autocall

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.callme.autocall.databinding.ActivityMainBinding
import com.callme.autocall.service.AutoCallService
import com.callme.autocall.utils.PermissionHelper
import com.callme.autocall.utils.PreferenceHelper

/**
 * Main activity for the auto call application
 * Provides UI for configuration and service control
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize preference helper
        preferenceHelper = PreferenceHelper(this)
        
        // Setup UI
        setupUI()
        
        // Check permissions
        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    /**
     * Setup UI components and listeners
     */
    private fun setupUI() {
        // Load saved API URL
        binding.etApiUrl.setText(preferenceHelper.getApiUrl())
        
        // Save API URL button
        binding.btnSaveUrl.setOnClickListener {
            val url = binding.etApiUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                preferenceHelper.setApiUrl(url)
                Toast.makeText(this, "API URL saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Start service button
        binding.btnStartService.setOnClickListener {
            if (checkAllPermissions()) {
                startAutoCallService()
            } else {
                showPermissionDialog()
            }
        }
        
        // Stop service button
        binding.btnStopService.setOnClickListener {
            stopAutoCallService()
        }
        
        // Check permissions button
        binding.btnCheckPermissions.setOnClickListener {
            checkAndRequestPermissions()
        }
        
        // Update status
        updateServiceStatus()
    }

    /**
     * Update service status display
     */
    private fun updateServiceStatus() {
        val isRunning = preferenceHelper.isServiceRunning()
        binding.tvServiceStatus.text = if (isRunning) {
            "Service Status: Running"
        } else {
            "Service Status: Stopped"
        }
        
        binding.btnStartService.isEnabled = !isRunning
        binding.btnStopService.isEnabled = isRunning
    }

    /**
     * Check if all required permissions are granted
     */
    private fun checkAllPermissions(): Boolean {
        val hasBasicPermissions = PermissionHelper.hasAllPermissions(this)
        val hasOverlayPermission = PermissionHelper.hasOverlayPermission(this)
        val isBatteryOptDisabled = PermissionHelper.isBatteryOptimizationDisabled(this)
        
        return hasBasicPermissions && hasOverlayPermission && isBatteryOptDisabled
    }

    /**
     * Check and request all required permissions
     */
    private fun checkAndRequestPermissions() {
        val permissionStatus = StringBuilder()
        
        // Check basic permissions
        val hasBasicPermissions = PermissionHelper.hasAllPermissions(this)
        permissionStatus.append("Basic Permissions: ${if (hasBasicPermissions) "✓" else "✗"}\n")
        
        // Check overlay permission
        val hasOverlayPermission = PermissionHelper.hasOverlayPermission(this)
        permissionStatus.append("Overlay Permission: ${if (hasOverlayPermission) "✓" else "✗"}\n")
        
        // Check battery optimization
        val isBatteryOptDisabled = PermissionHelper.isBatteryOptimizationDisabled(this)
        permissionStatus.append("Battery Optimization: ${if (isBatteryOptDisabled) "Disabled ✓" else "Enabled ✗"}\n")
        
        binding.tvPermissionStatus.text = permissionStatus.toString()
        
        // Request missing permissions
        if (!hasBasicPermissions) {
            PermissionHelper.requestPermissions(this)
        } else if (!hasOverlayPermission) {
            PermissionHelper.requestOverlayPermission(this)
        } else if (!isBatteryOptDisabled) {
            PermissionHelper.requestDisableBatteryOptimization(this)
        }
    }

    /**
     * Show permission explanation dialog
     */
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage(
                "This app requires the following permissions to function:\n\n" +
                "• Phone Call Permission - To make calls\n" +
                "• Phone State Permission - To monitor call status\n" +
                "• Overlay Permission - To show over other apps\n" +
                "• Battery Optimization - To run in background\n\n" +
                "Please grant all required permissions."
            )
            .setPositiveButton("Grant Permissions") { _, _ ->
                checkAndRequestPermissions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Start the auto call service
     */
    private fun startAutoCallService() {
        try {
            AutoCallService.start(this)
            preferenceHelper.setServiceRunning(true)
            updateServiceStatus()
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Stop the auto call service
     */
    private fun stopAutoCallService() {
        try {
            AutoCallService.stop(this)
            preferenceHelper.setServiceRunning(false)
            updateServiceStatus()
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
                checkAndRequestPermissions()
            } else {
                Toast.makeText(this, "Some permissions denied", Toast.LENGTH_SHORT).show()
                checkAndRequestPermissions()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PermissionHelper.OVERLAY_PERMISSION_REQUEST_CODE) {
            checkAndRequestPermissions()
        }
    }
}
