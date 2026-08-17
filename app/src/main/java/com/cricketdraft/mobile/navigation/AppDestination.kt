package com.cricketdraft.mobile.navigation

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object Login : AppDestination("login")
    data object Tournaments : AppDestination("tournaments")
    data object TournamentDetail : AppDestination("tournaments/{slug}") {
        fun create(slug: String) = "tournaments/$slug"
    }
    data object LiveDrafts : AppDestination("live-drafts")
    data object MatchCenter : AppDestination("matches")
    data object Standings : AppDestination("standings")
    data object Reports : AppDestination("reports")
    data object Profile : AppDestination("profile")
    data object CaptainDraft : AppDestination("captain/draft")
    data object ScorerRoom : AppDestination("scorer/room")
    data object AdminWorkspace : AppDestination("admin/workspace")
    data object SuperAdmin : AppDestination("super-admin")
}

fun roleDestinations(roles: List<String>, permissions: List<String>): List<AppDestination> = buildList {
    add(AppDestination.Home)
    add(AppDestination.Tournaments)
    add(AppDestination.MatchCenter)
    add(AppDestination.Standings)
    if (roles.contains("captain") && permissions.contains("make draft pick")) add(AppDestination.CaptainDraft)
    if (permissions.contains("control draft")) add(AppDestination.ScorerRoom)
    if (permissions.contains("manage tournaments")) add(AppDestination.AdminWorkspace)
    if (permissions.contains("manage system")) add(AppDestination.SuperAdmin)
    add(AppDestination.Reports)
    add(AppDestination.Profile)
}
