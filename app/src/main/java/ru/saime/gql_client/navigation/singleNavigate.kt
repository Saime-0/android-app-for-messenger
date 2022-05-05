package ru.saime.gql_client.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navDeepLink

fun NavController.singleNavigate(route: String) {
//	navDeepLink {  }
	navigate(route)
	{
		popUpTo(graph.findStartDestination().id) {
			saveState = true
		}
		launchSingleTop=true
		restoreState=true
	}
}