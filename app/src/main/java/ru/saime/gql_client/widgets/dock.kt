package ru.saime.gql_client.widgets

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.saime.gql_client.DockButtonCC
import ru.saime.gql_client.DockButtonTextCC
import ru.saime.gql_client.backend.Backend

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