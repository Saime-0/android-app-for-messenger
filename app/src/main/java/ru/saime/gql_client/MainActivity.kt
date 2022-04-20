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
import ru.saime.gql_client.screens.Home
import ru.saime.gql_client.screens.Login
import ru.saime.gql_client.ui.theme.Gql_clientTheme
import ru.saime.gql_client.navigation.Screen
import ru.saime.gql_client.screens.RoomMessages


class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val apolloClient = ApolloClient.Builder()
			.serverUrl("http://chating.ddns.net:8080/query")
			.build()
		setContent {
			Gql_clientTheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colors.background
				) {
					val view = View(apolloClient, rememberNavController(), getSharedPreferences(PrefTableName, MODE_PRIVATE))
					// A surface container using the 'background' color from the theme
					NavHost(
						navController = view.mainNavController,
						startDestination =
						if (view.refreshTokenLoaded()) Screen.Home.routeRef
						else Screen.Login.routeRef
					) {
						composable(
							Screen.RoomMessages().routeRef,
							arguments = listOf(navArgument(Screen.RoomMessages.Args.RoomID.name) { type = NavType.IntType }
							)
						) {
							if (it.arguments != null)
								RoomMessages(view, it.arguments!!.getInt(Screen.RoomMessages.Args.RoomID.name))
						}
						composable(Screen.Home.routeRef) { Home(view) }
						composable(Screen.Login.routeRef) { Login(view) }
//						composable(Screen.Loading.route) { /*TODO*/ }
					}
				}
			}
		}
	}
}