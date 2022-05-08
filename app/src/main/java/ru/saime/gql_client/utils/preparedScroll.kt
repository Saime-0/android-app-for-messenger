package ru.saime.gql_client.utils

import kotlinx.coroutines.delay
import pkg.type.MsgCreated
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.findRoomMessage
import ru.saime.gql_client.backend.orderRoomMessages
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.cache.Room

suspend fun Room.scrollToIndex(
	backend: Backend,
	index: Int,
	scrollOffset: Int = 0,
	animated: Boolean = false,
) {
	messagesLazyOrder[index].let { lazyMessage ->
		Cache.Data.messages[lazyMessage.messageID]?.let { msg ->

			if (msg.next != null && !Cache.Data.messages.containsKey(msg.next))
				backend.orderRoomMessages(roomID, MsgCreated.AFTER, msg.msgID, 10)

			if (msg.prev != null && !Cache.Data.messages.containsKey(msg.prev))
				backend.orderRoomMessages(roomID, MsgCreated.BEFORE, msg.msgID, 10)

			delay(50L)
			messagesLazyOrder.map { it.messageID }.indexOf(msg.msgID).let { newIndex ->
				this.lazyListState.run {
					if (animated) animateScrollToItem(newIndex, scrollOffset)
					else scrollToItem(newIndex, scrollOffset)
				}
			}


		}
	}
}
suspend fun Room.scrollToMsg(
	backend: Backend,
	msgID: Int,
	scrollOffset: Int = 0,
	animated: Boolean = false,
) {
	messagesLazyOrder.map { it.messageID }.indexOf(msgID).let { index ->
//		if (it == -1) throw Throwable("oh fuck, messagesLazyOrder contain'dnt msgID = $msgID")
		if (index==-1) {
			backend.findRoomMessage(msgID = msgID)
			delay(50L)
			scrollToIndex(
				backend,
				messagesLazyOrder.map { it.messageID }.indexOf(msgID),
				scrollOffset,
				animated
			)
		} else
			scrollToIndex(backend, index, scrollOffset, animated)
	}
}