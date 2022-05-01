package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pkg.type.MsgCreated
import pkg.type.RoomType
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Message
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.widgets.EmptyScreen
import ru.saime.gql_client.widgets.rememberForeverLazyListState

@Composable
fun RoomMessages(view: View, room: Room) {
	val isLoading = remember {
		mutableStateOf(false)
	}
	val isError = remember {
		mutableStateOf(Pair(false, ""))
	}
	val isOk = remember {
		mutableStateOf(false)
	}
	val isEmpty = remember {
		mutableStateOf(false)
	}

	Scaffold(
		backgroundColor = BackgroundCC,
		topBar = {
			TopAppBar(
				navigationIcon = {
					IconButton(onClick = {
						view.mainNavController.popBackStack()
					}) {
						Icon(Icons.Filled.ArrowBack, null, tint = MainTextCC)
					}
				},
				backgroundColor = TopBarCC,
				title = {
					Row(
						verticalAlignment = Alignment.CenterVertically
					) {
						Image(
							painter = painterResource(id = R.drawable.avatar),
							contentDescription = "",
							modifier = Modifier
								.padding(end = 17.dp, top = 4.dp, bottom = 4.dp)
								.size(40.dp)
								.clip(CircleShape)
						)
						Text(
							room.name,
							color = MainTextCC
						)
					}
				})
		},
	) {
		if (room.lastMsgID == 0)
			isEmpty.value = true
		else if (room.messages.isEmpty()) {
			if (!(isError.value.first || isOk.value || isLoading.value))
				CoroutineScope(Dispatchers.Main).launch {
					isLoading.value = true
					view.orderRoomMessages(
						roomID = room.roomID,
						startMsg = room.lastMsgID,
						created = MsgCreated.BEFORE,
					) { err ->
						if (err != null) isError.value = Pair(true, err) else isOk.value = true
					}
					isLoading.value = false
				}
		} else isOk.value = true

		Loading(isDisplayed = isLoading.value, modifier = Modifier.fillMaxSize())
		ErrorComponent(
			isDisplayed = isError.value.first,
			msg = isError.value.second,
			modifier = Modifier.fillMaxSize()
		)
		EmptyScreen(isDisplayed = isEmpty.value)
		ShowMessages(isDisplayed = isOk.value, view = view, room = room)

		// message input
		if (room.view == RoomType.TALK)
			Box(
				modifier = Modifier
					.fillMaxSize(),
				contentAlignment = Alignment.BottomCenter,
			) {
				Row(
					modifier = Modifier
						.background(DividerDarkCC)
						.padding(horizontal = 15.dp, vertical = 2.dp),
					verticalAlignment = Alignment.Bottom
				) {

					MessageInput()
					IconButton(onClick = { /*TODO*/ }) {
						Icon(Icons.Filled.Send, null, Modifier.padding(3.dp), tint = MainTextCC)
					}
				}

			}
	}
}

@Composable
fun ShowMessages(
	isDisplayed: Boolean,
	view: View,
	room: Room,
	modifier: Modifier = Modifier
) {
	println("загружается ShowMessages? $isDisplayed")

	val lazyListState = rememberForeverLazyListState(Screen.RoomMessages(room.roomID).routeWithArgs)
	if (isDisplayed) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(start = 8.dp, end = 8.dp, bottom = 80.dp),
			state = lazyListState,
			reverseLayout = true,
			verticalArrangement = Arrangement.spacedBy(7.dp)
		) {
			var prevMsg: Message? = null
			itemsIndexed(
//				items = messages.values.sortedByDescending { it.msgID },
//				items = room.messages.values.toList(),
				items = room.order,
				key = { _, id -> id }
			) { _, msgID ->
				println("подгружается сообщение $msgID")
//				lazyListState.layoutInfo.visibleItemsInfo.lastIndex
				Box(
					Modifier.fillMaxWidth(),
					if (room.messages[msgID]!!.empID != Cache.Me.ID) Alignment.CenterStart else Alignment.CenterEnd
				) {
					MessageBody(
						room.messages[msgID]!!,
						displayAuthor = room.messages[msgID]!!.empID != Cache.Me.ID && (prevMsg == null || prevMsg!!.empID != room.messages[msgID]!!.empID)
					)
				}
				prevMsg = room.messages[msgID]!!
			}

//			if (lazyListState.firstVisibleItemIndex == messages[0]?.msgID) {
//				println("first visible = ${lazyListState.firstVisibleItemIndex}")
//			}

		}
		LaunchedEffect(key1 = lazyListState) {
			snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.last().key }
				.map { key -> key == room.order.last()}
				.distinctUntilChanged()
				.filter { it }
				.collect {
					if (room.messages[room.order.last()]!!.prev != null) {
						view.orderRoomMessages(
							roomID = room.roomID,
							startMsg = room.order.last(),
							created = MsgCreated.BEFORE,
						)
//						messages.let {  }
					}
				}
