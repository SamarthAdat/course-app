package com.samarth.courseapp.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samarth.courseapp.R
import com.samarth.courseapp.data.auth.MockAuthApi
import com.samarth.courseapp.domain.validation.CredentialsValidator
import com.samarth.courseapp.ui.toMessageRes

/**
 * Stateful entry point: owns the ViewModel and the navigation callback.
 *
 * [LoginScreen] below is stateless, which keeps it previewable and testable without Hilt.
 */
@Composable
fun LoginRoute(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One-shot event rather than state, so rotating the device cannot re-trigger navigation.
    LaunchedEffect(viewModel) {
        viewModel.loginSucceeded.collect { onLoggedIn() }
    }

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onSubmit = viewModel::onSubmit,
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    fun submit() {
        keyboardController?.hide()
        onSubmit()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.login_email_label)) },
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { error ->
                    { Text(error.message()) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.login_password_label)) },
                singleLine = true,
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { error ->
                    { Text(error.message()) }
                },
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(onClick = onTogglePasswordVisibility) {
                        Text(
                            stringResource(
                                if (uiState.isPasswordVisible) {
                                    R.string.login_hide_password
                                } else {
                                    R.string.login_show_password
                                }
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.submitError != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(uiState.submitError.toMessageRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { submit() },
                enabled = uiState.isSubmitEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.login_submit))
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(
                    R.string.login_demo_hint,
                    MockAuthApi.DEMO_EMAIL,
                    MockAuthApi.DEMO_PASSWORD,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun CredentialsValidator.EmailError.message(): String = stringResource(
    when (this) {
        CredentialsValidator.EmailError.Empty -> R.string.login_error_email_empty
        CredentialsValidator.EmailError.Malformed -> R.string.login_error_email_malformed
    }
)

@Composable
private fun CredentialsValidator.PasswordError.message(): String = when (this) {
    CredentialsValidator.PasswordError.Empty ->
        stringResource(R.string.login_error_password_empty)

    CredentialsValidator.PasswordError.TooShort -> stringResource(
        R.string.login_error_password_too_short,
        CredentialsValidator.MIN_PASSWORD_LENGTH,
    )
}
