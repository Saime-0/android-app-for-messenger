package ru.saime.gql_client.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
		modifier = modifier
			.fillMaxWidth()
			.width(1.dp)
			.padding(8.dp)
	)
}