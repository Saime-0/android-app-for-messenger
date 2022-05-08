package ru.saime.gql_client.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
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
import ru.saime.gql_client.R
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.navigation.singleNavigate

@Composable
fun Avatar(modifier: Modifier = Modifier) {
	Image(
		painter = painterResource(id = R.drawable.avatar),
		contentDescription = null,
		modifier = modifier
	)
}