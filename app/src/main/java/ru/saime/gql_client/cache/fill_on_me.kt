package ru.saime.gql_client.cache

import pkg.ProfileQuery

fun Cache.fillMe(data: ProfileQuery.OnMe) {
	Cache.fillEmployee(data.employee)
	if (data.employee.tags.tags != null)
		for (tag in data.employee.tags.tags) Cache.fillTag(tag)
}

fun Cache.fillTag(data: ProfileQuery.Tag) {
	Cache.Data.tags[data.tagID.toInt()] = Tag(
		tagID = data.tagID.toInt(),
		name = data.name
	)
}

fun Cache.fillEmployee(data: ProfileQuery.Employee) {
	Cache.Data.employees[data.empID.toInt()] = Employee(
		empID = data.empID.toInt(),
		firstName = data.firstName,
		lastName = data.lastName,
		joinedAt = 0,
	)
}


