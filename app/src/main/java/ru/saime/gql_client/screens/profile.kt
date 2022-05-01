package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.saime.gql_client.*
import ru.saime.gql_client.R
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.widgets.DividerV2CC
import ru.saime.gql_client.widgets.DockSpacer
import ru.saime.gql_client.widgets.EmptyScreen
import java.util.*


@Composable
fun Profile(view: View, empID: Int, modifier: Modifier = Modifier) {
	val isLoading = remember {
		mutableStateOf(false)
	}
	val isError = remember {
		mutableStateOf(Pair(false, ""))
	}
	val isOk = remember {
		mutableStateOf(false)
	}

	Loading(isDisplayed = isLoading.value, modifier = Modifier.fillMaxSize())
	ErrorComponent(
		isDisplayed = isError.value.first,
		msg = isError.value.second,
		modifier = Modifier.fillMaxSize()
	)
	ShowProfileV2(isDisplayed = isOk.value, empID = empID)
	Box {
//		println(!(isError.value.first || isOk.value))
		if (!(isError.value.first || isOk.value || isLoading.value))
			CoroutineScope(Dispatchers.Main).launch {
				isLoading.value = true
				if (empID == 0)
					view.orderMe { err ->
						if (err != null) isError.value = Pair(true, err) else isOk.value = true
					}
				else
					view.orderEmployeeProfile(empID = empID) { err ->
						if (err != null) isError.value = Pair(true, err) else isOk.value = true
					}
				isLoading.value = false
			}

	}

}

@Composable
fun ShowProfileV2(
	isDisplayed: Boolean,
	empID: Int,
	modifier: Modifier = Modifier
) {
	val scrollStateProfile = rememberScrollState()
	val scrollStateTags = rememberLazyListState()
	if (isDisplayed)
		Cache.Data.employees[if (empID == 0) Cache.Me.ID else empID]?.let {

			Column(
				modifier = modifier
					.padding(top = 20.dp)
					.verticalScroll(scrollStateProfile),
				verticalArrangement = Arrangement.spacedBy(28.dp),
				horizontalAlignment = Alignment.Start
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
						.heightIn(0.dp, 400.dp),
					header = { TextSmallProfile("Должности:", color = ProfileSelectionHeaderCC) }
				) {
					for (tagID in it.tagIDs) {
						TextValuesProfile(Cache.Data.tags[tagID]?.name.toString())
					}
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
		elevation = 3.dp
	) {
		Column(
			modifier
//				.fillMaxSize()
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