package ru.saime.gql_client.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import ru.saime.gql_client.navigation.Screen


@Composable
fun Dock(navController: NavController) {
	val navigate = CategoryNavigate(navController)
	Card(
		shape = RoundedCornerShape(20.dp),
		elevation = 10.dp,
		modifier = Modifier
//			.width(600.dp)
			.fillMaxWidth()
	) {
		Row(
			modifier = Modifier.height(70.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceAround
		) {
			navigate.DockCategory(Screen.Guide.route)
			navigate.DockCategory(Screen.Rooms.route)
			navigate.DockCategory(Screen.Profile.route)
		}
	}
}

class CategoryNavigate(val navController: NavController) {
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
				backgroundColor = Color.DarkGray,
				contentColor = Color.LightGray
			)
		) {
			Text(text = route)
		}
	}
}
