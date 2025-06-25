package com.mctrio.estudiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mctrio.estudiapp.utils.UserPreferences

// --- Composable para el Selector de Color RGB ---
@Composable
fun RGBColorPicker(initialHex: String, onColorChanged: (String) -> Unit) {
    var red by remember { mutableStateOf(255f) }
    var green by remember { mutableStateOf(0f) }
    var blue by remember { mutableStateOf(0f) }

    // Actualiza los valores de color desde el hex inicial al principio
    // o cuando initialHex cambie.
    LaunchedEffect(initialHex) {
        try {
            val parsedColor = android.graphics.Color.parseColor(initialHex)
            red = ((parsedColor shr 16) and 0xFF).toFloat()
            green = ((parsedColor shr 8) and 0xFF).toFloat()
            blue = (parsedColor and 0xFF).toFloat()
        } catch (e: IllegalArgumentException) {
            // Manejar color inválido, usar un valor por defecto o loggear el error
            println("Invalid HEX color: $initialHex. Using default red.")
            red = 255f
            green = 0f
            blue = 0f
        }
    }

    val color = Color(red / 255f, green / 255f, blue / 255f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text("Selecciona un Color", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Slider para cada componente de color (Rojo, Verde, Azul)
        ColorSlider("Rojo", red) { newRed ->
            red = newRed
            onColorChanged(Color(newRed / 255f, green / 255f, blue / 255f).toHex())
        }
        ColorSlider("Verde", green) { newGreen ->
            green = newGreen
            onColorChanged(Color(red / 255f, newGreen / 255f, blue / 255f).toHex())
        }
        ColorSlider("Azul", blue) { newBlue ->
            blue = newBlue
            onColorChanged(Color(red / 255f, green / 255f, newBlue / 255f).toHex())
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(color, shape = MaterialTheme.shapes.small)
                .border(2.dp, Color.Gray.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val hex = color.toHex()
        Text("HEX: $hex", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

// --- Función de extensión para convertir Color a HEX ---
fun Color.toHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

// --- Composable para el Slider de Color individual ---
@Composable
fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ${value.toInt()}", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            valueRange = 0f..255f,
            steps = 254, // Permite 255 valores discretos (0-255)
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = when (label) {
                    "Rojo" -> Color.Red
                    "Verde" -> Color.Green
                    "Azul" -> Color.Blue
                    else -> Color.Gray
                },
                inactiveTrackColor = when (label) {
                    "Rojo" -> Color.Red.copy(alpha = 0.3f)
                    "Verde" -> Color.Green.copy(alpha = 0.3f)
                    "Azul" -> Color.Blue.copy(alpha = 0.3f)
                    else -> Color.Gray.copy(alpha = 0.3f)
                }
            )
        )
    }
}

// --- La pantalla principal de Selección de Color ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    // Carga el color guardado o usa un valor por defecto (e.g., "#6200EE" que es un morado común)
    var currentSelectedColorHex by remember {
        mutableStateOf(UserPreferences.getThemeColor(context) ?: "#6200EE")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferencias de Color") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Llama al selector de color RGB
            RGBColorPicker(initialHex = currentSelectedColorHex) { newHex ->
                currentSelectedColorHex = newHex
                // Guarda el color inmediatamente al mover los sliders
                UserPreferences.saveThemeColor(context, newHex)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // El color ya se guarda en tiempo real, este botón solo para volver
                    Toast.makeText(context, "Color aplicado con éxito.", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar y Volver")
            }
        }
    }
}