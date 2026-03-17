package com.example.calculator

import androidx.compose.ui.graphics.Color

data class CalculatorState(
    val number1: String = "",
    val number2: String = "",
    val operation: CalculatorOperation? = null,
    val history: List<String> = emptyList(),
    val themeColor: Color = Color(0xFFFF9800) // Default Orange
)
