package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.saime.gql_client.DividerDarkCC

@Composable
fun DividerCC(
	modifier: Modifier = Modifier,
	color: Color = DividerDarkCC
) {
	Divider(
		color = color,
		modifier = modifier.fillMaxWidth()
	)
}
@Composable
fun Divider2CC(
	modifier: Modifier = Modifier,
	color: Color = DividerDarkCC
) {
	Box(
		modifier = modifier
			.height(1.dp)
			.fillMaxWidth()
			.background(color)
	)

}