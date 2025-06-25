package com.mctrio.estudiapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // <--- Ensure this import exists
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mctrio.estudiapp.data.models.Proyecto
import com.mctrio.estudiapp.viewmodel.ProyectosViewModel
import java.time.LocalDate // <--- Add this import!
import java.time.LocalTime // <--- Add this import!
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProyectosScreen(navController: NavController, proyectosViewModel: ProyectosViewModel = viewModel()) {
    val proyectos by proyectosViewModel.proyectos.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        proyectosViewModel.loadProyectos()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Proyectos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addProyecto") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Proyecto")
            }
        }
    ) { paddingValues ->
        if (proyectos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay proyectos. ¡Añade uno!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(proyectos) { proyecto ->
                    ProyectoItem(
                        proyecto = proyecto,
                        onEditClick = { navProyecto ->
                            navController.navigate("editProyecto/${navProyecto.id}")
                        },
                        onDeleteClick = { navProyecto ->
                            proyectosViewModel.deleteProyecto(navProyecto.id!!)
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun ProyectoItem(
    proyecto: Proyecto,
    onEditClick: (Proyecto) -> Unit,
    onDeleteClick: (Proyecto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(), // Removed .clickable here if you want edit icon to be primary edit action
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(proyecto.nombre, style = MaterialTheme.typography.titleMedium)
                // --- CRITICAL CHANGES HERE: Parse String to LocalDate/LocalTime for display ---
                val displayDate = try {
                    LocalDate.parse(proyecto.fechaLimite).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e: Exception) {
                    "Fecha inválida" // Handle parsing error
                }

                val displayTime = try {
                    LocalTime.parse(proyecto.horaLimite).format(DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e: Exception) {
                    "Hora inválida" // Handle parsing error
                }

                Text(
                    "Fecha Límite: $displayDate $displayTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // --- END CRITICAL CHANGES ---
                Text(
                    "Estado: ${proyecto.estado}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Materia ID: ${proyecto.materia_id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onEditClick(proyecto) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Proyecto", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { onDeleteClick(proyecto) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Proyecto", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}