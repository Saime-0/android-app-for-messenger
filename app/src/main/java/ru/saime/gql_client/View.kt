package ru.saime.gql_client

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import pkg.EmployeeQuery
import pkg.LoginMutation
import pkg.ProfileQuery
import ru.saime.gql_client.cache.*


class View(
	private val apolloClient: ApolloClient,
	val mainNavController: NavHostController,
	private val pref: SharedPreferences
) {
	//	private var accessToken: String = MyToken
	private var accessToken: String = MyToken
	private var refreshToken: String = ""

	fun isAuthenticated() = refreshToken != ""

	init { refreshToken = pref.getString(PrefRefreshTokenKey, "") ?: ""	}

	suspend fun orderMe(callback: (err: String?) -> Unit) {
		if (Cache.LoadedData.containsKey(LoadedDataType.Me)) {
			println("Me уже был запрошен")
			callback.invoke(null)
			return
		}
		println("Попытка запросить Me")

		val response: ApolloResponse<ProfileQuery.Data>
		try {
			response = apolloClient
				.query(ProfileQuery())
				.addHttpHeader("authorization", accessToken)
				.execute()
			if (response.data != null)
				if (response.data!!.me.onMe != null) {
					Cache.fillMe(response.data!!.me.onMe!!)
					Cache.LoadedData[LoadedDataType.Me] = Unit
					callback.invoke(null)
				}
				else
					callback.invoke(response.data!!.me.onAdvancedError!!.toString())
			else if (response.errors != null)
				callback.invoke(response.errors!!.toString())
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(ex.toString())
		}
		return
	}

	suspend fun orderEmployeeProfile(empID:Int, callback: (err: String?) -> Unit) {
		if (Cache.Data.employees.containsKey(empID)) {
			println("EmployeeProfile empID = $empID - уже был запрошен")
			callback.invoke(null)
			return
		}
		println("Попытка запросить EmployeeProfile empID = $empID")

		val response: ApolloResponse<EmployeeQuery.Data>
		try {
			response = apolloClient
				.query(EmployeeQuery(empID))
				.addHttpHeader("authorization", accessToken)
				.execute()
			if (response.data != null)
				if (response.data!!.employees.onEmployees != null)
				if (response.data!!.employees.onEmployees!!.employees != null)
				if (response.data!!.employees.onEmployees!!.employees!!.isNotEmpty()){
					Cache.fillEmployee(response.data!!.employees.onEmployees!!.employees!![0])
					Cache.LoadedData[LoadedDataType.Me] = Unit
					callback.invoke(null)
				}
				else
					callback.invoke("not-found")
				else
					callback.invoke(response.data!!.employees.onAdvancedError!!.toString())
			else if (response.errors != null)
				callback.invoke(response.errors!!.toString())
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(ex.toString())
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
			accessToken = """Bearer ${response.data!!.login.onTokenPair!!.accessToken}"""
			refreshToken = response.data!!.login.onTokenPair!!.refreshToken
			pref.edit {
				putString(PrefRefreshTokenKey, refreshToken)
			}
			println("успешно залогинился, токены обновлены, $accessToken")
			callback.invoke(true, null)
		} catch (ex: Exception) {
			println(ex)
			callback.invoke(false, response.data.toString() + "/// "+ ex.toString())
			return
		}
	}
}
