package org.fosser.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.fosser.app.di.AppContainer
import org.fosser.app.ui.details.DetailsScreen
import org.fosser.app.ui.history.HistoryScreen
import org.fosser.app.ui.home.HomeScreen
import org.fosser.app.ui.saved.SavedScreen
import org.fosser.app.ui.settings.SettingsScreen
import org.fosser.app.ui.webview.WebViewScreen

@Composable
fun FosserNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm = remember { container.homeViewModel() }
            HomeScreen(
                viewModel = vm,
                onOpenDetails = { pkg -> navController.navigate(Routes.details(pkg)) },
                onOpenSaved = { navController.navigate(Routes.SAVED) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenWebView = { url -> navController.navigate(Routes.webView(url)) },
            )
        }
        composable(
            Routes.DETAILS,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType }),
        ) { backStack ->
            val pkg = backStack.arguments?.getString("packageName").orEmpty()
            val vm = remember(pkg) { container.detailsViewModel() }
            DetailsScreen(
                packageName = pkg,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenWebView = { url -> navController.navigate(Routes.webView(url)) },
            )
        }
        composable(Routes.SAVED) {
            val vm = remember { container.savedViewModel() }
            SavedScreen(
                viewModel = vm,
                onOpenDetails = { pkg -> navController.navigate(Routes.details(pkg)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.HISTORY) {
            val vm = remember { container.historyViewModel() }
            HistoryScreen(
                viewModel = vm,
                onOpenDetails = { pkg -> navController.navigate(Routes.details(pkg)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            val vm = remember { container.settingsViewModel() }
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            Routes.WEB_VIEW,
            arguments = listOf(navArgument("url") { type = NavType.StringType }),
        ) { backStack ->
            val url = backStack.arguments?.getString("url").orEmpty()
            WebViewScreen(
                url = url,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
