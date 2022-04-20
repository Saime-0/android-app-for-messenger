package ru.saime.gql_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import ru.saime.gql_client.widgets.DividerCC
import ru.saime.gql_client.widgets.DockSpacer
import java.util.*


@Composable
fun LoadProfile(view: View, empID: Int, modifier: Modifier = Modifier) {
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
	ShowProfile(isDisplayed = isOk.value, empID = empID)
	Box {
		println(!(isError.value.first || isOk.value))
		if (!(isError.value.first || isOk.value))
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
fun ShowProfile(
	isDisplayed: Boolean,
	empID: Int,
	modifier: Modifier = Modifier
) {
	val scrollState = rememberScrollState()
	if (isDisplayed)
		Column(
			modifier = modifier
				.fillMaxSize()
				.background(BackgroundCC)
				.padding(14.dp)
				.verticalScroll(scrollState),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Image(
				painter = painterResource(id = R.drawable.avatar),
				contentDescription = "",
				modifier = Modifier
					.padding(36.dp)
					.clip(RoundedCornerShape(30.dp))
			)
			Cache.Data.employees[if (empID == 0) Cache.Me.ID else empID]?.let {
				TextLargeProfile("${it.firstName} ${it.lastName}")
				TextMediumProfile("сотрудник № ${it.empID}", color = ProfileDimCC)

				DividerCC(modifier = Modifier.padding(18.dp)) //


				if (empID == 0)
				Column(
					verticalArrangement = Arrangement.spacedBy(20.dp),
					horizontalAlignment = Alignment.Start
				) {
					TextSmallProfile("Контакты:", color = ProfileSelectionHeaderCC)
					TextValuesProfile(Date(it.joinedAt.toLong()).toString())
					TextValuesProfile(text = Cache.Me.email)
					TextValuesProfile(text = Cache.Me.phone)
				}
			}
			DockSpacer()
		}
}

@Composable
fun TextLargeProfile(
	text: String,
	color: Color = MainTextCC,
	fontSize: TextUnit = 30.sp
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
	color: Color = ProfileYellowCC,
	fontSize: TextUnit = 18.sp
) {
	Text(
		text = text,
		color = color,
		fontWeight = FontWeight.Medium,
		fontFamily = FontFamily.SansSerif,
		fontSize = fontSize
	)
}