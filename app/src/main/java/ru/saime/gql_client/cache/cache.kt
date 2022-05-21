package ru.saime.gql_client.cache

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import pkg.type.RoomType
import java.util.*
import kotlin.reflect.KProperty

enum class LoadedDataType {
	Me,
	Tags,
	RoomList,
}

object Cache {

	val LoadedData: MutableMap<LoadedDataType, Unit> =
		EnumMap(LoadedDataType::class.java)

	object Me {
		var ID: Int = 0
		var NotificationsEnable by mutableStateOf(false)
	}

	object Orders {
		var roomOrder by mutableStateOf(listOf<Int>())
	}

	object Data {
		val rooms = mutableMapOf<Int, Room>()
		val employees = mutableMapOf<Int, Employee>()
		val tags = mutableMapOf<Int, Tag>()
		val messages = mutableMapOf<Int, Message>()

	}
}


data class LazyMessage(
	val messageID: Int,
	val employeeID: Int?,
	val alignment: Alignment,
	val backgroundColor: Color,
	var displayingData: Boolean,
	var displayingName: Boolean,
	var addTopPadding: Boolean,

	)

data class MarkedPair(
	val messageID: MutableState<Int?>,
	var indexInColumn: Int
) {
	fun clear() {
		messageID.value = null
		indexInColumn = 0
	}

	fun set(msgID: Int, columnIndex: Int) {
		indexInColumn = columnIndex
		messageID.value = msgID
	}
}

data class Room(
	var pos: Int,
	val roomID: Int,
	val name: String,
	val photoUrl: String,
	val photo: MutableState<ImageBitmap?>,
	val view: RoomType,
	var lastMsgID: MutableState<Int?>,
	val lastMsgRead: MutableState<Int?>,
	val notify: MutableState<Boolean>,
) {
	//	val messagesOrder = mutableListOf<OrderPair>()
//	var messagesLazyOrder by mutableStateOf(emptyList<LazyMessage>())
//	var messagesLazyOrder by mutableStateOf(mapOf<Int, LazyMessage>())
	var messagesLazyOrder = mutableStateListOf<LazyMessage>()
//	val orderCopy = mutableListOf<LazyMessage>()
	//	States
	val currentInputMessageText = mutableStateOf("")
	val lazyListState = LazyListState()
	var markedMessage = MarkedPair(mutableStateOf(null), 0)
	val displayingGoDown = mutableStateOf(false)
}

data class Employee(
	val empID: Int,
	val firstName: String,
	val lastName: String,
	val photoUrl: String,
	val photo: MutableState<ImageBitmap?>,
	val email: String,
	val phone: String,
	val tagIDs: MutableList<Int> = mutableListOf(),
) {
	operator fun getValue(nothing: Nothing?, property: KProperty<*>): Employee = this
}

data class Message(
	val msgID: Int,
	val roomID: Int,
	val empID: Int?,
	val targetID: Int?,
	val body: String,
	val createdAt: Long,
	var prev: Int?,
	var next: Int?,
) {
	operator fun getValue(nothing: Nothing?, property: KProperty<*>): Message = this
}

data class Tag(
	val tagID: Int,
	val name: String,
)
