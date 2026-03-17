package com.example.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Calculator(
    state: CalculatorState,
    modifier: Modifier = Modifier,
    buttonSpacing: Dp = 8.dp,
    onAction: (CalculatorAction) -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            // Display History from Cloud
            state.history.take(3).forEach { entry ->
                Text(
                    text = entry,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 20.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            val displayText = (state.number1 + (state.operation?.symbol ?: "") + state.number2).ifEmpty { "0" }
            
            val fontSize = when {
                displayText.length > 12 -> 35.sp
                displayText.length > 8 -> 50.sp
                else -> 70.sp
            }

            Text(
                text = displayText,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                onAction(CalculatorAction.CopyToClipboard)
                            }
                        )
                    },
                fontWeight = FontWeight.Light,
                fontSize = fontSize,
                color = Color.White,
                maxLines = 1,
                softWrap = false
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(
                    symbol = "AC",
                    modifier = Modifier
                        .background(Color.LightGray)
                        .aspectRatio(2f)
                        .weight(2f),
                    color = Color.Black,
                    onClick = { onAction(CalculatorAction.Clear) }
                )
                CalculatorButton(
                    symbol = "Del",
                    modifier = Modifier
                        .background(Color.LightGray)
                        .aspectRatio(1f)
                        .weight(1f),
                    color = Color.Black,
                    onClick = { onAction(CalculatorAction.Delete) }
                )
                CalculatorButton(
                    symbol = "/",
                    modifier = Modifier
                        .background(state.themeColor)
                        .aspectRatio(1f)
                        .weight(1f),
                    onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.Divide)) }
                )
            }
            // ... (остальные ряды кнопок аналогично используют state.themeColor для операций)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(symbol = "7", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(7)) })
                CalculatorButton(symbol = "8", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(8)) })
                CalculatorButton(symbol = "9", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(9)) })
                CalculatorButton(symbol = "x", modifier = Modifier.background(state.themeColor).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.Multiply)) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(symbol = "4", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(4)) })
                CalculatorButton(symbol = "5", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(5)) })
                CalculatorButton(symbol = "6", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(6)) })
                CalculatorButton(symbol = "-", modifier = Modifier.background(state.themeColor).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.Subtract)) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(symbol = "1", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(1)) })
                CalculatorButton(symbol = "2", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(2)) })
                CalculatorButton(symbol = "3", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Number(3)) })
                CalculatorButton(symbol = "+", modifier = Modifier.background(state.themeColor).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.Add)) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(symbol = "0", modifier = Modifier.background(Color.DarkGray).aspectRatio(2f).weight(2f), onClick = { onAction(CalculatorAction.Number(0)) })
                CalculatorButton(symbol = ".", modifier = Modifier.background(Color.DarkGray).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Decimal) })
                CalculatorButton(symbol = "=", modifier = Modifier.background(state.themeColor).aspectRatio(1f).weight(1f), onClick = { onAction(CalculatorAction.Calculate) })
            }
        }
    }
}
