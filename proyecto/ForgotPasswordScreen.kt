package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.data.remote.ApiService
import com.mctrio.estudiapp.data.remote.RetrofitClient
import com.mctrio.estudiapp.data.models.RecoverPasswordResponse // Asegúrate de importar este
import com.mctrio.estudiapp.utils.UserPreferences// Importa PreferenceId para la IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    // NUEVA VARIABLE DE ESTADO PARA ALMACENAR LA CONTRASEÑA RECUPERADA
    var recoveredPassword by remember { mutableStateOf<String?>(null) }

    val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA.Z]{2,4}".toRegex()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Ícono Recuperar Contraseña",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Recuperar Contraseña",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                recoveredPassword = null // Limpiar la contraseña mostrada si el usuario edita el email
            },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Icono Correo") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = errorMessage.isNotEmpty(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        recoveredPassword?.let { password ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tu contraseña es:",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = password, // Aquí se usa la 'contrasena' recibida
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                // Limpiar la contraseña mostrada y el mensaje de error al intentar una nueva recuperación
                recoveredPassword = null
                errorMessage = ""

                if (email.isBlank()) {
                    errorMessage = "Por favor, ingresa tu correo electrónico."
                    return@Button
                }
                if (!email.matches(emailPattern)) {
                    errorMessage = "Por favor, ingresa un correo electrónico válido."
                    return@Button
                }

                val ipAddress = UserPreferences.getIpAddress(context)
                if (ipAddress.isNullOrEmpty()) {
                    Toast.makeText(context, "Por favor, configura la IP del servidor primero.", Toast.LENGTH_LONG).show()
                    navController.navigate("ipConfig")
                    return@Button
                }

                isLoading = true

                val apiService = RetrofitClient.getInstance(context).create(ApiService::class.java)
                val call = apiService.recoverPassword(email)

                call.enqueue(object : Callback<RecoverPasswordResponse> {
                    override fun onResponse(call: Call<RecoverPasswordResponse>, response: Response<RecoverPasswordResponse>) {
                        isLoading = false

                        if (response.isSuccessful) {
                            val recoverResponse = response.body()
                            if (recoverResponse?.estado == "ok") {
                                // ASIGNAR LA CONTRASEÑA RECUPERADA USANDO EL NOMBRE 'contrasena'
                                recoveredPassword = recoverResponse.contrasena
                                Toast.makeText(context, recoverResponse.mensaje, Toast.LENGTH_LONG).show()
                                // No navegues de vuelta automáticamente si quieres que la contraseña se quede visible
                                // navController.popBackStack()
                            } else {
                                errorMessage = recoverResponse?.mensaje ?: "Error desconocido"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            errorMessage = "Error del servidor: ${response.code()} - ${response.message()}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<RecoverPasswordResponse>, t: Throwable) {
                        isLoading = false
                        errorMessage = "Error de red: ${t.message}. Revisa tu conexión."
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        t.printStackTrace()
                    }
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Recuperar Contraseña", fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isLoading
        ) {
            Text("Volver a Iniciar Sesión")
        }
    }
}