package ru.saime.gql_client.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.saime.gql_client.View
import ru.saime.gql_client.navigation.Screen

@Composable
fun ErrorComponent(
	isDisplayed: Boolean,
	msg: String,
	modifier: Modifier = Modifier
) {
	println("загружается ErrorComponent? $isDisplayed")
	if (isDisplayed)
		Box(
			modifier = modifier
				.fillMaxWidth()
				.background(Color.Yellow),
			contentAlignment = Alignment.Center
		) {
			Text(text = msg, color = Color.Red)
		}
}