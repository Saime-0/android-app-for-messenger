package ru.saime.gql_client.backend

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.retryWhen
import pkg.SubscribeSubscription
import ru.saime.gql_client.AuthorizationHeader
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.fillOnNewMessage

suspend fun Backend.subscribe() {
	println("оформляю подписку...")
	Backend.States.WebSocketConnectionEstablished = true // Чтобы знать что подписка активна
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
					// * а ничего не сломается если переместить обратно под изменение стейта комнат или сломаейтся хм
					// добавляю новое сообщение в кэш
					Cache.fillOnNewMessage(this, msg)

					// меняю стейт комнаты
					Cache.Data.rooms[msg.roomID]?.let { room ->
						room.lastMsgID.value = msg.msgID // установить ид последнего сообщения в комнате
						if (msg.employeeID == Cache.Me.ID)
							room.lastMsgRead.value = msg.msgID // если это свое сообщение то сделать его прочитанным
						Cache.Data.messages[msg.prev]?.let { prevMsg ->
							prevMsg.next =
								msg.msgID // для предыдущего сообщения меняю msg.next на id нового сообения
						}
					}

					// дальше
					eventFlow.newMessage.emit(msg)

					println(activity.lifecycle.currentState)
					if (Cache.Me.NotificationsEnable && msg.employeeID != Cache.Me.ID && activity.lifecycle.currentState != Lifecycle.State.RESUMED)
						notificationHelper.newMessage(msg)
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
	Backend.States.WebSocketConnectionEstablished =
		false // А теперь снова можно будет подписать если конечно стоит проверка на это поле
}