package com.samarth.courseapp.data.auth

import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Accepts one hard-coded demo account and rejects everything else, so both the success and the
 * failure path of the login screen are reachable in a demo.
 *
 * Deliberately *not* connectivity-gated: login has to work in the offline walkthrough, and gating
 * it would only prove that the network check works, which the course API already shows.
 */
class MockAuthApi @Inject constructor(
    private val ioDispatcher: CoroutineDispatcher,
) : AuthApi {

    override suspend fun login(email: String, password: String): String =
        withContext(ioDispatcher) {
            delay(LATENCY_MS)

            val matches = email.trim().equals(DEMO_EMAIL, ignoreCase = true) &&
                password == DEMO_PASSWORD
            if (!matches) throw InvalidCredentialsException()

            UUID.randomUUID().toString()
        }

    companion object {
        const val DEMO_EMAIL = "learner@example.com"
        const val DEMO_PASSWORD = "password123"
        private const val LATENCY_MS = 1_200L
    }
}
