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
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.runBlocking
import ru.saime.gql_client.backend.Backend
import ru.saime.gql_client.backend.refreshTokens
import ru.saime.gql_client.cache.Cache
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.screens.Login
import ru.saime.gql_client.screens.Profile
import ru.saime.gql_client.screens.RoomMessages
import ru.saime.gql_client.screens.Rooms
import ru.saime.gql_client.ui.theme.Gql_clientTheme
import ru.saime.gql_client.widgets.AppKeyboardFocusManager


class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			println(ServerUrl)
			val backend = Backend(
				this,
				rememberNavController(),
				getSharedPreferences(PrefTableName, MODE_PRIVATE)
			)
			ProvideWindowInsets {
				AppKeyboardFocusManager()
				Gql_clientTheme(this) {
					Surface(
						modifier = Modifier.fillMaxSize(),
						color = BackgroundCC
					) {
						// A surface container using the 'background' color from the theme
						NavHost(
							navController = backend.mainNavController,
							startDestination =
							if (backend.refreshTokenLoaded()) {
								val err: String?
								runBlocking {
									err = backend.refreshTokens()
								}
								if (err == null) Screen.Rooms.routeRef
								else Screen.Login.routeRef
							} else Screen.Login.routeRef
						) {
							composable(
								Screen.RoomMessages().routeRef,
								arguments = listOf(navArgument(Screen.RoomMessages.Args.RoomID.name) {
									type = NavType.IntType
								})
							) {
								if (it.arguments != null)
									if (Cache.Data.rooms[it.arguments!!.getInt(Screen.RoomMessages.Args.RoomID.name)] != null)
										RoomMessages(
											backend,
											Cache.Data.rooms[it.arguments!!.getInt(Screen.RoomMessages.Args.RoomID.name)]!!
										)
							}
							composable(Screen.Login.routeRef) { Login(backend) }
							composable(Screen.Rooms.routeRef) { Rooms(backend) }
							composable(
								Screen.Profile().routeRef,
								arguments = listOf(navArgument(Screen.Profile.Args.EmpID.name) {
									type = NavType.IntType
								})
							) {
								if (it.arguments != null) {
									Profile(
										backend = backend,
										empID = it.arguments!!.getInt(Screen.Profile.Args.EmpID.name),
									)
								}
							}
//						composable(Screen.Loading.route) { /*TODO*/ }
						}
					}
				}
			}
		}
	}
}