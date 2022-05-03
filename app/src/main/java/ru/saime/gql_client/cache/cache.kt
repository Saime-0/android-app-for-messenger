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
		val rooms: MutableMap<Int, Room> = hashMapOf()
		val employees: MutableMap<Int, Employee> = hashMapOf()
		val tags: MutableMap<Int, Tag> = hashMapOf()
	}
}

data class OrderPair(
	val messageID: Int,
	val employeeID: Int
)

data class Room(
	val roomID: Int,
	val name: String,
	val view: RoomType,
	var lastMsgID: Int,
	val lastMsgRead: Int,
) {
//	val order = mutableStateListOf<Int>()
	val orderPaired = mutableStateListOf<OrderPair>()
	val messages = mutableMapOf<Int, Message>()
	val currentInputMessageText = mutableStateOf("")

//	val messages = mutableStateMapOf<Int, Message>()
//	val messages: SortedMap<Int, Message> = sortedMapOf(comparator = compareByDescending { it })
//	val messages: MutableState<SortedMap<Int, Message>> = mutableStateOf(sortedMapOf<Int, Message>(comparator = compareByDescending { it }))
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
	val empID: Int,
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
