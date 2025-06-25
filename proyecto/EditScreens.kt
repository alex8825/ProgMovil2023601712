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

import com.mctrio.estudiapp.data.models.Materia // Necesario para MateriaDropdown
import com.mctrio.estudiapp.data.models.Tarea // Asegúrate de importar tus modelos
import com.mctrio.estudiapp.data.models.Proyecto
import com.mctrio.estudiapp.data.models.Examen

import com.mctrio.estudiapp.viewmodel.MateriasViewModel
import com.mctrio.estudiapp.viewmodel.TareasViewModel
import com.mctrio.estudiapp.viewmodel.ProyectosViewModel
import com.mctrio.estudiapp.viewmodel.ExamenesViewModel

// IMPORTS DE COMPONENTES COMPARTIDOS (Asegúrate que la ruta sea correcta)
import com.mctrio.estudiapp.utils.DatePickerButton
import com.mctrio.estudiapp.utils.TimePickerButton
import com.mctrio.estudiapp.utils.MateriaDropdown
import com.mctrio.estudiapp.utils.PrioridadDropdown
import com.mctrio.estudiapp.utils.TipoExamenDropdown
import com.mctrio.estudiapp.utils.EstadoProyectoDropdown
import com.mctrio.estudiapp.utils.EstadoExamenDropdown
import java.time.format.DateTimeFormatter

