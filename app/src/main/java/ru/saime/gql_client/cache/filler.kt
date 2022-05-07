package ru.saime.gql_client.cache

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import pkg.ProfileQuery
import pkg.SubscribeSubscription
import pkg.fragment.*
import pkg.type.EventSubjectAction
import pkg.type.EventType
import pkg.type.RoomType
import ru.saime.gql_client.MarkedMessageBackgroundCC
import ru.saime.gql_client.MessageBackgroundCC
import ru.saime.gql_client.MessageMeBackgroundCC
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.editSubscribeList
import ru.saime.gql_client.backend.orderEmployeeProfile
import ru.saime.gql_client.backend.orderRoomMessage
import java.util.*


fun Cache.fillMe(data: ProfileQuery.OnMe) {
	Cache.fillEmployee(data.employee.fullEmployee)

	Cache.Me.run {
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

suspend fun Cache.fillRooms(backend: Backend, data: RoomsWithoutMembers) { // todo
	println("начинаю заполнять комнаты")
	for (room in data.rooms) {
		Cache.Data.rooms[room.roomWithoutMembers.roomID] = Room(
			pos = room.roomWithoutMembers.pos,
			roomID = room.roomWithoutMembers.roomID,
			name = room.roomWithoutMembers.name,
			view = room.roomWithoutMembers.view,
			lastMsgID = room.roomWithoutMembers.lastMessageID,
			lastMsgRead = mutableStateOf(room.roomWithoutMembers.lastMessageRead),
		)

	}
	backend.editSubscribeList(
		action = EventSubjectAction.ADD,
		listenEvents = listOf(EventType.all),
		targetRooms = data.rooms.map { it.roomWithoutMembers.roomID }
	).let { err ->
		println(
			if (err.isNullOrEmpty()) "editSubscribeList successful += ${data.rooms.map { it.roomWithoutMembers.roomID }}"
			else "editSubscribeList failed with - $err"
		)
	}
//	Cache.roomsResorting()
	println("комнаты заполнены")
}


fun Cache.fillTag(data: FullTag) {
	Cache.Data.tags[data.tagID] = Tag(
		tagID = data.tagID,
		name = data.name
	)
}

suspend fun Cache.fillOnNewMessage(backend: Backend, msg: SubscribeSubscription.OnNewMessage) {
	Message(
		roomID = msg.roomID, // todo: order if not exists... why?
		msgID = msg.msgID,
		empID = msg.employeeID,
		targetID = msg.targetMsgID,
		body = msg.body,
		createdAt = msg.createdAt * 1000L,
		prev = msg.prev,
		next = null,
	).let { msg ->
		Cache.Data.messages[msg.msgID] = msg

		// сначала подгружаю недостающие данные, тк клиент после пополения списка не перерисует экран если бы я подгружал автора позже
		Cache.orderTargetMessageIfNotExists(backend, msg.targetID)
		Cache.orderEmployeeMessageIfNotExists(backend, msg.empID)

		Cache.Data.rooms[msg.roomID]?.let { room ->
			if (!room.messagesLazyOrder.map { it.messageID == msg.msgID }.contains(true))
				room.addLazyMessage(msg)
		}
	}

}

suspend fun Cache.fillRoomMessages(backend: Backend, messages: MessagesForRoom) {
	for (msgForRoom in messages.messages) {
		Cache.Data.rooms[msgForRoom.room.roomID]?.let { room ->

			Message(
				roomID = msgForRoom.room.roomID, // todo: order if not exists... why?
				msgID = msgForRoom.msgID,
				empID = msgForRoom.employee?.empID,
				targetID = msgForRoom.targetMsg?.msgID,
				body = msgForRoom.body,
				createdAt = msgForRoom.createdAt * 1000L,
				prev = msgForRoom.prev,
				next = msgForRoom.next,
			).let { msg ->
				Cache.Data.messages[msg.msgID] = msg

				// сначала подгружаю недостающие данные, тк клиент после пополения списка не перерисует экран если бы я подгружал автора позже
				Cache.orderTargetMessageIfNotExists(backend, msgForRoom.targetMsg?.msgID)
				Cache.orderEmployeeMessageIfNotExists(backend, msgForRoom.employee?.empID)

				if (!room.messagesLazyOrder.map { it.messageID == msgForRoom.msgID }.contains(true))
					room.addLazyMessage(msg)
			}
		}
	}

}

fun Room.addLazyMessage(msg: Message) {
	println("Room.addLazyMessage")
	// Создаю объект
	LazyMessage(
		messageID = msg.msgID,
		employeeID = msg.empID,
		alignment = if (msg.empID == null || msg.empID != Cache.Me.ID) Alignment.CenterStart else Alignment.CenterEnd,
		displayingData = false, // nope
		displayingName = false, // nope
		backgroundColor = Color.White, // nope
		addTopPadding = false, // nope
	).let { newMsg ->
		messagesLazyOrder.add(newMsg) //потом добавляю его в список
		messagesLazyOrder.sortByDescending { it.messageID } // сортирую список
		messagesLazyOrder.map { it.messageID }
			.indexOf(newMsg.messageID)
			.let { index -> // нахожу index объекта в отсортированном списке
				computeLazyMessage(this, messagesLazyOrder, index)
				// modify previous
				if (index - 1 >= 0) computeLazyMessage(this, messagesLazyOrder, index - 1)
			}
	}
}

fun computeLazyMessage(room: Room, list: SnapshotStateList<LazyMessage>, index: Int) {
	list[index].let { newMsg ->

		displayingLazyDataTag(room, index).let { displayingData ->
			newMsg.displayingData = displayingData
			displayingLazyEmployeeName(room, index).let { displayingName ->
				newMsg.backgroundColor =
					if (newMsg.messageID != room.markedMessage.messageID.value)
						if (newMsg.employeeID == null || newMsg.employeeID != Cache.Me.ID) MessageBackgroundCC else MessageMeBackgroundCC
					else MarkedMessageBackgroundCC
				newMsg.displayingName =
					newMsg.employeeID != null && newMsg.employeeID != Cache.Me.ID && room.view != RoomType.BLOG && (displayingData || displayingName)
				newMsg.addTopPadding = displayingName && !displayingData
			}
		}

	}
}

fun displayingLazyEmployeeName(room: Room, indexOfLazyMessage: Int): Boolean {
	return indexOfLazyMessage + 1 == room.messagesLazyOrder.size || room.messagesLazyOrder[indexOfLazyMessage].employeeID != room.messagesLazyOrder[indexOfLazyMessage + 1].employeeID
}

val c1: Calendar = Calendar.getInstance()
val c2: Calendar = Calendar.getInstance()
fun displayingLazyDataTag(room: Room, indexOfLazyMessage: Int): Boolean {
	if (indexOfLazyMessage + 1 == room.messagesLazyOrder.size) return true

	c1.setTime(Date(Cache.Data.messages[room.messagesLazyOrder[indexOfLazyMessage].messageID]!!.createdAt))
	c2.setTime(Date(Cache.Data.messages[room.messagesLazyOrder[indexOfLazyMessage + 1].messageID]!!.createdAt))

	return c1.get(Calendar.DAY_OF_YEAR) != c2.get(Calendar.DAY_OF_YEAR)
			|| c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)
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

