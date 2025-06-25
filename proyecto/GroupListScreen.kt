package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.data.models.GroupInfo
import com.mctrio.estudiapp.utils.UserPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userGroups = remember { mutableStateListOf<GroupInfo>() }

    LaunchedEffect(Unit) {
        val storedGroups = UserPreferences.getGroupList(context)
        userGroups.clear()
        userGroups.addAll(storedGroups)

        if (userGroups.isEmpty()) {
            Toast.makeText(context, "No tienes clases grupales inscritas.", Toast.LENGTH_SHORT).show()
            // Considera si quieres navegar de vuelta automáticamente o mostrar un mensaje permanente.
            // navController.popBackStack() // Si esto se hace muy rápido, el Toast podría no verse.
        }
    }

    // --- ¡REINTODUCIMOS EL SCAFFOLD AQUÍ! ---
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Clases Grupales") }) // Puedes tener un título específico aquí
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // <-- ¡Aplicamos el padding del Scaffold aquí!
                .padding(horizontal = 16.dp), // Mantén tu padding horizontal adicional
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userGroups.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                Text("Cargando tus clases...", modifier = Modifier.padding(top = 16.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp)) // Este Spacer puede ser opcional ahora con el padding
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(userGroups) { group ->
                        GroupListItem(group = group) { selectedGroup ->
                            coroutineScope.launch {
                                UserPreferences.saveSelectedMode(context, "grupal")
                                UserPreferences.saveSelectedGroupId(context, selectedGroup.id_grupo) // Guarda el ID del grupo seleccionado
                                navController.navigate("dashboard/grupal?groupId=${selectedGroup.id_grupo}") {
                                    popUpTo("groupListScreen") { inclusive = true }
                                }
                                Toast.makeText(context, "Accediendo a ${selectedGroup.nombre_grupo}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupListItem(group: GroupInfo, onClick: (GroupInfo) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(group) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Group,
                contentDescription = "Icono de Grupo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.nombre_grupo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Código: ${group.codigo_grupo}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (group.es_admin) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Administrador",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}