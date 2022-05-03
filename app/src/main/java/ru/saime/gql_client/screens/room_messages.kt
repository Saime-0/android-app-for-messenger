package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pkg.type.MsgCreated
import pkg.type.RoomType
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.orderRoomMessages
import ru.saime.gql_client.backend.sendMessage
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Message
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.utils.ScreenStatus
import ru.saime.gql_client.utils.equal
import ru.saime.gql_client.utils.set
import ru.saime.gql_client.widgets.EmptyScreen
import ru.saime.gql_client.widgets.rememberForeverLazyListState
import java.util.*

suspend fun sendMessage(backend: Backend, room: Room): Boolean {
	backend.sendMessage(room.roomID, room.currentInputMessageText.value, null).let { err ->
		if (err == null) {
			room.currentInputMessageText.value = ""
			return true
		} else
			return false
	}

}

val c1: Calendar = Calendar.getInstance()
val c2: Calendar = Calendar.getInstance()


@Composable
fun RoomMessages(backend: Backend, room: Room) {
	val screenStatus = rememberSaveable {
		mutableStateOf(ScreenStatus.NONE)
	}
	var errMsg: String = remember { "" }


	Scaffold(
		backgroundColor = BackgroundCC,
		topBar = {
			TopAppBar(
				navigationIcon = {
					IconButton(onClick = {
						backend.mainNavController.popBackStack()
					}) {
						Icon(Icons.Filled.ArrowBack, null, tint = MainTextCC)
					}
				},
				backgroundColor = BackgroundCC,
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
		SideEffect { // внутри скаффолд потому что переклдючение статуса на OK отрабатывает перед скаффолд, я так думаю...

			println("echo RoomMessages.SideEffect")
			if (room.lastMsgID == 0) {
				screenStatus.set(ScreenStatus.EMPTY)
			} else if (room.messages.isEmpty()) {
				if (screenStatus.equal(ScreenStatus.NONE))
					MainScope().launch {
						screenStatus.set(ScreenStatus.LOADING)
						backend.orderRoomMessages(
							roomID = room.roomID,
							startMsg = room.lastMsgID,
							created = MsgCreated.BEFORE,
						) { err ->
							screenStatus.set(
								if (err != null) {
									errMsg = err
									ScreenStatus.ERROR
								} else ScreenStatus.OK
							)
						}
					}
			} else
				screenStatus.set(ScreenStatus.OK)
		}

		when (screenStatus.value) {
			ScreenStatus.LOADING -> Loading(Modifier.fillMaxSize())
			ScreenStatus.ERROR -> ErrorComponent(errMsg, Modifier.fillMaxSize())
			ScreenStatus.OK -> ShowMessages(backend, room)
			ScreenStatus.EMPTY -> EmptyScreen(true)
			else -> {
				EmptyScreen(true)
			}
		}

		// message input
		if (room.view == RoomType.TALK)
			Box(
				modifier = Modifier
					.fillMaxSize(),
				contentAlignment = Alignment.BottomCenter,
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.background(DividerDarkCC),
					verticalAlignment = Alignment.Bottom,
					horizontalArrangement = Arrangement.Center,
				) {
					MessageInput(room)
					IconButton(
						modifier = Modifier.size(66.dp),
						onClick = {
							MainScope().launch {
								sendMessage(backend, room)
							}
						}
					) {
						Icon(Icons.Filled.Send, null, tint = SendingMessageIconCC)
					}
				}

			}
	}
}

fun displayingEmployeeName(room: Room, indexOfPairInOrder: Int): Boolean {
	return indexOfPairInOrder + 1 == room.orderPaired.size || room.orderPaired[indexOfPairInOrder].employeeID != room.orderPaired[indexOfPairInOrder + 1].employeeID
}

fun displayingDataTag(room: Room, indexOfPairInOrder: Int): Boolean {
	if (indexOfPairInOrder + 1 == room.orderPaired.size) return true

	c1.setTime(Date(room.messages[room.orderPaired[indexOfPairInOrder].messageID]!!.createdAt))
	c2.setTime(Date(room.messages[room.orderPaired[indexOfPairInOrder + 1].messageID]!!.createdAt))

	return c1.get(Calendar.DAY_OF_YEAR) != c2.get(Calendar.DAY_OF_YEAR)
			|| c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)
}

