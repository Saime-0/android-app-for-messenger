package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.saime.gql_client.DividerDarkCC

@Composable
fun ScreenHorizontalPadding(
	padding: Dp = 14.dp,
	content: @Composable () -> Unit
) {
	Box(Modifier.padding(horizontal = padding)) {
		content()
	}
}
@Composable
fun ScreenVerticalPadding(
	padding: Dp = 14.dp,
	content: @Composable () -> Unit
) {
	Box(Modifier.padding(vertical = padding)) {
		content()
	}
}
