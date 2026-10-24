# Android Easy TV Launcher — Project Plan & Feature Catalogue

## 1. Vision & Core Value Proposition
A lightweight, fast, and distraction-free Android TV launcher built for seniors, kids, minimalists, and kiosk setups.
- **Zero Ads / Bloatware**: No sponsored rows or content recommendations.
- **High-Contrast, Large-Tile UI**: Easy to read from 10+ feet away.
- **Fail-Safe Operation**: Caregiver PIN lock prevents accidental app deletion or settings tampering.
- **Instant Remote (D-Pad) Responsiveness**: Uncluttered linear navigation with clear glowing focus rings.

---

## 2. Feature Catalogue

### Core Navigation & Experience
1. **High-Visibility Focus Indicator**: Glowing/accent border and subtle zoom scaling so the user never loses cursor position.
2. **Simplified Layout Options**:
   - *Senior/Simple Mode*: 4 to 6 extra-large tiles per screen.
   - *Standard Grid*: 2 rows of 5 tiles with customizable favorites.
3. **App Pinning & Reordering**: D-Pad long-press to pin, reorder, or hide apps.
4. **App Name Simplification**: Rename complex package labels (e.g. rename obscure IPTV app to "Live Channels").

### Accessibility & "Easy" Controls
5. **Caregiver / Child PIN Lock**: Lock system settings, Google Play Store, and uninstall functions behind a 4-digit code.
6. **Glanceable Status Header**: Oversized digital clock, date, clear Wi-Fi signal icon, and basic weather.
7. **Audio Cues**: Optional gentle click feedback when moving between tiles.
8. **Sleep Timer**: One-click 30/60/90 minute TV sleep timer tile.

### Hardware & TV Controls
9. **Direct Input Switcher**: Quick tiles for `HDMI 1`, `HDMI 2`, `AV`, and `USB Media`.
10. **Auto-Launch on Boot**: Option to automatically start a default app (e.g., Live TV) upon TV boot.
11. **Backup / Restore**: Export launcher configuration to a USB thumb drive.

---

## 3. Recommended Technical Stack
- **Language**: Kotlin
- **UI Framework**: **Jetpack Compose for TV** (`androidx.tv.tv-foundation`, `androidx.tv.tv-material`)
- **Storage**: Jetpack DataStore (Preferences) + Room (Ordered apps and categories)
- **Target OS**: Android 8.0 (API 26) through Android 14+ (API 34+)
- **Manifest Requirements**:
  - `android.intent.category.HOME`
  - `android.intent.category.LEANBACK_LAUNCHER`
  - `android.intent.category.DEFAULT`

---

## 4. Development Roadmap

| Phase | Milestone | Deliverables |
| :--- | :--- | :--- |
| **Phase 1** | **Core Shell (MVP)** | Android TV project setup, Compose TV grid, `PackageManager` query, D-pad navigation, launch intent |
| **Phase 2** | **Simplicity & Customization** | Pinned favorites dock, hide bloat apps, XL tile sizing, high-contrast themes |
| **Phase 3** | **Lock & Utilities** | Caregiver PIN lock, status bar (Clock/Wi-Fi/Weather), HDMI input tiles |
| **Phase 4** | **Polish & Packaging** | Remote button sound effects, USB backup/restore, Leanback compliance & release APK |
