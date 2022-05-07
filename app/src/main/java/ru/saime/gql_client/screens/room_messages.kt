package ru.saime.gql_client.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pkg.type.MsgCreated
import pkg.type.RoomType
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.orderRoomMessages
import ru.saime.gql_client.backend.readMessage
import ru.saime.gql_client.backend.sendMessage
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Message
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.utils.*
import ru.saime.gql_client.widgets.EmptyScreen
import java.util.*

suspend fun sendMessage(
	backend: Backend,
	room: Room,
	callback: (success: Boolean) -> Unit = {}
): Boolean {
	backend.sendMessage(
		room.roomID,
		room.currentInputMessageText.value,
		room.markedMessage.messageID.value
	)
		.let { err ->
			if (err == null) {
				room.currentInputMessageText.value = ""
				room.markedMessage.clear()
				callback(true)
				return true
			} else {
				callback(false)
				return false
			}
		}

}

val c1: Calendar = Calendar.getInstance()
val c2: Calendar = Calendar.getInstance()


@Composable
fun RoomMessages(backend: Backend, room: Room) {
	val screenStatus = remember {
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
				contentColor = MainTextCC,
				backgroundColor = DefaultTripleBarBackgroundCC,
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
		if (screenStatus.equal(ScreenStatus.EMPTY)) {
			LaunchedEffect(room.messagesLazyOrder) {
				// когда приходит сообщение надо будет обновить статут экрана для того чтобы отобразился компонент ShowMessages
				snapshotFlow { room.messagesLazyOrder.size }
					.filter { it > 0 }
					.collect {
						screenStatus.set(ScreenStatus.NONE)
					}
			}
		}

		SideEffect { // внутри скаффолд потому что переклдючение статуса на OK отрабатывает перед скаффолд, я так думаю...
			println("echo RoomMessages.SideEffect")
			if (room.lastMsgID == null) {
				screenStatus.set(ScreenStatus.EMPTY)
			} else if (room.messagesLazyOrder.isEmpty()) {
				if (screenStatus.equal(ScreenStatus.NONE))
					MainScope().launch {
						screenStatus.set(ScreenStatus.LOADING)
						backend.orderRoomMessages(
							roomID = room.roomID,
							startMsg = room.lastMsgRead.value ?: 1,
							created =
							if (room.lastMsgRead.value != null)
								MsgCreated.BEFORE
							else MsgCreated.AFTER,
						) { err ->
							screenStatus.set(
								if (err != null) {
									errMsg = err
									ScreenStatus.ERROR
								} else {
									if (room.lastMsgRead.value != null)
										room.messagesLazyOrder.forEachIndexed { i, pair ->
											println("i - $i, pair - $pair")
											if (pair.messageID == room.lastMsgRead.value!!) {
												println("go to - ${pair.messageID}")
												MainScope().launch {
													room.lazyListState.scrollToItem(
														i
													)
												}
												return@forEachIndexed
											}

										}
									ScreenStatus.OK
								}
							)
						}
					}
			} else
				screenStatus.set(ScreenStatus.OK)
		}

		Column {
			Box(modifier = Modifier.weight(1f)) {
				when (screenStatus.value) {
					ScreenStatus.LOADING -> Loading(Modifier.fillMaxSize())
					ScreenStatus.ERROR -> ErrorComponent(errMsg, Modifier.fillMaxSize())
					ScreenStatus.OK -> ShowMessages(backend, room)
					ScreenStatus.EMPTY -> EmptyScreen(true)
					else -> {
						EmptyScreen(true)
					}
				}
			}
			if (room.view == RoomType.TALK) {
				MarkedMessage(
					msgID = room.markedMessage.messageID.value,
				)
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
								room.lazyListState.scrollToItem(0)
							}
						}
					) {
						Icon(Icons.Filled.Send, null, tint = SendingMessageIconCC)
					}
				}


			}
		}

	}
}

@Composable
fun GoToDown(
	room: Room,
	scope: CoroutineScope,
	modifier: Modifier = Modifier
) {
//	if (isDisplaying)
//		Icon(
//			Icons.Default.ArrowDropDown,
//			contentDescription = null,
//			tint = ProfileDimCC
//		)
	if (room.displayingGoDown.value)
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
			onClick = { scope.launch { room.lazyListState.scrollToItem(0) } },
			backgroundColor = MessageBackgroundCC,
			contentColor = Color.White,
		)
//		IconButton(
//			onClick = {
//				scope.launch { room.lazyListState.animateScrollToItem(0) }
//			},
//			modifier = modifier
//				.size(50.dp)
//				.background(MessageBackgroundCC)
//				.border(1.dp, Color.Red, shape = CircleShape)
//		) {
//			Icon(
//				Icons.Default.ArrowDropDown,
//				contentDescription = null,
//				tint = ProfileDimCC
//			)
//
//		}
}

