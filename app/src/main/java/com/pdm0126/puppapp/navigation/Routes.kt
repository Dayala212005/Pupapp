package com.pdm0126.puppapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Login : Route
    
    @Serializable
    data object Register : Route
    
    @Serializable
    data object Orders : Route
}
