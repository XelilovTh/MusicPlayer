# Nota

Nota is an offline-first Android music player. It reads audio files already stored on the device through Android MediaStore. It does not ship sample tracks, stream music, or require an account.

## Features

- Device music library using `READ_MEDIA_AUDIO` on Android 13+ and `READ_EXTERNAL_STORAGE` on Android 12 and earlier.
- Library, artist and album views, local search, album artwork, and user-created playlists.
- Playback with Media3 ExoPlayer, media-session notification, lock-screen controls, audio focus handling, shuffle, repeat and seeking.
- Device-supported equalizer, bass boost and virtualizer effects. Availability depends on the Android audio driver.
- Sleep timer, listening history and persistent favorites.
- Dark espresso and amber visual theme built with Jetpack Compose.

## Requirements

- Android Studio with JDK 17.
- Android SDK Platform 34 and corresponding build tools.
- Android 8.0 (API 26) or newer.

## Build and test

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Music permission

On first use, choose **Allow access** in the empty-library card. Nota requests only access to audio files, not broad file-management access. If permission is denied permanently, open Android App info → Permissions → Music and audio and allow access. Return to Nota and rescan the library.

The library is empty until the device has indexed supported local audio files. This is intentional: Nota never inserts demo or placeholder tracks.

## Project structure

- `data/scanner`: MediaStore audio query and permission checks.
- `data/repository`: local library and playlist data access.
- `playback` and `service`: Media3 playback controller and background media session.
- `audio`: device audio effects.
- `ui`: Compose screens, components and theme.

## Continuous integration

GitHub Actions runs unit tests and assembles a debug APK for pushed branches and pull requests. The APK is uploaded as a workflow artifact when the job succeeds.
