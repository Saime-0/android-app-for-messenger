package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsWithImePadding
import pkg.type.RoomType
import ru.saime.gql_client.R
import ru.saime.gql_client.View
import ru.saime.gql_client.cache.Cache

@Composable
fun RoomMessages(view: View, roomID: Int) {
	val room = Cache.Data.rooms[roomID]
	Scaffold(
		topBar = {
			TopAppBar(
				navigationIcon = {
					IconButton(onClick = {
						view.mainNavController.popBackStack()
					}) {
						Icon(Icons.Filled.ArrowBack, "backIcon")
					}
				},
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
					Text(room?.name.toString())
				}
			})
		},
//		bottomBar = {
//			if (room?.view == RoomType.TALK)
//			BottomAppBar(
//			) {
//				MessageInput()
//			}
//		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(10.dp),
			contentAlignment = Alignment.BottomEnd,
		) {
			if (room?.view == RoomType.TALK)
				MessageInput()
		}
	}
}

@Composable
fun MessageInput(modifier: Modifier = Modifier) {
	val (text, setText) = rememberSaveable { mutableStateOf("") }
	OutlinedTextField(
		value = text,
		onValueChange = { setText(it) },
		modifier = Modifier.navigationBarsWithImePadding().fillMaxWidth(),
		singleLine = false,
		maxLines = 3,
		shape = RoundedCornerShape(10.dp)
	)
}