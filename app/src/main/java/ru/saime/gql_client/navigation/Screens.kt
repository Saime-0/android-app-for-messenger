package ru.saime.gql_client.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

sealed class Screen(val route: String) {
    object Home: Screen("Home")
    object Members: Screen("Members")
    object SearchEmployee: Screen("SearchEmployee")
    object Profile: Screen("Profile")
    object RoomMessages: Screen("RoomMessages/{roomID}")
    object Login: Screen("Login")
    object Loading: Screen("Loading")
    object Guide: Screen("Guide")
    object Rooms: Screen("Rooms")
}

abstract class ScreenInterface {
    abstract val name: String
    abstract var navController: NavController?
    @Composable
    abstract fun Component()
}

object SomeScreen: ScreenInterface() {
    override val name: String
        get() = TODO("Not yet implemented")
    override var navController: NavController?
        get() = TODO("Not yet implemented")
        set(value) {}

    @Composable
    override fun Component(){

    }
}
