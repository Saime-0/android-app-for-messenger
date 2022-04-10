package ru.saime.gql_client.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Loading(
	isDisplayed: Boolean,
	modifier: Modifier = Modifier
) {
	if(isDisplayed)
		Row(
			modifier = modifier
				.fillMaxWidth()
				.padding(50.dp),
			horizontalArrangement = Arrangement.Center
		) {
			CircularProgressIndicator(
				color = Color.Red
			)
		}
}