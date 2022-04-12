package ru.saime.gql_client.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.saime.gql_client.View
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.widgets.Dock

@Composable
fun Home(view: View) {
	val navController = rememberNavController()
	var profileID = remember {	mutableStateOf(0)}
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
			composable(Screen.Profile.route) { LoadProfile(view = view, empID = profileID.value)}
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