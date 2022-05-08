package ru.saime.gql_client.cache

import android.provider.ContactsContract
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
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
	}

	object Data {
		val rooms = mutableStateMapOf<Int, Room>()
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
	val view: RoomType,
	var lastMsgID: Int?,
	val lastMsgRead: MutableState<Int?>,
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
	val email: String,
	val phone: String,
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
