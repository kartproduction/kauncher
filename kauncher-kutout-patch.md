# Kauncher: add the Kutout suggestion (3 small edits in MainActivity.kt)

This copies the way Kalendar is suggested: until an app with "kutout" in its name is installed, All apps shows a "Kutout" row. Tapping it opens the Kutout page. Nothing else changes, so your 4.9 Nepali date code is untouched.

Open `app/src/main/java/com/studio/launcher/MainActivity.kt` and make these three edits.

## Edit 1: find this line

```kotlin
val kalendarMissing = apps.none { it.label.contains("kalendar", ignoreCase = true) }
```

and add this line right under it:

```kotlin
val kutoutMissing = apps.none { it.label.contains("kutout", ignoreCase = true) }
```

## Edit 2: find these two lines

```kotlin
if (kalendarMissing && "kalendar by kasign".contains(q, ignoreCase = true))
    entries.add(ListEntry("Kalendar", "kalendar", "by Kasign · tap to get the app", null))
```

and add these two lines right under them:

```kotlin
if (kutoutMissing && "kutout background remover".contains(q, ignoreCase = true))
    entries.add(ListEntry("Kutout", "kutout", "Free background remover · tap to get the app", null))
```

## Edit 3: find this block

```kotlin
"kalendar" -> try {
    ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://kartproduction.github.io/kalendarbykasign/download.html")))
} catch (ex: Exception) { }
```

and add this block right under it, before the `else ->` line:

```kotlin
"kutout" -> try {
    ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://kartproduction.github.io/kutout/#get")))
} catch (ex: Exception) { }
```

## Then

- In `app/build.gradle.kts` set `versionName = "4.10"` and raise `versionCode` by 1 from your 4.9 value.
- Build, install, open All apps, and check that a "Kutout" row appears and opens the page.
- If the app name on your phone is not "Kutout", the row will keep showing. Change the word `"kutout"` in Edit 1 to match the installed app's label.
