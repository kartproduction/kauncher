# Kauncher

A calm, minimalist home screen for people who make things. See the time, write down what you're making today, start a focus session, and reach your creative tools in one tap. Everything else stays out of the way.

By [Kart Production](https://kartproduction.github.io/kart/).

**Official download (the only one):** https://kartproduction.github.io/kauncher/

> **Please read.** Copies from other websites, chats or app stores are not from us. They may be modified or unsafe. Kart Production will never ask you for money, passwords, codes or personal details through Kauncher.
>
> **View-only source.** This repository is public so you can see how Kauncher was made. The code is not free to copy, repackage, or republish. See [License](#license).

---

## What it does

- **Daily focus line:** type what you are making today, then start a focus session. The session length is chosen automatically (30 or 60 minutes), and you can change the lengths in Kettings. While a session is running, the Back button does nothing so it can't reset your session by accident.
- **Creative shortcuts:** Krite and Ketch open Kotepad for notes and sketches, Kord records your voice, Kapture takes photos, and there is a call shortcut.
- **Full launcher (Android):** swipe up for all your apps, swipe down to come back home. Long-press an app to see its info or uninstall it.
- **Folders and backup:** group apps into folders with their own icons. Back up your folders as a text code and paste it back after a reinstall.
- **Kettings:** focus lengths, light or dark theme, clock style, backup and restore, and a shortcut to your phone's own settings.
- **Games:** Tic-Tac-Toe and chess, for two players or against the computer. They are only available when no focus session is running.
- **Works with [Kalendar by Kasign](https://github.com/kartproduction/kalendar):** if it is installed, it appears in All apps.

## Built-in tools

| Tool | What it is |
| --- | --- |
| **Kotepad** | A simple notepad. Type notes or draw sketches. Included in backups. |
| **Kord** | A voice recorder. |
| **Kapture** | A camera. |

On Android these are built in. Photos, recordings, notes and sketches are kept inside Kauncher, and you can save photos and recordings to your phone's gallery or music folder. The same tools also run in a browser at the links on the official page.

## Privacy

- The Android app does **not** ask for internet permission.
- It asks for the camera and microphone (for Kapture and Kord), permission to set the wallpaper, and permission to request app uninstalls (for long-press uninstall).
- Your focus line, folders, notes and settings are stored on your device.

## Get it

- **Android:** download the latest APK from the [official page](https://kartproduction.github.io/kauncher/) or from [Releases](https://github.com/kartproduction/kauncher/releases/latest). Android may ask you to allow installs from this source. You may see a Play Protect warning, because Kauncher is not on the Play Store yet. Only install it if you trust the official page. Then press Home and choose Kauncher as your default home app. Needs Android 8.0 or newer.
- **iPhone:** open the web app in Safari, tap Share, then Add to Home Screen. iPhone doesn't allow custom home screens, so it works as a focus screen with shortcuts.

## How it was made

| Part | What it uses |
| --- | --- |
| Android app | Kotlin and Jetpack Compose |
| Camera | CameraX |
| Build | Gradle (Android Gradle Plugin 8.5, Kotlin 2.0, Java 17) |
| Web pages | Plain HTML, CSS and JavaScript, one file per tool |
| Hosting | GitHub Pages for the website and iPhone web app |
| Builds and releases | GitHub Actions |

### Repository layout

```
kauncher/
├── Kauncher.zip          # the Android project source (Kotlin, Gradle, resources)
├── index.html            # the official download page
├── app.html              # the iPhone web app
├── kotepad.html          # Kotepad in the browser
├── kord.html             # Kord in the browser
├── kapture.html          # Kapture in the browser
├── chess.html            # chess
├── tictactoe.html        # Tic-Tac-Toe
├── build.gradle.kts      # Gradle settings
├── settings.gradle.kts
├── gradle.properties
├── .github/workflows/    # APK build and release
├── LICENSE
└── README.md
```

## Build and release

These notes are for the maintainer.

- The *Build APK* workflow unpacks `Kauncher.zip`, builds the APK with Gradle, and saves it as a downloadable build.
- It publishes a release only when you push a tag such as `v3.4` or enter a version when running the workflow by hand. The tag must match `versionName` in the app, or the release is stopped.
- Official builds are signed with a private key stored in GitHub Secrets. The key is never in the repository. Without it (on your own computer), Gradle uses the normal local debug key.

## Contributing

Bug reports and suggestions are welcome in [Issues](https://github.com/kartproduction/kauncher/issues). The owner decides what gets changed. Please don't send code you don't have the right to share.

## License

Copyright (c) 2026 Kart Production. All rights reserved.

The software and its source code are proprietary. Viewing the code does not give you permission to copy, modify, redistribute, rename, repackage or publish it, in whole or in part, or to offer it under any other name, publisher or identity, without written permission from Kart Production. See [LICENSE](LICENSE) for the full terms.

## Contact

Kart Production · GitHub: [@kartproduction](https://github.com/kartproduction)
