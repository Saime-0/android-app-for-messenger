package ru.saime.gql_client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.saime.gql_client.*

private val DarkColorPalette = darkColors(
	primary = Color(0xFF2B2B2B),
	primaryVariant = Color(0xFF1D1D1D),
	secondary = Color(0xFFD3A452),
	background = Color(0xFF1D1D1D),
)

private val LightColorPalette = lightColors(
	primary = Purple500,
	primaryVariant = Purple700,
	secondary = Teal200,

//        Other default colors to override
//        background = Color.White
//        surface = Color.White,
//        onPrimary = Color.White,
//        onSecondary = Color.Black,
//        onBackground = Color.Black,
//        onSurface = Color.Black,
)

@Composable
fun Gql_clientTheme(activity: MainActivity, darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

	val systemUiController = rememberSystemUiController().apply {
		setNavigationBarColor(
			if (darkTheme) NavigationBarCC else Color.White
		)
		setStatusBarColor(
			if (darkTheme) BackgroundCC else Color.White
		)
	}

	val colors = if (darkTheme) DarkColorPalette else LightColorPalette
	MaterialTheme(
		colors = colors,
		typography = Typography,
		shapes = Shapes,
		content = content
	)

	activity.getWindow().getDecorView().setBackgroundColor(
		if (darkTheme) BackgroundCCLong.toInt() else BackgroundLightCCLong.toInt()
	);

}