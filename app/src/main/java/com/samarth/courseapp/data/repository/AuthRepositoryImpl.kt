package com.samarth.courseapp.data.repository

import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.data.auth.AuthApi
import com.samarth.courseapp.data.auth.InvalidCredentialsException
import com.samarth.courseapp.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

/**
 * Holds the session for the lifetime of the process only.
 *
 * That is a conscious choice, not an omission: persisting a bearer token correctly means the
 * Keystore-backed storage described in the README, and a half-done version (plain
 * SharedPreferences) would be worse than none. Restarting the app therefore returns to login.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
) : AuthRepository {

    /** Never exposed outside this class; the UI only learns whether login succeeded. */
    private var sessionToken: String? = null

    override suspend fun login(email: String, password: String): AppResult<Unit> = try {
        sessionToken = authApi.login(email, password)
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (rejected: InvalidCredentialsException) {
        AppResult.Failure(AppError.InvalidCredentials)
    } catch (unexpected: Exception) {
        AppResult.Failure(AppError.Unexpected(unexpected))
    }

    override fun logout() {
        sessionToken = null
    }
}
