package ru.saime.gql_client.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.saime.gql_client.View
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.navigation.singleNavigate

@Composable
fun Login(view: View) {
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
				OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Эл. почта")})
				OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Пароль")})
				Button(onClick = {
					CoroutineScope(Dispatchers.Main).launch {
						view.loginByCredentials(login, pass) { success, err ->
							if (success) {
								view.mainNavController.singleNavigate(Screen.Home.routeRef)
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
			}
		}
	}
	SnackbarHost(snackbarHostState.value)
//	Dock()
}