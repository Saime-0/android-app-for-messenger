package ru.saime.gql_client.cache

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import pkg.type.RoomType
import java.lang.Exception
import java.util.*

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
		var email: String = ""
		var phone: String = ""
	}

	object Data {
		val rooms = mutableStateMapOf<Int, Room>()
		val employees = mutableMapOf<Int, Employee>()
		val tags = mutableMapOf<Int, Tag>()
		val messages = mutableMapOf<Int, Message>()

	}
}

data class OrderPair(
	val messageID: Int,
	val employeeID: Int?
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
	val view: RoomType,
	var lastMsgID: Int?,
	val lastMsgRead: MutableState<Int?>,
) {
	val messagesOrder = mutableStateListOf<OrderPair>()

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
	val tagIDs: MutableList<Int> = mutableListOf(),
)

data class Message(
	val msgID: Int,
	val roomID: Int,
	val empID: Int?,
	val targetID: Int?,
	val body: String,
	val createdAt: Long,
	var prev: Int?,
	var next: Int?,
)

data class Tag(
	val tagID: Int,
	val name: String,
)
