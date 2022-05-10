package ru.saime.gql_client.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import pkg.SubscribeSubscription
import ru.saime.gql_client.MainActivity
import ru.saime.gql_client.R
import ru.saime.gql_client.cache.Cache

object NotifyOffsetID{
	const val NewMessage = 1000
}

const val CHANNEL_ID = "default_channel"

class NotificationHelper(private val activity: MainActivity) {
	val manager =
		activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	private val notifyIntent = Intent(activity, MainActivity::class.java).apply {
		flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
	}

	init {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val defaultChannel =
				NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
			manager.createNotificationChannel(defaultChannel)
		}
	}

	fun newMessage(msg: SubscribeSubscription.OnNewMessage) {


		val notify = NotificationCompat.Builder(activity, CHANNEL_ID)
			.setSmallIcon(R.drawable.avatar)   //This small icon can be seen on status bar
			.setContentTitle(Cache.Data.rooms[msg.roomID]?.name)
			.setContentText("${Cache.Data.employees[msg.employeeID]?.firstName}: ${msg.body}")
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setAutoCancel(true)
			.setContentIntent(
				PendingIntent.getActivity(
					activity,
					0,
					notifyIntent,
					PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
				)
			)
			.build()

		manager.notify(NotifyOffsetID.NewMessage+msg.roomID, notify)
	}


}