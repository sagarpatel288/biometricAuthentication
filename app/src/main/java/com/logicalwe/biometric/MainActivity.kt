package com.logicalwe.biometric

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt

/**
 * [Reference Tutorial](https://www.kodeco.com/18782293-android-biometric-api-getting-started)
 */
class MainActivity : AppCompatActivity(), BiometricAuthListener {

    private lateinit var buttonBiometricsLogin: Button
    private lateinit var buttonBiometricsEnroll: Button

    private val biometricAuthPromptLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onBiometricAuthPromptResult(it)
        }

    private fun onBiometricAuthPromptResult(activityResult: ActivityResult) {
        if (Activity.RESULT_OK == activityResult.resultCode) {
            Log.d(" :$LOG_APP_NAME: ", "MainActivity: :onBiometricAuthPromptResult: result ok")
            Toast.makeText(this, "onBiometricAuthPromptResult: result ok ", Toast.LENGTH_SHORT).show()
            // Show / hide UI based on biometric authentication capability
            showBiometricLoginOption()
        } else if (Activity.RESULT_CANCELED == activityResult.resultCode) {
            Log.d(" :$LOG_APP_NAME: ", "MainActivity: :onBiometricAuthPromptResult: result cancelled")
            Toast.makeText(this, "onBiometricAuthPromptResult: result cancelled", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(" :$LOG_APP_NAME: ", "MainActivity: :onBiometricAuthPromptResult: unknown error")
            Toast.makeText(this, "onBiometricAuthPromptResult: unknown error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonBiometricsLogin = findViewById(R.id.buttonBiometricsLogin)
        buttonBiometricsEnroll = findViewById(R.id.buttonBiometricsEnroll)
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        //button visibility
        showBiometricLoginOption()
    }

    private fun setClickListeners() {
        buttonBiometricsEnroll.setOnClickListener {
            onBiometricAuthenticationPrompt()
        }
    }


    fun onClickBiometrics(view: View) {
        BiometricUtil.showBiometricPrompt(
            activity = this, listener = this, cryptoObject = null)
    }

    override fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult) {
        Log.d(" :$LOG_APP_NAME: ", "MainActivity: :onBiometricAuthenticationSuccess: ")
        Toast.makeText(this, "onBiometricAuthenticationSuccess: ", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticationError(errorCode: Int, errorMessage: String) {
        Log.d(" :$LOG_APP_NAME: ", "MainActivity: :onBiometricAuthenticationError: ")
        Toast.makeText(this, "onBiometricAuthenticationError: ", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticationPrompt() {
        val enrollIntent: Intent = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                    .putExtra(EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                Intent(Settings.ACTION_FINGERPRINT_ENROLL)
            }
            else -> {
                Intent(Settings.ACTION_SECURITY_SETTINGS)
            }
        }
        biometricAuthPromptLauncher.launch(enrollIntent)
    }

    private fun showBiometricLoginOption() {
        buttonBiometricsLogin.visibility = if (BiometricUtil.isBiometricReady(this, this)) View.VISIBLE
        else View.GONE
        buttonBiometricsEnroll.visibility = if (BiometricUtil.isBiometricReady(this, this)) View.GONE
        else View.VISIBLE
    }
}
