package ru.saime.gql_client.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.saime.gql_client.DividerDarkCC
/**
 * Static field, contains all scroll values
 */
private val SaveMap = mutableMapOf<String, KeyParams>()

private data class KeyParams(
	val params: String = "",
	val index: Int,
	val scrollOffset: Int
)

/**
 * Save scroll state on all time.
 * @param key value for comparing screen
 * @param params arguments for find different between equals screen
 * @param initialFirstVisibleItemIndex see [LazyListState.firstVisibleItemIndex]
 * @param initialFirstVisibleItemScrollOffset see [LazyListState.firstVisibleItemScrollOffset]
 */
@Composable
fun rememberForeverLazyListState(
	key: String,
	params: String = "",
	initialFirstVisibleItemIndex: Int = 0,
	initialFirstVisibleItemScrollOffset: Int = 0
): LazyListState {
	val scrollState = rememberSaveable(saver = LazyListState.Saver) {
		var savedValue = SaveMap[key]
		if (savedValue?.params != params) savedValue = null
		val savedIndex = savedValue?.index ?: initialFirstVisibleItemIndex
		val savedOffset = savedValue?.scrollOffset ?: initialFirstVisibleItemScrollOffset
		LazyListState(
			savedIndex,
			savedOffset
		)
	}
	DisposableEffect(Unit) {
		onDispose {
			val lastIndex = scrollState.firstVisibleItemIndex
			val lastOffset = scrollState.firstVisibleItemScrollOffset
			SaveMap[key] = KeyParams(params, lastIndex, lastOffset)
		}
	}
	return scrollState
}