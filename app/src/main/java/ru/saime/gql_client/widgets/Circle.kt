package ru.saime.gql_client.widgets


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp


@Composable
fun Circle(
	size: Dp,
	color: Color,
	modifier: Modifier = Modifier,
) {
	Box(
		modifier
			.size(size)
			.clip(CircleShape)
			.background(color)
	)
}