package ru.saime.gql_client.cache

import pkg.EmployeeQuery
import pkg.MeRoomsListQuery
import pkg.ProfileQuery
import ru.saime.gql_client.cache.Cache.Me.ID
import ru.saime.gql_client.cache.Cache.Me.email
import ru.saime.gql_client.cache.Cache.Me.phone

fun Cache.fillMe(data: ProfileQuery.OnMe) {
	Cache.fillEmployee(data.employee)

	if (data.employee.tags.tags != null)
		for (tag in data.employee.tags.tags) Cache.fillTag(tag)

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
		joinedAt = data.joinedAt,
	)
}

fun Cache.fillRooms(data: MeRoomsListQuery.Rooms) { // todo
	if (data.rooms != null)
		for (room in data.rooms) {
			Cache.Data.rooms[room.roomID] = Room(
				roomID = room.roomID,
				name = room.name,
				view = room.view,
				lastMsgID = room.lastMessageID,

			)
		}

}


fun Cache.fillEmployee(data: EmployeeQuery.Employee) {
	Cache.Data.employees[data.empID] = Employee(
		empID = data.empID,
		firstName = data.firstName,
		lastName = data.lastName,
		joinedAt = data.joinedAt,
	)
	if (data.tags.tags != null)
		for (tag in data.tags.tags) Cache.fillTag(tag)
}

fun Cache.fillTag(data: EmployeeQuery.Tag) {
	Cache.Data.tags[data.tagID] = Tag(
		tagID = data.tagID,
		name = data.name
	)
}

