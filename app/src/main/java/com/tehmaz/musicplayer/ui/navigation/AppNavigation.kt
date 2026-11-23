package com.tehmaz.musicplayer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import com.tehmaz.musicplayer.ui.components.FloatingBottomBar
import com.tehmaz.musicplayer.ui.components.MiniPlayerBar
import com.tehmaz.musicplayer.ui.components.NavTab
import com.tehmaz.musicplayer.ui.screens.eq.EQScreen
import com.tehmaz.musicplayer.ui.screens.home.HomeScreen
import com.tehmaz.musicplayer.ui.screens.library.LibraryScreen
import com.tehmaz.musicplayer.ui.screens.player.NowPlayingScreen
import com.tehmaz.musicplayer.ui.screens.playlist.PlaylistScreen
import com.tehmaz.musicplayer.ui.screens.search.SearchScreen
import com.tehmaz.musicplayer.ui.screens.settings.SettingsScreen
import com.tehmaz.musicplayer.ui.screens.sleeptimer.SleepTimerScreen
import com.tehmaz.musicplayer.ui.theme.DarkBackground
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Home.route,
        Screen.Playlists.route,
        Screen.Search.route,
        Screen.Library.route
    )

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToPlaylist = { playlistId ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                        },
                        onNavigateToLibrary = { navController.navigate(Screen.Library.route) }
                    )
                }

                composable(Screen.Playlists.route) {
                    // Playlists tab displays default curated playlist or overview
                    PlaylistScreen(
                        playlistId = -1L,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToPlaylist = { id -> navController.navigate(Screen.PlaylistDetail.createRoute(id)) }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen()
                }

                composable(Screen.Library.route) {
                    LibraryScreen()
                }

                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(
                        navArgument("playlistId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 1L
                    PlaylistScreen(
                        playlistId = playlistId,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToPlaylist = { id -> navController.navigate(Screen.PlaylistDetail.createRoute(id)) }
                    )
                }

                composable(Screen.NowPlaying.route) {
                    NowPlayingScreen(
                        onCollapse = { navController.popBackStack() },
                        onNavigateToEQ = { navController.navigate(Screen.EQ.route) },
                        onNavigateToSleepTimer = { navController.navigate(Screen.SleepTimer.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.EQ.route) {
                    EQScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.SleepTimer.route) {
                    SleepTimerScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // Floating Bottom Navigation Bar (visible on primary tabs)
            if (isTopLevelDestination) {
                FloatingBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            val showMiniPlayer = currentRoute !in listOf(
                Screen.NowPlaying.route,
                Screen.EQ.route,
                Screen.SleepTimer.route,
                Screen.Settings.route
            )
            if (showMiniPlayer) {
                GlobalMiniPlayer(
                    onOpenPlayer = { navController.navigate(Screen.NowPlaying.route) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isTopLevelDestination) 88.dp else 12.dp)
                )
            }
        }
    }
}

@Composable
private fun GlobalMiniPlayer(
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackManager: AudioPlaybackManager = koinInject()
    val playbackState by playbackManager.playbackState.collectAsState()
    val song = playbackState.currentSong ?: return

    MiniPlayerBar(
        currentSong = song,
        isPlaying = playbackState.isPlaying,
        progress = playbackState.progress,
        onPlayPauseClick = playbackManager::playPause,
        onNextClick = playbackManager::next,
        onBarClick = onOpenPlayer,
        modifier = modifier
    )
}
