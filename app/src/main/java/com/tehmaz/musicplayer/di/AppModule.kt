package com.tehmaz.musicplayer.di

import com.tehmaz.musicplayer.audio.EqualizerController
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.data.repository.AudioRepositoryImpl
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import com.tehmaz.musicplayer.data.store.FavoritesStore
import com.tehmaz.musicplayer.data.store.HistoryStore
import com.tehmaz.musicplayer.data.store.KeyValueStore
import com.tehmaz.musicplayer.data.store.UserPlaylistStore
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import com.tehmaz.musicplayer.ui.components.SongActionsViewModel
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
 */
val appModule = module {
    // Persistence (favorites, user playlists, listening history)
    single { KeyValueStore(androidContext()) }
    single { FavoritesStore(get<KeyValueStore>()) }
    single { UserPlaylistStore(get<KeyValueStore>()) }
    single { HistoryStore(get<KeyValueStore>()) }

    // Real audio effects attached by PlaybackService
    single { EqualizerController(androidContext()) }

    // Media scanner for querying local device audio
    single { AudioScanner(androidContext()) }

    // Repository providing the user's local music (no placeholder catalog)
    single<AudioRepository> { AudioRepositoryImpl(get(), androidContext(), get(), get()) }

    // Playback manager wrapping the Media3 controller; records listening history
    single {
        val repository = get<AudioRepository>()
        AudioPlaybackManager(androidContext()).apply {
            onSongStarted = { song -> repository.recordPlayed(song.id) }
        }
    }

    // Screen ViewModels
    viewModel { SongActionsViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { NowPlayingViewModel(get(), get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { PlaylistViewModel(get(), get(), get()) }
}
