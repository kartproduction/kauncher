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
- **Focus log:** every finished or stopped session is saved with what you were learning and creating, any note you typed during the session, and how long you worked, in hours and minutes. See today, the last 7 days, your total and your streak. A **focus calendar** (Kettings > Focus log > Calendar) shades each day of the month by how long you focused. Tap a day to see its sessions and its Nepali date.
- **During focus:** everything is blocked except **Khone**, which opens the phone dialer so you can make a call (not messages). A note box lets you type what you are doing; it is saved to your focus log.
- **Creative tools:** **Krite** and **Ketch** open Kotepad for notes and sketches, **Kord** records your voice, **Kamera** takes photos and video, and **Khone** opens the phone dialer. During a focus session only Khone works.
- **Full launcher (Android):** swipe up for all your apps, swipe down to come back home. Long-press an app to see its info or uninstall it. The list is in alphabetical order.
- **Folders and backup:** group apps into folders with their own icons. Back up your folders, notes, sketches and focus log as a text code and paste it back after a reinstall.
- **Kettings:** focus lengths, auto-start, screen lock, focus log, light or dark theme, colour theme, clock style, backup and restore, and shortcuts to your phone's own settings.
- **Pomodoro mode:** in Calm tools, run 25 minutes of focus and a 5 minute break with a gentle sound, again and again. Every fourth break is 15 minutes.
- **Calm tools:** pause before opening chosen apps, hide apps until searched ("quiet apps"), daily app limits, built-in focus sounds (rain, ticking clock, soft noise, ocean waves, wind, fire crackle, night crickets, warm hum), a break offer and gentle sound after each session, and do not disturb during focus. All sounds are made by the phone itself.
- **Pause screen and open counter:** the pause screen shows your next intention for the day and how many times you opened that app from Kauncher today.
- **Sleep timer:** plays your focus sound, fades out and stops by itself after 15, 30 or 60 minutes.
- **Intentions and evening reflection:** choose up to three things to make today and tick them off. At a time you choose between 6 and 10 PM, Kauncher asks "What did you make today?" once. Your line is saved in Kotepad and shown in the weekly report.
- **Focus hours:** choose days and hours when apps you set to "Pause before opening" are hidden.
- **Bedtime mode:** between the times you choose, Kauncher turns gray and hides games, social apps and your "Pause before opening" apps. Apps that do not say what they are may stay visible.
- **Weekly report and phone time cap:** a Sunday summary of your focus time and phone time, with a daily cap you can set and a weekly focus goal. Worked out on your phone and never uploaded.
- **Colour themes:** in Kettings, choose Kauncher, Paper, Forest, Ocean, Sunset or Mono (black, white and gray only). Each has a light and a dark version.
- **Edit lock:** protects Calm tools, the focus lengths in Kettings, app pause and limit changes, and Restore. Choose a PIN or a wait of 1, 5 or 15 minutes. After you enter the PIN or wait, they stay open for 10 minutes. It lives inside Kauncher, so it cannot stop someone from uninstalling the app.
- **Quick capture and search shortcuts:** tap Krite to open a new note straight away, hold Kord to start recording. In All apps, type `n buy milk` to save a note or `t 20` to start a 20 minute timer.
- **Streak:** one missed day does not break your streak, at most once a week (you can turn this off). A small widget and an optional lock screen wallpaper show your streak.
- **Kauncher in Nepali:** when your phone is set to Nepali, the app speaks Nepali almost everywhere.
- **Kames:** Bagh-chal, chess, Chungi, Keshey, Ludo and Tic-Tac-Toe in one place, for friends, against the computer or on your own (Chungi supports one to four players, with an optional computer opponent). Kames remembers your wins, losses and draws against the computer, and your best Chungi score. Kames are only available when no focus session is running.
- **Nepali date and festivals (optional):** turn on "Show Nepali date and next festival" to see today's Bikram Sambat date and the next Nepali festival or public holiday, with names in English and Nepali. Built in, so it works offline. You can also save **Nepali birthdays and anniversaries** by Nepali date. They come back every year, and the next one within 30 days shows on the home screen. Dates work for Nepali years 2082 to 2088.
- **Works with [Kalendar by Kasign](https://github.com/kartproduction/kalendar):** if it is installed, it appears in All apps. If not, a row there takes you to its download page.
- **Suggests [Kutout](https://kartproduction.github.io/kutout/):** a free background remover that runs on your device. If it is not installed, a row in All apps takes you to its page.
- **Calm by default:** on a new install, games, the Nepali date and app suggestions are off. Turn them on in Kettings under Extras.
- **Updates:** Kettings > Help > Check for updates opens the official download page. To get updates automatically, you can follow this repository's releases with an app such as Obtainium.
- **Privacy:** see [privacy.html](https://kartproduction.github.io/kauncher/privacy.html).

## Built-in tools

| Tool | What it is |
| --- | --- |
| **Kotepad** | A simple notepad. Type notes or draw sketches, with undo and redo. Included in backups. |
| **Kord** | A voice recorder. |
| **Kamera** | A camera for photos and video, with pinch or button zoom. |
| **Kames** | Bagh-chal, chess, Chungi, Keshey, Ludo and Tic-Tac-Toe. |

On Android these are built in. Photos, videos, recordings, notes and sketches are kept inside Kauncher, and you can save photos, videos and recordings to your phone's gallery or music folder. The same tools also run in a browser at the links on the official page.

## Privacy

- The Android app does **not** ask for internet permission.
- It asks for the camera and microphone (for Kamera and Kord), permission to set the wallpaper, and permission to request app uninstalls (for long-press uninstall).
- Usage access (optional) lets the weekly report, phone time cap and daily app limits work out how long you used your phone. Everything is worked out on your phone and nothing is uploaded.
- Notifications are used for the Sunday weekly report and for the gentle nudge on the day you pass your phone time cap. Starting at boot lets Kauncher book the next report again after a restart. Do Not Disturb access is only used if you turn on "Do not disturb during focus". The alarm permission is only used to start a timer in your phone's clock app.
- The edit lock, bedtime mode, focus hours, themes and birthdays add no permissions.
- Your focus line, focus log, folders, notes, sketches, scores and settings are stored on your device.
- Kauncher suggests [Mojeek](https://www.mojeek.com/support/smartphones/#ma), a search engine, once after install and in All apps until you install it. Tapping it opens Mojeek's setup page in your browser. Kauncher itself still makes no network connections.

## Get it

- **Android:** download the latest APK from the [official page](https://kartproduction.github.io/kauncher/) or from [Releases](https://github.com/kartproduction/kauncher/releases/latest). Android may ask you to allow installs from this source. You may see a Play Protect warning, because Kauncher is not on the Play Store yet. Only install it if you trust the official page. Then press Home and choose Kauncher as your default home app. Needs Android 8.0 or newer.
- **iPhone:** open the web app in Safari, tap Share, then Add to Home Screen. iPhone doesn't allow custom home screens, so it works as a focus screen with shortcuts. Krite, Ketch, Kord and Kamera open the browser versions inside Kauncher, and Khone opens the phone dialer.

## How it was made

| Part | What it uses |
| --- | --- |
| Android app | Kotlin and Jetpack Compose |
| Camera and video | CameraX |
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
├── kamera.html           # Kamera in the browser
├── kames.html            # Kames: pick a game
├── baghchal.html         # Bagh-chal
├── chess.html            # chess
├── chungi.html           # Chungi
├── keshey.html           # Keshey
├── ludo.html             # Ludo
├── tictactoe.html        # Tic-Tac-Toe
├── screenshots/          # pictures shown on the official page
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
- It publishes a release only when you push a tag such as `v4.6` or enter a version when running the workflow by hand. The tag must match `versionName` in the app, or the release is stopped.
- Official builds are signed with a private key stored in GitHub Secrets. The key is never in the repository. Without it (on your own computer), Gradle uses the normal local debug key.
- Each update needs a higher `versionName` and `versionCode` in `app/build.gradle.kts`, or Android will not install it over the old version.

## Contributing

Bug reports and suggestions are welcome in [Issues](https://github.com/kartproduction/kauncher/issues). The owner decides what gets changed. Please don't send code you don't have the right to share.

## License

Copyright (c) 2026 Kart Production. All rights reserved.

The software and its source code are proprietary. Viewing the code does not give you permission to copy, modify, redistribute, rename, repackage or publish it, in whole or in part, or to offer it under any other name, publisher or identity, without written permission from Kart Production. See [LICENSE](LICENSE) for the full terms.

## Contact

Kart Production · GitHub: [@kartproduction](https://github.com/kartproduction)
