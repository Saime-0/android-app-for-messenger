package ru.saime.gql_client.widgets

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
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

@Composable
fun Photo(bitmap: ImageBitmap?, modifier: Modifier = Modifier) {
	if (bitmap == null)
		Image(
			painter = painterResource(id = R.drawable.avatar),
			contentDescription = null,
			modifier = modifier,
			contentScale = ContentScale.Crop,
		)
	else
		Image(
			bitmap = bitmap,
			contentDescription = null,
			modifier = modifier,
			contentScale = ContentScale.Crop,
		)
}