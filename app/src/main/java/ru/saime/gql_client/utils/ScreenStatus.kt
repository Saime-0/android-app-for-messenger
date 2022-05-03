package ru.saime.gql_client.utils

import androidx.compose.runtime.MutableState

enum class ScreenStatus {
	LOADING,
	ERROR,
	OK,
	NONE,

	EMPTY,
}

//fun ScreenStatus.equal(status: ScreenStatus): Boolean = this == status
fun MutableState<ScreenStatus>.equal(status: ScreenStatus): Boolean = this.value == status
fun MutableState<ScreenStatus>.set(status: ScreenStatus) {
	this.value = status
}
