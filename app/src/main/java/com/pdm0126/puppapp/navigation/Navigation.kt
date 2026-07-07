package com.pdm0126.puppapp.navigation

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.pdm0126.puppapp.data.remote.KtorClient
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.puppapp.components.PupappBottomNav
import com.pdm0126.puppapp.data.local.SessionManager
import com.pdm0126.puppapp.screens.OrdersView.activeOrderView.ActiveOrdersScreen
import com.pdm0126.puppapp.screens.authorView.loginview.LoginScreen
import com.pdm0126.puppapp.screens.authorView.registrerView.RegisterScreen
import com.pdm0126.puppapp.screens.historyView.HistoryScreen
import com.pdm0126.puppapp.screens.menuView.MenuScreen
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.pdm0126.puppapp.data.remote.PupappAPI.AuthAPI
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PupappNavigation(sessionManager: SessionManager, authAPI: AuthAPI) {
    val scope = rememberCoroutineScope()
    // Se fuerza la recomposicion de todo la navegación y ViewModels cuando cambia el estado de la sesion.
    key(sessionManager.isLoggedIn) {
        val backStack: NavBackStack<NavKey> = rememberNavBackStack(
            if (sessionManager.isLoggedIn) Route.Orders as NavKey else Route.Login as NavKey
        )

        val pagerState = if (sessionManager.isLoggedIn) {
            rememberPagerState(pageCount = { 3 })
        } else null

        val currentRoute = backStack.lastOrNull()
        val showBars =
            currentRoute != null && currentRoute !is Route.Login && currentRoute !is Route.Register
        val businessName = sessionManager.getBusinessDisplayName() ?: "Mi Negocio"

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showBars) {
                    TopAppBar(
                        title = {
                            Column(
                                modifier = Modifier.padding(
                                    start = 12.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                )
                            ) {
                                Text(
                                    text = "Pupapp",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = businessName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                            }
                        },
                        navigationIcon = {},
                        actions = {
                            IconButton(onClick = {
                                scope.launch {
                                    authAPI.logout()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Cerrar sesión",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            },
            bottomBar = {
                if (showBars) {
                    if (pagerState != null) {
                        PupappBottomNav(
                            selectedIndex = pagerState.currentPage,
                            onItemSelected = { index ->
                                scope.launch {
                                    pagerState.scrollToPage(index)
                                }
                            }
                        )
                    } else {
                        val selectedIndex = when (currentRoute) {
                            is Route.Orders -> 0
                            is Route.Menu -> 1
                            is Route.History -> 2
                            else -> 0
                        }
                        PupappBottomNav(
                            selectedIndex = selectedIndex,
                            onItemSelected = { index ->
                                when (index) {
                                    0 -> {
                                        backStack.clear()
                                        backStack.add(Route.Orders)
                                    }
                                    1 -> {
                                        backStack.clear()
                                        backStack.add(Route.Menu)
                                    }
                                    2 -> {
                                        backStack.clear()
                                        backStack.add(Route.History)
                                    }
                                }
                            })
                    }
                }
            }
        ) { innerPadding ->
            val context = LocalContext.current
            val viewModelStore = remember { ViewModelStore() }
            val owner = remember(viewModelStore, context) {
                object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
                    override val viewModelStore: ViewModelStore = viewModelStore
                    override val defaultViewModelProviderFactory: ViewModelProvider.Factory =
                        (context.applicationContext as? Application)?.let {
                            ViewModelProvider.AndroidViewModelFactory.getInstance(it)
                        } ?: ViewModelProvider.NewInstanceFactory()

                    override val defaultViewModelCreationExtras: CreationExtras
                        get() = MutableCreationExtras().apply {
                            (context.applicationContext as? Application)?.let {
                                set(APPLICATION_KEY, it)
                            }
                        }
                }
            }
            DisposableEffect(viewModelStore) {
                onDispose { viewModelStore.clear() }
            }

            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                if (sessionManager.isLoggedIn && pagerState != null) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1
                    ) { page ->
                        when (page) {
                            0 -> ActiveOrdersScreen(padding = innerPadding)
                            1 -> MenuScreen(padding = innerPadding)
                            2 -> HistoryScreen(padding = innerPadding)
                        }
                    }
                } else {
                    val entryProvider = remember(innerPadding) {
                        entryProvider<NavKey> {
                            entry<Route.Login> {
                                LoginScreen(
                                    onNavigateToOrders = {
                                        // Solo navegamos si la sesión se ha iniciado correctamente
                                        if (sessionManager.isLoggedIn) {
                                            backStack.clear()
                                            backStack.add(Route.Orders)
                                        }
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
                        }
                    }

                    val entries = rememberDecoratedNavEntries(
                        backStack = backStack,
                        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                        entryProvider = entryProvider
                    )

                    NavDisplay(
                        entries = entries,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
