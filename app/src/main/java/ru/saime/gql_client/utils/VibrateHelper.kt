package ru.saime.gql_client.utils

import android.Manifest
import android.Manifest.permission.VIBRATE
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.apollographql.apollo3.api.ExecutionContext
import ru.saime.gql_client.MainActivity


/**
 * @author darren by Darren1009@qq.com - 2020/09/24
 */
class VibrateHelper constructor(context: Context) {
	private val vibrator: Vibrator?

	init {
		vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
	}


	/**
	 * Vibrate.
	 *
	 * Must hold `<uses-permission android:name="android.permission.VIBRATE" />`
	 *
	 * @param milliseconds The number of milliseconds to vibrate.
	 */
	@RequiresPermission(VIBRATE)
	fun vibrate(milliseconds: Long) {
		val vibrator = vibrator ?: return
		vibrator.vibrate(milliseconds)
	}

	/**
	 * Vibrate.
	 *
	 * Must hold `<uses-permission android:name="android.permission.VIBRATE" />`
	 *
	 * @param pattern An array of longs of times for which to turn the vibrator on or off.
	 * @param repeat  The index into pattern at which to repeat, or -1 if you don't want to repeat.
	 */
	@RequiresPermission(VIBRATE)
	fun vibrate(pattern: LongArray?, repeat: Int) {
		val vibrator = vibrator ?: return
		vibrator.vibrate(pattern, repeat)
	}

	/**
	 * Cancel vibrate.
	 *
	 * Must hold `<uses-permission android:name="android.permission.VIBRATE" />`
	 */
	@RequiresPermission(VIBRATE)
	fun cancel() {
		val vibrator = vibrator ?: return
		vibrator.cancel()
	}
}