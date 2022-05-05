package ru.saime.gql_client.cache

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import pkg.type.RoomType
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
		val rooms = mutableMapOf<Int, Room>()
		val employees = mutableMapOf<Int, Employee>()
		val tags = mutableMapOf<Int, Tag>()
		val messages = mutableMapOf<Int, Message>()
	}
}

data class OrderPair(
	val messageID: Int,
	val employeeID: Int?
)

data class Room(
	val roomID: Int,
	val name: String,
	val view: RoomType,
	var lastMsgID: Int?,
	val lastMsgRead: Int?,
) {
	val messagesOrder = mutableStateListOf<OrderPair>()
	val currentInputMessageText = mutableStateOf("")
	var markedMessage: MutableState<Int?> = mutableStateOf(null)
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
