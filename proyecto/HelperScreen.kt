package com.mctrio.estudiapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.ui.theme.AppTypography // Asegúrate de que esta importación sea correcta

---

## HelpScreen.kt

```kotlin
package com.mctrio.estudiapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// Importa tu tipografía si tienes una personalizada, si no, usa MaterialTheme.typography
// import com.mctrio.estudiapp.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayuda de EstudiApp", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "¡Bienvenido/a a EstudiApp! Aquí te ayudamos a empezar.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HelpSection(
                title = "1. Tu Primer Paso: La Conexión",
                content = "Al abrir la app, te pediremos la dirección IP de tu servidor (un número como **192.168.1.1**). Es la \"dirección\" donde se guardarán tus datos para que todo funcione bien. Después, inicia sesión o regístrate.\n" +
                        "Puedes cambiar esta IP cuando quieras en \"**Configuración**\"."
            )

            HelpSection(
                title = "2. ¿Solo o en Grupo? Tú Decides",
                content = "Después de iniciar sesión, elige tu forma de estudiar:\n\n" +
                        "**Modo Individual**: Tu agenda personal. Organiza tus tareas, exámenes y proyectos solo para ti.\n" +
                        "**Modo Grupal**: ¡Para estudiar con tu equipo!\n" +
                        "  **Crear Grupo**: Inicia un grupo, ponle nombre y obtén un código para tus amigos.\n" +
                        "  **Unirse a un Grupo**: Usa un código para unirte a un grupo ya existente.\n" +
                        "  **Ver Mis Clases Grupales**: Consulta los grupos a los que ya perteneces."
            )

            HelpSection(
                title = "3. Muévete por la App: El Menú Principal",
                content = "Para ir a cualquier parte de EstudiApp, usa el **Menú Principal**. Lo encuentras tocando el icono de tres líneas (**☰**) en la esquina superior izquierda de tu pantalla principal.\n" +
                        "Desde ahí, puedes ir a:\n" +
                        "  **Inicio**: Vuelve al Dashboard.\n" +
                        "  **Materias, Exámenes, Tareas, Proyectos**: Para organizar tus estudios.\n" +
                        "  **Mis Clases Grupales**: (Solo si estás en un grupo) Ve tus grupos fácilmente.\n" +
                        "  **Configuración**: Personaliza la app (colores, IP) y ajusta tus preferencias.\n" +
                        "  **Ayuda**: Esta misma sección."
            )

            HelpSection(
                title = "4. Sincroniza tus Datos",
                content = "Si usas el modo grupal o el individual, verás el botón \"**Sincronizar Ahora**\" (con un icono 🔄) en tu Dashboard. Tócalo para actualizar los datos.\n" +
                        "**Importante**: Tu teléfono debe estar conectado a la misma red Wi-Fi que el servidor de la IP que configuraste."
            )

            HelpSection(
                title = "5. Personaliza tu App",
                content = "En \"**Configuración**\", puedes:\n" +
                        "  **Cambiar Colores**: Elige los colores de tu calendario y tarjetas. ¡Se actualizan al instante!\n" +
                        "  **Modificar IP**: Actualiza la dirección IP del servidor si cambia o si usas otra red."
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Alguna duda? Estamos aquí para ayudarte. ¡Disfruta EstudiApp!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun HelpSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary, // Un color que resalte para los títulos
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp // Mejora la legibilidad del texto largo
        )
    }
}