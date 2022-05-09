package ru.saime.gql_client.backend


import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.ws.GraphQLWsProtocol
import com.apollographql.apollo3.network.ws.SubscriptionWsProtocol
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import pkg.SubscribeSubscription
import ru.saime.gql_client.AuthorizationHeader
import ru.saime.gql_client.MainActivity
import ru.saime.gql_client.MustLengthSessionKey
import ru.saime.gql_client.PrefRefreshTokenKey
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.utils.VibrateHelper
import ru.saime.gql_client.utils.getRandomString
import ru.saime.gql_client.utils.triggerRebirth


class Backend(
	val activity: MainActivity,
	val mainNavController: NavHostController,
	val pref: SharedPreferences
) {
	val vibrateHelper = VibrateHelper(activity.baseContext)
	object States {
		var SendingMessage: Boolean = false
		var ReadingMessage: Boolean = false
		var WebSocketConnectionEstablished: Boolean = false
	}

	var accessToken: String = ""
	var refreshToken: String = ""
	val sessionKey: String = getRandomString(MustLengthSessionKey)

	var subscriptionJob: Job? = null

	val apolloClient: ApolloClient = ApolloClient.Builder()
		.serverUrl("http://chating.ddns.net:8080/query")
		.webSocketServerUrl("http://chating.ddns.net:8080/query")
		.wsProtocol(GraphQLWsProtocol.Factory())
		.wsProtocol(
			SubscriptionWsProtocol.Factory(
				connectionPayload = {
					mapOf(
						AuthorizationHeader to accessToken
					)
				}
			))
		.build()

	fun refreshTokenLoaded() = refreshToken != ""

//	val clearEventFlow: Flow<EventResult> =

	data class EventFlow(
		val newMessage: MutableSharedFlow<SubscribeSubscription.OnNewMessage>
	)
	val eventFlow: EventFlow = EventFlow(
		newMessage = MutableSharedFlow()
	)
	init {
		refreshToken = pref.getString(PrefRefreshTokenKey, "") ?: ""

	}
}



fun Backend.pleaseSubscribe() {
	if (subscriptionJob == null)
		subscriptionJob = MainScope().launch { subscribe() }
}




fun Backend.logout() {
	pref.edit(true) {
		this.remove(PrefRefreshTokenKey) // удалить refresh token
	}
	// отменить подписку
	subscriptionJob?.cancel()
	subscriptionJob = null

	Cache.Me.run {
		ID = 0
	}
	Cache.Data.run {
		rooms.clear()
		employees.clear()
		tags.clear()
		messages.clear()
	}
	Cache.Orders.roomOrder = emptyList()
	Cache.LoadedData.clear()


	triggerRebirth(activity.applicationContext) // перезапуск приложения
}

