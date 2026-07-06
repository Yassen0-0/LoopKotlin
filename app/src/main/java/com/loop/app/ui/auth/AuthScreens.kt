package com.loop.app.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.loop.app.ui.components.FullScreenSurface
import com.loop.app.ui.components.LoopPrimaryButton
import com.loop.app.ui.theme.LoopRadius
import com.loop.app.ui.theme.LoopTheme

@Composable
fun AuthGate(
    viewModel: AuthViewModel = viewModel(),
    content: @Composable (AuthUser, () -> Unit) -> Unit,
) {
    val uiState = viewModel.uiState
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    when {
        uiState.isCheckingAuth -> AuthLoadingScreen()
        uiState.user == null -> AuthScreen(
            uiState = uiState,
            onLogin = viewModel::signIn,
            onSignup = viewModel::createAccount,
            onForgotPassword = viewModel::sendPasswordReset,
            onGoogle = {
                val intent = viewModel.googleSignInIntent()
                if (intent != null) {
                    googleLauncher.launch(intent)
                }
            },
            onDismissMessage = viewModel::clearMessages,
        )
        else -> key(uiState.user.uid) {
            content(uiState.user, viewModel::signOut)
        }
    }
}

@Composable
private fun AuthLoadingScreen() {
    LoopTheme(darkTheme = isSystemInDarkTheme()) {
        FullScreenSurface {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CircularProgressIndicator()
                    Text("Checking account", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AuthScreen(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onSignup: (String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onGoogle: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    var mode by remember { mutableStateOf(AuthMode.Login) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LoopTheme(darkTheme = isSystemInDarkTheme()) {
        FullScreenSurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(LoopRadius.xl),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = if (mode == AuthMode.Login) "Sign in to Loop" else "Create your Loop account",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "Firebase login is required before opening your planner, habits, journal, goals, and reviews.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        AuthTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            icon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                            keyboardType = KeyboardType.Email,
                            enabled = !uiState.isLoading,
                        )
                        AuthTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            icon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            keyboardType = KeyboardType.Password,
                            password = true,
                            enabled = !uiState.isLoading,
                        )

                        LoopPrimaryButton(
                            label = when {
                                uiState.isLoading -> "Please wait..."
                                mode == AuthMode.Login -> "Sign in"
                                else -> "Create account"
                            },
                            onClick = {
                                if (mode == AuthMode.Login) onLogin(email, password) else onSignup(email, password)
                            },
                            enabled = !uiState.isLoading,
                        )

                        OutlinedButton(
                            onClick = onGoogle,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(LoopRadius.pill),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (mode == AuthMode.Login) "Create account" else "I already have an account",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                                    mode = if (mode == AuthMode.Login) AuthMode.Signup else AuthMode.Login
                                    onDismissMessage()
                                },
                            )
                            TextButton(
                                onClick = { onForgotPassword(email) },
                                enabled = !uiState.isLoading,
                            ) {
                                Text("Forgot password?")
                            }
                        }

                        Text(
                            text = "Android may ask you to choose the Google account or allow network access. No offline or fake login is available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                        )
                    }
                }
            }

            val dialogText = uiState.errorMessage ?: uiState.infoMessage
            if (dialogText != null) {
                AlertDialog(
                    onDismissRequest = onDismissMessage,
                    confirmButton = {
                        TextButton(onClick = onDismissMessage) {
                            Text("OK")
                        }
                    },
                    title = { Text(if (uiState.errorMessage != null) "Account error" else "Account") },
                    text = { Text(dialogText) },
                )
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    enabled: Boolean,
    password: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        leadingIcon = icon,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(LoopRadius.lg),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

private enum class AuthMode {
    Login,
    Signup,
}
