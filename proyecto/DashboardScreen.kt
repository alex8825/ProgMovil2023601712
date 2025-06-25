package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.utils.UserPreferences
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate
import java.time.YearMonth
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.mctrio.estudiapp.data.models.GroupInfo
import com.mctrio.estudiapp.data.remote.RetrofitClient
import com.mctrio.estudiapp.repository.AppRepository
import com.mctrio.estudiapp.ui.viewmodels.DashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mctrio.estudiapp.utils.LocalPreferenceHelper
import com.mctrio.estudiapp.data.models.Examen
import com.mctrio.estudiapp.data.models.Tarea
import com.mctrio.estudiapp.data.models.Proyecto
import com.mctrio.estudiapp.data.models.Evento
import com.mctrio.estudiapp.ui.viewmodels.DashboardViewModelFactory
import kotlinx.coroutines.flow.collectLatest // Import collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    currentMode: String, // 'individual' or 'grupal'
    groupId: String?     // The group ID, if in group mode
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val apiService = remember { RetrofitClient.getApiService(context) }
    val repository = remember { AppRepository(apiService) }
    val dashboardViewModel: DashboardViewModel = viewModel(factory = remember { DashboardViewModelFactory(repository) })


    val userName = UserPreferences.getUserName(context) ?: "Usuario"
    val userId = UserPreferences.getUserId(context) ?: ""
    val currentGroupInfo = remember { mutableStateOf<GroupInfo?>(null) }

    // Observe ViewModel states
    val materias by dashboardViewModel.materias.collectAsState()
    val examenes by dashboardViewModel.examenes.collectAsState()
    val tareas by dashboardViewModel.tareas.collectAsState()
    val proyectos by dashboardViewModel.proyectos.collectAsState()
    val eventos by dashboardViewModel.eventos.collectAsState()
    val tasksCount by dashboardViewModel.tasksCount.collectAsState()
    val examsCount by dashboardViewModel.examsCount.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    val errorMessage by dashboardViewModel.errorMessage.collectAsState()

    // --- LOGIC FOR NAVIGATING ON IP ERROR IS ADDED HERE ---
    LaunchedEffect(Unit) {
        dashboardViewModel.navigateToIpConfig.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            // Navigate to the IP configuration screen.
            // *** IMPORTANT: REPLACE "settings_screen_route" WITH THE ACTUAL ROUTE OF YOUR SETTINGS/IP SCREEN ***
            navController.navigate("settings_screen_route") {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
            }
        }
    }
    // -------------------------------------------------------

    LaunchedEffect(userId, currentMode, groupId) {
        if (userId.isNotEmpty()) {
            dashboardViewModel.loadDashboardData(userId, currentMode, groupId)
        } else {
            Toast.makeText(context, "No se pudo obtener el ID de usuario.", Toast.LENGTH_LONG).show()
            navController.navigate("login") { popUpTo("dashboard/{modo}") { inclusive = true } }
        }

        val ruta = if (currentMode == "grupal" && !groupId.isNullOrEmpty()) {
            "dashboard/grupal?groupId=$groupId"
        } else {
            "dashboard/individual"
        }
        UserPreferences.saveLastRoute(context, ruta)
        if (!groupId.isNullOrEmpty()) {
            UserPreferences.saveSelectedGroupId(context, groupId)
        } else {
            UserPreferences.saveSelectedGroupId(context, null)
        }
    }

    LaunchedEffect(currentMode, groupId) {
        if (currentMode == "grupal" && !groupId.isNullOrEmpty()) {
            val groups = UserPreferences.getGroupList(context)
            val foundGroup = groups.find { it.id_grupo == groupId }
            currentGroupInfo.value = foundGroup
        } else {
            currentGroupInfo.value = null
        }
    }

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            dashboardViewModel.clearErrorMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 18.dp)
    ) {
        Text(
            text = "Hola, $userName!. Tu día de estudio.",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Hoy tienes $tasksCount tareas pendientes y $examsCount examen próximo.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )
        }

        // --- Calendar integration ---
        AppCalendarView(
            examenes = examenes,
            tareas = tareas,
            proyectos = proyectos,
            eventos = eventos
        )
        // -------------------------------------

        Spacer(modifier = Modifier.height(16.dp))

        Text("Próximos Eventos", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        val allUpcomingEvents = remember(examenes, tareas, proyectos, eventos) {
            val today = LocalDate.now()
            val eventsList = mutableListOf<Triple<LocalDate, String, String>>()

            examenes.filter { it.fecha.isAfter(today.minusDays(1)) }
                .sortedBy { it.fecha }
                .forEach { examen ->
                    val horaExamenFormatted = examen.hora?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "N/A"
                    eventsList.add(Triple(
                        examen.fecha,
                        examen.nombre,
                        "Materia: ${materias.find { m -> m.id == examen.materia_id }?.nombre ?: examen.materia_id}, Tipo: ${examen.tipo}, Hora: $horaExamenFormatted"
                    ))
                }

            tareas.forEach { tarea ->
                val tareaFecha = try {
                    LocalDate.parse(tarea.fecha_limite, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    println("Error parsing tarea.fecha_limite in DashboardScreen: ${e.message}")
                    null
                }
                val tareaHora = try {
                    LocalTime.parse(tarea.hora_limite, DateTimeFormatter.ISO_LOCAL_TIME)
                } catch (e: Exception) {
                    println("Error parsing tarea.hora_limite in DashboardScreen: ${e.message}")
                    null
                }
                val horaTareaFormatted = tareaHora?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "N/A"

                if (tareaFecha != null && !(tarea.completada ?: false) && tareaFecha.isAfter(today.minusDays(1))) {
                    eventsList.add(Triple(
                        tareaFecha,
                        tarea.nombre,
                        "Materia: ${materias.find { m -> m.id == tarea.materia_id }?.nombre ?: tarea.materia_id}, Prioridad: ${tarea.prioridad}${if (!tarea.descripcion.isNullOrBlank()) ", Descripción: ${tarea.descripcion}" else ""}, Hora: ${horaTareaFormatted}"
                    ))
                }
            }
            proyectos.filter { proyecto ->
                try {
                    val projectDate = LocalDate.parse(proyecto.fechaLimite, DateTimeFormatter.ISO_LOCAL_DATE)
                    projectDate.isAfter(today.minusDays(1))
                } catch (e: Exception) {
                    println("Error parsing project date in Dashboard: ${proyecto.fechaLimite} - ${e.message}")
                    false
                }
            }
                .sortedBy { proyecto ->
                    try {
                        LocalDate.parse(proyecto.fechaLimite, DateTimeFormatter.ISO_LOCAL_DATE)
                    } catch (e: Exception) {
                        LocalDate.MAX
                    }
                }
                .forEach { proyecto ->
                    val horaProyectoFormatted = try {
                        LocalTime.parse(proyecto.horaLimite, DateTimeFormatter.ISO_LOCAL_TIME).format(DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        println("Error parsing project time in Dashboard: ${proyecto.horaLimite} - ${e.message}")
                        "N/A"
                    }

                    val fechaProyectoParsed = try {
                        LocalDate.parse(proyecto.fechaLimite, DateTimeFormatter.ISO_LOCAL_DATE)
                    } catch (e: Exception) {
                        println("Error parsing project date for Triple in Dashboard: ${proyecto.fechaLimite} - ${e.message}")
                        LocalDate.now()
                    }

                    eventsList.add(Triple(
                        fechaProyectoParsed,
                        proyecto.nombre,
                        "Materia: ${materias.find { m -> m.id == proyecto.materia_id }?.nombre ?: proyecto.materia_id}, Estado: ${proyecto.estado}, Hora: $horaProyectoFormatted"
                    ))
                }
            eventsList.sortedBy { it.first }.take(5)
        }


        if (allUpcomingEvents.isNotEmpty()) {
            allUpcomingEvents.forEach { (date, name, details) ->
                EventCard(
                    eventName = name,
                    eventDate = "Fecha: ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    eventDetails = details,
                    icon = Icons.Filled.Menu
                )
            }
        } else {
            Text("No hay eventos próximos.", modifier = Modifier.padding(vertical = 8.dp))
        }

        if (currentMode == "grupal") {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Modo Grupal Activo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val actualGroupCode = currentGroupInfo.value?.codigo_grupo
            val actualGroupName = currentGroupInfo.value?.nombre_grupo

            if (!actualGroupCode.isNullOrEmpty() && !actualGroupName.isNullOrEmpty()) {
                Text(
                    text = "Estás en el grupo: ${actualGroupName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Código: ", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = actualGroupCode,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(actualGroupCode))
                            Toast.makeText(context, "Código copiado: $actualGroupCode", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar código de grupo")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { /* Navegar a miembros del grupo */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver Miembros del Grupo")
                }
            } else {
                Text(
                    "No se ha podido cargar la información del grupo o no tienes un grupo seleccionado.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(onClick = { navController.navigate("groupModeSelection") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Unirse/Crear Otro Grupo")
                }
            }
        }
    }
}

