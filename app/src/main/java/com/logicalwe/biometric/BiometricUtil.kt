package com.logicalwe.biometric

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
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
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: no hardware, biometric unavailable")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(
                    " :$LOG_APP_NAME: ",
                    "BiometricUtil: :hasBiometricCapability: hw unavailable, biometric unavailable")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :hasBiometricCapability: none enrolled, prompting")
                listener.onBiometricAuthenticationPrompt()
            }
        }
        return biometricManager.canAuthenticate()
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
    private fun setBiometricPromptInfo(title: String, subtitle: String, description: String,
                                       allowDeviceCredential: Boolean): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
              .setTitle(title)
              .setSubtitle(subtitle)
              .setDescription(description)

    // Use Device Credentials if allowed, otherwise show Cancel Button
    builder.apply {
      if (allowDeviceCredential) setDeviceCredentialAllowed(true)
      else setNegativeButtonText("Cancel")
    }

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
                listener.onBiometricAuthenticationError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationFailed: ")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationSucceeded: ")
                when (result.authenticationType) {
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> {
                        Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationSucceeded: auth type biometric")
                    }
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> {
                        Log.d(
                            " :$LOG_APP_NAME: ",
                            "BiometricUtil: :onAuthenticationSucceeded: auth type device credentials")
                    }
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_UNKNOWN -> {
                        Log.d(" :$LOG_APP_NAME: ", "BiometricUtil: :onAuthenticationSucceeded: auth type unknown")
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
    fun showBiometricPrompt(title: String = "Biometric Authentication",
                            subtitle: String = "Enter biometric credentials to proceed.",
                            description: String = "Input your Fingerprint or FaceID to ensure it's you!",
                            activity: AppCompatActivity, listener: BiometricAuthListener,
                            cryptoObject: BiometricPrompt.CryptoObject? = null,
                            allowDeviceCredential: Boolean = true) {
        // Prepare BiometricPrompt Dialog
        val promptInfo = setBiometricPromptInfo(
            title, subtitle, description, allowDeviceCredential)

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
