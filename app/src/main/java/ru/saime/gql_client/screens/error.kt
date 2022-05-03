package ru.saime.gql_client.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ErrorComponent(
	msg: String,
	modifier: Modifier = Modifier
) {
	println("загружается ErrorComponent")
	Box(
		modifier = modifier
			.fillMaxWidth()
			.background(Color.Yellow),
		contentAlignment = Alignment.Center
	) {
		Text(text = msg, color = Color.Red)
	}
}