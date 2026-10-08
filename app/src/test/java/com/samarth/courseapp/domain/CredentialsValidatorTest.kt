package com.samarth.courseapp.domain

import com.samarth.courseapp.domain.validation.CredentialsValidator
import com.samarth.courseapp.domain.validation.CredentialsValidator.EmailError
import com.samarth.courseapp.domain.validation.CredentialsValidator.PasswordError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialsValidatorTest {

    @Test
    fun `well formed emails are accepted`() {
        listOf(
            "learner@example.com",
            "first.last+tag@sub.example.co.in",
            "a_b-c%d@example-host.org",
            "  learner@example.com  ", // leading/trailing space is the user's, not an error
        ).forEach { email ->
            assertNull("expected $email to be valid", CredentialsValidator.validateEmail(email))
        }
    }

    @Test
    fun `malformed emails are rejected`() {
        listOf(
            "learner",
            "learner@",
            "@example.com",
            "learner@example",
            "learner@@example.com",
            "learner example@test.com",
        ).forEach { email ->
            assertEquals(
                "expected $email to be rejected",
                EmailError.Malformed,
                CredentialsValidator.validateEmail(email),
            )
        }
    }

    @Test
    fun `an empty email is reported as empty rather than malformed`() {
        // Different message: "enter your email" is more useful than "that is not an email".
        assertEquals(EmailError.Empty, CredentialsValidator.validateEmail(""))
        assertEquals(EmailError.Empty, CredentialsValidator.validateEmail("   "))
    }

    @Test
    fun `passwords shorter than the minimum are rejected`() {
        assertEquals(PasswordError.Empty, CredentialsValidator.validatePassword(""))
        assertEquals(PasswordError.TooShort, CredentialsValidator.validatePassword("12345"))
        assertNull(CredentialsValidator.validatePassword("123456"))
    }
}
