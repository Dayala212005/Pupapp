package com.pdm0126.puppapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.puppapp.data.local.SessionManager
import com.pdm0126.puppapp.screens.OrdersView.activeOrderView.ActiveOrdersScreen
import com.pdm0126.puppapp.screens.authorView.loginview.LoginScreen

@Composable
fun PupappNavigation(sessionManager: SessionManager) {
    //Forzamiento (Eliminar despues)
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(
        if (sessionManager.isLoggedIn) Route.Orders as NavKey else Route.Login as NavKey
    )

    val entryProvider = remember {
        entryProvider<NavKey> {
            entry<Route.Login> {
                LoginScreen(
                    onNavigateToOrders = {
                        backStack.clear()
                        backStack.add(Route.Orders)
                    },
                    onNavigateToRegister = {
                        backStack.add(Route.Register)
                    }
                )
            }
            entry<Route.Register> {
                // RegisterScreen(...)
                Box(Modifier.fillMaxSize())
            }
            entry<Route.Orders> {
                ActiveOrdersScreen(
                    onNewOrder = {
                        // Navegar a creación de orden si tienes esa ruta
                    },
                    onSelectTab = { _ ->
                        // Manejar cambio de tab si es necesario
                    },
                    onLogout = {
                        sessionManager.clearSession()
                    }
                )
            }
        }
    }

    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider
    )

    // Manejo de token (si expira el refesh vuelve al login)
    val isLoggedIn = sessionManager.isLoggedIn
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            backStack.clear()
            backStack.add(Route.Login)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            entries = entries,
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}
