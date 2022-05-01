package ru.saime.gql_client.cache

import pkg.EmployeeQuery
import pkg.MeRoomsListQuery
import pkg.ProfileQuery
import pkg.fragment.*
import ru.saime.gql_client.View
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

suspend fun Cache.fillRoomMessages(view: View, messages: MessagesForRoom) {
	for (msg in messages.messages) {
		if (Cache.Data.rooms[msg.room.roomID] != null) {
			Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID] = Message(
				roomID = msg.room.roomID, // todo: order if not exists... why?
				msgID = msg.msgID,
				empID = msg.employee.empID,
				targetID = msg.targetMsg?.msgID,
				body = msg.body,
				createdAt = msg.createdAt,
				prev = msg.prev,
				next = msg.next,
			)
			if (!Cache.Data.rooms[msg.room.roomID]!!.order.contains(msg.msgID)) {
				Cache.Data.rooms[msg.room.roomID]!!.order.let { list ->
					list.add(msg.msgID)
					list.sortByDescending { it }
				}
			}
//			Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]
			if (
				Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]!!.targetID != null
				&& Cache.Data.rooms[msg.room.roomID]!!.messages[
						Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]!!.targetID!!
				] == null
			) {
				view.orderRoomMessage(roomID = msg.room.roomID, msgID = Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]!!.targetID!!)
			}
			if (
				Cache.Data.employees[
						Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]!!.empID
				] == null
			) {
				view.orderEmployeeProfile(Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID]!!.empID)
			}
		}
	}
}