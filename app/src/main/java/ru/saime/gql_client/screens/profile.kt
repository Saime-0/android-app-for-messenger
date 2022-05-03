package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
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
import ru.saime.gql_client.widgets.DividerV2CC
import ru.saime.gql_client.widgets.DockSpacer
import ru.saime.gql_client.widgets.EmptyScreen
import java.util.*


@Composable
fun Profile(backend: Backend, empID: Int, modifier: Modifier = Modifier) {
	val screenStatus = rememberSaveable {
		mutableStateOf(ScreenStatus.NONE)
	}
	var errMsg: String = remember { "" }

	SideEffect {
		if (screenStatus.equal(ScreenStatus.NONE))
			MainScope().launch {
				screenStatus.set(ScreenStatus.LOADING)
				run {
					if (empID == 0) backend.orderMe()
					else backend.orderEmployeeProfile(empID = empID)
				}.let { err ->
					if (err != null) {
						errMsg = err
						screenStatus.set(ScreenStatus.ERROR)
					} else screenStatus.set(ScreenStatus.OK)
				}
			}

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
	Cache.Data.employees[if (empID == 0) Cache.Me.ID else empID]?.let {

		Column(
			modifier = Modifier
				.padding(top = 20.dp)
				.verticalScroll(scrollStateProfile),
			verticalArrangement = Arrangement.spacedBy(28.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Row(
				verticalAlignment = Alignment.Bottom
			) {
				Image(
					painter = painterResource(id = R.drawable.avatar),
					contentDescription = "",
					modifier = Modifier
//						.padding(36.dp)
						.size(100.dp)
						.clip(RoundedCornerShape(10.dp))
				)

				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					TextLargeProfile("${it.firstName} ${it.lastName}")
					TextMediumProfile("сотрудник № ${it.empID}", color = ProfileDimCC)

					DividerV2CC(Modifier.padding(top = 18.dp, start = 20.dp, end = 20.dp))
				}

			}


			if (empID == 0)
				ProfileSection(
					header = {
						TextSmallProfile(
							"Контакты:",
							color = ProfileSelectionHeaderCC
						)
					},
				) {
					TextValuesProfile(Cache.Me.email)
					TextValuesProfile(Cache.Me.phone)
				}

			ProfileSection(
				modifier = Modifier
					.heightIn(0.dp, 400.dp)
					.verticalScroll(scrollStateTags),
				header = { TextSmallProfile("Должности:", color = ProfileSelectionHeaderCC) }
			) {
				for (tagID in it.tagIDs) {
					TextValuesProfile(Cache.Data.tags[tagID]?.name.toString())
				}
			}

			if (empID == 0)
				IconButton(
					modifier = Modifier.size(66.dp),
					onClick = {
						backend.logout()
					}
				) {
					Icon(Icons.Filled.ExitToApp, null, tint = Color.Red)
				}

			DockSpacer()
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
				.padding(20.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			header.invoke()
			Column(
				modifier = modifier.padding(start = 8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				content.invoke()
			}
		}
	}
}


@Composable
fun TextLargeProfile(
	text: String,
	color: Color = MainTextCC,
	fontSize: TextUnit = 27.sp
) {
	Text(
		text = text.uppercase(Locale.getDefault()),
		color = color,
		fontFamily = FontFamily.SansSerif,
		fontSize = fontSize
	)
}

@Composable
fun TextMediumProfile(
	text: String,
	color: Color = MainTextCC,
	fontSize: TextUnit = 21.sp
) {
	Text(
		text = text.uppercase(Locale.getDefault()),
		color = color,
		fontFamily = FontFamily.SansSerif,
		fontSize = fontSize
	)
}

@Composable
fun TextSmallProfile(
	text: String,
	color: Color = MainTextCC,
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
	fontSize: TextUnit = 18.sp
) {
	Text(
		text = text,
		color = color,
		fontWeight = FontWeight.Light,
		fontFamily = FontFamily.SansSerif,
		fontSize = fontSize
	)
}