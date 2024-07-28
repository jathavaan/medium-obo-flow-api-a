package com.example.mediumoboflowapia.userService

import io.github.serpro69.kfaker.Faker
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import java.util.*
import kotlin.random.Random

@Service
class UserService {
    fun getUsersByRole(authUser: OidcUser, userRole: UserRole): List<UserDto> {
        val users = generateUsers()
        return users.filter { it.userRole == userRole.value }
    }

    private fun generateUsers(): List<UserDto> {
        val faker = Faker()
        val users: ArrayList<UserDto> = arrayListOf()

        for (i in 1..45) {
            val userId = UUID.randomUUID().toString()
            val firstName = faker.name.firstName()
            val lastName = faker.name.lastName()
            val email = "$firstName.$lastName@email.com".lowercase(Locale.getDefault())
            val userRole = Random.nextInt(0, 3)

            val userDto = UserDto(userId, firstName, lastName, email, userRole)
            users.add(userDto)
        }

        return users
    }
}