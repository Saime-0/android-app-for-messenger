package ru.saime.gql_client

import android.content.SharedPreferences
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pkg.LoginMutation
import pkg.ProfileQuery
import kotlin.math.log


class View(
	private val apolloClient: ApolloClient,
	val mainNavController: NavHostController,
	pref: SharedPreferences
) {
	//	private var accessToken: String = MyToken
	private var accessToken: String = "Bearer eyJUeXAiOiJKV1QiLCJBbGciOiJIUzI1NiIsIkN0eSI6IiJ9.eyJlbXBsb3llZWlkIjo4LCJleHAiOjE2NDk1NDE4NDcsImlhdCI6MTY0ODY3Nzg0N30.qQIaQOHNJOZBzkgjZHeVo9LsCAnLAyAGbuhM-v8svG4"
	private var refreshToken: String = ""

	fun isAuthenticated() = refreshToken != ""

	init { refreshToken = pref.getString(PrefRefreshTokenKey, "") ?: ""	}

	suspend fun orderMe(callback: (result: ProfileQuery.OnMe?, err: String?) -> Unit) {
		val response: ApolloResponse<ProfileQuery.Data>
		try {
			response = apolloClient
				.query(ProfileQuery())
				.addHttpHeader("Authorization", accessToken)
				.execute()
			if (response.data != null)
				if (response.data!!.me.onMe != null)
					callback.invoke(response.data!!.me.onMe!!, null)
				else
					callback.invoke(null, response.data!!.me.onAdvancedError!!.toString())
			else if (response.errors != null)
				callback.invoke(null, response.errors!!.toString())
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(null, ex.toString())
		}
		return
	}


	suspend fun login(
		login: String,
		pass: String,
		callback: (success: Boolean, err: String?) -> Unit
	) {
//		var success = false
//		var response: ApolloResponse<LoginMutation.Data>?
		val response = apolloClient
			.mutation(LoginMutation(login, pass))
			.execute()
		try {
			accessToken = response.data!!.login.onTokenPair!!.accessToken
			refreshToken = response.data!!.login.onTokenPair!!.refreshToken
			callback.invoke(true, null)
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(false, response.data.toString() + "/// "+ ex.toString())
			return
		}
	}
}