//fun displayingEmployeeName(room: Room, indexOfPairInOrder: Int): Boolean {
//	return indexOfPairInOrder + 1 == room.messagesOrder.size || room.messagesOrder[indexOfPairInOrder].employeeID != room.messagesOrder[indexOfPairInOrder + 1].employeeID
//}
//
//
//fun displayingDataTag(room: Room, indexOfPairInOrder: Int): Boolean {
//	if (indexOfPairInOrder + 1 == room.messagesOrder.size) return true
//
//	c1.setTime(Date(Cache.Data.messages[room.messagesOrder[indexOfPairInOrder].messageID]!!.createdAt))
//	c2.setTime(Date(Cache.Data.messages[room.messagesOrder[indexOfPairInOrder + 1].messageID]!!.createdAt))
//
//	return c1.get(Calendar.DAY_OF_YEAR) != c2.get(Calendar.DAY_OF_YEAR)
//			|| c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)
//}


@Composable
fun MarkedMessage(msgID: Int?) {
	if (msgID != null) {
		Cache.Data.messages[msgID]?.let { msg ->
			Cache.Data.rooms[msg.roomID]?.let { room ->
				Row(
					modifier = Modifier
						.background(DividerDarkCC)
						.padding(horizontal = 40.dp, vertical = 6.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					ReplayedMessage(
						modifier = Modifier
							.weight(1f)
							.clickable {
								MainScope().launch {
									room.lazyListState.scrollToItem(room.markedMessage.indexInColumn)
								}
							},
						msg = msg
					)
					IconButton(onClick = { room.markedMessage.clear() }) {
						Icon(
							Icons.Filled.Close,
							null,
							tint = MainTextCC
						)
					}
				}
				Box( // нижняя граница
					modifier = Modifier
						.fillMaxWidth()
						.height(2.dp)
						.background(BackgroundCC)
				)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowMessages(
	backend: Backend,
	room: Room,
	modifier: Modifier = Modifier
) {
	val coroutineScope = rememberCoroutineScope()

	println("загружается ShowMessages")
//	val lazyListState = rememberForeverLazyListState(Screen.RoomMessages(room.roomID).routeWithArgs)
	LazyColumn(
		modifier = modifier
			.padding(horizontal = 8.dp),
		state = room.lazyListState,
		reverseLayout = true,
		verticalArrangement = Arrangement.spacedBy(7.dp)
	) {
		itemsIndexed(
			items = room.messagesLazyOrder,
			key = { _, orderPair -> orderPair.messageID }
		) { indexInColumn, lazyMessage ->

			Cache.Data.messages[lazyMessage.messageID]?.let { msg ->
				if (indexInColumn == 0)
					Box(Modifier.height(5.dp))
				Row(
					verticalAlignment = Alignment.Bottom,
					horizontalArrangement = Arrangement.Start
				) {
					if (room.markedMessage.messageID.value != null && msg.msgID == room.markedMessage.messageID.value)
						Card(
							modifier = Modifier
								.padding(3.dp)
								.size(30.dp),
							shape = CircleShape,
							backgroundColor = MainBrightCC
						) {
							Icon(Icons.Filled.Check, null, tint = MainTextCC)
						}

					Box(
						Modifier.weight(1f),
						lazyMessage.alignment
					) {

						MessageBody(
							msg = msg,
							backend = backend,
							modifier = Modifier.combinedClickable(
								onClick = {
									if (room.markedMessage.messageID.value == lazyMessage.messageID)
										room.markedMessage.messageID.value = null
								},
								onLongClick = {
									if (room.markedMessage.messageID.value != lazyMessage.messageID) {
										room.markedMessage.set(
											lazyMessage.messageID,
											indexInColumn
										)
										backend.vibrateHelper.vibrate(50)
									}
								},
							),
							backgroundColor = lazyMessage.backgroundColor,
							displayAuthor = lazyMessage.displayingName,
							addTopPadding = lazyMessage.addTopPadding
						)

					}

				}
				// здесь потому что LazyColumn.reverseLayout = true
				if (lazyMessage.displayingData) DataTag(msg.createdAt)

			}

		}

	}
	// Кнопка го даун
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
		GoToDown(room = room, scope = coroutineScope)
	}

	LaunchedEffect(room.lazyListState) {

		// фокусироваться на последнем на новом сообщении если прошлое было видно на экране.
		launch {
			snapshotFlow { room.lazyListState.layoutInfo.totalItemsCount }
				.map { room.lazyListState.firstVisibleItemIndex == 1 }
				.filter { it }
				.collect { room.lazyListState.animateScrollToItem(0) }
		}

		// показывать скрелку вниз для скорла к последним сообщениям
		launch { // если добавить диспатчер то приложение будет падать с ошибкой "стейт читается когда снимок еще не был создан"
			snapshotFlow { room.lazyListState.layoutInfo.visibleItemsInfo }
				.map { list ->
					if (list.isNotEmpty())
						list.first().index > 4
					else false
				}
				.distinctUntilChanged()
				.collect { room.displayingGoDown.value = it }
		}

		// чтение сообщений
		launch {
			snapshotFlow { room.lazyListState.layoutInfo.visibleItemsInfo }
				.map {
					room.lastMsgID != null && room.lastMsgRead.value == null || it.first().key as Int > room.lastMsgRead.value!!
				}
				.filter { it }
				.collect {
					backend.readMessage(
						room.roomID,
						room.lazyListState.layoutInfo.visibleItemsInfo.first().key as Int
					)
				}
		}

		// прогрузка сообщений при скролинге
		launch(Dispatchers.IO) {
			snapshotFlow { room.lazyListState.layoutInfo.visibleItemsInfo }
				.map { items ->
					when {
						items.last().key == room.messagesLazyOrder.last().messageID -> 1 // при скроле вверх
						items.first().key == room.messagesLazyOrder.first().messageID -> 2 // при скроле вниз
						else -> {
							0
						}
					}
				}
				.filter { it > 0 }
				.collect {
					when (it) {
						1 -> {
							if (Cache.Data.messages[room.messagesLazyOrder.last().messageID]!!.prev != null)
								backend.orderRoomMessages(
									roomID = room.roomID,
									startMsg = room.messagesLazyOrder.last().messageID,
									created = MsgCreated.BEFORE,
								)
						}
						2 -> {
							if (Cache.Data.messages[room.messagesLazyOrder.first().messageID]!!.next != null)
								backend.orderRoomMessages(
									roomID = room.roomID,
									startMsg = room.messagesLazyOrder.first().messageID,
									created = MsgCreated.AFTER,
								)
						}
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
	backend: Backend,
	modifier: Modifier = Modifier,
	backgroundColor: Color = MessageBackgroundCC,
	displayAuthor: Boolean = true,
	addTopPadding: Boolean = false,
) {

	Card(
		modifier = modifier.padding(top = if (addTopPadding) 5.dp else 0.dp),
		shape = RoundedCornerShape(18.dp),
		backgroundColor = backgroundColor,
	) {
		Row(
			modifier = Modifier.padding(vertical = 4.dp, horizontal = 11.dp),
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
					Cache.Data.employees[msg.empID]?.let { emp ->
						TextMessageAuthor(
							emp.firstName + " " + emp.lastName,
							modifier = Modifier
								.clickable {
									backend.mainNavController.navigate(Screen.Profile(emp.empID).routeWithArgs)
								}
						)
					}
				if (msg.targetID != null && Cache.Data.messages[msg.targetID] != null)
					ReplayedMessage(Cache.Data.messages[msg.targetID]!!)
				TextMessageBody(msg.body)
				TextMessageData("(${msg.msgID})")
			}
			TextMessageData(DateFormats.messageDate(msg.createdAt))
		}
	}
}

@Composable
fun ReplayedMessage(msg: Message, modifier: Modifier = Modifier) {
	Row(
		modifier = modifier
			.padding(top = 1.dp, bottom = 1.dp, start = 4.dp),
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			Modifier
				.width(2.dp)
				.height(38.dp)
				.background(MessageReplyLineCC)
		)
		Column {
			TextMessageAuthor(
				Cache.Data.employees[msg.empID]?.let {
					it.firstName + " " + it.lastName
				}.toString()
			)
			TextMessageBody(
				msg.body,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

@Composable
fun MessageInput(room: Room, modifier: Modifier = Modifier) {
	TextField(
		value = room.currentInputMessageText.value,
		onValueChange = { room.currentInputMessageText.value = it },
		modifier = modifier
			.width(250.dp)
			.padding(bottom = 10.dp, top = 5.dp, start = 5.dp, end = 5.dp)
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
	)

}

@Composable
fun TextMessageAuthor(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = MessageAuthorCC,
	fontSize: TextUnit = 14.sp
) {
	Text(
		modifier = modifier,
		text = text,
		color = color,
		fontSize = fontSize
	)
}

@Composable
fun TextMessageData(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = MessageDataCC,
	fontSize: TextUnit = 11.sp,
) {
	Text(
		text = text,
		modifier = modifier,
		color = color,
		fontSize = fontSize
	)
}

@Composable
fun TextMessageBody(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = MessageTextCC,
	overflow: TextOverflow = TextOverflow.Clip,
	maxLines: Int = Int.MAX_VALUE,
	onTextLayout: (TextLayoutResult) -> Unit = {},
//	fontSize: TextUnit = 16.sp
) {
	Text(
		text = text,
		modifier = modifier,
		color = color,
		overflow = overflow,
		maxLines = maxLines,
		onTextLayout = onTextLayout,
//		fontFamily = FontFamily.SansSerif,
//		fontSize = fontSize
	)
}