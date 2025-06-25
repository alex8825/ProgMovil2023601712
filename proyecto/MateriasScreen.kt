package com.mctrio.estudiapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState // Importa esto
import com.mctrio.estudiapp.data.models.Materia
import com.mctrio.estudiapp.viewmodel.MateriasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriasScreen(navController: NavController, materiasViewModel: MateriasViewModel = viewModel()) {
    val materias by materiasViewModel.materias.collectAsState()

    // Obtener el estado de la entrada actual del back stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Cargar materias cada vez que la pantalla vuelve al foco
    LaunchedEffect(navBackStackEntry) {
        materiasViewModel.loadMaterias()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Materias") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addMateria") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Materia")
            }
        }
    ) { paddingValues ->
        if (materias.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay materias. ¡Añade una!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(materias) { materia ->
                    MateriaItem(
                        materia = materia,
                        onEditClick = { navMateria ->
                            navController.navigate("editMateria/${navMateria.id}")
                        },
                        onDeleteClick = { navMateria ->
                            materiasViewModel.deleteMateria(navMateria.id)
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun MateriaItem(
    materia: Materia,
    onEditClick: (Materia) -> Unit,
    onDeleteClick: (Materia) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Podrías navegar a una pantalla de detalles de la materia si existiera */ },
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
                Text(materia.nombre, style = MaterialTheme.typography.titleMedium)
                materia.descripcion?.let {
                    if (it.isNotBlank()) {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row {
                IconButton(onClick = { onEditClick(materia) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Materia")
                }
                IconButton(onClick = { onDeleteClick(materia) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Materia", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}