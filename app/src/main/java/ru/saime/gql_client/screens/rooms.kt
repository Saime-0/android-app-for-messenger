package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
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
import ru.saime.gql_client.*
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.widgets.DockSpacer

@Composable
fun Rooms(
	view: View,
	modifier: Modifier = Modifier
) {
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
	ShowRooms(isDisplayed = isOk.value, view, modifier)
	Box {
		if (!(isError.value.first || isOk.value))
			CoroutineScope(Dispatchers.Main).launch {
				isLoading.value = true
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
	view: View,
	modifier: Modifier = Modifier
) {
	val rooms = remember {
		Cache.Data.rooms
	}
	val scrollState = rememberScrollState()
	if (isDisplayed)
		Scaffold(
			topBar = { TopAppBar(title = { Text("my rooms", color = MainTextCC) }, backgroundColor = BackgroundCC) }
		) {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.background(BackgroundCC)
			) {
				itemsIndexed(ArrayList(rooms.values)) { _, item ->
					RoomCard(item.roomID, view, Modifier.padding(top = 9.dp))
				}
			}
			DockSpacer()
		}
}

@Composable
fun RoomCard(
	id: Int,
	view: View,
	modifier: Modifier = Modifier
) {
	val room = Cache.Data.rooms[id]!!
	Box(
		modifier = modifier
			.fillMaxWidth()
			.clickable {
				view.mainNavController.navigate(Screen.RoomMessages(id).routeWithArgs,)
			},
	)
	{
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
//			Box(modifier = Modifier.width(8.dp))
			Image(
				painter = painterResource(id = ru.saime.gql_client.R.drawable.avatar),
				contentDescription = "",
				modifier = Modifier
					.padding(horizontal = 12.dp, vertical = 2.dp)
					.size(50.dp)
					.clip(CircleShape)
			)
			Column(modifier = Modifier.fillMaxWidth()) {
				Text(text = "(id:${room.roomID}) ${room.name}",
					color = MainTextCC)
				Text(text = room.view.name,
					color = MainTextCC)
				Text(text = room.lastMsgID.toString(),
					color = MainTextCC)
			}
		}
	}
}