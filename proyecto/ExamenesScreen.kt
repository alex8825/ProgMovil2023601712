package com.mctrio.estudiapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School // Icono para exámenes, puedes cambiarlo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState // Importa esto
import com.mctrio.estudiapp.data.models.Examen
import com.mctrio.estudiapp.viewmodel.ExamenesViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamenesScreen(navController: NavController, examenesViewModel: ExamenesViewModel = viewModel()) {
    val examenes by examenesViewModel.examenes.collectAsState()

    // Obtener el estado de la entrada actual del back stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Cargar exámenes cada vez que la pantalla vuelve al foco
    LaunchedEffect(navBackStackEntry) {
        examenesViewModel.loadExamenes()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Exámenes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addExamen") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Examen")
            }
        }
    ) { paddingValues ->
        if (examenes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay exámenes. ¡Añade uno!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(examenes) { examen ->
                    ExamenItem(
                        examen = examen,
                        onEditClick = { navExamen ->
                            navController.navigate("editExamen/${navExamen.id}")
                        },
                        onDeleteClick = { navExamen ->
                            examenesViewModel.deleteExamen(navExamen.id!!)
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun ExamenItem(
    examen: Examen,
    onEditClick: (Examen) -> Unit,
    onDeleteClick: (Examen) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick(examen) }, // Hacemos la tarjeta clickeable para editar
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
                Text(examen.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Fecha: ${examen.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} ${examen.hora.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tipo: ${examen.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Estado: ${examen.estado ?: "Desconocido"}", // Muestra "Desconocido" si el estado es nulo
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Materia ID: ${examen.materia_id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onEditClick(examen) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Examen", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { onDeleteClick(examen) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Examen", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}