# ctom~player

Native Android music and video player built with Kotlin, Jetpack Compose, Material 3, and Android Media3.

## Open

Open the `ctom-player` directory in Android Studio. The project targets Android 8.0+ (API 26) and compiles against API 35.

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

Outputs:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

Media stays on-device. The app uses MediaStore for discovery and Android's MediaSession APIs for notification, lock-screen, Bluetooth, and headset controls.

## GitHub builds

The included GitHub Actions workflow provisions the Android SDK and builds both APKs on every push to `main`. Push a version tag such as `v1.0.0` to create a GitHub Release with both APK files attached.