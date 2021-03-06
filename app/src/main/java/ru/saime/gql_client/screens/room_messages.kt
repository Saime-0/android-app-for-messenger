package ru.saime.gql_client.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import ru.saime.gql_client.navigation.Screen
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
fun TextRoomNameTopBar(name: String) {
	Text(
		text = name,
		color = MainTextCC,
		fontSize = 19.sp,
		fontWeight = FontWeight.Normal,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
	)
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
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(end = 30.dp)
					) {
						Photo(
							room.photo.value,
							Modifier
								.padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
								.size(33.dp)
								.clip(CircleShape)
						)
						TextRoomNameTopBar(room.name)
					}
				})
		},
	) {
		if (screenStatus.equal(ScreenStatus.EMPTY)) {
			LaunchedEffect(room.messagesLazyOrder) {
				// ?????????? ???????????????? ?????????????????? ???????? ?????????? ???????????????? ???????????? ???????????? ?????? ???????? ?????????? ?????????????????????? ?????????????????? ShowMessages
				snapshotFlow { room.messagesLazyOrder.size }
					.filter { it > 0 }
					.collect {
						screenStatus.set(ScreenStatus.NONE)
					}
			}
		}

		SideEffect { // ???????????? ???????????????? ???????????? ?????? ?????????????????????????? ?????????????? ???? OK ???????????????????????? ?????????? ????????????????, ?? ?????? ??????????...
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
										run find@{ // ???????????? ???? ???????????? ???? ?????????????? ???? ??????????
											room.messagesLazyOrder.forEachIndexed { i, pair ->
												println("i - ($i)")
												// <= ???????????? ?????? ???????????? ?????? ???????????? ???????? ???????????????????????????? ???? ????????????????
												// ?????????? ?????????????????? ???????????????? ?????????????? ???????? ???????????? ???????????????????? ??????????????????????,
												// ???? ???????????????????? ?? ????????, ???? ???????? ???? ???????? ?????????????? ???????? ?????????? ?????????????????? ???????? ???????????????? ??????????????????
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
	val unreadTad = remember {
		mutableStateOf(room.lastMsgRead.value)
	}
	var displayingLoading by remember {
		mutableStateOf(MessagesLoadingDirection.NONE)
	}

	println("?????????????????????? ShowMessages")
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
						.padding(
							start = if (room.view == RoomType.BLOG) 8.dp else 0.dp,
							end = 8.dp,
						),
					verticalAlignment = Alignment.Bottom,
					horizontalArrangement = Arrangement.Start
				) {

					if (lazyMessage.displayingName)
						Cache.Data.employees[msg.empID!!]?.let { emp ->
						Box(
							modifier = Modifier
								.padding(MessagePhotoPadding.dp)
								.size(MessagePhotoSize.dp)
								.clip(CircleShape)
								.align(Alignment.Top)
								.clickable {
									backend.mainNavController.navigate(Screen.Profile(emp.empID).routeWithArgs)
								}
						) {
							Photo(emp.photo.value)
						}
				}
					Box(
						Modifier
							.padding(
								start = if (!lazyMessage.displayingName) MessageWithoutPhotoPadding.dp else 0.dp
							)
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
				} // ?????????? ???????????? ?????? LazyColumn.reverseLayout = true:

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

	// ???????????? ???? ????????
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
		GoToDown(backend, room, coroutineScope)
	}

	LaunchedEffect(room.lazyListState) {

		// ???????????????????????????? ???? ?????????????????? ???? ?????????? ?????????????????? ???????? ?????????????? ???????? ?????????? ???? ????????????.
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

		// ???????????????????? ?????????????? ???????? ?????? ???????????? ?? ?????????????????? ????????????????????
		launch { // ???????? ???????????????? ?????????????????? ???? ???????????????????? ?????????? ???????????? ?? ?????????????? "?????????? ???????????????? ?????????? ???????????? ?????? ???? ?????? ????????????"
			snapshotFlow { room.lazyListState.layoutInfo.visibleItemsInfo }
				.map { list ->
					if (list.isNotEmpty())
						list.first().index > 4
					else false
				}
				.distinctUntilChanged()
				.collect { room.displayingGoDown.value = it }
		}

		// ???????????? ??????????????????
		launch {
			snapshotFlow { room.lazyListState.layoutInfo.visibleItemsInfo }
				.map {
//					???????? ?????????? ???????? ???? ???????????????? ?????????? ????????????????:
//					room.messagesLazyOrder[it.first().index].employeeID != Cache.Me.ID &&
//					???? ?????? ???????? ?? ????null???? ?? ???? ?????? ??????????????????????,  ?????????? ?????????? ???? ?????????? ???????????? ?????????????????????? ???? ???????? ????????????????, ?? ?????????? ?????????? ??????????????????
//					?????????? ???????????????? ?????? ???????? ???????? ?????? ???????? ?????????????????? ?? ?????? ??????????????????(prev == null)
//					???? ?????????? ???? ???????? ????????????..
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

		// ?????????????????? ?????????????????? ?????? ??????????????????
		launch(Dispatchers.IO) {
			snapshotFlow { room.lazyListState.layoutInfo.visibleItemsInfo }
				.map { items ->

					when {
						// ?????????????? ?????????????????? java.util.NoSuchElementException: List is empty
						room.lazyListState.layoutInfo.visibleItemsInfo.isEmpty() -> null

						Cache.Data.messages[items.last().key]?.prev != null
								&& !Cache.Data.messages.containsKey(Cache.Data.messages[items.last().key]?.prev)
						-> Pair(items.last().key as Int, MsgCreated.BEFORE) // ?????? ???????????? ??????????

						Cache.Data.messages[items.first().key]?.next != null
								&& !Cache.Data.messages.containsKey(Cache.Data.messages[items.first().key]?.next)
						-> Pair(items.first().key as Int, MsgCreated.AFTER) // ?????? ???????????? ????????

						else -> {
							null
						}
					}
				}
				.filter { it != null }
				.collect {
					delay(5L) // ?????????? ?????? ???????????????? ???????????? ?????????????????? ?????????? ???????????????????????? ????????????????
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
		Text("??????????????????????????", Modifier.padding(2.dp), color = MainTextCC)
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
