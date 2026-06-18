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
import com.pdm0126.puppapp.screens.OrdersView.newOrderView.NewOrderScreen
import com.pdm0126.puppapp.screens.authorView.loginview.LoginScreen
import com.pdm0126.puppapp.screens.authorView.registrerView.RegisterScreen
import com.pdm0126.puppapp.screens.historyView.HistoryScreen
import com.pdm0126.puppapp.screens.menuView.MenuScreen

@Composable
fun PupappNavigation(sessionManager: SessionManager) {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(
        if (sessionManager.isLoggedIn) Route.Orders as NavKey else Route.Login as NavKey
    )

    fun handleTabSelection(index: Int) {
        when (index) {
            0 -> { backStack.clear(); backStack.add(Route.Orders) }
            1 -> { backStack.clear(); backStack.add(Route.NewOrder) }
            2 -> { backStack.clear(); backStack.add(Route.Menu) }
            3 -> { backStack.clear(); backStack.add(Route.History) }
        }
    }

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
                RegisterScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<Route.Orders> {
                ActiveOrdersScreen(
                    onNewOrder = { backStack.add(Route.NewOrder) },
                    onSelectTab = { handleTabSelection(it) },
                    onLogout = {
                        sessionManager.clearSession()
                    }
                )
            }
            entry<Route.NewOrder> {
                NewOrderScreen(
                    onNavigateBack = {
                        backStack.clear()
                        backStack.add(Route.Orders)
                    }
                )
            }
            entry<Route.Menu> {
                MenuScreen(
                    onSelectTab = { handleTabSelection(it) }
                )
            }
            entry<Route.History> {
                HistoryScreen(
                    onSelectTab = { handleTabSelection(it) }
                )
            }
        }
    }

    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider
    )

    val isLoggedIn = sessionManager.isLoggedIn
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            backStack.clear()
            backStack.add(Route.Login)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            entries  = entries,
            onBack   = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}
