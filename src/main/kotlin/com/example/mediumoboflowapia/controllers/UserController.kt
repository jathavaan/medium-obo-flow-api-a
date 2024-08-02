package com.example.mediumoboflowapia.controllers

import com.example.mediumoboflowapia.userService.UserRole
import com.example.mediumoboflowapia.userService.UserService
import com.example.mediumoboflowapia.userService.UserVm
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{userRole}")
    fun getUsersByRole(
        @PathVariable userRole: Int
    ): ResponseEntity<List<UserVm>> {
        val role = UserRole.fromValue(userRole) ?: return ResponseEntity.ok(emptyList())
        val users = userService.getUsersByRole(role)
        val userVms = users.map { user ->
            UserVm(user.userId, user.firstName, user.surname, user.email)
        }

        return ResponseEntity.ok(userVms)
    }
}