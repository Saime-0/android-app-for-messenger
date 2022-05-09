package ru.saime.gql_client.backend

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import pkg.*
import pkg.type.MsgCreated
import ru.saime.gql_client.AuthorizationHeader
import ru.saime.gql_client.CountOfOrderedMessages
import ru.saime.gql_client.CountOfOrderedRooms
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


suspend fun Backend.orderMeRooms(
	offset: Int,
	limit: Int = CountOfOrderedRooms,
): String? {
	println("попытка запросить RoomList")

	apolloClient
		.query(
			MeRoomsListQuery(
				offset = Optional.presentIfNotNull(offset),
				limit = Optional.presentIfNotNull(limit)
			)
		)
		.addHttpHeader(AuthorizationHeader, accessToken)
		.execute().let { response ->
			try {
				if (response.data != null)
					return if (response.data!!.me.onMe != null) {
						Cache.fillRooms(this, response.data!!.me.onMe!!.rooms.roomsWithoutMembers)
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
}

suspend fun Backend.orderRoomMessages(
	roomID: Int,
	created: MsgCreated,
	startMsg: Int,
	count: Int = CountOfOrderedMessages,
): String? {
	println("попытка запросить orderRoomMessages(ByCreated)")
	apolloClient.query(
		RoomMessagesByCreatedQuery(
			roomID = roomID,
			created = created,
			startMsg = startMsg,
			count = count,
		)
	).addHttpHeader(AuthorizationHeader, accessToken).execute().let { response ->
		return try {

			if (response.data != null)
				if (response.data!!.roomMessages.onMessages != null) {
					Cache.fillRoomMessages(
						this,
						response.data!!.roomMessages.onMessages!!.messagesForRoom
					)
					null
				} else response.data!!.roomMessages.onAdvancedError!!.toString()
			else response.errors!!.toString()
		} catch (ex: Exception) {
			println(ex)
			ex.toString()
		}
	}
}

suspend fun Backend.orderRoomMessages(
	roomID: Int,
	start: Int,
	inDirection: Int,
): String? {
	println("попытка запросить orderRoomMessages(ByRange)")
	apolloClient.query(
		RoomMessagesByRangeQuery(
			roomID = roomID,
			start = start,
			inDirection = inDirection,
		)
	).addHttpHeader(AuthorizationHeader, accessToken).execute().let { response ->
		return try {
			if (response.data != null)
				if (response.data!!.roomMessages.onMessages != null) {
					Cache.fillRoomMessages(
						this,
						response.data!!.roomMessages.onMessages!!.messagesForRoom
					)
					null
				} else response.data!!.roomMessages.onAdvancedError!!.toString()
			else response.errors!!.toString()
		} catch (ex: Exception) {
			println(ex)
			ex.toString()
		}
	}
}

suspend fun Backend.findRoomMessage(
	roomID: Int? = null,
	msgID: Int? = null,
	empID: Int? = null,
	targetID: Int? = null,
	textFragment: String? = null,
): String? {
	println("попытка запросить findRoomMessage")
	apolloClient.query(
		FindMessagesQuery(
			roomID = Optional.presentIfNotNull(roomID),
			msgID = Optional.presentIfNotNull(msgID),
			empID = Optional.presentIfNotNull(empID),
			targetID = Optional.presentIfNotNull(targetID),
			textFragment = Optional.presentIfNotNull(textFragment),
		)
	).addHttpHeader(AuthorizationHeader, accessToken).execute().let { response ->
		return try {
			if (response.data != null)
				if (response.data!!.messages.onMessages != null) {
					Cache.fillRoomMessages(
						this,
						response.data!!.messages.onMessages!!.messagesForRoom
					)
					null
				} else
					response.data!!.messages.onAdvancedError!!.toString()
			else response.errors!!.toString()
		} catch (ex: Exception) {
			println(ex)
			ex.toString()
		}
	}
}