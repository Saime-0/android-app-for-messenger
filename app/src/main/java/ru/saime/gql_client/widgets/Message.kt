package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Message
import ru.saime.gql_client.cache.Room
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.utils.scrollToIndex
import ru.saime.gql_client.utils.scrollToMsg


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
//				TextMessageData("(${msg.msgID})") // id
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


@Composable
fun MessageInput(room: Room, modifier: Modifier = Modifier) {
	TextField(
		value = room.currentInputMessageText.value,
		onValueChange = { room.currentInputMessageText.value = it },
		modifier = modifier
			.width(250.dp)
			.padding(bottom = 10.dp, start = 5.dp)
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