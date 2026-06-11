package com.pdm0126.puppapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.pdm0126.puppapp.data.local.SessionManager
import com.pdm0126.puppapp.data.remote.KtorClient
import com.pdm0126.puppapp.ui.PupappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializamos el SessionManager y lo pasamos al KtorClient
        KtorClient.sessionManager = SessionManager(this)

        enableEdgeToEdge()
        setContent {
            PupappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                }
            }
        }
    }
}

