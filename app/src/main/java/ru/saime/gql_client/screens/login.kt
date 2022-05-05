package ru.saime.gql_client.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.loginByCredentials
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.navigation.singleNavigate

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Login(backend: Backend) {
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { mutableStateOf(SnackbarHostState()) }

	var login by rememberSaveable { mutableStateOf("") }
	var pass by rememberSaveable { mutableStateOf("") }
	val focusManager = LocalFocusManager.current

	Column(
		Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Card(
			modifier = Modifier.size(300.dp),
			shape = RoundedCornerShape(20.dp),
			elevation = 10.dp,
			backgroundColor = ProfileSectionBackgroundCC
		) {

			Column(
				modifier = Modifier.padding(vertical=30.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceAround,
			) {
				Column(
					modifier = Modifier.height(200.dp),
					verticalArrangement = Arrangement.spacedBy(20.dp)
				) {

					LoginTextField(
						value = login,
						onValueChange = { login = it },
						modifier = Modifier.onPreviewKeyEvent {
							if (it.key == Key.Tab) {
								focusManager.moveFocus(FocusDirection.Down)
								true
							} else {
								false
							}
						},
						label = { Text("Эл. почта") },
						keyboardOptions = KeyboardOptions(
							keyboardType = KeyboardType.Email,
							imeAction = ImeAction.Next,
						),
						keyboardActions = KeyboardActions(
							onNext = { focusManager.moveFocus(FocusDirection.Down) }
						),
					)
					LoginTextField(
						value = pass,
						onValueChange = { pass = it },
						label = { Text("Пароль") },
						visualTransformation = PasswordVisualTransformation(),
						keyboardOptions = KeyboardOptions(
							keyboardType = KeyboardType.Password,
							imeAction = ImeAction.Go,
						),
						keyboardActions = KeyboardActions(
							onGo = {
								MainScope().launch {
									backend.loginByCredentials(login, pass).let { err ->
										if (err == null) {
											backend.mainNavController.navigate(Screen.Rooms.routeRef)
										} else {
											scope.launch {
												snackbarHostState.value.showSnackbar(err.toString())
											}
										}

									}
								}
							}
						),
					)
				}
				LoginButton(onClick = {
					MainScope().launch {
						backend.loginByCredentials(login, pass).let { err ->
							if (err == null) {
								backend.mainNavController.navigate(Screen.Rooms.routeRef)
							} else {
								scope.launch {
									snackbarHostState.value.showSnackbar(err.toString())
								}
							}

						}
					}
				})
			}
		}
	}
	SnackbarHost(snackbarHostState.value)
}

@Composable
fun LoginButton(
	onClick: () -> Unit,
) {
	Button(
		onClick = onClick,
		elevation = ButtonDefaults.elevation(
			defaultElevation = 3.dp
		),
		colors = ButtonDefaults.buttonColors(
			backgroundColor = DefaultTripleBarBackgroundCC,
			contentColor = MainTextCC,
		)
	) {
		Text(
			text = "Войти",
			fontSize = 17.sp,
			fontWeight = FontWeight.Normal,
		)
	}
}


@Composable
fun LoginTextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	readOnly: Boolean = false,
	textStyle: TextStyle = LocalTextStyle.current,
	label: @Composable (() -> Unit)? = null,
	placeholder: @Composable (() -> Unit)? = null,
	leadingIcon: @Composable (() -> Unit)? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	isError: Boolean = false,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default,
	singleLine: Boolean = true,
	maxLines: Int = Int.MAX_VALUE,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	shape: Shape = MaterialTheme.shapes.small,
	colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		modifier = modifier.width(250.dp),
		singleLine = singleLine,
		label = label,
		keyboardOptions = keyboardOptions,
		keyboardActions = keyboardActions,
		visualTransformation = visualTransformation,
		colors = TextFieldDefaults.outlinedTextFieldColors(
			cursorColor = MainBrightCC,
			textColor = MainTextCC,
			focusedBorderColor = MainBrightCC,
			unfocusedBorderColor = ProfileDimCC,
			focusedLabelColor = MainBrightCC,
			unfocusedLabelColor = MainTextCC,

		)
	)
}