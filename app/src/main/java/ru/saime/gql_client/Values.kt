package ru.saime.gql_client

import java.text.SimpleDateFormat
import java.util.*

const val PrefTableName = "user-table"
const val PrefRefreshTokenKey = "rt"
const val PrefNotificationEnable = "ne"

const val AuthorizationHeader = "Authorization"

const val CountOfOrderedRooms = 20

const val CountOfOrderedMessages = 50
const val CountOfOrderedMessagesOnPreload = 50

const val MustLengthSessionKey = 20

const val MessagePhotoSize = 38
const val MessagePhotoPadding = 8
const val MessageWithoutPhotoPadding = MessagePhotoSize + MessagePhotoPadding * 2

const val ServerUrl = "http://chating.ddns.net:8080/query"

object DateFormats {
	private val messageDateFormat = SimpleDateFormat("HH:mm", Locale.US)
	fun messageDate(epoch: Long): String = messageDateFormat.format(epoch)

	private val tagDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
	fun tagDate(epoch: Long): String = tagDateFormat.format(epoch)
}