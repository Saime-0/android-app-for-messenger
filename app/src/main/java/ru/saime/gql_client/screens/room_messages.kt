package ru.saime.gql_client.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.saime.gql_client.View

@Composable
fun RoomMessages(view: View, roomID: Int) {
	Scaffold(
		topBar = {
				 Text(text = "room $roomID")
		},
		bottomBar = {
			MessageInput()
		}
	) {

	}
}

@Composable
fun MessageInput(modifier: Modifier = Modifier) {
	val (text, setText) = rememberSaveable { mutableStateOf("") }
	TextField(
		value = text,
		onValueChange = { setText(it) },
		singleLine = false,
		maxLines = 3,
		shape = RoundedCornerShape(10.dp)
	)
}