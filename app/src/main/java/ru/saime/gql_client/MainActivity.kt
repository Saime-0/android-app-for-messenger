package ru.saime.gql_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.runBlocking
import ru.saime.gql_client.screens.Home
import ru.saime.gql_client.screens.Login
import ru.saime.gql_client.ui.theme.Gql_clientTheme
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.screens.RoomMessages
import com.google.accompanist.insets.ProvideWindowInsets
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.refreshTokens
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.utils.VibrateHelper
import ru.saime.gql_client.widgets.AppKeyboardFocusManager
import android.content.Context



class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			ProvideWindowInsets {
				AppKeyboardFocusManager()
				Gql_clientTheme(this) {
					Surface(
						modifier = Modifier.fillMaxSize(),
						color = BackgroundCC
					) {
						val backend = Backend(
							this,
							rememberNavController(),
							getSharedPreferences(PrefTableName, MODE_PRIVATE)
						)
						// A surface container using the 'background' color from the theme
						NavHost(
							navController = backend.mainNavController,
							startDestination =
							if (backend.refreshTokenLoaded()) {
								val resultRefreshTokens: Boolean
								runBlocking {
									resultRefreshTokens = backend.refreshTokens {}
								}
								if (resultRefreshTokens)
									Screen.Home.routeRef
								else
									Screen.Login.routeRef
							} else Screen.Login.routeRef
						) {
							composable(
								Screen.RoomMessages().routeRef,
								arguments = listOf(navArgument(Screen.RoomMessages.Args.RoomID.name) {
									type = NavType.IntType
								}
								)
							) {
								if (it.arguments != null)
									if (Cache.Data.rooms[it.arguments!!.getInt(Screen.RoomMessages.Args.RoomID.name)] != null)
									RoomMessages(
										backend,
										Cache.Data.rooms[it.arguments!!.getInt(Screen.RoomMessages.Args.RoomID.name)]!!
									)
							}
							composable(Screen.Home.routeRef) { Home(backend) }
							composable(Screen.Login.routeRef) { Login(backend) }
//						composable(Screen.Loading.route) { /*TODO*/ }
						}
					}
				}
			}
		}
	}
}