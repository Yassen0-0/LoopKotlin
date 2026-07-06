package com.loop.app.ui.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.loop.app.R

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val googleClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(appContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(appContext, options)
    }

    fun addAuthStateListener(onChanged: (AuthUser?) -> Unit): FirebaseAuth.AuthStateListener {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            onChanged(auth.currentUser.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        return listener
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth.removeAuthStateListener(listener)
    }

    fun createAccount(email: String, password: String, onResult: (Result<AuthUser>) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(firebaseAuth.currentUser.toRequiredAuthUser())
                } else {
                    onResult(Result.failure(task.exception.toUserFacingAuthException()))
                }
            }
    }

    fun signIn(email: String, password: String, onResult: (Result<AuthUser>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(firebaseAuth.currentUser.toRequiredAuthUser())
                } else {
                    onResult(Result.failure(task.exception.toUserFacingAuthException()))
                }
            }
    }

    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(Result.failure(task.exception.toUserFacingAuthException()))
                }
            }
    }

    fun googleSignInIntent(): Intent = googleClient.signInIntent

    fun signInWithGoogleResult(data: Intent?, onResult: (Result<AuthUser>) -> Unit) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnCompleteListener { accountTask ->
                if (!accountTask.isSuccessful) {
                    val message = (accountTask.exception as? ApiException)?.statusCode?.let {
                        "Google Sign-In failed. Status code: $it"
                    } ?: "Google Sign-In was cancelled or failed."
                    onResult(Result.failure(IllegalStateException(message)))
                    return@addOnCompleteListener
                }

                val account = accountTask.result
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    onResult(Result.failure(IllegalStateException("Google Sign-In did not return an ID token. Check Firebase web client configuration.")))
                    return@addOnCompleteListener
                }

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            onResult(firebaseAuth.currentUser.toRequiredAuthUser())
                        } else {
                            onResult(Result.failure(authTask.exception.toUserFacingAuthException()))
                        }
                    }
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleClient.signOut()
    }

    private fun FirebaseUser?.toAuthUser(): AuthUser? = this?.let {
        AuthUser(uid = uid, email = email, displayName = displayName)
    }

    private fun FirebaseUser?.toRequiredAuthUser(): Result<AuthUser> {
        val user = toAuthUser()
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(IllegalStateException("Firebase did not return a signed-in user."))
        }
    }

    private fun Throwable?.toUserFacingAuthException(): Throwable {
        val message = when ((this as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email already has an account."
            "ERROR_WEAK_PASSWORD" -> "Use a stronger password with at least 6 characters."
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Email or password is incorrect."
            "ERROR_USER_NOT_FOUND" -> "No account exists for this email."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection and try again."
            else -> this?.localizedMessage ?: "Authentication failed. Try again."
        }
        return IllegalStateException(message, this)
    }
}
