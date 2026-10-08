package com.samarth.courseapp.feature.login

import app.cash.turbine.test
import com.samarth.courseapp.core.result.AppError
import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.domain.repository.AuthRepository
import com.samarth.courseapp.domain.validation.CredentialsValidator
import com.samarth.courseapp.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = RecordingAuthRepository()

    @Test
    fun `invalid input is rejected locally without calling the auth api`() = runTest {
        val viewModel = LoginViewModel(authRepository)

        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("123")
        viewModel.onSubmit()

        val state = viewModel.uiState.value
        assertEquals(CredentialsValidator.EmailError.Malformed, state.emailError)
        assertEquals(CredentialsValidator.PasswordError.TooShort, state.passwordError)
        assertFalse(state.isSubmitting)
        assertEquals("a round trip must not be spent on input we know is invalid", 0, authRepository.loginCallCount)
    }

    @Test
    fun `editing a field clears the error so the user is not shouted at mid-fix`() = runTest {
        val viewModel = LoginViewModel(authRepository)

        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onSubmit()
        assertEquals(CredentialsValidator.EmailError.Empty, viewModel.uiState.value.emailError)

        viewModel.onEmailChange("l")
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `rejected credentials end the loading state and expose the error`() = runTest {
        authRepository.result = AppResult.Failure(AppError.InvalidCredentials)
        val viewModel = LoginViewModel(authRepository)

        viewModel.onEmailChange("learner@example.com")
        viewModel.onPasswordChange("wrong-password")
        viewModel.onSubmit()

        val state = viewModel.uiState.value
        assertFalse("the button must become usable again after a failure", state.isSubmitting)
        assertTrue(state.isSubmitEnabled)
        assertEquals(AppError.InvalidCredentials, state.submitError)
    }

    @Test
    fun `a successful login emits the navigation event once and clears the password`() = runTest {
        val viewModel = LoginViewModel(authRepository)

        // Subscribed before submitting: the event is a one-shot SharedFlow with no replay, so a
        // late collector would legitimately miss it (and so would a rotated screen).
        viewModel.loginSucceeded.test {
            viewModel.onEmailChange(" learner@example.com ")
            viewModel.onPasswordChange("password123")
            viewModel.onSubmit()

            awaitItem()
            expectNoEvents()
        }

        assertEquals(
            "the email is trimmed before it reaches the api",
            "learner@example.com",
            authRepository.lastEmail,
        )
        assertEquals("the password is dropped from state once it is no longer needed", "", viewModel.uiState.value.password)
    }

    private class RecordingAuthRepository : AuthRepository {

        var result: AppResult<Unit> = AppResult.Success(Unit)
        var loginCallCount = 0
            private set
        var lastEmail: String? = null
            private set

        override suspend fun login(email: String, password: String): AppResult<Unit> {
            loginCallCount++
            lastEmail = email
            return result
        }

        override fun logout() = Unit
    }
}
