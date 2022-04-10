package ru.saime.gql_client.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.saime.gql_client.View
import ru.saime.gql_client.navigation.Screen

@Composable
fun Login(client: View) {
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { mutableStateOf(SnackbarHostState()) }
	var login by rememberSaveable { mutableStateOf("") }
	var pass by rememberSaveable { mutableStateOf("") }

	Column(
		Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Card(
			modifier = Modifier
				.height(300.dp)
				.width(300.dp),
			elevation = 10.dp,
		) {

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceAround,
			) {
				TextField(value = login, onValueChange = { login = it })
				TextField(value = pass, onValueChange = { pass = it })
				Button(onClick = {
					CoroutineScope(Dispatchers.Main).launch {
						client.login(login, pass) { success, err ->
							if (success) {
								client.mainNavController.navigate(Screen.Home.route) {
									popUpTo(client.mainNavController.graph.findStartDestination().id) {
										saveState = true
									}
									launchSingleTop=true
									restoreState=true
								}
							}
							else {
								scope.launch {
									snackbarHostState.value.showSnackbar(err.toString())
								}
							}

						}
					}
				}) {
					Text(text = "login")
				}
				Button(onClick = { /*TODO*/ }) {
					Text(text = "profile")
				}
			}
		}
	}
	SnackbarHost(snackbarHostState.value)
//	Dock()
}