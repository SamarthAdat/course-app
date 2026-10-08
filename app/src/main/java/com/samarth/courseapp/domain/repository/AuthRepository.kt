package com.samarth.courseapp.domain.repository

import com.samarth.courseapp.core.result.AppResult

interface AuthRepository {

    suspend fun login(email: String, password: String): AppResult<Unit>

    fun logout()
}
