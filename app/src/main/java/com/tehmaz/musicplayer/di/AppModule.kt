package com.tehmaz.musicplayer.di

import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.data.repository.AudioRepositoryImpl
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import com.tehmaz.musicplayer.ui.screens.home.HomeViewModel
import com.tehmaz.musicplayer.ui.screens.library.LibraryViewModel
import com.tehmaz.musicplayer.ui.screens.player.NowPlayingViewModel
import com.tehmaz.musicplayer.ui.screens.playlist.PlaylistViewModel
import com.tehmaz.musicplayer.ui.screens.search.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Main application dependency injection module.
 * Provides AudioScanner, AudioRepository, AudioPlaybackManager, and Jetpack Compose ViewModels.
 */
val appModule = module {
    // Media scanner for querying local device audio
    single { AudioScanner(androidContext()) }

    // Repository providing local audio tracks with curated fallback catalog
    single<AudioRepository> { AudioRepositoryImpl(get(), androidContext()) }

    // Audio playback manager handling Media3 playback service and reactive StateFlow
    single { AudioPlaybackManager(androidContext()) }

    // Screen ViewModels
    viewModel { HomeViewModel(get(), get()) }
    viewModel { NowPlayingViewModel(get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { PlaylistViewModel(get(), get()) }
}
