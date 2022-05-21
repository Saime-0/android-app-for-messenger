package ru.saime.gql_client.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import ru.saime.gql_client.ProfileDimCC

@Composable
fun EmptyScreen(
	isDisplayed: Boolean,
	modifier: Modifier = Modifier,
	text: String = "здесь пусто :(",
) {
	println("загружается EmptyScreen? $isDisplayed")
	if (isDisplayed)
	Box(
		modifier = modifier
			.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = text,
			color = ProfileDimCC,
			fontSize = 17.sp,
		)
	}
}