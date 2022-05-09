package ru.saime.gql_client.widgets

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import ru.saime.gql_client.R

@Composable
fun Avatar(modifier: Modifier = Modifier) {
	Image(
		painter = painterResource(id = R.drawable.avatar),
		contentDescription = null,
		modifier = modifier
	)
}