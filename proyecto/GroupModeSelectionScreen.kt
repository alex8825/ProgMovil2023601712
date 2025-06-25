package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.data.remote.ApiService
import com.mctrio.estudiapp.data.remote.RetrofitClient
import com.mctrio.estudiapp.data.models.CreateGroupResponse
import com.mctrio.estudiapp.data.models.JoinGroupResponse
import com.mctrio.estudiapp.data.models.GroupInfo
import com.mctrio.estudiapp.utils.UserPreferences
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// IMPORTS PARA EL SCROLL
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupModeSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var groupCodeToJoin by remember { mutableStateOf("") }
    var newGroupName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // No Scaffold interno, el padding principal debe venir del Scaffold de AppNavigation
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp) // Mantén tu padding propio para el contenido interno
            .verticalScroll(rememberScrollState()), // <-- ¡AÑADIDO AQUÍ!
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Esto centrará el contenido si cabe en la pantalla sin scroll
    ) {
        Text(
            text = "Gestiona tus grupos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Sección para Unirse a un Grupo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Unirse a un Grupo Existente", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = groupCodeToJoin,
                    onValueChange = { groupCodeToJoin = it },
                    label = { Text("Código de Grupo") },
                    leadingIcon = { Icon(Icons.Filled.GroupAdd, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (groupCodeToJoin.isBlank()) {
                            errorMessage = "Ingresa un código de grupo."
                            return@Button
                        }
                        errorMessage = ""

                        val userId = UserPreferences.getUserId(context)
                        if (userId.isNullOrEmpty()) {
                            Toast.makeText(context, "Error: Usuario no logueado.", Toast.LENGTH_SHORT).show()
                            navController.navigate("login")
                            return@Button
                        }

                        val ipAddress = UserPreferences.getIpAddress(context)
                        if (ipAddress.isNullOrEmpty()) {
                            Toast.makeText(context, "Por favor, configura la IP del servidor primero.", Toast.LENGTH_LONG).show()
                            navController.navigate("ipConfig")
                            return@Button
                        }

                        val apiService = RetrofitClient.getInstance(context).create(ApiService::class.java)
                        apiService.joinGroup(groupCodeToJoin, userId).enqueue(object : Callback<JoinGroupResponse> {
                            override fun onResponse(call: Call<JoinGroupResponse>, response: Response<JoinGroupResponse>) {
                                if (response.isSuccessful) {
                                    val joinResponse = response.body()
                                    if (joinResponse?.estado == "ok") {
                                        scope.launch {
                                            Toast.makeText(context, joinResponse.mensaje, Toast.LENGTH_SHORT).show()
                                            UserPreferences.saveSelectedMode(context, "grupal")
                                            joinResponse.id_grupo?.let { id ->
                                                joinResponse.codigo_grupo?.let { code ->
                                                    joinResponse.nombre_grupo?.let { name ->
                                                        val newGroupInfo = GroupInfo(id, name, code, false)
                                                        UserPreferences.addGroupToList(context, newGroupInfo)
                                                    }
                                                }
                                            }
                                            navController.navigate("dashboard/grupal?groupId=${joinResponse.id_grupo}") {
                                                popUpTo("modeSelection") { inclusive = false }
                                            }
                                        }
                                    } else {
                                        errorMessage = joinResponse?.mensaje ?: "Error al unirse al grupo."
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    errorMessage = "Error de servidor: ${response.code()}"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<JoinGroupResponse>, t: Throwable) {
                                errorMessage = "Error de red: ${t.message}"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Unirse", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(24.dp))

        // Sección para Crear un Grupo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Crear Nuevo Grupo", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Nombre del Grupo") },
                    leadingIcon = { Icon(Icons.Filled.GroupAdd, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newGroupName.isBlank()) {
                            errorMessage = "Ingresa un nombre para el grupo."
                            return@Button
                        }
                        errorMessage = ""

                        val userId = UserPreferences.getUserId(context)
                        if (userId.isNullOrEmpty()) {
                            Toast.makeText(context, "Error: Usuario no logueado.", Toast.LENGTH_SHORT).show()
                            navController.navigate("login")
                            return@Button
                        }

                        val ipAddress = UserPreferences.getIpAddress(context)
                        if (ipAddress.isNullOrEmpty()) {
                            Toast.makeText(context, "Por favor, configura la IP del servidor primero.", Toast.LENGTH_LONG).show()
                            navController.navigate("ipConfig")
                            return@Button
                        }

                        val apiService = RetrofitClient.getInstance(context).create(ApiService::class.java)
                        apiService.createGroup(userId, newGroupName).enqueue(object : Callback<CreateGroupResponse> {
                            override fun onResponse(call: Call<CreateGroupResponse>, response: Response<CreateGroupResponse>) {
                                if (response.isSuccessful) {
                                    val createResponse = response.body()
                                    if (createResponse?.estado == "ok") {
                                        scope.launch {
                                            Toast.makeText(context, createResponse.mensaje, Toast.LENGTH_SHORT).show()
                                            UserPreferences.saveSelectedMode(context, "grupal")
                                            createResponse.id_grupo?.let { id ->
                                                createResponse.codigo_grupo?.let { code ->
                                                    createResponse.nombre_grupo?.let { name ->
                                                        val newGroupInfo = GroupInfo(id, name, code, true)
                                                        UserPreferences.addGroupToList(context, newGroupInfo)
                                                    }
                                                }
                                            }
                                            navController.navigate("dashboard/grupal?groupId=${createResponse.id_grupo}") {
                                                popUpTo("modeSelection") { inclusive = false }
                                            }
                                        }
                                    } else {
                                        errorMessage = createResponse?.mensaje ?: "Error al crear el grupo."
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    errorMessage = "Error de servidor: ${response.code()}"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<CreateGroupResponse>, t: Throwable) {
                                errorMessage = "Error de red: ${t.message}"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Crear Grupo", fontSize = 16.sp)
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("groupListScreen") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text("Ver Mis Clases Grupales")
        }
    }
}