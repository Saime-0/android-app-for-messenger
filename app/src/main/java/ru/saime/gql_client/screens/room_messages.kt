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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pkg.type.MsgCreated
import pkg.type.RoomType
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.orderRoomMessages
import ru.saime.gql_client.backend.readMessage
import ru.saime.gql_client.backend.sendMessage
import ru.saime.gql_client.cache.*
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.utils.*
import ru.saime.gql_client.widgets.EmptyScreen

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
//				room.lastMsgRead.value = room.messagesLazyOrder.first().messageID
				callback(true)
				return true
			} else {
				callback(false)
				return false
			}
		}

}



@Composable
fun RoomMessages(backend: Backend, room: Room) {
	val screenStatus = remember {
		mutableStateOf(ScreenStatus.NONE)
	}
	var errMsg by remember { mutableStateOf("") }
	val focusRequester = remember { FocusRequester() }

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
						).let { err ->
							screenStatus.set(
								if (err != null) {
									errMsg = err
									ScreenStatus.ERROR
								} else {
									if (room.lastMsgRead.value != null)
										run find@{ // потому из форича по другому не выйти
											room.messagesLazyOrder.forEachIndexed { i, pair ->
												println("i - ($i)")
												// <= потому что список уже должен быть осортированным по убыванию
												// когда находится значение меньшее либо равное последнему прочитаному,
												// то скроллится к нему, то есть до того которое либо точно прочитано либо прочтино последнее
												if (pair.messageID <= room.lastMsgRead.value!!) {
//													println("go to - ${pair.messageID}")
													MainScope().launch {
														delay(20L)
														room.scrollToIndex(backend, i)
													}
													return@find
												}

											}
										}
									else if (room.lastMsgID != null && room.lazyListState.layoutInfo.totalItemsCount > 1)
										MainScope().launch {
											delay(20L)
											room.scrollToIndex(backend, room.lazyListState.layoutInfo.totalItemsCount - 1)
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
				LaunchedEffect(room.lazyListState) {
					snapshotFlow { room.markedMessage.messageID.value }
						.map { it != null }
						.filter { it }
						.collect { focusRequester.requestFocus() }
				}
				MarkedMessage(
					backend,
					room.markedMessage.messageID.value,
				)
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.background(DefaultTripleBarBackgroundCC),
					verticalAlignment = Alignment.Bottom,
					horizontalArrangement = Arrangement.Center,
				) {
					MessageInput(room, Modifier.focusRequester(focusRequester))
					IconButton(
						modifier = Modifier.size(66.dp),
						onClick = {
							MainScope().launch {
								sendMessage(backend, room)
								delay(100L)
								room.scrollToIndex(backend, 0)
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
	backend: Backend,
	room: Room,
	scope: CoroutineScope,
	modifier: Modifier = Modifier
) {
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
			onClick = { scope.launch { room.scrollToIndex(backend, 0) } },
			backgroundColor = MessageBackgroundCC,
			contentColor = Color.White,
		)
}


@Composable
fun MarkedMessage(backend: Backend, msgID: Int?) {
	if (msgID != null) {
		Cache.Data.messages[msgID]?.let { msg ->
			Cache.Data.rooms[msg.roomID]?.let { room ->
				Row(
					modifier = Modifier
						.background(DefaultTripleBarBackgroundCC)
						.padding(horizontal = 40.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					ReplayedMessage(
						modifier = Modifier
							.weight(1f)
							.clickable {
								MainScope().launch {
									room.scrollToIndex(backend, room.markedMessage.indexInColumn)
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

fun displayingUnreadTag(room: Room, unreadMsgID: Int?, indexOfLazyMessage: Int): Boolean {
	return indexOfLazyMessage != 0
			&& indexOfLazyMessage + 1 != room.messagesLazyOrder.size
			&& unreadMsgID != null
			&& (room.messagesLazyOrder[indexOfLazyMessage + 1].messageID <= unreadMsgID && room.messagesLazyOrder[indexOfLazyMessage].messageID > unreadMsgID)

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowMessages(
	backend: Backend,
	room: Room,
	modifier: Modifier = Modifier
) {
	val coroutineScope = rememberCoroutineScope()
	val unreadTad = remember {
		room.lastMsgRead.value
	}
	var displayingLoading by remember {
		mutableStateOf(MessagesLoadingDirection.NONE)
	}
//	var prevLazyMessage by remember {
//		mutableStateOf(null as Message?)
//	}
	var prevLazyMessageID = remember {
		null as Int?
	}
	println("загружается ShowMessages")
//	val lazyListState = rememberForeverLazyListState(Screen.RoomMessages(room.roomID).routeWithArgs)


	LazyColumn(
		modifier = modifier,
		state = room.lazyListState,
		reverseLayout = true,
//		verticalArrangement = Arrangement.spacedBy(7.dp)
	) {
		itemsIndexed(
			items = room.messagesLazyOrder,
			key = { _, orderPair -> orderPair.messageID }
		) { indexInColumn, lazyMessage ->

			Cache.Data.messages[lazyMessage.messageID]?.let { msg ->
				// bottom padding
				Spacer(Modifier.height(3.dp))

				Row(
					modifier = Modifier
						.padding(horizontal = 8.dp),
					verticalAlignment = Alignment.Bottom,
					horizontalArrangement = Arrangement.Start
				) {

//					if (room.markedMessage.messageID.value != null && msg.msgID == room.markedMessage.messageID.value)
//						Card(
//							modifier = Modifier
//								.padding(3.dp)
//								.size(30.dp),
//							shape = CircleShape,
//							backgroundColor = MainBrightCC
//						) {
//							Icon(Icons.Filled.Check, null, tint = Color.White)
//						}

					Box(
						Modifier
							.weight(1f),
						lazyMessage.alignment
					) {

						MessageBody(
							msg = msg,
							backend = backend,
							room = room,
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
							backgroundColor = (
									if (msg.msgID != room.markedMessage.messageID.value)
										lazyMessage.backgroundColor
									else MarkedMessageBackgroundCC),
							displayAuthor = lazyMessage.displayingName,
//							addTopPadding = lazyMessage.addTopPadding
						)

					}

				} // здесь потому что LazyColumn.reverseLayout = true:

				// top padding
				Spacer(Modifier.height(if (lazyMessage.addTopPadding) 10.dp else 3.dp))
				// different data tag
				if (lazyMessage.displayingData) DataTag(msg.createdAt)
				// unread tag
				if (displayingUnreadTag(room, unreadTad, indexInColumn)) UnreadTag()
			}
		}

	}
	// if loading in bottom messages
	if (displayingLoading != MessagesLoadingDirection.NONE)
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment =
			if (displayingLoading == MessagesLoadingDirection.TOP) Alignment.TopCenter
			else Alignment.BottomCenter
		) {
			Loading()
		}

	// Кнопка го даун
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
		GoToDown(backend, room, coroutineScope)
	}

	LaunchedEffect(room.lazyListState) {

		// фокусироваться на последнем на новом сообщении если прошлое было видно на экране.
		launch {
			snapshotFlow { room.lazyListState.layoutInfo.totalItemsCount }
				.map {
					println("room.lazyListState.firstVisibleItemIndex == 1 = ${room.lazyListState.firstVisibleItemIndex <= 1} (${room.lazyListState.firstVisibleItemIndex})")
					room.lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty() && room.lazyListState.firstVisibleItemIndex <= 1 && Cache.Data.messages[room.lazyListState.layoutInfo.visibleItemsInfo.first().key as Int]?.prev == null
				}
				.filter { it }
				.collect {
					delay(50L)
					room.scrollToIndex(backend,0, animated = true)
				}
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
//					Сюда можно было бы добавить такую проверку:
//					room.messagesLazyOrder[it.first().index].employeeID != Cache.Me.ID &&
//					Но что если я заnullил в бд его прочитанное,  тогда клиет не будет совсем реагировать на свои сообщеня, а лучше пусть реагирует
//					Можно добавить что если если это свое сообщение и оно последнее(prev == null)
//					То тогда не надо читать..
					room.lastMsgID != null && (room.lastMsgRead.value == null || it.first().key as Int > room.lastMsgRead.value!!)
				}
				.filter { it }
				.collect {
					backend.readMessage(
						room.roomID,
						room.lazyListState.layoutInfo.visibleItemsInfo.first().key as Int
					).let {
						if (it != null) println(it)
					}
				}
		}

		// прогрузка сообщений при скролинге
		launch(Dispatchers.IO) {
			snapshotFlow { room.lazyListState.layoutInfo.visibleItemsInfo }
				.map { items ->
					when {
//						items.last().key == room.messagesLazyOrder.last().messageID -> 1 // при скроле вверх
//						items.first().key == room.messagesLazyOrder.first().messageID -> 2 // при скроле вниз

						Cache.Data.messages[items.last().key]?.prev != null
								&& !Cache.Data.messages.containsKey(Cache.Data.messages[items.last().key]?.prev)
						-> Pair(items.last().key as Int, MsgCreated.BEFORE) // при скроле вверх

						Cache.Data.messages[items.first().key]?.next != null
								&& !Cache.Data.messages.containsKey(Cache.Data.messages[items.first().key]?.next)
						-> Pair(items.first().key as Int, MsgCreated.AFTER) // при скроле вниз

						else -> {
							null
						}
					}
				}
				.filter { it != null }
				.collect {
					delay(5L) // иначе при отправке своего сообщения будет отображаться загрузка
					displayingLoading = MessagesLoadingDirection.TOP
					backend.orderRoomMessages(
						roomID = room.roomID,
						startMsg = it!!.first,
						created = it.second,
					)
					displayingLoading = MessagesLoadingDirection.NONE
				}
		}

	}
}

@Composable
fun UnreadTag(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(vertical = 7.dp)
			.background(ProfileSectionBackgroundCC),
		contentAlignment = Alignment.Center
	) {
		Text("Непрочитанные", Modifier.padding(2.dp), color = MainTextCC)
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
	room: Room,
	modifier: Modifier = Modifier,
	backgroundColor: Color = MessageBackgroundCC,
	displayAuthor: Boolean = true,
//	addTopPadding: Boolean = false,
) {

	Card(
		modifier = modifier,
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
					ReplayedMessage(
						msg = Cache.Data.messages[msg.targetID]!!,
						modifier = Modifier.clickable {
							MainScope().launch { room.scrollToMsg(backend, msg.targetID) }
						}
					)
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
			color = Color.White,
		),
		placeholder = { Text("Сообщение", color = MessageMeBackgroundCC, fontSize = 18.sp) },
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