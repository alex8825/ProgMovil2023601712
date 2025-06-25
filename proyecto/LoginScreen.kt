package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.mctrio.estudiapp.data.models.LoginResponse
import com.mctrio.estudiapp.data.remote.ApiService
import com.mctrio.estudiapp.data.remote.RetrofitClient
import com.mctrio.estudiapp.utils.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.launch
import com.mctrio.estudiapp.repository.GroupRepository // Importar GroupRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}".toRegex()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Iniciar sesión",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = errorMessage.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Completa todos los campos"
                        return@Button
                    }

                    if (!email.matches(emailPattern)) {
                        errorMessage = "Por favor, ingresa un correo electrónico válido."
                        return@Button
                    }

                    errorMessage = ""

                    val ipAddress = UserPreferences.getIpAddress(context)
                    if (ipAddress.isNullOrEmpty()) {
                        Toast.makeText(context, "Por favor, configura la IP del servidor primero.", Toast.LENGTH_LONG).show()
                        navController.navigate("ipConfig")
                        return@Button
                    }

                    val apiService = RetrofitClient.getInstance(context).create(ApiService::class.java)
                    val call = apiService.login(email, password)

                    call.enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.isSuccessful) {
                                val loginResponse = response.body()
                                if (loginResponse != null && loginResponse.estado == "ok") {
                                    coroutineScope.launch {
                                        UserPreferences.saveIsLoggedIn(context, true)
                                        UserPreferences.saveUserId(context, loginResponse.id_usuario ?: "")
                                        UserPreferences.saveUserName(context, loginResponse.nombre_usuario ?: "")
                                        UserPreferences.saveUserEmail(context, email)
                                        // No se guarda groupCode aquí, se cargará en ModeSelectionScreen

                                        Toast.makeText(context, loginResponse.mensaje ?: "Login exitoso", Toast.LENGTH_SHORT).show()

                                        // Siempre navegar a ModeSelectionScreen después de un login exitoso
                                        navController.navigate("modeSelection") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                } else {
                                    errorMessage = loginResponse?.mensaje ?: "Error desconocido"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            } else {
                                errorMessage = "Error de servidor: ${response.code()}"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            errorMessage = "Error de red: ${t.message}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            t.printStackTrace()
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Iniciar sesión", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { navController.navigate("forgotPassword") }) {
                Text("¿Olvidaste tu contraseña?")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                thickness = 2.dp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            TextButton(onClick = {
                navController.navigate("register")
            }) {
                Text("Crear una cuenta", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}