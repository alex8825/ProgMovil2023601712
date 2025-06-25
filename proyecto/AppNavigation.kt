package com.mctrio.estudiapp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mctrio.estudiapp.utils.UserPreferences // Se usa UserPreferences para todas las prefs
import com.mctrio.estudiapp.data.models.GroupInfo
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var currentRouteBase by remember { mutableStateOf<String?>(null) }

    var appBarTitle by remember { mutableStateOf("EstudiApp") }
    var appBarActions by remember { mutableStateOf<(@Composable RowScope.() -> Unit)?>(null) }
    val currentGroupInfo = remember { mutableStateOf<GroupInfo?>(null) }

    LaunchedEffect(navController.currentBackStackEntryAsState().value) {
        val navBackStackEntry = navController.currentBackStackEntry
        val fullRoute = navBackStackEntry?.destination?.route
        currentRouteBase = fullRoute?.split("?")?.get(0)

        appBarActions = null
        currentGroupInfo.value = null

        when (currentRouteBase) {
            "ipConfig" -> appBarTitle = "Configuración de IP"
            "login" -> appBarTitle = "Iniciar Sesión"
            "register" -> appBarTitle = "Registrarse"
            "forgotPassword" -> appBarTitle = "Recuperar Contraseña"
            "modeSelection" -> appBarTitle = "Modo de Estudio"
            "groupModeSelection" -> appBarTitle = "Modo Grupal"
            "groupListScreen" -> appBarTitle = "Mis Clases Grupales"
            "dashboard/{modo}" -> {
                val modo = navBackStackEntry?.arguments?.getString("modo")
                val groupId = navBackStackEntry?.arguments?.getString("groupId")

                if (modo == "individual") {
                    appBarTitle = "Inicio Individual"
                    appBarActions = {
                        IconButton(onClick = {
                            scope.launch {
                                UserPreferences.saveSelectedMode(context, null)
                                UserPreferences.saveLastRoute(context, null)
                                UserPreferences.saveSelectedGroupId(context, null)
                                navController.navigate("modeSelection") {
                                    popUpTo("modeSelection") { inclusive = true }
                                }
                                Toast.makeText(context, "Modo individual finalizado", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = "Salir de Modo Individual", tint = Color.White)
                        }
                    }
                } else if (modo == "grupal" && !groupId.isNullOrEmpty()) {
                    val groups = UserPreferences.getGroupList(context)
                    val foundGroup = groups.find { it.id_grupo == groupId }
                    currentGroupInfo.value = foundGroup
                    appBarTitle = "Grupo: ${foundGroup?.nombre_grupo ?: "Cargando..."}"

                    // ACCIONES PARA MODO GRUPAL: COMPARTIR Y SALIR
                    appBarActions = {
                        // Icono para Compartir Código
                        IconButton(onClick = {
                            val groupCode = foundGroup?.codigo_grupo
                            if (!groupCode.isNullOrEmpty()) {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "¡Únete a mi grupo de estudio en EstudiApp con el código: $groupCode!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir código de grupo"))
                            } else {
                                Toast.makeText(context, "Código de grupo no disponible para compartir.", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir código de grupo", tint = Color.White)
                        }

                        // Icono para Salir de Grupo
                        IconButton(onClick = {
                            scope.launch {
                                UserPreferences.saveSelectedMode(context, null)
                                UserPreferences.saveLastRoute(context, null)
                                UserPreferences.saveSelectedGroupId(context, null)
                                navController.navigate("modeSelection") {
                                    popUpTo("dashboard/{modo}") { inclusive = true }
                                }
                                Toast.makeText(context, "Has salido del grupo ${foundGroup?.nombre_grupo ?: ""}", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = "Salir de Grupo", tint = Color.White)
                        }
                    }
                }
            }
            "materias" -> appBarTitle = "Mis Materias"
            "examenes" -> appBarTitle = "Mis Exámenes"
            "tareas" -> appBarTitle = "Mis Tareas"
            "proyectos" -> appBarTitle = "Mis Proyectos"
            "addMateria" -> appBarTitle = "Añadir Materia"
            "addExamen" -> appBarTitle = "Añadir Examen"
            "addTarea" -> appBarTitle = "Añadir Tarea"
            "addProyecto" -> appBarTitle = "Añadir Proyecto"
            "editMateria/{materiaId}" -> appBarTitle = "Editar Materia" // Actualizado para reflejar el ID
            "editExamen/{examenId}" -> appBarTitle = "Editar Examen"
            "editTarea/{tareaId}" -> appBarTitle = "Editar Tarea"
            "editProyecto/{proyectoId}" -> appBarTitle = "Editar Proyecto"
            "configuracion" -> appBarTitle = "Configuración"
            "ayuda" -> appBarTitle = "Ayuda"
            else -> appBarTitle = "EstudiApp"
        }

        if (fullRoute != null &&
            currentRouteBase != "login" &&
            currentRouteBase != "register" &&
            currentRouteBase != "ipConfig" &&
            currentRouteBase != "forgotPassword" &&
            currentRouteBase != "modeSelection" &&
            currentRouteBase != "groupModeSelection" &&
            currentRouteBase != "groupListScreen"
        ) {
            UserPreferences.saveLastRoute(context, fullRoute)
        }
    }

    val startDestination = remember {
        val ipConfigured = !UserPreferences.getIpAddress(context).isNullOrEmpty() // Usar UserPreferences
        val isLoggedIn = UserPreferences.getIsLoggedIn(context)
        val selectedMode = UserPreferences.getSelectedMode(context)
        val userGroups = UserPreferences.getGroupList(context)
        val lastRoute = UserPreferences.getLastRoute(context)
        val lastSelectedGroupId = UserPreferences.getSelectedGroupId(context)

        when {
            !ipConfigured -> "ipConfig"
            !isLoggedIn -> "login"
            isLoggedIn && !lastRoute.isNullOrEmpty() &&
                    (lastRoute.startsWith("dashboard/individual") || (lastRoute.startsWith("dashboard/grupal") && !lastSelectedGroupId.isNullOrEmpty())) -> {
                if (lastRoute.startsWith("dashboard/grupal") && !lastSelectedGroupId.isNullOrEmpty()) {
                    "dashboard/grupal?groupId=$lastSelectedGroupId"
                } else {
                    lastRoute
                }
            }
            isLoggedIn -> {
                when {
                    selectedMode == "grupal" && userGroups.isNotEmpty() -> "groupListScreen"
                    selectedMode == "individual" -> "dashboard/individual"
                    else -> "modeSelection"
                }
            }
            else -> "login"
        }
    }

    val routesWithNoTopAppBarAndDrawer = remember {
        listOf(
            "ipConfig",
            "login",
            "register",
            "forgotPassword"
        )
    }

    val routesWithoutGlobalBackButton = remember {
        listOf(
            "modeSelection",
            "dashboard/{modo}" // No mostrar botón de atrás en el dashboard principal
        )
    }

    val showGlobalTopAppBar = currentRouteBase !in routesWithNoTopAppBarAndDrawer
    val showDrawerMenuIcon = currentRouteBase == "dashboard/{modo}"

    val showGlobalBackButton = remember(currentRouteBase) {
        showGlobalTopAppBar &&
                !showDrawerMenuIcon && // Si no muestra menú, puede mostrar atrás
                currentRouteBase !in routesWithoutGlobalBackButton &&
                navController.previousBackStackEntry != null // Solo si hay una pantalla previa
    }

    // --- CAMBIO CLAVE: ModalNavigationDrawer ENVUELVE AL SCAFFOLD ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawerMenuIcon, // Solo permitir gestos si el icono de menú está visible
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(), // <--- systemBarsPadding() REMOVIDO DE AQUÍ
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column( // Esta columna es el contenedor principal del contenido del drawer que ahora manejará el scroll
                    modifier = Modifier
                        .fillMaxSize() // Ocupa todo el espacio disponible en el drawer sheet
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column( // Este es tu recuadro de perfil
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .padding(vertical = 24.dp)
                            .systemBarsPadding(), // <--- ¡APLICADO AQUÍ!
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "User Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            UserPreferences.getUserName(context) ?: "Invitado",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            UserPreferences.getUserEmail(context) ?: "Sin correo",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val options = remember { mutableStateListOf(
                        "Inicio" to Icons.Default.Home,
                        "Materias" to Icons.Default.Book,
                        "Examenes" to Icons.Default.Assignment,
                        "Tareas" to Icons.Default.CheckCircle,
                        "Proyectos" to Icons.Default.Folder,
                        "Configuracion" to Icons.Default.Settings,
                        "Ayuda" to Icons.Default.Info
                    )}

                    val userHasGroups = remember {
                        mutableStateOf(UserPreferences.getGroupList(context).isNotEmpty())
                    }
                    LaunchedEffect(UserPreferences.getGroupList(context).size) {
                        val hasGroups = UserPreferences.getGroupList(context).isNotEmpty()
                        if (hasGroups != userHasGroups.value) {
                            userHasGroups.value = hasGroups
                            if (hasGroups && options.none { it.first == "Mis Clases Grupales" }) {
                                options.add(1, "Mis Clases Grupales" to Icons.Default.Group)
                            } else if (!hasGroups && options.any { it.first == "Mis Clases Grupales" }) {
                                options.removeIf { it.first == "Mis Clases Grupales" }
                            }
                        }
                    }

                    options.forEach { (optionName, icon) ->
                        NavigationDrawerItem(
                            label = { Text(optionName, fontSize = 16.sp) },
                            icon = { Icon(icon, contentDescription = null) },
                            selected = currentRouteBase?.startsWith(optionName.lowercase(Locale.getDefault())) == true ||
                                    (optionName == "Mis Clases Grupales" && currentRouteBase == "groupListScreen") ||
                                    (optionName == "Inicio" && (currentRouteBase == "dashboard/{modo}" || currentRouteBase == "modeSelection")),
                            onClick = {
                                scope.launch { drawerState.close() }
                                when (optionName) {
                                    "Inicio" -> {
                                        val lastValidDashboardRoute = UserPreferences.getLastRoute(context)
                                        val lastSelectedGroupIdForDashboard = UserPreferences.getSelectedGroupId(context)

                                        if (!lastValidDashboardRoute.isNullOrEmpty() &&
                                            (lastValidDashboardRoute.startsWith("dashboard/individual") || lastValidDashboardRoute.startsWith("dashboard/grupal"))) {
                                            val routeToNavigate = if (lastValidDashboardRoute.startsWith("dashboard/grupal") && !lastSelectedGroupIdForDashboard.isNullOrEmpty()) {
                                                "dashboard/grupal?groupId=$lastSelectedGroupIdForDashboard"
                                            } else {
                                                lastValidDashboardRoute
                                            }
                                            navController.navigate(routeToNavigate) {
                                                popUpTo("modeSelection") { inclusive = false }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        } else {
                                            val currentSavedMode = UserPreferences.getSelectedMode(context)
                                            val userGroupList = UserPreferences.getGroupList(context)

                                            if (currentSavedMode == "grupal" && userGroupList.isNotEmpty()) {
                                                navController.navigate("groupListScreen") {
                                                    popUpTo("modeSelection") { inclusive = false }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            } else if (currentSavedMode == "individual") {
                                                navController.navigate("dashboard/individual") {
                                                    popUpTo("modeSelection") { inclusive = false }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            } else {
                                                navController.navigate("modeSelection") {
                                                    popUpTo("modeSelection") { inclusive = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    }
                                    "Mis Clases Grupales" -> {
                                        navController.navigate("groupListScreen") {
                                            popUpTo("modeSelection") { inclusive = false }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    "Materias" -> navController.navigate("materias") { popUpTo("dashboard/{modo}") { saveState = true }; launchSingleTop = true; restoreState = true }
                                    "Examenes" -> navController.navigate("examenes") { popUpTo("dashboard/{modo}") { saveState = true }; launchSingleTop = true; restoreState = true }
                                    "Tareas" -> navController.navigate("tareas") { popUpTo("dashboard/{modo}") { saveState = true }; launchSingleTop = true; restoreState = true }
                                    "Proyectos" -> navController.navigate("proyectos") { popUpTo("dashboard/{modo}") { saveState = true }; launchSingleTop = true; restoreState = true }
                                    "Configuracion" -> navController.navigate("configuracion") { popUpTo("dashboard/{modo}") { saveState = true }; launchSingleTop = true; restoreState = true }
                                    "Ayuda" -> navController.navigate("ayuda") { popUpTo("dashboard/{modo}") { saveState = true }; launchSingleTop = true; restoreState = true }
                                    else -> { /* Manejo por defecto o error */ }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión", fontSize = 16.sp) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            scope.launch {
                                UserPreferences.clearUserPreferences(context)
                            }
                            navController.navigate("login") {
                                popUpTo("dashboard/{modo}") { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = MaterialTheme.colorScheme.error,
                            unselectedIconColor = MaterialTheme.colorScheme.error
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) { // <-- CIERRA ModalNavigationDrawer
        Scaffold( // <-- SCAFFOLD AHORA ESTÁ DENTRO DEL DRAWER
            topBar = {
                if (showGlobalTopAppBar) {
                    TopAppBar(
                        title = {
                            Text(
                                text = appBarTitle,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            if (showDrawerMenuIcon) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú", tint = Color.White)
                                }
                            } else if (showGlobalBackButton) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                                }
                            }
                        },
                        actions = appBarActions ?: {},
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            }
        ) { paddingValues -> // paddingValues de este Scaffold ahora serán aplicados al NavHost
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues) // Aplicamos el padding del Scaffold aquí
            ) {
                composable("ipConfig") { IpConfigScreen(navController) }
                composable("login") { LoginScreen(navController) }
                composable("register") { RegisterScreen(navController) }
                composable("forgotPassword") { ForgotPasswordScreen(navController) }
                composable("modeSelection") { ModeSelectionScreen(navController) }
                composable("groupListScreen") { GroupListScreen(navController = navController) }
                composable("groupModeSelection") { GroupModeSelectionScreen(navController = navController) }
                composable(
                    route = "dashboard/{modo}?groupId={groupId}",
                    arguments = listOf(
                        navArgument("modo") {
                            type = NavType.StringType
                            defaultValue = "individual"
                        },
                        navArgument("groupId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val modo = backStackEntry.arguments?.getString("modo") ?: "individual"
                    val groupIdArg = backStackEntry.arguments?.getString("groupId")
                    DashboardScreen(
                        navController = navController,
                        drawerState = drawerState,
                        scope = scope,
                        currentMode = modo,
                        groupId = groupIdArg
                    )
                }
                composable("materias") { MateriasScreen(navController) }
                composable("examenes") { ExamenesScreen(navController) }
                composable("tareas") { TareasScreen(navController) }
                composable("proyectos") { ProyectosScreen(navController) }

                composable("addMateria") { AddMateriaScreen(navController) }
                composable("addExamen") { AddExamenScreen(navController) }
                composable("addTarea") { AddTareaScreen(navController) }
                composable("addProyecto") { AddProyectoScreen(navController) }

                // --- RUTAS DE EDICIÓN ACTUALIZADAS ---
                composable(
                    route = "editMateria/{materiaId}", // CAMBIO: Usar materiaId
                    arguments = listOf(navArgument("materiaId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val materiaId = backStackEntry.arguments?.getString("materiaId")
                    EditMateriaScreen(navController, materiaId)
                }
                composable(
                    route = "editExamen/{examenId}",
                    arguments = listOf(navArgument("examenId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val examenId = backStackEntry.arguments?.getString("examenId")
                    EditExamenScreen(navController, examenId)
                }
                composable(
                    route = "editTarea/{tareaId}",
                    arguments = listOf(navArgument("tareaId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tareaId = backStackEntry.arguments?.getString("tareaId")
                    EditTareaScreen(navController, tareaId)
                }
                composable(
                    route = "editProyecto/{proyectoId}",
                    arguments = listOf(navArgument("proyectoId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val proyectoId = backStackEntry.arguments?.getString("proyectoId")
                    EditProyectoScreen(navController, proyectoId)
                }

                composable("configuracion") { SettingsScreen(navController) }
                // La nueva pantalla de selección de colores
                composable("color_selection_screen") { ColorSelectionScreen(navController) }
            }
        }
    }
}