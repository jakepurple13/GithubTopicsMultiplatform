package com.programmersbox.common

internal sealed class Screen(val route: String) {
    data object App : Screen("app")
    data object RepoReadMe : Screen("repoReadMe")
    data object Settings : Screen("settings")
    data object LibrariesUsed : Screen("librariesUsed")
    data object Favorites : Screen("favorites")
}