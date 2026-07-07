package com.pdm0126.puppapp.screens.authorView.loginview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.puppapp.R

val bgPupappGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE5A64E),
        Color(0xFFB97664),
        Color(0xFF8E5D70)
    )
)

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) {
            viewModel.resetLoginState()
            onNavigateToOrders()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgPupappGradient)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 48.dp)
            ) {

                Spacer(Modifier.height(16.dp))

                AsyncImage(
                    model = R.drawable.pupapp_png,
                    contentDescription = "Logo de la aplicación",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text       = "Pupapp",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Text(
                    text  = "Gestión de pedidos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(Modifier.height(56.dp))

                // Formulario sin Card blanca, directo sobre el fondo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Spacer(Modifier.height(14.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color.White,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.White,
                            backgroundColor = Color.White.copy(alpha = 0.4f)
                        )
                    )

                    OutlinedTextField(
                        value         = viewModel.sessionName,
                        onValueChange = { viewModel.sessionName = it },
                        label         = { Text("Nombre de acceso") },
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        enabled       = !viewModel.isLoading,
                        colors        = textFieldColors
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value         = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label         = { Text("Contraseña") },
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                    tint = Color.White
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = !viewModel.isLoading,
                        colors   = textFieldColors
                    )

                    viewModel.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFFFCDD2),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                        Button(
                            onClick  = { viewModel.onLoginClick() },
                            shape    = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled  = !viewModel.isLoading
                        ) {
                            Text("Iniciar sesión", fontWeight = FontWeight.Medium)
                        }
                    }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "¿No tienes cuenta?",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    TextButton(onClick = onNavigateToRegister, enabled = !viewModel.isLoading) {
                        Text(
                            "Regístrate",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

