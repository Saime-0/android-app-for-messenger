package ru.saime.gql_client.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pkg.type.MsgCreated
import pkg.type.RoomType
import ru.saime.gql_client.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.orderRoomMessages
import ru.saime.gql_client.backend.readMessage
import ru.saime.gql_client.backend.sendMessage
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.utils.ScreenStatus
import ru.saime.gql_client.utils.equal
import ru.saime.gql_client.utils.scrollToIndex
import ru.saime.gql_client.utils.set
import ru.saime.gql_client.widgets.*

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
						Avatar(
							Modifier
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
			if (room.lastMsgID.value == null) {
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
									else if (room.lastMsgID.value != null && room.lazyListState.layoutInfo.totalItemsCount > 1)
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
	var unreadTad = remember {
		mutableStateOf(room.lastMsgRead.value)
	}
	var displayingLoading by remember {
		mutableStateOf(MessagesLoadingDirection.NONE)
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
				if (displayingUnreadTag(room, unreadTad.value, indexInColumn)) UnreadTag()
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
			backend.eventFlow.newMessage
				.map {
					println("room.lazyListState.layoutInfo.visibleItemsInfo.first().key - ${room.lazyListState.layoutInfo.visibleItemsInfo.first().key}")
					room.lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()
//							&& room.lazyListState.firstVisibleItemIndex < 1
							&& room.lazyListState.layoutInfo.visibleItemsInfo.first().key == it.prev
//							Cache.Data.messages[]?.next == room.messagesLazyOrder.last().messageID
				}
				.filter { it }
				.collect {

					delay(50L)
					room.scrollToIndex(backend,0, animated = true)
					unreadTad.value = room.lastMsgID.value
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
					it.isNotEmpty() && room.lastMsgID.value != null && (room.lastMsgRead.value == null || it.first().key as Int > room.lastMsgRead.value!!)
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
						// пытаюсь пофиксить java.util.NoSuchElementException: List is empty
						room.lazyListState.layoutInfo.visibleItemsInfo.isEmpty() -> null

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
					displayingLoading = if (it!!.second == MsgCreated.BEFORE) MessagesLoadingDirection.TOP else MessagesLoadingDirection.BOTTOM
					backend.orderRoomMessages(
						roomID = room.roomID,
						startMsg = it.first,
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
