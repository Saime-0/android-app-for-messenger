package ru.saime.gql_client.cache

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pkg.ProfileQuery
import pkg.SubscribeSubscription
import pkg.fragment.FullEmployee
import pkg.fragment.FullTag
import pkg.fragment.MessagesForRoom
import pkg.fragment.RoomsWithoutMembers
import pkg.type.EventSubjectAction
import pkg.type.EventType
import pkg.type.MsgCreated
import pkg.type.RoomType
import ru.saime.gql_client.MessageBackgroundCC
import ru.saime.gql_client.MessageMeBackgroundCC
import ru.saime.gql_client.backend.*
import ru.saime.gql_client.utils.loadPicture
import java.util.*


fun Cache.fillMe(backend: Backend, data: ProfileQuery.OnMe) {
	Cache.fillEmployee(backend, data.employee.fullEmployee)

	Cache.Me.ID = data.employee.fullEmployee.empID

}

fun Cache.fillEmployee(backend: Backend, data: FullEmployee) {
	Cache.Data.employees[data.empID] = Employee(
		empID = data.empID,
		firstName = data.firstName,
		lastName = data.lastName,
		photoUrl = data.photoUrl,
		photo = mutableStateOf(null),
		email = data.email,
		phone = data.phoneNumber,
	).apply {
		if (photoUrl.isNotEmpty())
			backend.loadPicture(photoUrl) {
				this.photo.value = it
			}
	}
	for (tag in data.tags.fullTags.tags) {
		Cache.fillTag(tag.fullTag)
		Cache.Data.employees[data.empID]!!.tagIDs.add(tag.fullTag.tagID)
	}
}

suspend fun Cache.fillRooms(backend: Backend, data: RoomsWithoutMembers) { // todo
	println("начинаю заполнять комнаты")
	for (room1 in data.rooms) {
		 Room(
			 pos = room1.roomWithoutMembers.pos,
			 roomID = room1.roomWithoutMembers.roomID,
			 name = room1.roomWithoutMembers.name,
			 photoUrl = room1.roomWithoutMembers.photoUrl,
			 photo = mutableStateOf(null),
			 view = room1.roomWithoutMembers.view,
			 lastMsgID = mutableStateOf(room1.roomWithoutMembers.lastMessageID),
			 lastMsgRead = mutableStateOf(room1.roomWithoutMembers.lastMessageRead),
			 notify = mutableStateOf(room1.roomWithoutMembers.notify)
		 ).let { room ->
			 Cache.Data.rooms[room.roomID] = room
			 if (room.lastMsgID.value != null)
				 backend.orderRoomMessages(
					 room.roomID,
					 MsgCreated.BEFORE,
					 room.lastMsgID.value!!,
					 1
				 )
			 if (room.photoUrl.isNotEmpty())
				 backend.loadPicture(room.photoUrl) {
					 room.photo.value = it
				 }
		 }
	}
	Cache.Orders.roomOrder = Cache.Data.rooms.values
		.map { Pair(it.roomID, it.pos) }
		.sortedByDescending { it.second }
		.map { it.first }

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

suspend fun Cache.fillOnNewMessage(
	backend: Backend,
	newMessage: SubscribeSubscription.OnNewMessage
) {
	Message(
		roomID = newMessage.roomID, // todo: order if not exists... why?
		msgID = newMessage.msgID,
		empID = newMessage.employeeID,
		targetID = newMessage.targetMsgID,
		body = newMessage.body,
		createdAt = newMessage.createdAt * 1000L,
		prev = newMessage.prev,
		next = null,
	).let { msg ->
		Cache.Data.messages[msg.msgID] = msg

		// сначала подгружаю недостающие данные, тк клиент после пополения списка не перерисует экран если бы я подгружал автора позже
		Cache.orderTargetMessageIfNotExists(backend, msg.targetID)
		Cache.orderEmployeeMessageIfNotExists(backend, msg.empID)

		MainScope().launch { // fix Key 668 was already used
			Cache.Data.rooms[msg.roomID]?.let { room ->
				if (!room.messagesLazyOrder.map { it.messageID == msg.msgID }.contains(true))
					room.addLazyMessage(msg)
			}
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
				MainScope().launch { // fix Key 668 was already used
					if (!room.messagesLazyOrder.map { it.messageID }
							.contains(msgForRoom.msgID))
						room.addLazyMessage(msg)

				}
			}
		}
	}
}

suspend fun Room.addLazyMessage(msg: Message) {
	println("Room.addLazyMessage - ${msg.msgID}")
	// Создаю объект
	LazyMessage(
		messageID = msg.msgID,
		employeeID = msg.empID,
		alignment = if (msg.empID == null || msg.empID != Cache.Me.ID) Alignment.CenterStart else Alignment.CenterEnd,
		backgroundColor = (
				if (msg.empID == null || msg.empID != Cache.Me.ID) MessageBackgroundCC
				else MessageMeBackgroundCC),
		displayingData = false, // nope
		displayingName = false, // nope
		addTopPadding = false, // nope
	).let { newMsg ->
		messagesLazyOrder.add(newMsg) //потом добавляю его в список
		delay(30L)
		messagesLazyOrder.sortByDescending { it.messageID } // сортирую список
//		println(messagesLazyOrder.map { it.messageID })
		messagesLazyOrder.map { it.messageID }
			.indexOf(newMsg.messageID)
			.let { index -> // нахожу index объекта в отсортированном списке
				computeLazyMessage(this, messagesLazyOrder, index)
				// modify previous
				if (index - 1 >= 0) computeLazyMessage(this, messagesLazyOrder, index - 1)
			}
	}
}

fun computeLazyMessage(room: Room, list: List<LazyMessage>, index: Int) {
	list[index].let { newMsg ->

		displayingLazyDataTag(room, index).let { displayingData ->
			newMsg.displayingData = displayingData
			displayingLazyEmployeeName(room, index).let { displayingName ->
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
		backend.findRoomMessage(msgID = targetMsgID)
	}
}

suspend fun Cache.orderEmployeeMessageIfNotExists(backend: Backend, empID: Int?) {
	if (empID != null && Cache.Data.employees[empID] == null) {
		backend.orderEmployeeProfile(empID)
	}
}

