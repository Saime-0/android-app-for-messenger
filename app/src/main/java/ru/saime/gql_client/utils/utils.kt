package ru.saime.gql_client.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import ru.saime.gql_client.MainActivity

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
