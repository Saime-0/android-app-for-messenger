package ru.saime.gql_client.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.orderEmployeeProfile
import ru.saime.gql_client.backend.orderMe
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.utils.ScreenStatus
import ru.saime.gql_client.utils.equal
import ru.saime.gql_client.utils.set
import ru.saime.gql_client.widgets.DockSpacer
import ru.saime.gql_client.widgets.EmptyScreen
import ru.saime.gql_client.widgets.ScreenHorizontalPadding


@Composable
fun Profile(backend: Backend, empID: Int) {
	val screenStatus = remember {
		mutableStateOf(ScreenStatus.NONE)
	}
	var errMsg by remember { mutableStateOf("") }

	SideEffect {
		if (empID != 0 && Cache.Data.employees[empID] == null) {
	println("надо сделать запрос потому то employees[empID] = ${Cache.Data.employees[empID] == null} а stratus = ${screenStatus.value}")
			MainScope().launch {
				screenStatus.set(ScreenStatus.LOADING)
				backend.orderEmployeeProfile(empID = empID).let { err ->
					if (err != null) {
						errMsg = err
						screenStatus.set(ScreenStatus.ERROR)
					} else screenStatus.set(ScreenStatus.OK)
				}

			}
		} else
			screenStatus.set(ScreenStatus.OK)

	}

	when (screenStatus.value) {
		ScreenStatus.LOADING -> Loading(Modifier.fillMaxSize())
		ScreenStatus.ERROR -> ErrorComponent(msg = errMsg, modifier = Modifier.fillMaxSize())
		ScreenStatus.OK -> ShowProfileV2(empID, backend)
		else -> {
			EmptyScreen(true)
		}
	}

}

@Composable
fun ShowProfileV2(
	empID: Int,
	backend: Backend
) {
	val scrollStateProfile = rememberScrollState()
	val scrollStateTags = rememberScrollState()
	Cache.Data.employees[if (empID == Cache.Me.ID) Cache.Me.ID else empID]?.let { emp ->
		Scaffold(
			topBar = {
				TopAppBar(
					navigationIcon = {
						IconButton(onClick = {
							backend.mainNavController.popBackStack()
						}) {
							Icon(Icons.Filled.ArrowBack, null, tint = MainTextCC)
						}
					},
					contentColor = MainTextCC,
					backgroundColor = DefaultTripleBarBackgroundCC,
					title = { Text("Сотрудник № ${emp.empID}")})
			},
			backgroundColor = BackgroundCC
		) {

				Column(
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.verticalScroll(scrollStateProfile),
					verticalArrangement = Arrangement.spacedBy(23.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Box(Modifier.padding(5.dp)) // top padding

					Image(
						painter = painterResource(id = R.drawable.avatar),
						contentDescription = "",
						modifier = Modifier
//						.padding(36.dp)
							.clip(RoundedCornerShape(10.dp))
							.size(200.dp)
					)
					Column( // описание
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.spacedBy(4.dp)
					) {
//						Box(modifier = Modifier.height(5.dp))
						TextLargeProfile("${emp.firstName} ${emp.lastName}")
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(4.dp)
						) {
							TextOnlineStatusProfile(
								if (true) "онлайн?" else "был в сети в 23:32",
								color = OnlineIndicatorCC
							)
						}
					}



					ProfileSection(
						header = { TextSectionHeaderProfile("Контакты:") },
					) {
						TextValuesProfile(emp.email)
						TextValuesProfile(emp.phone)
					}

					ProfileSection(
						modifier = Modifier
							.heightIn(0.dp, 400.dp)
							.verticalScroll(scrollStateTags),
						header = { TextSectionHeaderProfile("Должности:") }
					) {
						for (tagID in emp.tagIDs) {
							TextValuesProfile(Cache.Data.tags[tagID]?.name.toString())
						}
					}

					if (empID == Cache.Me.ID)
						IconButton(
							modifier = Modifier.size(66.dp),
							onClick = {
								backend.logout()
							}
						) {
							Icon(Icons.Filled.ExitToApp, null, tint = Color.Red)
						}

					Box(Modifier.padding(5.dp)) // bottom padding
				}
		}
	}
}

@Composable
fun ProfileSection(
	modifier: Modifier = Modifier,
	header: @Composable () -> Unit = {},
	content: @Composable () -> Unit = {}
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(20.dp),
		backgroundColor = ProfileSectionBackgroundCC,
		elevation = 2.dp
	) {
		Column(
			modifier
				.fillMaxWidth()
				.padding(horizontal = 15.dp, vertical = 13.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			header.invoke()
			Column(
				modifier = modifier.padding(start = 8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				content()
			}
		}
	}
}


@Composable
fun TextLargeProfile(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = MainTextCC,
	fontSize: TextUnit = 22.sp
) {
	Text(
		text = text,
		modifier = modifier,
		color = color,
		fontFamily = FontFamily.SansSerif,
		fontSize = fontSize
	)
}

@Composable
fun TextOnlineStatusProfile(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = ProfileDimCC,
	fontSize: TextUnit = 18.sp,
	fontWeight: FontWeight = FontWeight.Light,
	fontFamily: FontFamily = FontFamily.SansSerif,
) {
	Text(
		text = text,
		modifier = modifier,
		color = color,
		fontWeight = fontWeight,
		fontFamily = fontFamily,
		fontSize = fontSize
	)
}

@Composable
fun TextSectionHeaderProfile(
	text: String,
	color: Color = ProfileSelectionHeaderCC,
	fontSize: TextUnit = 16.sp
) {
	Text(
		text = text,
		color = color,
		fontFamily = FontFamily.SansSerif,
		fontSize = fontSize
	)
}

@Composable
fun TextValuesProfile(
	text: String,
	color: Color = MainTextCC,
	fontSize: TextUnit = 18.sp,
	fontWeight: FontWeight = FontWeight.Light,
	fontFamily: FontFamily = FontFamily.SansSerif,
) {
	Text(
		text = text,
		color = color,
		fontWeight = fontWeight,
		fontFamily = fontFamily,
		fontSize = fontSize
	)
}