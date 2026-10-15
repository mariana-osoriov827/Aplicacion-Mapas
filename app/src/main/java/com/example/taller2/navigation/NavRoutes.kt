package com.example.taller2.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Routes : NavKey {
    @Serializable data object Home : Routes()
}
