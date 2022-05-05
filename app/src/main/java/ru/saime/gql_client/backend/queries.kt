package ru.saime.gql_client.backend

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pkg.*
import pkg.type.MsgCreated
import ru.saime.gql_client.AuthorizationHeader
import ru.saime.gql_client.CountOfOrderedMessages
import ru.saime.gql_client.cache.*

suspend fun Backend.orderMe(): String? {
	if (Cache.LoadedData.containsKey(LoadedDataType.Me)) {
		println("Me уже был запрошен")
		return null
	}
//	if (!Backend.States.WebSocketConnectionEstablished) {
//		MainScope().launch { subscribe() }
//	}
	println("Попытка запросить Me")

	val response: ApolloResponse<ProfileQuery.Data>
	try {
		response = apolloClient
			.query(ProfileQuery())
			.addHttpHeader(AuthorizationHeader, accessToken)
			.execute()
		if (response.data != null)
			return if (response.data!!.me.onMe != null) {
				Cache.fillMe(response.data!!.me.onMe!!)
				Cache.LoadedData[LoadedDataType.Me] = Unit
				null
			} else
				response.data!!.me.onAdvancedError!!.toString()
		else if (response.errors != null)
			return response.errors!!.toString()
	} catch (ex: Exception) {
		println(ex)
		return ex.toString()
	}
	return null
}

suspend fun Backend.orderEmployeeProfile(empID: Int): String? {
	if (Cache.Data.employees.containsKey(empID)) {
		println("EmployeeProfile empID = $empID - уже был запрошен")
		return null
	}
	println("Попытка запросить EmployeeProfile empID = $empID")

	val response: ApolloResponse<EmployeeQuery.Data>
	try {
		response = apolloClient
			.query(EmployeeQuery(empID))
			.addHttpHeader(AuthorizationHeader, accessToken)
			.execute()
		if (response.data != null)
			if (response.data!!.employees.onEmployees != null)
				if (response.data!!.employees.onEmployees!!.fullEmployees.employees.isNotEmpty()) {
					Cache.fillEmployee(response.data!!.employees.onEmployees!!.fullEmployees.employees[0].fullEmployee)
					Cache.LoadedData[LoadedDataType.Me] = Unit
					return null
				} else
					return "not-found"
			else if (response.errors != null)
				return response.errors!!.toString()
	} catch (ex: Exception) {
		println(ex)
		return ex.toString()
	}
	return null
}


suspend fun Backend.orderMeRooms(): String? {
	if (Cache.LoadedData.containsKey(LoadedDataType.RoomList)) {
		println("RoomList уже был запрошен")
		return null
	}
	println("попытка запросить RoomList")

	val response: ApolloResponse<MeRoomsListQuery.Data>
	try {
		response = apolloClient.query(
			MeRoomsListQuery(
				Optional.presentIfNotNull(0),
				Optional.presentIfNotNull(20)
			)
		).addHttpHeader(AuthorizationHeader, accessToken).execute()
		if (response.data != null)
			return if (response.data!!.me.onMe != null) {
				Cache.fillRooms(response.data!!.me.onMe!!.rooms.roomsWithoutMembers)
				Cache.LoadedData[LoadedDataType.RoomList] = Unit
				null
			} else
				response.data!!.me.onAdvancedError!!.toString()
		else if (response.errors != null)
			return response.errors!!.toString()
	} catch (ex: Exception) {
		println(ex)
		return ex.toString()
	}
	return null
}

suspend fun Backend.orderRoomMessages(
	roomID: Int,
	created: MsgCreated,
	startMsg: Int,
	callback: (err: String?) -> Unit = {}
) {
	println("попытка запросить orderRoomMessages")
	val response: ApolloResponse<RoomMessagesByCreatedQuery.Data>
	try {
		response = apolloClient.query(
			RoomMessagesByCreatedQuery(
				roomID = roomID,
				created = created,
				startMsg = startMsg,
				count = CountOfOrderedMessages,
			)
		).addHttpHeader(AuthorizationHeader, accessToken).execute()
		if (response.data != null)
			if (response.data!!.roomMessages.onMessages != null) {
				Cache.fillRoomMessages(
					this,
					response.data!!.roomMessages.onMessages!!.messagesForRoom
				)
				callback.invoke(null)
			} else
				callback.invoke(response.data!!.roomMessages.onAdvancedError!!.toString())
		else if (response.errors != null)
			callback.invoke(response.errors!!.toString())
	} catch (ex: Exception) {
		println(ex)
		callback.invoke(ex.toString())
	}
	return
}

suspend fun Backend.orderRoomMessage(
	roomID: Int? = null,
	msgID: Int? = null,
	empID: Int? = null,
	targetID: Int? = null,
	textFragment: String? = null,
	callback: (err: String?) -> Unit = {}
) {
	println("попытка запросить orderRoomMessage")
	val response: ApolloResponse<FindMessagesQuery.Data>
	try {
		response = apolloClient.query(
			FindMessagesQuery(
				roomID = Optional.presentIfNotNull(roomID),
				msgID = Optional.presentIfNotNull(msgID),
				empID = Optional.presentIfNotNull(empID),
				targetID = Optional.presentIfNotNull(targetID),
				textFragment = Optional.presentIfNotNull(textFragment),
			)
		).addHttpHeader(AuthorizationHeader, accessToken).execute()
		if (response.data != null)
			if (response.data!!.messages.onMessages != null) {
				Cache.fillRoomMessages(
					this,
					response.data!!.messages.onMessages!!.messagesForRoom
				)
				callback.invoke(null)
			} else
				callback.invoke(response.data!!.messages.onAdvancedError!!.toString())
		else if (response.errors != null)
			callback.invoke(response.errors!!.toString())
	} catch (ex: Exception) {
		println(ex)
		callback.invoke(ex.toString())
	}
	return
}