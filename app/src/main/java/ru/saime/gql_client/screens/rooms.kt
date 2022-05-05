package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pkg.type.EventSubjectAction
import pkg.type.EventType
import ru.saime.gql_client.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.editSubscribeList
import ru.saime.gql_client.backend.orderMeRooms
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.utils.ScreenStatus
import ru.saime.gql_client.utils.equal
import ru.saime.gql_client.utils.set
import ru.saime.gql_client.widgets.CategoryNavigate
import ru.saime.gql_client.widgets.DockSpacer
import ru.saime.gql_client.widgets.EmptyScreen

@Composable
fun Rooms(
	backend: Backend
) {
	val screenStatus = rememberSaveable {
		mutableStateOf(ScreenStatus.NONE)
	}
	var errMsg: String = remember { "" }

	SideEffect {
		if (screenStatus.equal(ScreenStatus.NONE))
			MainScope().launch {
				screenStatus.set(ScreenStatus.LOADING)
				backend.orderMeRooms().let { err ->
					if (err != null) {
						errMsg = err
						screenStatus.set(ScreenStatus.ERROR)
					} else {
						backend.editSubscribeList(
							action = EventSubjectAction.ADD,
							listenEvents = listOf(EventType.all),
							targetRooms = Cache.Data.rooms.keys.toList()
						).let {
							println(
								if (it.isNullOrEmpty()) "editSubscribeList successful"
								else "editSubscribeList failed with - $it"
							)
						}
						screenStatus.set(ScreenStatus.OK)
					}
				}
			}
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
	val rooms = remember {
		Cache.Data.rooms
	}
	val scrollState = rememberLazyListState()
	val scaffoldState = rememberScaffoldState()
	val scope = rememberCoroutineScope()
	val navigate = CategoryNavigate(backend, scaffoldState,scope)

	Scaffold(
		scaffoldState = scaffoldState,
		drawerContent = {
			navigate.DockCategory(Screen.Guide.name, Screen.Guide.routeRef)
			navigate.DockCategory(Screen.Rooms.name, Screen.Rooms.routeRef)
			navigate.DockCategory(Screen.Profile().name, Screen.Profile(0).routeWithArgs)
		},
		topBar = {
			TopAppBar(
				navigationIcon = {
					IconButton(onClick = {
						scope.launch{ scaffoldState.drawerState.open() }
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
		}
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.background(BackgroundCC),
			state = scrollState,
		) {
			itemsIndexed(ArrayList(rooms.values)) { _, item ->
				RoomCard(item.roomID, backend, Modifier.padding(top = 9.dp))
			}
		}
		DockSpacer()
	}
}

@Composable
fun RoomCard(
	id: Int,
	backend: Backend,
	modifier: Modifier = Modifier
) {
	val room = Cache.Data.rooms[id]!!
	Box(
		modifier = modifier
			.fillMaxWidth()
			.clickable {
				backend.mainNavController.navigate(Screen.RoomMessages(id).routeWithArgs)
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