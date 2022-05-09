package ru.saime.gql_client

import java.text.SimpleDateFormat
import java.util.*

const val version = "22.130"

const val PrefTableName = "user-table"
const val PrefRefreshTokenKey = "rt"
const val PrefNotificationEnable = "ne"

const val AuthorizationHeader = "Authorization"

const val CountOfOrderedRooms = 20

const val CountOfOrderedMessages = 50
const val CountOfOrderedMessagesOnPreload = 35

const val MustLengthSessionKey = 20


object DateFormats {
	private val messageDateFormat = SimpleDateFormat("HH:mm", Locale.US)
	fun messageDate(epoch: Long): String = messageDateFormat.format(epoch)

	private val tagDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
	fun tagDate(epoch: Long): String = tagDateFormat.format(epoch)
}