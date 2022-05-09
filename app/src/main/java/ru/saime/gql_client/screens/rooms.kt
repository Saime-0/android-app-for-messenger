package ru.saime.gql_client.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.saime.gql_client.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.orderMeRooms
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.utils.ScreenStatus
import ru.saime.gql_client.utils.equal
import ru.saime.gql_client.utils.set
import ru.saime.gql_client.widgets.*

@Composable
fun Rooms(
	backend: Backend
) {
	val screenStatus = remember {
		mutableStateOf(ScreenStatus.NONE)
	}
	val scaffoldState = rememberScaffoldState()
	val scope = rememberCoroutineScope()
	var errMsg by remember { mutableStateOf("") }


	Scaffold(
		scaffoldState = scaffoldState,
		drawerBackgroundColor = SideMenuCC,
		drawerScrimColor = ProfileSectionBackgroundCC,
		drawerContent = { SideMenu(backend, scaffoldState, scope) },
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
		LaunchedEffect(Unit) {
			if (Cache.Orders.roomOrder.isEmpty() && screenStatus.equal(ScreenStatus.NONE)) {
				screenStatus.set(ScreenStatus.LOADING)
				backend.orderMeRooms(0).let { err ->
					if (err != null) {
						errMsg = err
						screenStatus.set(ScreenStatus.ERROR)
					} else {
						screenStatus.set(ScreenStatus.OK)
					}
				}

			} else
				screenStatus.set(ScreenStatus.OK)
		}
		SideEffect {
			println(screenStatus.value)
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

}


@Composable
fun ShowRooms(
	backend: Backend,
) {
	val lazyListState = rememberForeverLazyListState(Screen.Rooms.routeRef)
//	val sortedRoomList = Cache.Data.rooms.values.toList().sortedByDescending { it.pos }

	LaunchedEffect(lazyListState) {
		// для подгрузки
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
//			modifier = Modifier.fillMaxWidth(),
			state = lazyListState,
		) {
			itemsIndexed(
				items = Cache.Orders.roomOrder,
				key = { _, roomID -> roomID },
			) { index, roomID ->
				if (index == 0) Spacer(Modifier.height(10.dp))

				RoomCard(Cache.Data.rooms[roomID]!!, backend, Modifier.padding(vertical = 8.dp))

				if (index + 1 == Cache.Orders.roomOrder.size) Spacer(Modifier.height(30.dp))
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
	val lastMsg =
		if (room.lastMsgID.value != null)
			Cache.Data.messages[room.lastMsgID.value]
		else null
	val authorMsgName =
		if (lastMsg?.empID != null)
			Cache.Data.employees[lastMsg.empID]!!.let {
				if (it.empID == Cache.Me.ID) "Вы" else it.firstName
			}
		else "Объявление"

	
	Box(
		modifier = modifier
			.fillMaxWidth()
			.clickable {
				backend.mainNavController.navigate(Screen.RoomMessages(room.roomID).routeWithArgs)
			},
	)
	{
		Row(
			modifier = Modifier
				.padding(horizontal = 5.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
//			Box(modifier = Modifier.width(8.dp))
			Avatar(
				Modifier
					.padding(horizontal = 18.dp, vertical = 2.dp)
					.size(50.dp)
					.clip(CircleShape)
			)
			Column(
				modifier = Modifier
					.padding(end = 15.dp)
					.fillMaxWidth(),
//				verticalArrangement = Arrangement.spacedBy(5.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(10.dp)
				) {
					TextRoomName(room.name, Modifier.weight(1f))
					if (lastMsg != null)
						TextMessageData(DateFormats.messageDate(lastMsg!!.createdAt))
				}
				if (lastMsg != null) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(10.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						TextMessageAuthor("$authorMsgName:", color = ProfileDimCC, fontSize = 17.sp)
						TextMessageBody(
							lastMsg!!.body,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							color = RoomLastMessageCC,
							modifier = Modifier.weight(1f),
						)
						if (room.lastMsgID.value != null && (room.lastMsgRead.value == null || room.lastMsgRead.value!! < room.lastMsgID.value!!))
							NewMessageIndicator()
					}
				} else
					TextMessageBody("<Пусто>", color = RoomLastMessageCC)
//				Text(
//					text = room.lastMsgID.value.toString(),
//					color = MainTextCC
//				)
//				TextMessageData("${room.view.name} (${room.roomID})", Modifier.align(Alignment.End)) // id
			}
		}
	}
}

@Composable
fun NewMessageIndicator() {
	Box(
		Modifier
			.size(10.dp)
			.clip(CircleShape)
			.background(Color.White)
	)
}


@Composable
fun TextRoomName(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = Color.White,
	fontSize: TextUnit = 18.sp
) {
	Text(
		text = text,
		modifier = modifier,
		color = color,
		fontFamily = FontFamily.SansSerif,
		fontSize = fontSize,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
	)
}