package ru.saime.gql_client.utils

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import ru.saime.gql_client.MainActivity
import ru.saime.gql_client.cache.Room

fun getRandomString(length: Int) : String {
	val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
	return (1..length)
		.map { allowedChars.random() }
		.joinToString("")
}

// рестарт приложения
fun triggerRebirth(context: Context) {
	val packageManager: PackageManager = context.getPackageManager()
	val intent = packageManager.getLaunchIntentForPackage(context.getPackageName())
	val componentName = intent!!.component
	val mainIntent = Intent.makeRestartActivityTask(componentName)
	context.startActivity(mainIntent)
	Runtime.getRuntime().exit(0)
}

fun Context.getActivity(): MainActivity? = when (this) {
	is MainActivity -> this
	is ContextWrapper -> baseContext.getActivity()
	else -> null
}


fun foregrounded(): Boolean {
	val appProcessInfo = ActivityManager.RunningAppProcessInfo()
	ActivityManager.getMyMemoryState(appProcessInfo)
	return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE)
}

fun lastMessageDontRead(room: Room): Boolean {
	return room.lastMsgID.value != null && (room.lastMsgRead.value == null || room.lastMsgRead.value!! < room.lastMsgID.value!!)
}

fun getAppVersion(context: Context): String {
	var version = ""
	try {
		val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
		version = pInfo.versionName
	} catch (e: PackageManager.NameNotFoundException) {
		e.printStackTrace()
	}

	return version
}