// New Composable for the calendar
@Composable
fun AppCalendarView(
    examenes: List<Examen>,
    tareas: List<Tarea>,
    proyectos: List<Proyecto>,
    eventos: List<Evento>
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7 // Monday=0, Tuesday=1... Sunday=6
    val adjustedFirstDay = (firstDayOfWeek % 7) // Adjustment for week starting on Monday if DayOfWeek.MONDAY=1

    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = LocalPreferenceHelper.current
    val selectedColorHex = prefs.themeColor.value
    val selectedColor = Color(android.graphics.Color.parseColor(selectedColorHex))

    // Combine all event dates to mark them on the calendar
    val allEventDates = remember(examenes, tareas, proyectos, eventos) {
        val dates = mutableSetOf<LocalDate>()
        dates.addAll(examenes.map { it.fecha })

        dates.addAll(tareas.mapNotNull {
            try {
                LocalDate.parse(it.fecha_limite, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                println("Error parsing tarea.fecha_limite for calendar: ${e.message}")
                null
            }
        })
        dates.addAll(proyectos.mapNotNull {
            try {
                LocalDate.parse(it.fechaLimite, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                println("Error parsing proyecto.fechaLimite for calendar: ${e.message}")
                null
            }
        })
        dates.addAll(eventos.map { it.fecha })
        dates
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = selectedColor.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with month
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Mes anterior",
                        tint = selectedColor
                    )
                }
                Text(
                    "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = selectedColor
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Mes siguiente",
                        tint = selectedColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days of the week
            val diasSemana = listOf("D", "L", "Ma", "Mi", "J", "V", "S") // Starting on Sunday
            Row(modifier = Modifier.fillMaxWidth()) {
                diasSemana.forEach {
                    Text(
                        it,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = selectedColor
                    )
                }
            }

            // Month cells
            val totalCells = adjustedFirstDay + daysInMonth
            val rows = (totalCells / 7) + if (totalCells % 7 > 0) 1 else 0

            Column {
                repeat(rows) { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (day in 0..6) {
                            val index = week * 7 + day
                            if (index < adjustedFirstDay || index >= adjustedFirstDay + daysInMonth) {
                                Spacer(modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp))
                            } else {
                                val dayNumber = index - adjustedFirstDay + 1
                                val date = currentMonth.atDay(dayNumber)
                                val hasEvent = allEventDates.contains(date)
                                val isToday = date == today

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clickable {
                                            selectedDay = date
                                            showDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            dayNumber.toString(),
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isToday -> MaterialTheme.colorScheme.primary
                                                hasEvent -> selectedColor
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        if (hasEvent) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        selectedColor,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Day events dialog
    if (showDialog && selectedDay != null) {
        val examenesDelDia = examenes.filter { it.fecha == selectedDay }

        val tareasDelDia = tareas.filter {
            try {
                LocalDate.parse(it.fecha_limite, DateTimeFormatter.ISO_LOCAL_DATE) == selectedDay
            } catch (e: Exception) {
                println("Error parsing tarea.fecha_limite for dialog filter: ${e.message}")
                false
            }
        }

        val proyectosDelDia = proyectos.filter {
            try {
                LocalDate.parse(it.fechaLimite, DateTimeFormatter.ISO_LOCAL_DATE) == selectedDay
            } catch (e: Exception) {
                println("Error parsing proyecto.fechaLimite for dialog filter: ${e.message}")
                false
            }
        }

        val eventosDelDia = eventos.filter { it.fecha == selectedDay }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eventos del ${selectedDay!!.dayOfMonth}/${selectedDay!!.monthValue}", color = selectedColor) },
            text = {
                Column {
                    if (examenesDelDia.isEmpty() && tareasDelDia.isEmpty() && proyectosDelDia.isEmpty() && eventosDelDia.isEmpty()) {
                        Text("No hay eventos para este día.", color = selectedColor)
                    } else {
                        examenesDelDia.forEach { ex ->
                            val horaExamenFormatted = ex.hora?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "N/A"
                            Text("Examen: ${ex.nombre} (${ex.materia_id}) - ${horaExamenFormatted}", fontSize = 14.sp, color = selectedColor)
                        }
                        tareasDelDia.forEach { t ->
                            val tareaHora = try {
                                LocalTime.parse(t.hora_limite, DateTimeFormatter.ISO_LOCAL_TIME)
                            } catch (e: Exception) {
                                println("Error parsing tarea.hora_limite for dialog display: ${e.message}")
                                null
                            }
                            val horaTareaFormatted = tareaHora?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "N/A"
                            Text("Tarea: ${t.nombre} (${t.materia_id}) - Prioridad: ${t.prioridad}, Hora: ${horaTareaFormatted}", fontSize = 14.sp, color = selectedColor)
                        }
                        proyectosDelDia.forEach { p ->
                            val horaProyectoFormatted = try {
                                LocalTime.parse(p.horaLimite, DateTimeFormatter.ISO_LOCAL_TIME).format(DateTimeFormatter.ofPattern("HH:mm"))
                            } catch (e: Exception) {
                                println("Error parsing proyecto.horaLimite for dialog display: ${e.message}")
                                "N/A"
                            }
                            Text("Proyecto: ${p.nombre} (${p.materia_id}) - Estado: ${p.estado}, Hora: ${horaProyectoFormatted}", fontSize = 14.sp, color = selectedColor)
                        }
                        eventosDelDia.forEach { e ->
                            Text("Evento: ${e.nombre}", fontSize = 14.sp, color = selectedColor)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar", color = selectedColor)
                }
            }
        )
    }

}

@Composable
fun EventCard(eventName: String, eventDate: String, eventDetails: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp).padding(end = 8.dp))
            Column {
                Text(eventName, style = MaterialTheme.typography.titleMedium)
                Text(eventDate, style = MaterialTheme.typography.bodyMedium)
                Text(eventDetails, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}