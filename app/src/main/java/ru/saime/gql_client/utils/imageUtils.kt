package ru.saime.gql_client.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.saime.gql_client.R
import ru.saime.gql_client.backend.Backend

const val DEFAULT_PROFILE_IMAGE = R.drawable.avatar

@ExperimentalCoroutinesApi
@Composable
fun loadPicture(url: String, @DrawableRes defaultImage: Int): MutableState<Bitmap?> {

	val bitmapState = mutableStateOf(null as Bitmap?)

	// show default image while image loads
	Glide.with(LocalContext.current)
		.asBitmap()
		.load(defaultImage)
		.into(object : CustomTarget<Bitmap>() {
			override fun onLoadCleared(placeholder: Drawable?) { }
			override fun onResourceReady(
				resource: Bitmap,
				transition: Transition<in Bitmap>?
			) {
				bitmapState.value = resource
			}
		})

	// get network image
	Glide.with(LocalContext.current)
		.asBitmap()
		.load(url)
		.into(object : CustomTarget<Bitmap>() {
			override fun onLoadCleared(placeholder: Drawable?) { }
			override fun onResourceReady(
				resource: Bitmap,
				transition: Transition<in Bitmap>?
			) {
				bitmapState.value = resource
			}
		})

	return bitmapState
}

fun Backend.loadPicture(url: String, onLoaded: (bitmap: ImageBitmap?) -> Unit) {

	// get network image
	Glide.with(activity)
		.asBitmap()
		.load(url)
		.into(object : CustomTarget<Bitmap>() {
			override fun onLoadCleared(placeholder: Drawable?) { }
			override fun onResourceReady(
				resource: Bitmap,
				transition: Transition<in Bitmap>?
			) {
				onLoaded(resource.asImageBitmap())
			}
		})
}










