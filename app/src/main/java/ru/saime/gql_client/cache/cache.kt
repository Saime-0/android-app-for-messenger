package ru.saime.gql_client.cache

import java.util.*

enum class LoadedDataType {
	Me,
	Tags,

}

object Cache {

	val LoadedData: MutableMap<LoadedDataType, Unit> = EnumMap<LoadedDataType, Unit>(LoadedDataType::class.java)

	object Data {
		val rooms: MutableMap<Int, Room> = hashMapOf()
		val messages: MutableMap<Int, Message> = hashMapOf()
		val employees: MutableMap<Int, Employee> = hashMapOf()
		val tags: MutableMap<Int, Tag> = hashMapOf()
	}
}

enum class RoomType {
	BLOG,
	TALK
}

data class Room(
	val roomID: Int,
	val name: String,
	val view: RoomType,
	val lastMsgRead: Int,
)

data class Employee(
	val empID: Int,
	val firstName: String,
	val lastName: String,
	val joinedAt: Int,
	val tagIDs: List<Int> = emptyList(),
)

data class Message(
	val msgID: Int,
	val roomID: Int,
	val empID: Int,
	val targetID: Int,
	val body: String,
	val createdAt: Int,
)

data class Tag (
	val tagID: Int,
	val name: String,
)
