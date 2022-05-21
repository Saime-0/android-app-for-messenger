package ru.saime.gql_client.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.MessageBackgroundCC
import ru.saime.gql_client.ProfileDimCC
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.utils.lastMessageDontRead
import ru.saime.gql_client.utils.scrollToMsg


@Composable
fun GoToDown(
	backend: Backend,
	room: Room,
	scope: CoroutineScope,
	modifier: Modifier = Modifier
) {
	val density = LocalDensity.current
	AnimatedVisibility(
		visible = room.displayingGoDown.value,
		enter = slideInVertically{with(density) { 70.dp.roundToPx() } },
		exit = slideOutVertically{ with(density) { 70.dp.roundToPx() } }
	) {
		Box(
			modifier = Modifier.padding(10.dp),
			contentAlignment = Alignment.Center
		) {
			FloatingActionButton(
				content = {
					Icon(
						Icons.Default.KeyboardArrowDown,
						contentDescription = null,
						tint = ProfileDimCC
					)
				},
				modifier = modifier
					.size(45.dp),
				onClick = { scope.launch { room.scrollToMsg(backend, room.lastMsgID.value!!) } },
				backgroundColor = MessageBackgroundCC,
				contentColor = Color.White,
			)
			if (lastMessageDontRead(room))
				Circle(
					size = 15.dp,
					color = Color.White,
					modifier = Modifier.align(Alignment.TopStart)
				)
		}

	}

}