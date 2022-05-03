package ru.saime.gql_client.backend

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.ws.GraphQLWsProtocol
import com.apollographql.apollo3.network.ws.SubscriptionWsProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import pkg.*
import pkg.fragment.MessagesForRoom
import pkg.type.EventSubjectAction
import pkg.type.EventType
import pkg.type.MsgCreated
import ru.saime.gql_client.*
import ru.saime.gql_client.cache.*
import ru.saime.gql_client.utils.getRandomString


class Backend(
	val activity: MainActivity,
	val mainNavController: NavHostController,
	val pref: SharedPreferences
) {
	object States {
		var SendingMessage: Boolean = false
		var WebsocketConnectionEstablished: Boolean = false
	}

	var accessToken: String = ""
	var refreshToken: String = ""
	val sessionKey: String = getRandomString(MustLengthSessionKey)
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

	init {
		refreshToken = pref.getString(PrefRefreshTokenKey, "") ?: ""
//		MainScope().launch {
//			subscribe()
//		}
	}

	suspend fun subscribe() {
		println("оформляю подписку...")
		States.WebsocketConnectionEstablished = true // Чтобы знать что подписка активна
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
							room.messages[msg.prev]?.let { prevMsg ->
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
		States.WebsocketConnectionEstablished =
			false // А теперь снова можно будет подписать если конечно стоит проверка на это поле
	}


	fun logout() {
		pref.edit(true) {
			this.remove(PrefRefreshTokenKey)
		}
		activity.finish()
	}

}
