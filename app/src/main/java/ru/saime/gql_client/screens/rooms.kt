package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import pkg.type.EventSubjectAction
import pkg.type.EventType
import pkg.type.MsgCreated
import ru.saime.gql_client.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.editSubscribeList
import ru.saime.gql_client.backend.orderMeRooms
import ru.saime.gql_client.backend.orderRoomMessages
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.utils.ScreenStatus
import ru.saime.gql_client.utils.equal
import ru.saime.gql_client.utils.set
import ru.saime.gql_client.widgets.CategoryNavigate
import ru.saime.gql_client.widgets.EmptyScreen
import ru.saime.gql_client.widgets.rememberForeverLazyListState

@Composable
fun Rooms(
	backend: Backend
) {
	val screenStatus = remember {
		mutableStateOf(ScreenStatus.NONE)
	}
	var errMsg by remember { mutableStateOf("") }

	SideEffect {
		if (Cache.Data.rooms.isEmpty() && screenStatus.equal(ScreenStatus.NONE))
			MainScope().launch {
				screenStatus.set(ScreenStatus.LOADING)
				backend.orderMeRooms(0).let { err ->
					if (err != null) {
						errMsg = err
						screenStatus.set(ScreenStatus.ERROR)
					} else {
						screenStatus.set(ScreenStatus.OK)
					}
				}
			}
		else
			screenStatus.set(ScreenStatus.OK)
	}

	when (screenStatus.value) {
		ScreenStatus.LOADING -> Loading(Modifier.fillMaxSize())
		ScreenStatus.ERROR -> ErrorComponent(errMsg, Modifier.fillMaxSize())
		ScreenStatus.OK -> ShowRooms(backend)
		else -> {
			EmptyScreen(true)
		}
	}


}


@Composable
fun ShowRooms(
	backend: Backend
) {
	val lazyListState = rememberForeverLazyListState(Screen.Rooms.routeRef)
	val scaffoldState = rememberScaffoldState()
	val scope = rememberCoroutineScope()
	val navigate = CategoryNavigate(backend, scaffoldState, scope)

	val sortedRoomList = Cache.Data.rooms.values.toList().sortedByDescending { it.pos }

	Scaffold(
		scaffoldState = scaffoldState,
		drawerContent = {
			navigate.DockCategory(Screen.Guide.name, Screen.Guide.routeRef)
			navigate.DockCategory(Screen.Rooms.name, Screen.Rooms.routeRef)
			navigate.DockCategory(Screen.Profile().name, Screen.Profile(Cache.Me.ID).routeWithArgs)
		},
		topBar = {
			TopAppBar(
				navigationIcon = {
					IconButton(onClick = {
						scope.launch { scaffoldState.drawerState.open() }
					}) {
						Icon(Icons.Filled.Menu, null, tint = MainTextCC)
					}
				},
				title = {
					Text("Каналы", color = MainTextCC)
				},
				contentColor = MainTextCC,
				backgroundColor = DefaultTripleBarBackgroundCC
			)
		},
		backgroundColor = BackgroundCC,
	) {
		LaunchedEffect(lazyListState) {
			launch(Dispatchers.IO) {
				snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
					.map { list ->
						if (list.isNotEmpty())
							Cache.Data.rooms[list.last().key].let { room ->
								if (room != null)
									room.pos != 2 && Cache.Data.rooms.minOf { it.value.pos } == room.pos
								else
									false
							}
						else false
					}
					.filter { it }
					.collect {
						backend.orderMeRooms(offset = Cache.Data.rooms.size)
					}
			}

		}
		if (Cache.Data.rooms.isNotEmpty()) {
			LazyColumn(
				modifier = Modifier.fillMaxWidth(),
				state = lazyListState,
			) {
				itemsIndexed(
					items = sortedRoomList,
					key = { _, room -> room.roomID },
				) { _, room ->
					RoomCard(room, backend, Modifier.padding(top = 9.dp))
				}

			}
		}
	}
}

@Composable
fun RoomCard(
	room: Room,
	backend: Backend,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.clickable {
				backend.mainNavController.navigate(Screen.RoomMessages(room.roomID).routeWithArgs)
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
				Text(
					text = "(id:${room.roomID}) ${room.name}",
					color = MainTextCC
				)
				Text(
					text = room.view.name,
					color = MainTextCC
				)
				Text(
					text = room.lastMsgID.toString(),
					color = MainTextCC
				)
			}
		}
	}
}