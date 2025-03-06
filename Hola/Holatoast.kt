package com.example.holatoast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UIPrincipal() }
    }
}

@Composable
fun UIPrincipal() {
    val contexto = LocalContext.current
    var nombre by rememberSaveable { mutableStateOf("")}
    
    // Usamos Column para organizar los elementos verticalmente
    Column(
        modifier = Modifier.padding(16.dp) // Padding general para los elementos
    ) {
        Text(text = "Nombre:", modifier = Modifier.padding(bottom = 8.dp)) // Padding solo abajo

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Introduce tu nombre") },
            modifier = Modifier
                .fillMaxWidth() // Para que el textfield ocupe todo el ancho disponible
                .padding(bottom = 16.dp) // Padding debajo del TextField
        )

        Button(
            onClick = {
                Toast.makeText(contexto, "Hola $nombre!!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.padding(top = 8.dp) // Padding arriba del botón
        ) {
            Text("Saludar!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Previsualizacion() {
   UIPrincipal()
}
