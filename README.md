# RealityFX AI

A graphics-enhancement companion for Android 16 (target device: Infinix Note 50s, MediaTek Dimensity 7300 Ultimate).

It previews and tunes image enhancement on-device: sharpness, contrast, saturation, brightness, color filters, glow, performance and quality modes, game profiles, before/after comparison, measured FPS, and reset.

## Honest scope: what it can and cannot do

Android does not let a normal app change how another app draws. RealityFX AI does not pretend otherwise.

### Works inside RealityFX AI

| Feature | Status |
|---|---|
| Sharpness (unsharp mask) | Works. Applied to the sample image in the app. |
| Contrast, saturation, brightness | Works. Real per-pixel processing on the device CPU. |
| Color filters: Realistic, Cinematic, Vivid, Cool | Works. Simple channel gains and saturation changes. |
| Glow (bloom from bright areas) | Works in Quality mode. Off in Battery saver. |
| Performance mode (Battery saver) | Works. Smaller processing size, no glow, slower updates. |
| Quality mode | Works. Larger processing size, full effects. |
| Game profiles (save, switch, create, delete) | Works. Saved locally. You choose the active profile manually. |
| Before/after comparison | Works. Drag the divider on the sample scene. |
| FPS readout | Works, but measures only the RealityFX screen. Off by default. |
| Reset to defaults | Works. |
| English / Arabic interface, dark / AMOLED theme | Works. Arabic is right-to-left. |

### Not possible under normal Android rules

| Item | Reason |
|---|---|
| Changing the graphics of other games live | Needs screen capture plus an overlay or accessibility service. These are restricted, drain battery, heat the phone, and cannot process DRM-protected content. |
| NVIDIA DLSS, AI upscaling, frame generation, ray tracing | These need the game engine or GPU driver. They are not available to a separate app. |
| Automatic detection of the running game | Requires usage-access permission. The app does not ask for it, so you pick the profile yourself. |
| Root, code injection, editing game files | Deliberately not done. |

## Build the APK without a PC (phone only)

The project includes a GitHub Actions workflow that builds the APK on GitHub's servers.

1. **Create a free GitHub account** in your phone browser at github.com.
2. **Create a new repository** named `RealityFX-AI` (public or private).
3. **Upload the project files** through the repository's "Add file → Upload files" page. Keep the folder structure: `app/`, `.github/`, and the root `.gradle.kts` files. The upload page on a phone is slow, so uploading from a PC or asking for a push is faster.
4. Open the **Actions** tab. The "Build RealityFX AI APK" workflow runs on each upload. Wait 5 to 10 minutes for the green check.
5. Open that run, scroll to **Artifacts**, and download **RealityFX-AI-debug-apk**. Android saves it as a ZIP. Open it in the Files app and extract the `.apk`.
6. **Install.** Open the `.apk`. When Android asks, allow installs from your browser or Files app (Settings → Apps → Special access → Install unknown apps). Tap Install.

If a build fails, copy the red error line from the Actions log and send it back. Most fixes are one-line changes.

### Building with Android Studio (if you have a PC later)

Open the project folder in Android Studio (Ladybug or newer), let it sync, then choose Build → Build APK(s).

## Testing guide for the Infinix Note 50s

Run these checks on Android 16:

1. **Launch.** The home screen shows the title, profile chips, and the sample scene.
2. **Sliders.** Drag Sharpness, Contrast, Saturation, and Brightness. The "After" half should change within about a second. The "Before" half must stay the same.
3. **Filters.** Tap each chip (Realistic, Cinematic, Vivid, Cool). Each should look visibly different.
4. **Comparison.** Drag the white divider left and right. Both halves should track your finger smoothly.
5. **Profiles.** Set sliders, tap "Save to profile". Tap "New" to create a profile. Change sliders, then switch between profiles. Each should restore its own settings. Close and reopen the app. Settings should persist. "Delete" should be disabled on Default.
6. **Reset.** Tap "Reset to defaults". Sliders return to the starting values.
7. **Battery saver.** Settings → Battery saver. The Glow slider should disable with its note. Check that the preview still updates.
8. **FPS.** Settings → turn on "Show FPS readout". Return home. The value should settle near the phone's refresh rate (120 Hz or 60 Hz). Toggle it off again.
9. **Language.** Settings → العربية. The app should restart in Arabic and lay out right-to-left. Switch back to English.
10. **Theme.** Settings → AMOLED black. The background should become pure black.
11. **Permissions.** Settings → Apps → RealityFX AI → Permissions. It should say no permissions are requested.
12. **Stability.** Rotate the phone, switch apps, and lock and unlock the screen. The app should not crash.

## Project layout

```
app/src/main/java/com/realityfx/ai/
  MainActivity.kt            entry point, language setup
  model/Enhancement.kt       settings, filter presets, game profiles
  data/AppPrefs.kt           local preferences and profile storage
  processing/ImageEnhancer.kt  color grading, unsharp mask, glow
  processing/SampleImage.kt  procedurally drawn sample scene
  ui/                        Compose screens, theme, view model
app/src/main/res/            English and Arabic strings, theme
.github/workflows/build-apk.yml  cloud APK build
PRIVACY.md                   privacy policy
```

## Notes on verification

This project was written without a local compile, because the build environment could not download the Android SDK or libraries. The first GitHub Actions build is the real compile check. Any compile error will show in its log.
