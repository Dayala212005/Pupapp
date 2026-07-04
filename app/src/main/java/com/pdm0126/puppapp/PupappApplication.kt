package com.pdm0126.puppapp

import android.app.Application
import com.pdm0126.puppapp.data.AppProvider

class PupappApplication : Application() {
    // Propiedad 'by lazy' tal como muestra tu guía
    val appProvider by lazy { AppProvider(this) }
}