@Composable
fun ShowMessages(
	backend: Backend,
	room: Room,
) {
	println("загружается ShowMessages")

	val lazyListState = rememberForeverLazyListState(Screen.RoomMessages(room.roomID).routeWithArgs)
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.padding(start = 8.dp, end = 8.dp, bottom = 80.dp),
		state = lazyListState,
		reverseLayout = true,
		verticalArrangement = Arrangement.spacedBy(7.dp)
	) {
//			var prevMsg: Message? = null
		itemsIndexed(
//				items = messages.values.sortedByDescending { it.msgID },
//				items = room.messages.values.toList(),
			items = room.orderPaired,
			key = { _, orderPair -> orderPair.messageID }
		) { key, orderPair ->
//				println("подгружается сообщение $orderPair, key = $key")
			room.orderPaired.indexOf(orderPair).let { orderPairIndex ->
				displayingDataTag(room, orderPairIndex).let { displayingData ->

					Box(
						Modifier.fillMaxWidth(),
						if (room.messages[orderPair.messageID]!!.empID != Cache.Me.ID) Alignment.CenterStart else Alignment.CenterEnd
					) {
						displayingEmployeeName(room, orderPairIndex).let { displayingName ->
							MessageBody(
								msg = room.messages[orderPair.messageID]!!,
								lazyListState = lazyListState,
								displayAuthor = room.messages[orderPair.messageID]!!.empID != Cache.Me.ID && (displayingData || displayingName),
								addTopPadding = displayingName && !displayingData
							)
						}

					}
					if (displayingData)
						DataTag(room.messages[orderPair.messageID]!!.createdAt) // здесь потому что LazyColumn.reverseLayout = true
				}
			}
		}

	}
	LaunchedEffect(lazyListState) {
		// фокусироваться на последнем на новом сообщении если прошлое было видно на экране

		snapshotFlow { lazyListState.layoutInfo.totalItemsCount }
			.map {
				lazyListState.firstVisibleItemIndex == 1
			}
			.filter { it }
			.collect {
				lazyListState.animateScrollToItem(0)
			}
	}
	LaunchedEffect(lazyListState) {
		// прогрузка сообщений при скролинге
		snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
			.map { items ->
				when {
					items.last().key == room.orderPaired.last().messageID -> 1
					items.first().key == room.orderPaired.first().messageID -> 2
					else -> {
						0
					}
				}
			}
			.distinctUntilChanged()
			.filter { it > 0 }
			.collect {
				when (it) {
					1 -> {
						if (room.messages[room.orderPaired.last().messageID]!!.prev != null)
							backend.orderRoomMessages(
								roomID = room.roomID,
								startMsg = room.orderPaired.last().messageID,
								created = MsgCreated.BEFORE,
							)
					}
					2 -> {
						if (room.messages[room.orderPaired.first().messageID]!!.next != null)
							backend.orderRoomMessages(
								roomID = room.roomID,
								startMsg = room.orderPaired.first().messageID,
								created = MsgCreated.AFTER,
							)
					}
				}
			}

	}

}

@Composable
fun DataTag(epoch: Long, modifier: Modifier = Modifier) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(8.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = DateFormats.tagDate(epoch),
			color = MessageDataCC,
			fontSize = 13.sp
		)
	}
}

@Composable
fun MessageBody(
	msg: Message,
	lazyListState: LazyListState,
	modifier: Modifier = Modifier,
	displayAuthor: Boolean = true,
	addTopPadding: Boolean = false,
) {
	Card(
		modifier = modifier.padding(top = if (addTopPadding) 5.dp else 0.dp),
		shape = RoundedCornerShape(18.dp),
		backgroundColor = RoomCardBackgroundCC,
//		elevation = 3.dp
	) {
		Row(
			modifier = Modifier.padding(vertical = 4.dp, horizontal = 7.dp),
			verticalAlignment = Alignment.Bottom,
			horizontalArrangement = Arrangement.spacedBy(5.dp)
		) {
			Column(
				modifier = Modifier
					.widthIn(0.dp, 200.dp)
					.padding(3.dp),
				verticalArrangement = Arrangement.spacedBy(1.dp)
			) {
				if (displayAuthor)
					TextMessageAuthor(
						Cache.Data.employees[msg.empID]?.let { it.firstName + " " + it.lastName }
							.toString()
					)
				if (msg.targetID != null)
					Row(
						modifier = Modifier.padding(top = 1.dp, bottom = 1.dp, start = 4.dp),
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Box(
							Modifier
								.width(2.dp)
								.height(38.dp)
								.background(ProfileYellowCC)
						)
						Column(
//							Modifier.clickable {
//								lazyListState.scrollToItem(lazyListState.)
//							},
						) {
							TextMessageAuthor(
								Cache.Data.employees[
										Cache.Data.rooms[msg.roomID]?.messages?.get(msg.targetID)?.empID
								]?.let { it.firstName + " " + it.lastName }.toString()
							)
							TextMessageBody(
								Cache.Data.rooms[msg.roomID]?.messages?.get(
									msg.targetID
								)?.body.toString(),
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
							)
						}
					}
				TextMessageBody(msg.body)
			}
			TextMessageData(DateFormats.messageDate(msg.createdAt))
//			TextMessageData("(${msg.msgID})")
		}
	}
}

@Composable
fun MessageInput(room: Room, modifier: Modifier = Modifier) {
//	val text = rememberSaveable { mutableStateOf("") }

	TextField(
		value = room.currentInputMessageText.value,
		onValueChange = { room.currentInputMessageText.value = it },
		modifier = modifier
			.width(250.dp)
			.padding(bottom = 10.dp, top = 5.dp, start = 5.dp, end = 5.dp)
//			.background(Color.Green)
			.navigationBarsWithImePadding(),
		textStyle = TextStyle(
			fontSize = 18.sp,
			color = MainTextCC,
		),
		singleLine = false,
		maxLines = 3,
		colors = TextFieldDefaults.textFieldColors(
			backgroundColor = Color.Transparent,
			cursorColor = SendingMessageIconCC,
			focusedIndicatorColor = Color.Transparent,
			unfocusedIndicatorColor = Color.Transparent,
			errorIndicatorColor = Color.Red,
		),
		keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
		keyboardActions = KeyboardActions(
			onGo = { /*TODO*/ }
		),

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
	overflow: TextOverflow = TextOverflow.Clip,
	maxLines: Int = Int.MAX_VALUE,
//	fontSize: TextUnit = 16.sp
) {
	Text(
		text = text,
		color = color,
		overflow = overflow,
		maxLines = maxLines,
//		fontFamily = FontFamily.SansSerif,
//		fontSize = fontSize
	)
}