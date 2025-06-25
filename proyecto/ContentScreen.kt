package com.mctrio.estudiapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContentScreen(title: String, content: String) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp) .verticalScroll(rememberScrollState())) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(content, style = MaterialTheme.typography.bodyLarge)
    }
}