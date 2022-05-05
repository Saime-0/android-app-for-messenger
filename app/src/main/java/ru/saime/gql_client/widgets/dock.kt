package ru.saime.gql_client.widgets

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.DockButtonCC
import ru.saime.gql_client.DockButtonTextCC
import ru.saime.gql_client.DockCC
import ru.saime.gql_client.DockHeight
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.navigation.singleNavigate

class CategoryNavigate(
	val backend: Backend,
	val scaffoldState: ScaffoldState,
	val scope: CoroutineScope
) {
	@Composable
	fun DockCategory(
		text: String,
		route: String,
		modifier: Modifier = Modifier
	) {
		Button(
			onClick = {
				backend.mainNavController.navigate(route)
				scope.launch { scaffoldState.drawerState.close() }
			},
			modifier = modifier,
			colors = ButtonDefaults.buttonColors(
				backgroundColor = DockButtonCC,
				contentColor = DockButtonTextCC
			)
		) {
			Text(text = text, fontWeight = FontWeight.Normal)
		}
	}
}


@Composable
fun DockSpacer() {
	Box(modifier = Modifier.height(DockHeight))
}