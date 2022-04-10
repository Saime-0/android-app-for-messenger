package ru.saime.gql_client.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.saime.gql_client.View
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.widgets.Dock

@Composable
fun Home(view: View) {
	val navController = rememberNavController()

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Cyan)
	) {
		NavHost(
			navController = navController,
			startDestination = Screen.Profile.route
		) {
//			composable(Screen.Guide.route) { Guide() }
			composable(Screen.Rooms.route) { Rooms(view) }
			composable(Screen.Profile.route) {
				CoroutineScope(Dispatchers.IO).launch {
					view.orderMe {result, err ->
						if (err != null)
							println(err)
//						result!!.employee.

					}
				}
			}
		}
	}

	// float dock
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(10.dp),
		contentAlignment = Alignment.BottomEnd
	) {
		Dock(navController)
	}
}