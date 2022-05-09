package ru.saime.gql_client.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.navigation.singleNavigate
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
		FloatingActionButton(
			content = {
				Icon(
					Icons.Default.KeyboardArrowDown,
					contentDescription = null,
					tint = ProfileDimCC
				)
			},
			modifier = modifier
				.size(61.dp)
				.padding(10.dp),
			onClick = { scope.launch { room.scrollToMsg(backend, room.lastMsgID.value!!) } },
			backgroundColor = MessageBackgroundCC,
			contentColor = Color.White,
		)
	}

}