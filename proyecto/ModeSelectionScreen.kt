package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.repository.GroupRepository
import com.mctrio.estudiapp.utils.UserPreferences
import kotlinx.coroutines.launch
import com.mctrio.estudiapp.data.models.GroupInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val userName by remember {
        mutableStateOf(UserPreferences.getUserName(context) ?: "Usuario")
    }

    val userGroups = remember { mutableStateListOf<GroupInfo>() }

    LaunchedEffect(Unit) {
        val userId = UserPreferences.getUserId(context)
        if (!userId.isNullOrEmpty()) {
            GroupRepository.sincronizarGruposConBackend(context, userId)
            val updatedList = UserPreferences.getGroupList(context)
            userGroups.clear()
            userGroups.addAll(updatedList)
        }
    }

    // AQUI ES DONDE EMPIEZA TU COLUMN PRINCIPAL, SIN SCAFFOLD NI TOPAPPBAR AQUI.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hola $userName!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "¿Cómo quieres estudiar hoy?",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Opción 1: Modo Individual ---
        SelectionCard(
            title = "Modo Individual",
            description = "Organiza tus materias y temarios personales",
            icon = Icons.Filled.Person,
            onClick = {
                UserPreferences.saveSelectedMode(context, "individual")
                Toast.makeText(context, "Modo Individual Seleccionado", Toast.LENGTH_SHORT).show()
                navController.navigate("dashboard/individual") {
                    popUpTo("modeSelection") { inclusive = true }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Opción 2: Modo Grupal (Ahora siempre navega a groupModeSelection) ---
        SelectionCard(
            title = "Modo Grupal",
            description = "Crea o únete a grupos, o gestiona tus clases existentes", // Descripción más genérica
            icon = Icons.Filled.Group,
            onClick = {
                UserPreferences.saveSelectedMode(context, "grupal") // Marcar el modo como grupal
                Toast.makeText(context, "Modo Grupal Seleccionado", Toast.LENGTH_SHORT).show()

                navController.navigate("groupModeSelection") {
                    popUpTo("modeSelection") { inclusive = false } // No limpiamos modeSelection, para que puedan volver
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = description, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}