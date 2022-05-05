package ru.saime.gql_client.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pkg.ProfileQuery
import pkg.SubscribeSubscription
import pkg.fragment.*
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.orderEmployeeProfile
import ru.saime.gql_client.backend.orderRoomMessage
import ru.saime.gql_client.cache.Cache.Me.ID
import ru.saime.gql_client.cache.Cache.Me.email
import ru.saime.gql_client.cache.Cache.Me.phone

fun Cache.fillMe(data: ProfileQuery.OnMe) {
	Cache.fillEmployee(data.employee.fullEmployee)

	Cache.Me.let {
		ID = data.employee.fullEmployee.empID
		email = data.personal.email
		phone = data.personal.phoneNumber
	}

}

fun Cache.fillEmployee(data: FullEmployee) {
	Cache.Data.employees[data.empID] = Employee(
		empID = data.empID,
		firstName = data.firstName,
		lastName = data.lastName,
	)
	for (tag in data.tags.fullTags.tags) {
		Cache.fillTag(tag.fullTag)
		Cache.Data.employees[data.empID]!!.tagIDs.add(tag.fullTag.tagID)
	}
}

fun Cache.fillRooms(data: RoomsWithoutMembers) { // todo
	for (room in data.rooms) {
		Cache.Data.rooms[room.roomWithoutMembers.roomID] = Room(
			roomID = room.roomWithoutMembers.roomID,
			name = room.roomWithoutMembers.name,
			view = room.roomWithoutMembers.view,
			lastMsgID = room.roomWithoutMembers.lastMessageID,
			lastMsgRead = room.roomWithoutMembers.lastMessageRead,
		)
	}

}

fun Cache.fillTag(data: FullTag) {
	Cache.Data.tags[data.tagID] = Tag(
		tagID = data.tagID,
		name = data.name
	)
}

suspend fun Cache.fillRoomMessages(backend: Backend, messages: MessagesForRoom) {
	for (msg in messages.messages) {
		Cache.Data.rooms[msg.room.roomID]?.let { room ->

			Cache.Data.messages[msg.msgID] = Message(
				roomID = msg.room.roomID, // todo: order if not exists... why?
				msgID = msg.msgID,
				empID = msg.employee?.empID,
				targetID = msg.targetMsg?.msgID,
				body = msg.body,
				createdAt = msg.createdAt * 1000L,
				prev = msg.prev,
				next = msg.next,
			)

			// сначала подгружаю недостающие данные, тк клиент после пополения списка не перерисует экран если бы я подгружал автора позже
			Cache.orderTargetMessageIfNotExists(backend, msg.targetMsg?.msgID)
			Cache.orderEmployeeMessageIfNotExists(backend, msg.employee?.empID)

			if (!room.messagesOrder.contains(OrderPair(msg.msgID, msg.employee?.empID))) {
				room.messagesOrder.let { list ->
					list.add(OrderPair(msg.msgID, msg.employee?.empID))
					list.sortByDescending { it.messageID }
				}
			}


		}
	}
}

suspend fun Cache.orderTargetMessageIfNotExists(backend: Backend, targetMsgID: Int?) {
	if (targetMsgID != null && Cache.Data.messages[targetMsgID] == null) {
		backend.orderRoomMessage(msgID = targetMsgID)
	}
}

suspend fun Cache.orderEmployeeMessageIfNotExists(backend: Backend, empID: Int?) {
	if (empID != null && Cache.Data.employees[empID] == null) {
		backend.orderEmployeeProfile(empID)
	}
}

suspend fun Cache.fillOnNewMessage(backend: Backend, msg: SubscribeSubscription.OnNewMessage) {
	Cache.Data.messages[msg.msgID] = Message(
		roomID = msg.roomID, // todo: order if not exists... why?
		msgID = msg.msgID,
		empID = msg.employeeID,
		targetID = msg.targetMsgID,
		body = msg.body,
		createdAt = msg.createdAt * 1000L,
		prev = msg.prev,
		next = null,
	)

	// сначала подгружаю недостающие данные, тк клиент после пополения списка не перерисует экран если бы я подгружал автора позже
		println("1111 - первй пук")
		Cache.orderTargetMessageIfNotExists(backend, msg.targetMsgID)
		Cache.orderEmployeeMessageIfNotExists(backend, msg.employeeID)
		println("222 - второй пук")


	if (!Cache.Data.rooms[msg.roomID]!!.messagesOrder.contains(OrderPair(msg.msgID, msg.employeeID))) {
		Cache.Data.rooms[msg.roomID]!!.messagesOrder.let { list ->
			list.add(OrderPair(msg.msgID, msg.employeeID))
			list.sortByDescending { it.messageID }
		}
	}

}