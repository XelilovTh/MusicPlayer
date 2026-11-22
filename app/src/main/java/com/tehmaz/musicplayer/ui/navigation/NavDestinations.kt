package com.tehmaz.musicplayer.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Playlists : Screen("playlists")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    data object NowPlaying : Screen("now_playing")
    data object EQ : Screen("eq")
    data object SleepTimer : Screen("sleep_timer")
    data object Settings : Screen("settings")
}
