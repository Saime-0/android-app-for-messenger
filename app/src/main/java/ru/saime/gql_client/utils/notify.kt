package ru.saime.gql_client.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import pkg.SubscribeSubscription
import ru.saime.gql_client.MainActivity
import ru.saime.gql_client.R
import ru.saime.gql_client.cache.Cache


enum class Notifications {
	NewMessage
}
const val CHANNEL_ID = "default_channel"

class NotificationHelper(private val activity: MainActivity) {
	private val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	init {
		if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
			val defaultChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID,NotificationManager.IMPORTANCE_DEFAULT)
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
			.build()

			manager.notify(Notifications.NewMessage.ordinal, notify)
	}


}