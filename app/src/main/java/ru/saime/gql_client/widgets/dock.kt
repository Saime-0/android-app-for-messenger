package ru.saime.gql_client.widgets

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.saime.gql_client.DockButtonCC
import ru.saime.gql_client.DockButtonTextCC
import ru.saime.gql_client.DockCC
import ru.saime.gql_client.DockHeight
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.navigation.singleNavigate


@Composable
fun Dock(navController: NavController) {
	val navigate = CategoryNavigate(navController)
	Card(
		modifier = Modifier
			.fillMaxWidth(),
		shape = RoundedCornerShape(17.dp),
		backgroundColor = DockCC,
		elevation = 10.dp
	) {
		Row(
			modifier = Modifier.height(DockHeight),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceAround
		) {
			navigate.DockCategory(Screen.Guide.name, Screen.Guide.routeRef)
			navigate.DockCategory(Screen.Rooms.name, Screen.Rooms.routeRef) // ----xfixme: RoomID from view.state.roomid
			navigate.DockCategory(
				Screen.Profile().name,
				Screen.Profile(0).routeWithArgs,
				modifier = Modifier.pointerInput(Unit){
					detectTapGestures(
						onDoubleTap = {
							// perform some action here..
						}
					)
				}
			)
		}
	}
}

class CategoryNavigate(
	private val navController: NavController
) {
	@Composable
	fun DockCategory(
		text: String,
		route: String,
		modifier: Modifier = Modifier
	) {
		Button(
			onClick = {
				navController.singleNavigate(route)
			},
			modifier = modifier,
			colors = ButtonDefaults.buttonColors(
				backgroundColor = DockButtonCC,
				contentColor = DockButtonTextCC
			)
		) {
			Text(text = text, fontWeight = FontWeight.Normal)
		}
	}
}


@Composable
fun DockSpacer() {
	Box(modifier = Modifier.height(DockHeight))
}