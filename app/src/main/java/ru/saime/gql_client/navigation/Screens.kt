package ru.saime.gql_client.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

sealed class Screen(
	val name: String,
	val routeRef: String = name,
	val routeWithArgs: String = routeRef
) {
	object Members : Screen("Members")
	object Login : Screen("Login")
	object Loading : Screen("Loading")
	object Guide : Screen("Guide")
	object Rooms : Screen("Rooms")
	object SearchEmployee : Screen("SearchEmployee")

	class Profile(empID: Int = 0) : Screen(
		"Profile",
		"Profile/{${Args.EmpID}}",
		"Profile/$empID"
	) {
		enum class Args { EmpID }
	}

	class RoomMessages(roomID: Int = 0) : Screen(
		"RoomMessages",
		"RoomMessages/{${Args.RoomID}}",
		"RoomMessages/$roomID"
	) {
		enum class Args { RoomID }
	}
}

abstract class ScreenInterface {
	abstract val name: String
	abstract var navController: NavController?

	@Composable
	abstract fun Component()
}

object SomeScreen : ScreenInterface() {
	override val name: String
		get() = TODO("Not yet implemented")
	override var navController: NavController?
		get() = TODO("Not yet implemented")
		set(value) {}

	@Composable
	override fun Component() {

	}
}
