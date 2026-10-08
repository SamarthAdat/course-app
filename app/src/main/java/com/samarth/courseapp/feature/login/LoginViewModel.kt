package com.samarth.courseapp.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samarth.courseapp.core.result.AppResult
import com.samarth.courseapp.domain.repository.AuthRepository
import com.samarth.courseapp.domain.validation.CredentialsValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Navigation is a one-shot event, not state: replaying it on configuration change would push
     * the dashboard twice. `extraBufferCapacity` keeps the emit non-suspending.
     */
    private val _loginSucceeded = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val loginSucceeded: Flow<Unit> = _loginSucceeded.asSharedFlow()

    fun onEmailChange(email: String) = _uiState.update {
        // Errors are cleared on edit so the user is not shouted at while fixing the problem.
        it.copy(email = email, emailError = null, submitError = null)
    }

    fun onPasswordChange(password: String) = _uiState.update {
        it.copy(password = password, passwordError = null, submitError = null)
    }

    fun onTogglePasswordVisibility() = _uiState.update {
        it.copy(isPasswordVisible = !it.isPasswordVisible)
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isSubmitting) return

        // Validate locally first: no point spending a round trip on an empty password.
        val emailError = CredentialsValidator.validateEmail(current.email)
        val passwordError = CredentialsValidator.validatePassword(current.password)
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError, submitError = null)
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            when (val result = authRepository.login(current.email.trim(), current.password)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, password = "") }
                    _loginSucceeded.tryEmit(Unit)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, submitError = result.error)
                }
            }
        }
    }
}
