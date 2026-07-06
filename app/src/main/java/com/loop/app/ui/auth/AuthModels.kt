package com.loop.app.ui.auth

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
)

data class AuthUiState(
    val isCheckingAuth: Boolean = true,
    val isLoading: Boolean = false,
    val user: AuthUser? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