//				.map { key -> key == room.messages.lastKey()}
//				.distinctUntilChanged()
//				.filter { it }
//				.collect {
//					if (room.messages[room.messages.lastKey()]!!.prev != null) {
//						view.orderRoomMessages(
//							roomID = room.roomID,
//							startMsg = room.messages.lastKey(),
//							created = MsgCreated.BEFORE,
//						)
////						messages.let {  }
//					}
//				}

		}
	}
}

@Composable
fun MessageBody(
	msg: Message,
	modifier: Modifier = Modifier,
	displayAuthor: Boolean = true,
) {
	Card(
		modifier = modifier,
		shape = RoundedCornerShape(18.dp),
		backgroundColor = RoomCardBackgroundCC,
//		elevation = 3.dp
	) {
		Row(
			modifier = Modifier.padding(vertical = 6.dp, horizontal = 9.dp),
			verticalAlignment = Alignment.Bottom,
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Column(
				modifier = Modifier.widthIn(0.dp, 200.dp),
				verticalArrangement = Arrangement.spacedBy(1.dp)
			) {
				if (displayAuthor)
					TextMessageAuthor(
						Cache.Data.employees[msg.empID]?.let { it.firstName + " " + it.lastName }
							.toString()
					)
				if (msg.targetID != null)
					Row(
						modifier = Modifier.padding(top=1.dp, bottom = 1.dp, start = 4.dp),
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Box(
							Modifier
								.width(2.dp)
								.height(38.dp)
								.background(ProfileYellowCC)
						)
						Column() {
							TextMessageAuthor(
								Cache.Data.employees[
										Cache.Data.rooms[msg.roomID]?.messages?.get(msg.targetID)?.empID
								]?.let { it.firstName + " " + it.lastName }.toString()
							)
							TextMessageBody(
								Cache.Data.rooms[msg.roomID]?.messages?.get(
									msg.targetID
								)?.body.toString(), softWrap = false
							)
						}
					}
				TextMessageBody(msg.body)
			}
			TextMessageData(DateFormats.messageDate(msg.createdAt))
			TextMessageData("(${msg.msgID})")
		}
	}
}

@Composable
fun MessageInput(modifier: Modifier = Modifier) {
	val (text, setText) = rememberSaveable { mutableStateOf("") }
//	BasicTextField(value = "s", onValueChange = {})
	TextField(
		value = text,
		onValueChange = { setText(it) },
		modifier = modifier
			.width(250.dp)
			.background(DividerDarkCC)
			.navigationBarsWithImePadding(),
		placeholder = { Text("Сообщение", color = MainTextCC) },
//		singleLine = true,
		singleLine = false,
		maxLines = 3,
		colors = TextFieldDefaults.textFieldColors(
			textColor = MainTextCC,
			focusedIndicatorColor = DividerDarkCC,
//			unfocusedIndicatorColor = DividerDarkCC,
		)
	)
}

@Composable
fun TextMessageAuthor(
	text: String,
	color: Color = MessageAuthorCC,
	fontSize: TextUnit = 14.sp
) {
	Text(
		text = text,
		color = color,
		fontSize = fontSize
	)
}

@Composable
fun TextMessageData(
	text: String,
	color: Color = MessageDataCC,
	fontSize: TextUnit = 11.sp,
) {
	Text(
		text = text,
		color = color,
		fontSize = fontSize
	)
}

@Composable
fun TextMessageBody(
	text: String,
	color: Color = MainTextCC,
	softWrap: Boolean = true,
//	fontSize: TextUnit = 16.sp
) {
	Text(
		text = text,
		color = color,
//		fontFamily = FontFamily.SansSerif,
//		fontSize = fontSize
	)
}