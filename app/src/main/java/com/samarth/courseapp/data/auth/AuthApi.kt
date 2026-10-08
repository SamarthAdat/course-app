package com.samarth.courseapp.data.auth

/**
 * The login endpoint, mocked.
 *
 * Kept behind an interface for the same reason as the course API: the real one is an HTTP call and
 * nothing outside this package should know the difference.
 */
interface AuthApi {

    /**
     * @return an opaque session token.
     * @throws InvalidCredentialsException when the backend rejects the pair.
     */
    suspend fun login(email: String, password: String): String
}

class InvalidCredentialsException : Exception("Email or password is incorrect")
