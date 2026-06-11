package com.pdm0126.puppapp

import android.app.Application
import com.pdm0126.puppapp.data.local.SessionManager
import com.pdm0126.puppapp.data.remote.KtorClient

class PupApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializamos el SessionManager y lo pasamos al KtorClient
        KtorClient.sessionManager = SessionManager(this)
    }
}
