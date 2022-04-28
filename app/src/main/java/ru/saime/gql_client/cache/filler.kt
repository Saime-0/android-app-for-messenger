package ru.saime.gql_client.cache

import pkg.EmployeeQuery
import pkg.MeRoomsListQuery
import pkg.ProfileQuery
import pkg.fragment.MessagesForRoom
import ru.saime.gql_client.cache.Cache.Me.ID
import ru.saime.gql_client.cache.Cache.Me.email
import ru.saime.gql_client.cache.Cache.Me.phone

fun Cache.fillMe(data: ProfileQuery.OnMe) {
	Cache.fillEmployee(data.employee)

	Cache.Me.let {
		ID = data.employee.empID
		email = data.personal.email
		phone = data.personal.phoneNumber
	}

}

fun Cache.fillTag(data: ProfileQuery.Tag) {
	Cache.Data.tags[data.tagID] = Tag(
		tagID = data.tagID,
		name = data.name
	)
}

fun Cache.fillEmployee(data: ProfileQuery.Employee) {
	Cache.Data.employees[data.empID] = Employee(
		empID = data.empID,
		firstName = data.firstName,
		lastName = data.lastName,
	)
	for (tag in data.tags.tags) Cache.fillTag(tag)
}

fun Cache.fillRooms(data: MeRoomsListQuery.Rooms) { // todo
	for (room in data.rooms) {
		Cache.Data.rooms[room.roomID] = Room(
			roomID = room.roomID,
			name = room.name,
			view = room.view,
			lastMsgID = room.lastMessageID,
			lastMsgRead = room.lastMessageRead,
		)
	}

}


fun Cache.fillEmployee(data: EmployeeQuery.Employee) {
	Cache.Data.employees[data.empID] = Employee(
		empID = data.empID,
		firstName = data.firstName,
		lastName = data.lastName,
	)
	for (tag in data.tags.tags) Cache.fillTag(tag)
}

fun Cache.fillTag(data: EmployeeQuery.Tag) {
	Cache.Data.tags[data.tagID] = Tag(
		tagID = data.tagID,
		name = data.name
	)
}

fun Cache.fillRoomMessages(messages: MessagesForRoom) {
	for (msg in messages.messages) {
		if (Cache.Data.rooms[msg.room.roomID] != null)
			Cache.Data.rooms[msg.room.roomID]!!.messages[msg.msgID] = Message(
				roomID = msg.room.roomID, // todo: order if not exists
				msgID = msg.msgID,
				empID = msg.employee.empID, // todo: order if not exists
				targetID = msg.targetMsg?.msgID,
				body = msg.body,
				createdAt = msg.createdAt,
			)
	}
}