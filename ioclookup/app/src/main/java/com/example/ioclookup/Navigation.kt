package com.example.ioclookup

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ioclookup.theme.*
import com.example.ioclookup.ui.bookmarks.BookmarksScreen
import com.example.ioclookup.ui.history.HistoryScreen
import com.example.ioclookup.ui.lookup.LookupScreen
import com.example.ioclookup.ui.settings.SettingsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ioclookup.ui.history.HistoryViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Lookup    : Screen("lookup",    "Lookup",    Icons.Filled.Search)
    object History   : Screen("history",   "History",   Icons.Filled.History)
    object Bookmarks : Screen("bookmarks", "Bookmarks", Icons.Filled.Bookmark)
    object Settings  : Screen("settings",  "Settings",  Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    Screen.Lookup,
    Screen.History,
    Screen.Bookmarks,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(settingsViewModel: com.example.ioclookup.ui.settings.SettingsViewModel) {
    val navController = rememberNavController()
    val historyViewModel: HistoryViewModel = hiltViewModel()
    val appColors = LocalAppColors.current

    Scaffold(
        containerColor = appColors.background,
        bottomBar = {
            NavigationBar(
                containerColor = appColors.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.label)
                        },
                        label = {
                            Text(screen.label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = appColors.accent,
                            selectedTextColor = appColors.accent,
                            unselectedIconColor = appColors.textMuted,
                            unselectedTextColor = appColors.textMuted,
                            indicatorColor = appColors.accent.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Lookup.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Lookup.route) {
                LookupScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onItemClick = { result ->
                        // Pre-fill the lookup screen with cached result
                        navController.navigate(Screen.Lookup.route) {
                            popUpTo(Screen.Lookup.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(onItemClick = {
                    navController.navigate(Screen.Lookup.route) {
                        popUpTo(Screen.Lookup.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onClearHistory = {
                        historyViewModel.clearAll()
                    }
                )
            }
        }
    }
}
