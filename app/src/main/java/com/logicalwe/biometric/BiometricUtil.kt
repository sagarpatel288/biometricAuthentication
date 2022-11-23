package com.logicalwe.biometric

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val LOG_APP_NAME = " :biometricPoc: "

/**
 * Helper class for managing Biometric Authentication Process
 */
object BiometricUtil {

    /**
     * Checks if the device has Biometric support.
     * Returns [BiometricManager.BIOMETRIC_SUCCESS] If at least one biometric authentication is
     * present and enrolled.
     */
    private fun hasBiometricCapability(context: Context, listener: BiometricAuthListener): Int {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: biometric success")
                Toast.makeText(context, "hasBiometricCapability: biometric success", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: no hardware, biometric unavailable")
                Toast.makeText(context, "hasBiometricCapability: no hardware, biometric unavailable", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(
                    " :$LOG_APP_NAME: ",
                    "BiometricUtil: :hasBiometricCapability: hw unavailable, biometric unavailable")
                Toast.makeText(context, "hasBiometricCapability: hw unavailable, biometric unavailable", Toast
                    .LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // TODO by sagar patel: 22/11/22 API 31, 32 are falling here when no security is set
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: none enrolled, prompting")
                Toast.makeText(context, "hasBiometricCapability: none enrolled, prompting", Toast.LENGTH_SHORT).show()
                listener.onBiometricAuthenticationPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                // TODO by sagar patel: 22/11/22 API 28 is falling here with and without security
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: unsupported")
                Toast.makeText(context, "hasBiometricCapability: unsupported", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: security update required")
                Toast.makeText(context, "hasBiometricCapability: security update required", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: unknown")
                Toast.makeText(context, "hasBiometricCapability: unknown", Toast.LENGTH_SHORT).show()
            }
        }
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            biometricManager.canAuthenticate()
        else biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    }

    /**
     * Checks if Biometric Authentication (example: Fingerprint) is set in the device
     * See more: [hasBiometricCapability]
     */
    fun isBiometricReady(context: Context, listener: BiometricAuthListener) =
        hasBiometricCapability(context, listener) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Prepares PromptInfo dialog with provided configuration.
     * It can have either fingerprint or face or both depending upon different API.
     * Android 12 shows both the fingerprint and face options.
     * Android 10 having no face id support, shows only fingerprint option.
     * [Official Link](https://developer.android.com/training/sign-in/biometric-auth#addt-resources)
     */
    private fun setBiometricPromptInfo(title: String, subtitle: String,
                                       description: String): BiometricPrompt.PromptInfo {
        val builder =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText("Cancel")

        return builder.build()
    }

    /**
     * Initializes BiometricPrompt with the caller and callback handlers
     */
    private fun initBiometricPrompt(activity: AppCompatActivity, listener: BiometricAuthListener): BiometricPrompt {
        // Attach calling Activity
        val executor = ContextCompat.getMainExecutor(activity)

        // Attach callback handlers
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(
                    " :$LOG_APP_NAME: ",
                    "BiometricUtil: :onAuthenticationError: errorCode: $errorCode errorMessage: $errString")
                Toast.makeText(activity, "onAuthenticationError: code: $errorCode message: " +
                        "$errString", Toast.LENGTH_SHORT).show()
                listener.onBiometricAuthenticationPrompt()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationFailed: ")
                Toast.makeText(activity, "onAuthenticationFailed: ", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationSucceeded: ")
                when (result.authenticationType) {
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> {
                        Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationSucceeded: auth type biometric")
                        Toast.makeText(activity, "onAuthenticationSucceeded: auth type biometric: ",
                                       Toast.LENGTH_SHORT).show()
                    }
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> {
                        Log.d(
                            " :$LOG_APP_NAME: ",
                            "BiometricUtil: :onAuthenticationSucceeded: auth type device credentials")
                        Toast.makeText(activity, "onAuthenticationSucceeded: auth type device " +
                                "credentials: ", Toast.LENGTH_SHORT).show()
                    }
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_UNKNOWN -> {
                        Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationSucceeded: auth type unknown")
                        Toast.makeText(activity, "onAuthenticationSucceeded: auth type unknown: ",
                                       Toast.LENGTH_SHORT).show()
                    }
                }
                listener.onBiometricAuthenticationSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Displays a BiometricPrompt with provided configurations
     */
    fun showBiometricPrompt(title: String = "Custom Title",
                            subtitle: String = "Custom Sub-Title",
                            description: String = "Custom Description",
                            activity: AppCompatActivity, listener: BiometricAuthListener,
                            cryptoObject: BiometricPrompt.CryptoObject? = null) {
        // Prepare BiometricPrompt Dialog
        val promptInfo = setBiometricPromptInfo(
            title, subtitle, description)

        // Attach with caller and callback handler
        val biometricPrompt = initBiometricPrompt(activity, listener)

        // Authenticate with a CryptoObject if provided, otherwise default authentication
        biometricPrompt.apply {
            if (cryptoObject == null) authenticate(promptInfo)
            else authenticate(promptInfo, cryptoObject)
        }
    }

    /**
     * Navigates to Device's Settings screen Biometric Setup
     */
    fun launchBiometricSettings(context: Context) {
        ActivityCompat.startActivity(
            context, Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS), null)
    }

}