// Imports para el scroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// --- EditMateriaScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMateriaScreen(
    navController: NavController,
    materiaId: String?,
    materiasViewModel: MateriasViewModel = viewModel()
) {
    val materiaToEdit by materiasViewModel.getMateriaById(materiaId ?: "").collectAsState(initial = null)

    var nombre by remember(materiaToEdit) { mutableStateOf(materiaToEdit?.nombre ?: "") }
    var descripcion by remember(materiaToEdit) { mutableStateOf(materiaToEdit?.descripcion ?: "") }

    if (materiaToEdit == null && !materiaId.isNullOrBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            Text("Cargando materia...", modifier = Modifier.padding(top = 80.dp))
        }
        return
    }

    LaunchedEffect(materiaId, materiaToEdit) {
        if (materiaId.isNullOrBlank() || (materiaToEdit == null && !materiaId.isNullOrBlank())) {
            if (!materiaId.isNullOrBlank()) {
                println("No se encontró materia con ID: $materiaId. Volviendo atrás.")
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Materia") }) }
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
            Text("Editar Materia: ${materiaToEdit?.nombre ?: "Cargando..."}", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Materia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                    if (nombre.isNotBlank() && materiaId != null) {
                        materiasViewModel.updateMateria(
                            id = materiaId,
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim().ifBlank { null }
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
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

// --- EditTareaScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTareaScreen(
    navController: NavController,
    tareaId: String?,
    materiasViewModel: MateriasViewModel = viewModel(),
    tareasViewModel: TareasViewModel = viewModel()
) {
    val tareaToEdit by tareasViewModel.getTareaById(tareaId ?: "").collectAsState(initial = null)

    var nombre by remember(tareaToEdit) { mutableStateOf(tareaToEdit?.nombre ?: "") }
    var descripcion by remember(tareaToEdit) { mutableStateOf(tareaToEdit?.descripcion ?: "") }
    var selectedMateriaId by remember(tareaToEdit) { mutableStateOf<String?>(tareaToEdit?.materia_id) }

    var fechaLimite by remember(tareaToEdit) {
        mutableStateOf<LocalDate?>(
            tareaToEdit?.fecha_limite?.let {
                try {
                    LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    println("Error parsing fecha_limite for Tarea: ${e.message}")
                    null
                }
            }
        )
    }
    var horaLimite by remember(tareaToEdit) {
        mutableStateOf<LocalTime?>(
            tareaToEdit?.hora_limite?.let {
                try {
                    LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME)
                } catch (e: Exception) {
                    println("Error parsing hora_limite for Tarea: ${e.message}")
                    null
                }
            }
        )
    }

    var completada by remember(tareaToEdit) { mutableStateOf(tareaToEdit?.completada ?: false) }
    var prioridad by remember(tareaToEdit) { mutableStateOf(tareaToEdit?.prioridad ?: "Baja") }

    val materias by materiasViewModel.materias.collectAsState()

    if (tareaToEdit == null && !tareaId.isNullOrBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            Text("Cargando tarea...", modifier = Modifier.padding(top = 80.dp))
        }
        return
    }

    LaunchedEffect(tareaId, tareaToEdit) {
        if (tareaId.isNullOrBlank() || (tareaToEdit == null && !tareaId.isNullOrBlank())) {
            if (!tareaId.isNullOrBlank()) {
                println("No se encontró tarea con ID: $tareaId. Volviendo atrás.")
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Tarea") }) }
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
            Text("Editar Tarea: ${tareaToEdit?.nombre ?: "Cargando..."}", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Tarea") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DatePickerButton(
                    selectedDate = fechaLimite,
                    onDateSelected = { fechaLimite = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TimePickerButton(
                    selectedTime = horaLimite,
                    onTimeSelected = { horaLimite = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = completada, onCheckedChange = { completada = it })
                Text("Completada")
            }
            Spacer(Modifier.height(8.dp))

            PrioridadDropdown(selectedPrioridad = prioridad) { prioridad = it }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && selectedMateriaId != null && tareaId != null && fechaLimite != null && horaLimite != null) {
                        tareasViewModel.updateTarea(
                            id = tareaId,
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim().ifBlank { null },
                            materiaId = selectedMateriaId!!,
                            fecha = fechaLimite!!,
                            hora = horaLimite!!,
                            completada = completada,
                            prioridad = prioridad
                        )
                        navController.popBackStack()
                    } else {
                        println("Faltan campos para actualizar la tarea.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
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

// --- EditExamenScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExamenScreen(
    navController: NavController,
    examenId: String?,
    materiasViewModel: MateriasViewModel = viewModel(),
    examenesViewModel: ExamenesViewModel = viewModel()
) {
    val examenToEdit by examenesViewModel.getExamenById(examenId ?: "").collectAsState(initial = null)

    var nombre by remember(examenToEdit) { mutableStateOf(examenToEdit?.nombre ?: "") }
    var selectedMateriaId by remember(examenToEdit) { mutableStateOf<String?>(examenToEdit?.materia_id) }
    var fecha by remember(examenToEdit) { mutableStateOf<LocalDate?>(examenToEdit?.fecha) }
    var hora by remember(examenToEdit) { mutableStateOf<LocalTime?>(examenToEdit?.hora) }
    var tipo by remember(examenToEdit) { mutableStateOf(examenToEdit?.tipo ?: "Parcial") }
    var estado by remember(examenToEdit) { mutableStateOf(examenToEdit?.estado ?: "Próximo") }

    val materias by materiasViewModel.materias.collectAsState()

    if (examenToEdit == null && !examenId.isNullOrBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            Text("Cargando examen...", modifier = Modifier.padding(top = 80.dp))
        }
        return
    }

    LaunchedEffect(examenId, examenToEdit) {
        if (examenId.isNullOrBlank() || (examenToEdit == null && !examenId.isNullOrBlank())) {
            if (!examenId.isNullOrBlank()) {
                println("No se encontró examen con ID: $examenId. Volviendo atrás.")
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Examen") }) }
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
            Text("Editar Examen: ${examenToEdit?.nombre ?: "Cargando..."}", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Examen") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            MateriaDropdown(
                materias = materias,
                selectedMateriaId = selectedMateriaId,
                onMateriaSelected = { selectedMateriaId = it }
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DatePickerButton(
                    selectedDate = fecha,
                    onDateSelected = { fecha = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TimePickerButton(
                    selectedTime = hora,
                    onTimeSelected = { hora = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            TipoExamenDropdown(selectedTipo = tipo) { tipo = it }
            Spacer(Modifier.height(8.dp))

            EstadoExamenDropdown(selectedEstado = estado) { estado = it }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && selectedMateriaId != null && examenId != null && fecha != null && hora != null) {
                        examenesViewModel.updateExamen(
                            id = examenId,
                            nombre = nombre.trim(),
                            materiaId = selectedMateriaId!!,
                            fecha = fecha!!,
                            hora = hora!!,
                            tipo = tipo.trim(),
                            estado = estado
                        )
                        navController.popBackStack()
                    } else {
                        println("Faltan campos para actualizar el examen.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
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

// --- EditProyectoScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProyectoScreen(
    navController: NavController,
    proyectoId: String?,
    materiasViewModel: MateriasViewModel = viewModel(),
    proyectosViewModel: ProyectosViewModel = viewModel()
) {
    val proyectoToEdit by proyectosViewModel.getProyectoById(proyectoId ?: "")
        .collectAsState(initial = null)

    var nombre by remember(proyectoToEdit) { mutableStateOf(proyectoToEdit?.nombre ?: "") }
    var selectedMateriaId by remember(proyectoToEdit) { mutableStateOf<String?>(proyectoToEdit?.materia_id) }

    var fechaLimite by remember(proyectoToEdit) {
        mutableStateOf<LocalDate?>(
            proyectoToEdit?.fechaLimite?.let {
                try {
                    LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    println("Error parsing fechaLimite for Proyecto in EditScreen: ${e.message}")
                    null
                }
            }
        )
    }
    var horaLimite by remember(proyectoToEdit) {
        mutableStateOf<LocalTime?>(
            proyectoToEdit?.horaLimite?.let {
                try {
                    LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME)
                } catch (e: Exception) {
                    println("Error parsing horaLimite for Proyecto in EditScreen: ${e.message}")
                    null
                }
            }
        )
    }

    var estado by remember(proyectoToEdit) { mutableStateOf(proyectoToEdit?.estado ?: "Activo") }
    val materias by materiasViewModel.materias.collectAsState()

    if (proyectoToEdit == null && !proyectoId.isNullOrBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            Text("Cargando proyecto...", modifier = Modifier.padding(top = 80.dp))
        }
        return
    }

    LaunchedEffect(proyectoId, proyectoToEdit) {
        if (proyectoId.isNullOrBlank() || (proyectoToEdit == null && !proyectoId.isNullOrBlank())) {
            if (!proyectoId.isNullOrBlank()) {
                println("No se encontró proyecto con ID: $proyectoId. Volviendo atrás.")
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Proyecto") }) }
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
            Text("Editar Proyecto: ${proyectoToEdit?.nombre ?: "Cargando..."}", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Proyecto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            MateriaDropdown(
                materias = materias,
                selectedMateriaId = selectedMateriaId,
                onMateriaSelected = { selectedMateriaId = it }
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DatePickerButton(
                    selectedDate = fechaLimite,
                    onDateSelected = { fechaLimite = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TimePickerButton(
                    selectedTime = horaLimite,
                    onTimeSelected = { horaLimite = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            EstadoProyectoDropdown(selectedEstado = estado) { estado = it }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && selectedMateriaId != null && proyectoId != null && fechaLimite != null && horaLimite != null) {
                        proyectosViewModel.updateProyecto(
                            id = proyectoId,
                            nombre = nombre.trim(),
                            materiaId = selectedMateriaId!!,
                            fechaLimite = fechaLimite!!,
                            horaLimite = horaLimite!!,
                            estado = estado
                        )
                        navController.popBackStack()
                    } else {
                        println("Faltan campos para actualizar el proyecto.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
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