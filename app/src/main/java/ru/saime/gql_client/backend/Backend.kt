package ru.saime.gql_client.backend


import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.ws.GraphQLWsProtocol
import com.apollographql.apollo3.network.ws.SubscriptionWsProtocol
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.retryWhen
import pkg.*
import ru.saime.gql_client.*
import ru.saime.gql_client.cache.*
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

	private var subscriptionJob: Job? = null

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

	init {
		refreshToken = pref.getString(PrefRefreshTokenKey, "") ?: ""
	}

	fun pleaseSubscribe() {
		if (subscriptionJob == null)
			subscriptionJob = MainScope().launch { subscribe() }
	}

	private suspend fun subscribe() {
		println("оформляю подписку...")
		States.WebSocketConnectionEstablished = true // Чтобы знать что подписка активна
		apolloClient
			.subscription(SubscribeSubscription(sessionKey))
			.addHttpHeader(AuthorizationHeader, accessToken)
			.toFlow()
			.retryWhen { _, attempt ->
				println("попытка не пытка, подписка упала")
				delay(attempt * 1000)
				true
			}
			.collect {
				println("пришел новый ивент в подписку 0_о")
				it.data?.subscribe?.body?.let { event ->

					// Новое сообщение
					event.onNewMessage?.let { msg ->

						Cache.Data.rooms[msg.roomID]?.let { room ->
							room.lastMsgID =
								msg.msgID // установить ид последнего сообщения в комнате
							Cache.Data.messages[msg.prev]?.let { prevMsg ->
								prevMsg.next =
									msg.msgID // для предыдущего сообщения меняю msg.next на id нового сообения
							}
						}
						// добавляю новое сообщение в кэш
						Cache.fillOnNewMessage(this, it.data!!.subscribe!!.body.onNewMessage!!)
					}

					// Когда удаляется комната
					event.onDropRoom?.let { }

					// Когда удаляется тег(должность)
					event.onDropTag?.let { }

					// Когда сотруднику либо выдают тегИ либо забирают
					event.onEmpTagAction?.let { }

					// Сотрудника либо добавляют в комнаты либо исключают
					event.onMemberAction?.let { }

					// AccessToken перестал был валидным, надо выполнить mutation.RefreshTokens
					event.onTokenExpired?.let { }

				}

			}
		println("===>> subscribe failure")
		States.WebSocketConnectionEstablished =
			false // А теперь снова можно будет подписать если конечно стоит проверка на это поле
	}


	fun logout() {
		pref.edit(true) {
			this.remove(PrefRefreshTokenKey) // удалить refresh token
		}
		// отменить подписку
		subscriptionJob?.cancel()
		subscriptionJob = null

		Cache.Me.run {
			ID = 0
			email = ""
			phone = ""
		}
		Cache.Data.run {
			rooms.clear()
			employees.clear()
			tags.clear()
			messages.clear()
		}
		Cache.LoadedData.clear()


		triggerRebirth(activity.applicationContext) // перезапуск приложения
	}

}
