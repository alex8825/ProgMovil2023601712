package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.data.models.RegisterResponse
import com.mctrio.estudiapp.data.remote.ApiService
import com.mctrio.estudiapp.data.remote.RetrofitClient
import com.mctrio.estudiapp.utils.UserPreferences // Import UserPreferences
import kotlinx.coroutines.launch // Import for coroutineScope.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class) // Added this annotation for Material3 components
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope() // Create a CoroutineScope

    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícono y título superior
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Ícono Crear Cuenta",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Icono Nombre")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Icono Correo")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Icono Contraseña")
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmarContrasena,
            onValueChange = { confirmarContrasena = it },
            label = { Text("Confirmar contraseña") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Icono Confirmar contraseña")
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank() || confirmarContrasena.isBlank()) {
                    errorMessage = "Completa todos los campos"
                    return@Button
                } else if (!correo.matches(emailPattern)) {
                    errorMessage = "Correo inválido"
                    return@Button
                } else if (contrasena != confirmarContrasena) {
                    errorMessage = "Las contraseñas no coinciden"
                    return@Button
                } else {
                    errorMessage = "" // Clear error message if validation passes

                    // Check IP address before making API call
                    val ipAddress = UserPreferences.getIpAddress(context)
                    if (ipAddress.isNullOrEmpty()) {
                        Toast.makeText(context, "Por favor, configura la IP del servidor primero.", Toast.LENGTH_LONG).show()
                        navController.navigate("ipConfig")
                        return@Button
                    }

                    val apiService = RetrofitClient.getInstance(context).create(ApiService::class.java)
                    val call = apiService.register(nombre, correo, contrasena)

                    call.enqueue(object : Callback<RegisterResponse> {
                        override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                            if (response.isSuccessful) {
                                val registerResponse = response.body()
                                if (registerResponse?.estado == "ok") {
                                    coroutineScope.launch { // Launch a coroutine to call suspend functions
                                        Toast.makeText(context, registerResponse.mensaje, Toast.LENGTH_SHORT).show()
                                        navController.navigate("login") { // Navigate to login after successful registration
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }
                                } else {
                                    errorMessage = registerResponse?.mensaje ?: "Error desconocido"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            } else {
                                errorMessage = "Error de servidor: ${response.code()}"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                            errorMessage = "Error de red: ${t.message}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            t.printStackTrace() // For debugging
                        }
                    })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Registrar", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { navController.popBackStack() }) { // Go back to login
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}