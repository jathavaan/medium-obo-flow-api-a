package com.example.mediumoboflowapia.userService

enum class UserRole(val value: Int) {
    User(0),
    Moderator(1),
    Admin(2);

    companion object {
        fun fromValue(value: Int): UserRole? {
            return entries.find { it.value == value }
        }
    }
}
