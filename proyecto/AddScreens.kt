package com.mctrio.estudiapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.LocalTime

import com.mctrio.estudiapp.data.models.Materia
import com.mctrio.estudiapp.viewmodel.MateriasViewModel
import com.mctrio.estudiapp.viewmodel.TareasViewModel
import com.mctrio.estudiapp.viewmodel.ProyectosViewModel
import com.mctrio.estudiapp.viewmodel.ExamenesViewModel

import com.mctrio.estudiapp.utils.DatePickerButton
import com.mctrio.estudiapp.utils.TimePickerButton
import com.mctrio.estudiapp.utils.MateriaDropdown
import com.mctrio.estudiapp.utils.PrioridadDropdown
import com.mctrio.estudiapp.utils.TipoExamenDropdown
import com.mctrio.estudiapp.utils.EstadoProyectoDropdown
import com.mctrio.estudiapp.utils.EstadoExamenDropdown

// Imports para el scroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// --- AddMateriaScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMateriaScreen(navController: NavController, materiasViewModel: MateriasViewModel = viewModel()) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Añadir Nueva Materia") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // <-- ¡AÑADIDO AQUÍ!
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Materia") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        materiasViewModel.addMateria(nombre.trim(), descripcion.trim().takeIf { it.isNotBlank() })
                        navController.popBackStack()
                    } else {
                        println("El nombre de la materia no puede estar vacío.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir Materia")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
// --- AddTareaScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTareaScreen(navController: NavController, tareasViewModel: TareasViewModel = viewModel(), materiasViewModel: MateriasViewModel = viewModel()) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var selectedMateriaId: String? by remember { mutableStateOf(null) }
    var fechaLimite: LocalDate? by remember { mutableStateOf(null) }
    var horaLimite: LocalTime? by remember { mutableStateOf(null) }
    var prioridad by remember { mutableStateOf("Media") }
    val materias by materiasViewModel.materias.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Añadir Nueva Tarea") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // <-- ¡AÑADIDO AQUÍ!
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Tarea") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(8.dp))
            MateriaDropdown(
                materias = materias,
                selectedMateriaId = selectedMateriaId,
                onMateriaSelected = { selectedMateriaId = it }
            )
            Spacer(Modifier.height(8.dp))

            DatePickerButton(
                selectedDate = fechaLimite,
                onDateSelected = { newDate -> fechaLimite = newDate }
            )
            Spacer(Modifier.height(8.dp))
            TimePickerButton(
                selectedTime = horaLimite,
                onTimeSelected = { newTime -> horaLimite = newTime }
            )
            Spacer(Modifier.height(8.dp))

            PrioridadDropdown(selectedPrioridad = prioridad) { prioridad = it }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && selectedMateriaId != null && fechaLimite != null && horaLimite != null) {
                        println("DEBUG: Intentando añadir tarea con:")
                        println("DEBUG: Nombre: $nombre")
                        println("DEBUG: Descripción: $descripcion")
                        println("DEBUG: Materia ID: $selectedMateriaId")
                        println("DEBUG: Fecha Límite: $fechaLimite")
                        println("DEBUG: Hora Límite: $horaLimite")
                        println("DEBUG: Prioridad: $prioridad")

                        tareasViewModel.addTarea(
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim().takeIf { it.isNotBlank() },
                            materiaId = selectedMateriaId!!,
                            fecha = fechaLimite!!,
                            hora = horaLimite!!,
                            prioridad = prioridad,
                            completada = false
                        )
                        navController.popBackStack()
                    } else {
                        println("Faltan campos obligatorios para añadir la tarea (incluyendo fecha y hora).")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir Tarea")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}

// --- AddProyectoScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProyectoScreen(navController: NavController, proyectosViewModel: ProyectosViewModel = viewModel(), materiasViewModel: MateriasViewModel = viewModel()) {
    var nombre by remember { mutableStateOf("") }
    var selectedMateriaId: String? by remember { mutableStateOf(null) }
    var fechaLimite: LocalDate? by remember { mutableStateOf(null) }
    var horaLimite: LocalTime? by remember { mutableStateOf(null) }
    var estado by remember { mutableStateOf("Activo") }
    val materias by materiasViewModel.materias.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Añadir Nuevo Proyecto") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // <-- ¡AÑADIDO AQUÍ!
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Proyecto") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            MateriaDropdown(
                materias = materias,
                selectedMateriaId = selectedMateriaId,
                onMateriaSelected = { selectedMateriaId = it }
            )
            Spacer(Modifier.height(8.dp))

            DatePickerButton(
                selectedDate = fechaLimite,
                onDateSelected = { newDate -> fechaLimite = newDate }
            )
            Spacer(Modifier.height(8.dp))
            TimePickerButton(
                selectedTime = horaLimite,
                onTimeSelected = { newTime -> horaLimite = newTime }
            )
            Spacer(Modifier.height(8.dp))

            EstadoProyectoDropdown(selectedEstado = estado) { estado = it }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && selectedMateriaId != null && fechaLimite != null && horaLimite != null) {
                        proyectosViewModel.addProyecto(
                            nombre = nombre.trim(),
                            materiaId = selectedMateriaId!!,
                            fechaLimite = fechaLimite!!,
                            horaLimite = horaLimite!!,
                            estado = estado
                        )
                        navController.popBackStack()
                    } else {
                        println("Faltan campos obligatorios para añadir el proyecto.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir Proyecto")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamenScreen(navController: NavController, examenesViewModel: ExamenesViewModel = viewModel(), materiasViewModel: MateriasViewModel = viewModel()) {
    var nombre by remember { mutableStateOf("") }
    var selectedMateriaId: String? by remember { mutableStateOf(null) }
    var fecha: LocalDate? by remember { mutableStateOf(null) }
    var hora: LocalTime? by remember { mutableStateOf(null) }
    var tipo by remember { mutableStateOf("Parcial") }
    val materias by materiasViewModel.materias.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Añadir Nuevo Examen") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // <-- ¡AÑADIDO AQUÍ!
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Examen") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            MateriaDropdown(
                materias = materias,
                selectedMateriaId = selectedMateriaId,
                onMateriaSelected = { selectedMateriaId = it }
            )
            Spacer(Modifier.height(8.dp))

            DatePickerButton(
                selectedDate = fecha,
                onDateSelected = { newDate -> fecha = newDate }
            )
            Spacer(Modifier.height(8.dp))
            TimePickerButton(
                selectedTime = hora,
                onTimeSelected = { newTime -> hora = newTime }
            )
            Spacer(Modifier.height(8.dp))

            TipoExamenDropdown(selectedTipo = tipo) { tipo = it }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && selectedMateriaId != null && fecha != null && hora != null) {
                        examenesViewModel.addExamen(
                            nombre = nombre.trim(),
                            materiaId = selectedMateriaId!!,
                            fecha = fecha!!,
                            hora = hora!!,
                            tipo = tipo
                        )
                        navController.popBackStack()
                    } else {
                        println("Faltan campos obligatorios para añadir el examen.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir Examen")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}