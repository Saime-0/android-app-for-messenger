package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.saime.gql_client.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun TripleBar(
	modifier: Modifier = Modifier,
	backgroundColor: Color = DefaultTripleBarBackgroundCC,
	startContent: @Composable () -> Unit = {},
	endContent: @Composable () -> Unit = {},
	centerContent: @Composable () -> Unit,
	navigationIcon: @Composable (() -> Unit)? = null,
	actions: @Composable RowScope.() -> Unit = {},
	contentColor: Color = contentColorFor(backgroundColor),
	elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
	TopAppBar(
		title = {
			Row(
				modifier = modifier
					.fillMaxWidth()
					.padding(6.dp)
					.background(color = backgroundColor),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(5.dp)
			) {
				Box { startContent() }
				Box(Modifier.weight(1f)) { centerContent() }
				Box { endContent() }
			}

		},
		backgroundColor = backgroundColor
	)
}