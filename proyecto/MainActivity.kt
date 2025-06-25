package com.mctrio.estudiapp // Asegúrate de que el paquete sea el correcto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mctrio.estudiapp.ui.screens.AppNavigation
import com.mctrio.estudiapp.ui.theme.EstudiAppTheme
import com.mctrio.estudiapp.utils.UserPreferences // Importa UserPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ¡Inicializa UserPreferences aquí!
        UserPreferences.initialize(applicationContext)

        setContent {
            EstudiAppTheme {
                AppNavigation()
            }
        }
    }
}