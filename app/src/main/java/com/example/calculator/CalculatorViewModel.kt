package com.example.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CalculatorViewModel: ViewModel() {
    var state by mutableStateOf(CalculatorState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private var prefs: SharedPreferences? = null

    fun initPrefs(context: Context) {
        prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
        val savedPassKey = prefs?.getString("pass_key", null)
        state = state.copy(
            isPassKeySet = savedPassKey != null,
            isAuthorized = savedPassKey == null
        )
        loadDataFromCloud()
    }

    fun onAction(action: CalculatorAction, context: Context? = null) {
        when(action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> {
                state = state.copy(
                    number1 = "",
                    number2 = "",
                    operation = null
                )
            }
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.Delete -> performDeletion()
            is CalculatorAction.CopyToClipboard -> context?.let { copyToClipboard(it) }
            is CalculatorAction.LoadData -> loadDataFromCloud()
            is CalculatorAction.SetPassKey -> setPassKey(action.passKey)
            is CalculatorAction.EnterPassKey -> validatePassKey(action.passKey)
            is CalculatorAction.ResetPassKey -> resetPassKey()
            is CalculatorAction.AuthenticateBiometric -> {
                if (context is FragmentActivity) {
                    authenticateBiometric(context)
                }
            }
        }
    }

    private fun setPassKey(passKey: String) {
        if (passKey.length == 4) {
            prefs?.edit()?.putString("pass_key", passKey)?.apply()
            state = state.copy(isPassKeySet = true, isAuthorized = true, showPassKeySetup = false)
        }
    }

    private fun validatePassKey(enteredKey: String) {
        val savedKey = prefs?.getString("pass_key", null)
        if (enteredKey == savedKey) {
            state = state.copy(isAuthorized = true, authError = null)
        } else {
            state = state.copy(authError = "Incorrect Pass Key")
        }
    }

    private fun resetPassKey() {
        prefs?.edit()?.remove("pass_key")?.apply()
        state = state.copy(isPassKeySet = false, isAuthorized = true)
    }

    private fun authenticateBiometric(activity: FragmentActivity) {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                state = state.copy(authError = "No biometric hardware detected")
                return
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                state = state.copy(authError = "Biometric hardware is unavailable")
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                state = state.copy(authError = "No fingerprints enrolled. Use PIN.")
                Toast.makeText(activity, "Please set up fingerprint in system settings", Toast.LENGTH_LONG).show()
                return
            }
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    state = state.copy(isAuthorized = true, authError = null)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    state = state.copy(authError = errString.toString())
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Use Pass Key")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun loadDataFromCloud() {
        db.collection("settings").document("theme").get()
            .addOnSuccessListener { document ->
                val colorHex = document.getString("primaryColor")
                if (colorHex != null) {
                    try {
                        state = state.copy(themeColor = Color(android.graphics.Color.parseColor(colorHex)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        db.collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val historyList = snapshots?.mapNotNull { it.getString("expression") } ?: emptyList()
                state = state.copy(history = historyList)
            }
    }

    private fun saveCalculationToCloud(expression: String) {
        val data = hashMapOf(
            "expression" to expression,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        db.collection("history").add(data)
    }

    private fun copyToClipboard(context: Context) {
        val textToCopy = (state.number1 + (state.operation?.symbol ?: "") + state.number2).ifEmpty { "0" }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator Result", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun performDeletion() {
        if(state.number2.isNotBlank()) {
            state = state.copy(number2 = state.number2.dropLast(1))
        } else if(state.operation != null) {
            state = state.copy(operation = null)
        } else if(state.number1.isNotBlank()) {
            state = state.copy(number1 = state.number1.dropLast(1))
        }
    }

    private fun performCalculation() {
        val number1 = state.number1.toDoubleOrNull()
        val number2 = state.number2.toDoubleOrNull()
        if(number1 != null && number2 != null && state.operation != null) {
            val result = when(state.operation) {
                is CalculatorOperation.Add -> number1 + number2
                is CalculatorOperation.Subtract -> number1 - number2
                is CalculatorOperation.Multiply -> number1 * number2
                is CalculatorOperation.Divide -> if (number2 != 0.0) number1 / number2 else Double.NaN
                null -> return
            }
            
            val formattedResult = if (result.isNaN()) "Error" else if (result % 1 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString()
            }
            
            val expression = "${state.number1}${state.operation?.symbol}${state.number2} = $formattedResult"
            saveCalculationToCloud(expression)

            state = state.copy(
                number1 = formattedResult.take(15),
                number2 = "",
                operation = null
            )
        }
    }

    private fun enterOperation(operation: CalculatorOperation) {
        if(state.number1.isNotBlank()) {
            state = state.copy(operation = operation)
        }
    }

    private fun enterDecimal() {
        if(state.operation == null && !state.number1.contains(".") && state.number1.isNotBlank()) {
            state = state.copy(number1 = state.number1 + ".")
            return
        }
        if(!state.number2.contains(".") && state.number2.isNotBlank()) {
            state = state.copy(number2 = state.number2 + ".")
        }
    }

    private fun enterNumber(number: Int) {
        if(state.operation == null) {
            if(state.number1.length >= 8) return
            state = state.copy(number1 = state.number1 + number)
            return
        }
        if(state.number2.length >= 8) return
        state = state.copy(number2 = state.number2 + number)
    }
}
