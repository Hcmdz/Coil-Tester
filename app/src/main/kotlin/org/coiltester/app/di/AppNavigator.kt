package org.coiltester.app.di

import androidx.navigation.NavController

/**
 * Type-safe navigation wrapper
 */
class AppNavigator {
    private var navController: NavController? = null

    fun setNavController(controller: NavController) {
        navController = controller
    }

    fun navigate(route: AppRoute) {
        navController?.navigate(route.toRouteString())
    }

    fun navigateUp() {
        navController?.navigateUp()
    }

    fun popBackStack() {
        navController?.popBackStack()
    }
}

/**
 * Type-safe route serialization
 */
sealed interface AppRoute {
    data object Detection : AppRoute

    data object Settings : AppRoute

    data object About : AppRoute

fun toRouteString(): String {
        // codeql[java/local-variable-is-never-read]
        return when (this) {
            is Detection -> "detection"
            is Settings -> "settings"
            is About -> "about"
        }
    }

    companion object {
        fun fromRouteString(route: String): AppRoute? {
            return when {
                route == "detection" -> Detection
                route == "settings" -> Settings
                route == "about" -> About
                else -> null
            }
        }
    }
}
