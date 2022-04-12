package ru.saime.gql_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import ru.saime.gql_client.screens.Home
import ru.saime.gql_client.screens.Login
import ru.saime.gql_client.ui.theme.Gql_clientTheme
import okhttp3.OkHttpClient
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.screens.RoomMessages


class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val apolloClient = ApolloClient.Builder()
			.serverUrl("http://chating.ddns.net:8080/query")
			.build()
		val pref = getSharedPreferences(PrefTableName, MODE_PRIVATE)
		setContent {
			Gql_clientTheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colors.background
				) {
					val client = View(apolloClient, rememberNavController(), pref)
					// A surface container using the 'background' color from the theme
					NavHost(
						navController = client.mainNavController,
						startDestination =
						if (client.isAuthenticated()) Screen.Home.route
						else Screen.Login.route
					) {
						composable(
							Screen.RoomMessages.route,
							arguments = listOf(navArgument("roomID") { type = NavType.IntType }
							)
						) { backStackEntry ->
							if (backStackEntry.arguments != null)
								RoomMessages(client, backStackEntry.arguments!!.getInt("roomID"))
						}
						composable(Screen.Home.route) { Home(client) }
						composable(Screen.Login.route) { Login(client) }
//						composable(Screen.Loading.route) { /*TODO*/ }
					}
				}
			}
		}
	}
}