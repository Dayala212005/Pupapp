package com.pdm0126.puppapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pdm0126.puppapp.data.local.SessionManager
import com.pdm0126.puppapp.data.remote.KtorClient
import com.pdm0126.puppapp.navigation.PupappNavigation
import com.pdm0126.puppapp.ui.PupappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appProvider = (application as PupappApplication).appProvider
        val sessionManager = SessionManager(this)
        KtorClient.sessionManager = sessionManager
        val authRepository = appProvider.provideAuthRepository()
        KtorClient.onSessionExpired = { authRepository.logout() }

        enableEdgeToEdge()
        setContent {
            PupappTheme {
                PupappNavigation(sessionManager, appProvider.provideAuthRepository())
            }
        }
    }
}
