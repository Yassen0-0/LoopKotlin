package com.loop.app.ui.auth

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    var uiState by mutableStateOf(AuthUiState())
        private set

    private val listener = repository.addAuthStateListener { user ->
        uiState = uiState.copy(
            isCheckingAuth = false,
            isLoading = false,
            user = user,
        )
    }

    override fun onCleared() {
        repository.removeAuthStateListener(listener)
        super.onCleared()
    }

    fun clearMessages() {
        uiState = uiState.copy(errorMessage = null, infoMessage = null)
    }

    fun createAccount(email: String, password: String) {
        if (!validateEmailPassword(email, password)) return
        setLoading()
        repository.createAccount(email, password) { result ->
            applyAuthResult(result)
        }
    }

    fun signIn(email: String, password: String) {
        if (!validateEmailPassword(email, password)) return
        setLoading()
        repository.signIn(email, password) { result ->
            applyAuthResult(result)
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Enter your email first.", infoMessage = null)
            return
        }
        setLoading()
        repository.sendPasswordReset(email) { result ->
            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false,
                    errorMessage = null,
                    infoMessage = "Password reset email sent.",
                )
            } else {
                uiState.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message, infoMessage = null)
            }
        }
    }

    fun googleSignInIntent(): Intent? {
        return runCatching { repository.googleSignInIntent() }
            .onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Google Sign-In is not configured.",
                    infoMessage = null,
                )
            }
            .getOrNull()
    }

    fun handleGoogleSignInResult(data: Intent?) {
        setLoading()
        repository.signInWithGoogleResult(data) { result ->
            applyAuthResult(result)
        }
    }

    fun signOut() {
        repository.signOut()
        uiState = AuthUiState(isCheckingAuth = false)
    }

    private fun validateEmailPassword(email: String, password: String): Boolean {
        val message = when {
            email.isBlank() -> "Enter your email."
            password.length < 6 -> "Password must be at least 6 characters."
            else -> null
        }
        if (message != null) {
            uiState = uiState.copy(errorMessage = message, infoMessage = null)
            return false
        }
        return true
    }

    private fun setLoading() {
        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)
    }

    private fun applyAuthResult(result: Result<AuthUser>) {
        uiState = if (result.isSuccess) {
            uiState.copy(isLoading = false, user = result.getOrNull(), errorMessage = null, infoMessage = null)
        } else {
            uiState.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message, infoMessage = null)
        }
    }

    init {
        viewModelScope.launch {
            delay(3000)
            if (uiState.isCheckingAuth) {
                uiState = uiState.copy(
                    isCheckingAuth = false,
                    errorMessage = "Firebase Auth did not initialize. Check google-services.json and Firebase setup.",
                )
            }
        }
    }
}
