package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.saime.gql_client.BackgroundCC
import ru.saime.gql_client.DockCC
import ru.saime.gql_client.RoomCardBackgroundCC
import ru.saime.gql_client.View
import ru.saime.gql_client.cache.Cache

@Composable
fun Rooms(view: View) {
	val isLoading = remember {
		mutableStateOf(false)
	}
	val isError = remember {
		mutableStateOf(Pair(false, ""))
	}
	val isOk = remember {
		mutableStateOf(false)
	}

	Loading(isDisplayed = isLoading.value, modifier = Modifier.fillMaxSize())
	ErrorComponent(
		isDisplayed = isError.value.first,
		msg = isError.value.second,
		modifier = Modifier.fillMaxSize()
	)
	ShowRooms(isDisplayed = isOk.value)
	Box {
		if (!(isError.value.first || isOk.value))
			CoroutineScope(Dispatchers.Main).launch {
				view.orderMeRooms { err ->
					if (err != null) isError.value = Pair(true, err) else isOk.value = true
				}
				isLoading.value = false
			}
	}

}


@Composable
fun ShowRooms(
	isDisplayed: Boolean,
	modifier: Modifier = Modifier
) {
	val rooms = remember {
		Cache.Data.rooms
	}
	val scrollState = rememberScrollState()
	if (isDisplayed)
		Scaffold(
			modifier = modifier
				.fillMaxSize()
				.background(BackgroundCC)
				.padding(14.dp)
		) {
			LazyColumn {
				itemsIndexed(ArrayList(rooms.values)) { _, item ->
					RoomCard(id = item.roomID)

				}
			}

		}
}

@Composable
fun RoomCard(
	id: Int,
	modifier: Modifier = Modifier
) {
	val room = Cache.Data.rooms[id]!!
	Card(
		modifier = modifier
			.fillMaxWidth(),
		shape = RoundedCornerShape(17.dp),
		backgroundColor = RoomCardBackgroundCC,
		elevation = 4.dp
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Image(
				painter = painterResource(id = ru.saime.gql_client.R.drawable.avatar),
				contentDescription = "",
				modifier = Modifier
					.width(50.dp)
					.height(50.dp)
					.padding(36.dp)
					.clip(RoundedCornerShape(50.dp))
			)
			Column(modifier = Modifier.fillMaxWidth()) {
				Text(text = "(id:${room.roomID}) ${room.name}")
				Text(text = room.view.name)
				Text(text = room.lastMsgID.toString())
			}
		}
	}
}