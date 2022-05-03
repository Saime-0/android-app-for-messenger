package ru.saime.gql_client.backend

import androidx.core.content.edit
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import pkg.*
import pkg.type.EventSubjectAction
import pkg.type.EventType
import ru.saime.gql_client.AuthorizationHeader
import ru.saime.gql_client.PrefRefreshTokenKey

suspend fun Backend.editSubscribeList(
	action: EventSubjectAction,
	targetRooms: List<Int>,
	listenEvents: List<EventType>,
): String? {
	apolloClient.mutation(
		EditSubscriptionMutation(
			sessionKey = this.sessionKey,
			action = action,
			targetRooms = targetRooms,
			listenEvents = listenEvents,
		)
	).addHttpHeader(AuthorizationHeader, accessToken).execute().let { response ->
		try {
			if (response.data != null)
				return if (response.data!!.editListenEventCollection.onListenCollection != null) {
					null
				} else
					response.data!!.editListenEventCollection.onAdvancedError!!.toString()
			else if (response.errors != null)
				return response.errors!!.toString()
		} catch (ex: Exception) {
			println(ex)
			return ex.toString()
		}
	}
	return null
}

suspend fun Backend.loginByCredentials(
	login: String,
	pass: String,
	callback: (success: Boolean, err: String?) -> Unit
) {
	apolloClient
		.mutation(LoginMutation(login, pass))
		.execute().let { response ->
			try {
				accessToken = "Bearer ${response.data!!.login.onTokenPair!!.accessToken}"
				refreshToken = response.data!!.login.onTokenPair!!.refreshToken
				pref.edit(true) {
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
}

suspend fun Backend.refreshTokens(
	callback: (err: String?) -> Unit
): Boolean {
	val response = apolloClient
		.mutation(RefreshTokensMutation(refreshToken, Optional.presentIfNotNull(sessionKey)))
		.execute()
	return try {
		accessToken = "Bearer ${response.data!!.refreshTokens.onTokenPair!!.accessToken}"
		this.refreshToken = response.data!!.refreshTokens.onTokenPair!!.refreshToken
		pref.edit(true) {
			putString(PrefRefreshTokenKey, refreshToken)
		}
		callback.invoke(null)
		true
	} catch (ex: Exception) {
		println(ex)
		callback.invoke(response.data.toString() + "/// " + ex.toString())
		false
	}
}


suspend fun Backend.sendMessage(
	roomID: Int,
	text: String,
	targetID: Int? = null,
): String? {
	println("попытка отправить sendMessage")
	if (Backend.States.SendingMessage) return "дождитесь отправки предыдущего сообщения"
	Backend.States.SendingMessage = true
	apolloClient.mutation(
		SendMessageMutation(
			roomID = roomID,
			replyID = Optional.presentIfNotNull(targetID),
			text = text,
		)
	).addHttpHeader(AuthorizationHeader, accessToken).execute().let { response ->
		try {

			if (response.data != null)
				return if (response.data!!.sendMsg.onSuccessful != null) {
					null
				} else
					response.data!!.sendMsg.onAdvancedError!!.toString()
			else if (response.errors != null)
				return response.errors!!.toString()
		} catch (ex: Exception) {
			println(ex)
			return ex.toString()
		} finally {
			Backend.States.SendingMessage = false
		}
	}
	return null
}