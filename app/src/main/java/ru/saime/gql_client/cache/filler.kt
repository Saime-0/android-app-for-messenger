package ru.saime.gql_client.cache

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
		if (Cache.Data.rooms[msg.room.roomID] != null) {
			Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID] = Message(
				roomID = msg.room.roomID, // todo: order if not exists... why?
				msgID = msg.msgID,
				empID = msg.employee.empID,
				targetID = msg.targetMsg?.msgID,
				body = msg.body,
				createdAt = msg.createdAt * 1000L,
				prev = msg.prev,
				next = msg.next,
			)
			if (!Cache.Data.rooms[msg.room.roomID]!!.orderPaired.contains(OrderPair(msg.msgID, msg.employee.empID))) {
				Cache.Data.rooms[msg.room.roomID]!!.orderPaired.let { list ->
					list.add(OrderPair(msg.msgID, msg.employee.empID))
					list.sortByDescending { it.messageID }
				}
			}

			Cache.orderTargetMessageIfNotExists(backend, Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]!!)
			Cache.orderEmployeeMessageIfNotExists(backend, Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]!!)

		}
	}
}

suspend fun Cache.orderTargetMessageIfNotExists(backend: Backend, msg: Message) {
	if (
		msg.targetID != null
		&& Cache.Data.rooms[msg.roomID]!!.messages[msg.targetID] == null
	) {
		backend.orderRoomMessage(roomID = msg.roomID, msgID = msg.targetID)
	}
}

suspend fun Cache.orderEmployeeMessageIfNotExists(backend: Backend, msg: Message) {
	if (Cache.Data.employees[msg.empID] == null)
		backend.orderEmployeeProfile(msg.empID)
}

suspend fun Cache.fillOnNewMessage(backend: Backend, msg: SubscribeSubscription.OnNewMessage) {
	Cache.Data.rooms[msg.roomID]!!.messages[msg.msgID] = Message(
		roomID = msg.roomID, // todo: order if not exists... why?
		msgID = msg.msgID,
		empID = msg.empID,
		targetID = msg.targetMsgID,
		body = msg.body,
		createdAt = msg.createdAt * 1000L,
		prev = msg.prev,
		next = null,
	)

	if (!Cache.Data.rooms[msg.roomID]!!.orderPaired.contains(OrderPair(msg.msgID, msg.empID))) {
		Cache.Data.rooms[msg.roomID]!!.orderPaired.let { list ->
			list.add(OrderPair(msg.msgID, msg.empID))
			list.sortByDescending { it.messageID }
		}
	}

	Cache.orderTargetMessageIfNotExists(backend, Cache.Data.rooms[msg.roomID]!!.messages[msg.msgID]!!)
	Cache.orderEmployeeMessageIfNotExists(backend, Cache.Data.rooms[msg.roomID]!!.messages[msg.msgID]!!)
}