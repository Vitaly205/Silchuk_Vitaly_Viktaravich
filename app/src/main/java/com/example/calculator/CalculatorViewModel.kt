package com.example.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CalculatorViewModel: ViewModel() {
    var state by mutableStateOf(CalculatorState())
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        loadDataFromCloud()
    }

    fun onAction(action: CalculatorAction, context: Context? = null) {
        when(action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> state = CalculatorState()
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.Delete -> performDeletion()
            is CalculatorAction.CopyToClipboard -> {
                context?.let { copyToClipboard(it) }
            }
            is CalculatorAction.LoadData -> loadDataFromCloud()
        }
    }

    private fun loadDataFromCloud() {
        // Load Theme from Firestore
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

        // Load History from Firestore
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
