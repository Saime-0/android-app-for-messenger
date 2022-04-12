package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import ru.saime.gql_client.DockButtonCC
import ru.saime.gql_client.DockButtonTextCC
import ru.saime.gql_client.DockCC
import ru.saime.gql_client.DockHeight
import ru.saime.gql_client.navigation.Screen


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
			navigate.DockCategory(Screen.Guide.route)
			navigate.DockCategory(Screen.Rooms.route)
			navigate.DockCategory(Screen.Profile.route)
		}
	}
}

class CategoryNavigate(private val navController: NavController) {
	@Composable
	fun DockCategory(route: String) {
		Button(
			onClick = {
				navController.navigate(route) {
					popUpTo(navController.graph.findStartDestination().id) {
						saveState = true
					}
					launchSingleTop=true
					restoreState=true
				}
			},
			colors = ButtonDefaults.buttonColors(
				backgroundColor = DockButtonCC,
				contentColor = DockButtonTextCC
			)
		) {
			Text(text = route)
		}
	}
}


@Composable
fun DockSpacer() {
	Box(modifier = Modifier.height(DockHeight))
}