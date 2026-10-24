# Easy TV Launcher for Android TV

A lightweight, distraction-free, senior- & family-friendly launcher for Android TV, Google TV, and TV-boxes.

## Features
- **Zero Ads & Zero Recommendations**: Fast and clean.
- **High-Visibility D-Pad Focus**: Glowing amber/yellow borders and scale zoom on active tiles.
- **Senior / XL Tile Mode**: Extra-large tiles and bold typography for effortless readability from 10+ feet away.
- **Favorites & App Hiding**: Pin most-used streaming apps to the top row; hide carrier bloatware.
- **Caregiver PIN Lock**: Protect settings and app uninstallation behind a 4-digit PIN.
- **TV Hardware Controls**: Quick tiles for switching HDMI inputs, sleep timer, and system settings.
- **Real-Time App Sync**: Instantly detects newly installed or removed apps via `BroadcastReceiver`.

---

## Project Structure
```text
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # TV Leanback & Home intent declarations
│   │   ├── java/com/easytv/launcher/
│   │   │   ├── MainActivity.kt          # Main TV activity with back-key guard
│   │   │   ├── model/AppItem.kt         # Data models
│   │   │   ├── data/
│   │   │   │   ├── AppRepository.kt     # Scans Leanback & standard apps
│   │   │   │   └── LauncherPreferences.kt # DataStore for favorites, PIN, tile size
│   │   │   ├── viewmodel/MainViewModel.kt # State management
│   │   │   ├── ui/
│   │   │   │   ├── theme/               # High-contrast colors & TV typography
│   │   │   │   ├── components/          # TopStatusBar, AppCard, ActionCard, PinLockDialog
│   │   │   │   └── screens/HomeScreen.kt# Main TV layout
│   │   │   └── receiver/PackageReceiver.kt # Detects package additions/removals
│   │   └── res/                         # TV banner, app icon, strings, styles
│   └── build.gradle.kts                 # Jetpack Compose for TV dependencies
├── settings.gradle.kts
└── build.gradle.kts
```

---

## How to Install on Your TV

### Step 1: Enable "Install Unknown Apps" on Your TV
Before sideloading, allow your TV to install third-party apps:
- **Android TV / Google TV:** **Settings** > **Apps** > **Security & Restrictions** (or **Special app access**) > **Install unknown apps** -> Turn ON for your File Manager or Downloader app.
- **Fire TV / Fire Stick:** **Settings** > **My Fire TV** > **Developer Options** > **Install unknown apps** -> Turn ON for your File Manager.
*(Note: If Developer Options is hidden, go to **Settings > About**, and click **Build** 7 times to reveal it).*

---

### Step 2: Install the Release APK

#### Method A: USB Thumb Drive (No Computer Needed)
1. Copy [EasyTVLauncher-v1.0.0-release.apk](file:///e:/PlayGround/TV/EasyTVLauncher-v1.0.0-release.apk) onto a USB drive.
2. Plug the USB into your TV.
3. Open any file manager on your TV (e.g., *File Commander*), select the APK, and click **Install**.

#### Method B: Wireless ADB (Over Wi-Fi)
```bash
# Connect to your Android TV wirelessly (enable Network Debugging in TV Developer Options)
adb connect 192.168.1.50:5555

# Install the release APK
adb install -r EasyTVLauncher-v1.0.0-release.apk

# (Optional) Set as default launcher via ADB
adb shell cmd package set-home-activity com.easytv.launcher/.MainActivity
```
