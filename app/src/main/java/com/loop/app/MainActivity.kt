package com.loop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.loop.app.ui.LoopApp
import com.loop.app.ui.auth.AuthGate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuthGate { user, onLogout ->
                LoopApp(userId = user.uid, onLogout = onLogout)
            }
        }
    }
}
