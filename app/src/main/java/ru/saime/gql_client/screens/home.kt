package ru.saime.gql_client.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.widgets.Dock

@Composable
fun Home(backend: Backend) {
	val navController = rememberNavController()

	// state
	var profileID = 0


	Column(
		modifier = Modifier
			.fillMaxSize()
//			.background(BackgroundCC)
	) {
		NavHost(
			navController = navController,
			startDestination = Screen.Profile().routeRef,
		modifier = Modifier.padding(14.dp)
		) {
//			composable(Screen.Guide.route) { Guide() }
			composable(Screen.Rooms.routeRef) { Rooms(backend) }
			composable(
				Screen.Profile().routeRef,
				arguments = listOf(navArgument(Screen.Profile.Args.EmpID.name) {
					type = NavType.IntType
				})
			) {
				if (it.arguments != null) {
					Profile(
						backend = backend,
						empID = it.arguments!!.getInt(Screen.Profile.Args.EmpID.name),
					)
					profileID = it.arguments!!.getInt(Screen.Profile.Args.EmpID.name)
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