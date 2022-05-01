package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.saime.gql_client.DividerDarkCC

@Composable
fun EmptyScreen(
	modifier: Modifier = Modifier,
	isDisplayed: Boolean = true,
	text: String = "здесь пусто :(",
) {
	println("загружается EmptyScreen? $isDisplayed")
	if (isDisplayed)
	Box(
		modifier = modifier
			.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Text(text)
	}
}