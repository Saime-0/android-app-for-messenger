package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ru.saime.gql_client.DividerDarkCC
import ru.saime.gql_client.utils.KeyBoardManager

@Composable
fun AppKeyboardFocusManager() {
	val context = LocalContext.current
	val focusManager = LocalFocusManager.current
	DisposableEffect(key1 = context) {
		val keyboardManager = KeyBoardManager(context)
		keyboardManager.attachKeyboardDismissListener {
			focusManager.clearFocus()
		}
		onDispose {
			keyboardManager.release()
		}
	}
}