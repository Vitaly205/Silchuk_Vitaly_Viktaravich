package com.example.calculator

sealed class CalculatorAction {
    data class Number(val number: Int): CalculatorAction()
    object Clear: CalculatorAction()
    object Delete: CalculatorAction()
    object Decimal: CalculatorAction()
    object Calculate: CalculatorAction()
    data class Operation(val operation: CalculatorOperation): CalculatorAction()
    object CopyToClipboard: CalculatorAction()
    object LoadData: CalculatorAction()
    data class SetPassKey(val passKey: String): CalculatorAction()
    data class EnterPassKey(val passKey: String): CalculatorAction()
    object AuthenticateBiometric: CalculatorAction()
    object ResetPassKey: CalculatorAction()
}
