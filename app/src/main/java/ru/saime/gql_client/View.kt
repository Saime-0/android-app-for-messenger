package ru.saime.gql_client

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import pkg.*
import pkg.type.MsgCreated
import ru.saime.gql_client.cache.*


class View(
	private val apolloClient: ApolloClient,
	val mainNavController: NavHostController,
	private val pref: SharedPreferences
) {
	//	private var accessToken: String = MyToken
	private var accessToken: String = MyToken
	private var refreshToken: String = ""
	private var sessionKey: String? = null

	fun refreshTokenLoaded() = refreshToken != ""

	init {
		refreshToken = pref.getString(PrefRefreshTokenKey, "") ?: ""
	}

	suspend fun orderMe(callback: (err: String?) -> Unit) {
		if (Cache.LoadedData.containsKey(LoadedDataType.Me)) {
			println("Me уже был запрошен")
			callback.invoke(null)
			return
		}
		println("Попытка запросить Me")

		val response: ApolloResponse<ProfileQuery.Data>
		try {
			response = apolloClient
				.query(ProfileQuery())
				.addHttpHeader("authorization", accessToken)
				.execute()
			if (response.data != null)
				if (response.data!!.me.onMe != null) {
					Cache.fillMe(response.data!!.me.onMe!!)
					Cache.LoadedData[LoadedDataType.Me] = Unit
					callback.invoke(null)
				} else
					callback.invoke(response.data!!.me.onAdvancedError!!.toString())
			else if (response.errors != null)
				callback.invoke(response.errors!!.toString())
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(ex.toString())
		}
		return
	}

	suspend fun orderEmployeeProfile(empID: Int, callback: (err: String?) -> Unit = {}) {
		if (Cache.Data.employees.containsKey(empID)) {
			println("EmployeeProfile empID = $empID - уже был запрошен")
			callback.invoke(null)
			return
		}
		println("Попытка запросить EmployeeProfile empID = $empID")

		val response: ApolloResponse<EmployeeQuery.Data>
		try {
			response = apolloClient
				.query(EmployeeQuery(empID))
				.addHttpHeader("authorization", accessToken)
				.execute()
			if (response.data != null)
				if (response.data!!.employees.onEmployees != null)
					if (response.data!!.employees.onEmployees!!.fullEmployees.employees.isNotEmpty()) {
						Cache.fillEmployee(response.data!!.employees.onEmployees!!.fullEmployees.employees[0].fullEmployee)
						Cache.LoadedData[LoadedDataType.Me] = Unit
						callback.invoke(null)
					} else
						callback.invoke("not-found")
				else if (response.errors != null)
					callback.invoke(response.errors!!.toString())
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(ex.toString())
		}
		return
	}


	suspend fun loginByCredentials(
		login: String,
		pass: String,
		callback: (success: Boolean, err: String?) -> Unit
	) {
//		var success = false
//		var response: ApolloResponse<LoginMutation.Data>?
		val response = apolloClient
			.mutation(LoginMutation(login, pass))
			.execute()
		try {
			accessToken = "Bearer ${response.data!!.login.onTokenPair!!.accessToken}"
			refreshToken = response.data!!.login.onTokenPair!!.refreshToken
			pref.edit {
				putString(PrefRefreshTokenKey, refreshToken)
			}
			println("успешно залогинился, токены обновлены, $accessToken")
			callback.invoke(true, null)
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(false, response.data.toString() + "/// " + ex.toString())
			return
		}
	}

	suspend fun refreshTokens(
		callback: (err: String?) -> Unit
	): Boolean {
		val response = apolloClient
			.mutation(RefreshTokensMutation(refreshToken, Optional.presentIfNotNull(sessionKey)))
			.execute()
		return try {
			accessToken = "Bearer ${response.data!!.refreshTokens.onTokenPair!!.accessToken}"
			this.refreshToken = response.data!!.refreshTokens.onTokenPair!!.refreshToken
			pref.edit {
				putString(PrefRefreshTokenKey, this@View.refreshToken)
			}
			callback.invoke(null)
			true
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(response.data.toString() + "/// " + ex.toString())
			false
		}
	}

	suspend fun orderMeRooms(
		callback: (err: String?) -> Unit
	) {
		if (Cache.LoadedData.containsKey(LoadedDataType.RoomList)) {
			println("RoomList уже был запрошен")
			callback.invoke(null)
			return
		}
		println("попытка запросить RoomList")
		val response: ApolloResponse<MeRoomsListQuery.Data>
		try {
			response = apolloClient.query(
					MeRoomsListQuery(
						Optional.presentIfNotNull(0),
						Optional.presentIfNotNull(20)
					)
				)
				.addHttpHeader("authorization", accessToken)
				.execute()
			if (response.data != null)
				if (response.data!!.me.onMe != null) {
					Cache.fillRooms(response.data!!.me.onMe!!.rooms.roomsWithoutMembers)
					Cache.LoadedData[LoadedDataType.RoomList] = Unit
					callback.invoke(null)
				} else
					callback.invoke(response.data!!.me.onAdvancedError!!.toString())
			else if (response.errors != null)
				callback.invoke(response.errors!!.toString())
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(ex.toString())
		}
		return
	}

	suspend fun orderRoomMessages(
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
			)
				.addHttpHeader("authorization", accessToken)
				.execute()
			if (response.data != null)
				if (response.data!!.roomMessages.onMessages != null) {
					Cache.fillRoomMessages(
						this,
						response.data!!.roomMessages.onMessages!!.messagesForRoom
					)
					println(Cache.Data.rooms[roomID]!!.messages)
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

	suspend fun orderRoomMessage(
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
			).addHttpHeader("authorization", accessToken)
			.execute()
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
}
