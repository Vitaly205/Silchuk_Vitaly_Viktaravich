package com.example.calculator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.calculator.ui.theme.CalculatorTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        val viewModel by viewModels<CalculatorViewModel>()
        viewModel.initPrefs(this)
        
        setContent {
            CalculatorTheme {
                val state = viewModel.state
                val context = LocalContext.current
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    when {
                        !state.isPassKeySet -> {
                            SetupPassKeyScreen(onSet = { viewModel.onAction(CalculatorAction.SetPassKey(it)) })
                        }
                        !state.isAuthorized -> {
                            AuthScreen(
                                error = state.authError,
                                onEnter = { viewModel.onAction(CalculatorAction.EnterPassKey(it)) },
                                onBiometric = { viewModel.onAction(CalculatorAction.AuthenticateBiometric, context) }
                            )
                        }
                        else -> {
                            Calculator(
                                state = state,
                                onAction = { action -> viewModel.onAction(action, context) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetupPassKeyScreen(onSet: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set 4-digit Pass Key", color = Color.White, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        TextField(
            value = pin,
            onValueChange = { if(it.length <= 4) pin = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if(pin.length == 4) onSet(pin) }, enabled = pin.length == 4) {
            Text("Set Pass Key")
        }
    }
}

@Composable
fun AuthScreen(error: String?, onEnter: (String) -> Unit, onBiometric: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        onBiometric()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Pass Key", color = Color.White, fontSize = 24.sp)
        if (error != null) {
            Text(error, color = Color.Red, fontSize = 14.sp)
        }
        Spacer(Modifier.height(16.dp))
        TextField(
            value = pin,
            onValueChange = { 
                if(it.length <= 4) pin = it
                if(it.length == 4) onEnter(it)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBiometric) {
            Text("Use Fingerprint", color = Color(0xFFFF9800))
        }
    }
}
