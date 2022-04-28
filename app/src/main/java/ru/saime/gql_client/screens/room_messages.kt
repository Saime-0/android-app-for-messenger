package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pkg.type.MsgCreated
import pkg.type.RoomType
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Message
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.widgets.EmptyScreen
import kotlin.collections.ArrayList

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
		Loading(isDisplayed = isLoading.value, modifier = Modifier.fillMaxSize())
		ErrorComponent(
			isDisplayed = isError.value.first,
			msg = isError.value.second,
			modifier = Modifier.fillMaxSize()
		)
		EmptyScreen(isDisplayed = isEmpty.value)
		ShowMessages(isDisplayed = isOk.value, room = room)

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
		if (room.lastMsgID == 0)
			isEmpty.value = true
		else if (room.messages.isEmpty())
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
		else isOk.value = true
	}
}

@Composable
fun ShowMessages(
	isDisplayed: Boolean,
	room: Room,
	modifier: Modifier = Modifier
) {
	val messages = remember {
		room.messages
	}
	val lazyListState = rememberLazyListState()
	if (isDisplayed)
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(8.dp),
			state = lazyListState,
			verticalArrangement = Arrangement.spacedBy(7.dp)
		) {
			var prevMsg: Message? = null
			itemsIndexed(
				items = ArrayList(messages.values),
				key = { _, msg -> msg.msgID }
			) { _, msg ->
//				lazyListState.layoutInfo.visibleItemsInfo.lastIndex
				Box(
					Modifier.fillMaxWidth(),
					if (msg.empID != Cache.Me.ID) Alignment.CenterStart else Alignment.CenterEnd
				){
					MessageBody(
						msg,
						displayAuthor = msg.empID != Cache.Me.ID && (prevMsg == null || prevMsg!!.empID != msg.empID)
					)
				}
				prevMsg = msg
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
				verticalArrangement = Arrangement.spacedBy(1.dp)
			) {
				if (displayAuthor)
				Text(
					Cache.Data.employees[msg.empID]?.let {
						it.firstName + " " + it.lastName
					}.toString(),
					color = MessageAuthorCC,
					fontSize = 14.sp
				)
				Text(msg.body, color = MainTextCC)
			}
			TextSmallProfile(
				DateFormats.messageDate(msg.createdAt),
				color = MessageDataCC,
				fontSize = 11.sp,
			)
		}
	}
}

//@Preview
//@Composable
//fun PrevMessageBody() {
//	Cache.Data.employees[7] = Employee(
//		empID = 7,
//		firstName = "Vadick",
//		lastName = "Gadgi",
//	)
//	MessageBody(
//		msg = Message(
//			roomID = 3,
//			msgID = 10,
//			createdAt = 1651156897,
//			body = "hello",
//			targetID = null,
//			empID = 7
//		)
//	)
//}

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