package ru.saime.gql_client.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.loginByCredentials
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.navigation.singleNavigate

@Composable
fun Login(backend: Backend) {
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
			elevation = 7.dp,
		) {

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceAround,
			) {
				OutlinedTextField(
					value = login,
					onValueChange = { login = it },
					modifier = Modifier.width(250.dp),
					singleLine = true,
					label = { Text("Эл. почта") },
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Email
					)
				)
				OutlinedTextField(
					value = pass,
					onValueChange = { pass = it },
					modifier = Modifier.width(250.dp),
					singleLine = true,
					label = { Text("Пароль") },
					visualTransformation = PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Password,
						imeAction = ImeAction.Go,
					),
					keyboardActions = KeyboardActions(
						onGo = {
							MainScope().launch {
								backend.loginByCredentials(login, pass) { success, err ->
									if (success) {
										backend.mainNavController.singleNavigate(Screen.Home.routeRef)
									}
									else {
										scope.launch {
											snackbarHostState.value.showSnackbar(err.toString())
										}
									}

								}
							}
						}
					),
				)
				Button(onClick = {
					MainScope().launch {
						backend.loginByCredentials(login, pass) { success, err ->
							if (success) {
								backend.mainNavController.singleNavigate(Screen.Home.routeRef)
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
//
//suspend fun goLoginlogin: String, pass: String,) {
//
//}