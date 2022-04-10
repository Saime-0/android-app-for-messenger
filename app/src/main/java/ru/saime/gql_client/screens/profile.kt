package ru.saime.gql_client.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.saime.gql_client.View

@Composable
fun Profile(view: View, empID: Int, modifier: Modifier = Modifier) {
	var isLoading = remember {
		mutableStateOf(false)
	}
	var isError = remember {
		mutableStateOf(Pair(false,""))
	}
	Loading(isDisplayed = isLoading.value, modifier = Modifier.fillMaxSize())
	ErrorComponent(
		isDisplayed = isError.value.first,
		msg = isError.value.second,
		modifier = Modifier.fillMaxSize()
	)

	Column() {
		CoroutineScope(Dispatchers.IO).launch {
			isLoading.value = true
			view.orderMe { result, err ->
				if (err != null) isError.value = Pair(true,err)
			}
			isLoading.value = false
		}
	}

}