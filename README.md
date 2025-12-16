# AuroraShield Mobile

Privacy-focused browser for Android using Capacitor.

## Prerequisites

1. **Node.js** (v18+)
2. **Android Studio** dengan:
   - Android SDK
   - Android SDK Build-Tools
   - Android Emulator (optional)
3. **Java JDK 17**

## Quick Start

### 1. Install Dependencies
```bash
cd AuroraShield-Mobile
npm install
```

### 2. Build Web Assets
```bash
npm run build
```

### 3. Add Android Platform
```bash
npx cap add android
```

### 4. Sync & Open Android Studio
```bash
npx cap sync
npx cap open android
```

### 5. Build APK

**Di Android Studio:**
- Build → Build Bundle(s) / APK(s) → Build APK(s)
- APK akan ada di: `android/app/build/outputs/apk/debug/app-debug.apk`

**Atau via command line:**
```bash
cd android
./gradlew assembleDebug
```

## Development

### Preview di Browser
```bash
npm run dev
```
Buka http://localhost:3000

### Live Reload di Android
```bash
npx cap run android -l --external
```

## Build Release APK

1. Generate keystore:
```bash
keytool -genkey -v -keystore aurorashield.keystore -alias aurorashield -keyalg RSA -keysize 2048 -validity 10000
```

2. Build release:
```bash
cd android
./gradlew assembleRelease
```

## Features

- 🛡️ Ad & Tracker Blocking (500+ domains)
- 🚫 Popup & Redirect Blocking
- 🔒 HTTPS Security Indicator
- 📱 Tab Management
- ⚙️ Settings & Whitelist
- 🎨 Dark Theme

## Project Structure

```
AuroraShield-Mobile/
├── src/
│   ├── index.html      # Main HTML
│   ├── css/styles.css  # Styles
│   ├── js/
│   │   ├── app.js           # Main app logic
│   │   └── shields-engine.js # Ad blocking engine
│   └── assets/         # Icons, images
├── dist/               # Built files (generated)
├── android/            # Android project (generated)
├── package.json
├── capacitor.config.json
└── vite.config.js
```
