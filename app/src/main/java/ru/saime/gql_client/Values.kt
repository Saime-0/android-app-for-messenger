package ru.saime.gql_client

import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

const val version = "22.129"

const val PrefTableName = "user-table"
const val PrefRefreshTokenKey = "rt"
const val PrefMeEmpIDKey = "id"
const val PrefMeFnameKey = "fn"
const val PrefMeLnameKey = "ln"
const val AuthorizationHeader = "Authorization"

const val MyToken = "Bearer eyJUeXAiOiJKV1QiLCJBbGciOiJIUzI1NiIsIkN0eSI6IiJ9.eyJlbXBsb3llZWlkIjo4LCJleHAiOjE2NTA1NzcxNzYsImlhdCI6MTY0OTcxMzE3Nn0.9ukEEa-Vgd-59MbEJOiS7TA353MAoNq_Nxbm-r6QYi8"


val DockHeight = 70.dp
val MessageInputMinHeight = 40.dp

const val CountOfOrderedMessages = 20

const val MustLengthSessionKey = 20


object DateFormats {
	private val messageDateFormat = SimpleDateFormat("HH:mm", Locale.US)
	fun messageDate(epoch: Long): String = messageDateFormat.format(epoch)

	private val tagDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
	fun tagDate(epoch: Long): String = tagDateFormat.format(epoch)
}