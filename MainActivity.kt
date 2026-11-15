package com.studio.launcher

import android.content.Context
import android.app.WallpaperManager
import android.content.Intent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import android.content.SharedPreferences
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Bumped every time the Home button is pressed while Kauncher is already open.
val homeSignal = mutableIntStateOf(0)
// Bumped when the Sunday weekly-report notification is tapped.
val weeklySignal = mutableIntStateOf(0)

class MainActivity : ComponentActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homeSignal.intValue++
        if (intent.getBooleanExtra("openWeeklyReport", false)) weeklySignal.intValue++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent.getBooleanExtra("openWeeklyReport", false)) weeklySignal.intValue++
        setContent { Launcher(this) }
    }
}

// During a focus session everything is blocked except Khone, which opens the phone dialer (for calls only).
val FOCUS_TOOLS = setOf("Khone")

const val MOJEEK_URL = "https://www.mojeek.com/support/smartphones/#ma"
const val MOJEEK_PKG = "mojeek.app"

// Opens the Mojeek app on Google Play (Play Store app first, then the web page, then Mojeek's own page).
fun openMojeek(ctx: Context) {
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$MOJEEK_PKG"))) }
    catch (e: Exception) {
        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$MOJEEK_PKG"))) }
        catch (e2: Exception) { openMojeek(ctx) }
    }
}

fun openLink(ctx: Context, url: String) {
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))) } catch (e: Exception) { }
}

data class AppItem(val label: String, val pkg: String)

// One row of the All apps list: either an installed app or one of Kauncher's own items.
class ListEntry(val label: String, val special: String?, val sub: String?, val app: AppItem?)

fun uninstallApp(ctx: Context, pkg: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_DELETE, android.net.Uri.parse("package:$pkg")))
    } catch (e: Exception) {
        try {
            ctx.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:$pkg")))
        } catch (e2: Exception) { }
    }
}

fun appInfo(ctx: Context, pkg: String) {
    try {
        ctx.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:$pkg")))
    } catch (e: Exception) { }
}

fun loadApps(ctx: Context): List<AppItem> {
    val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val pm = ctx.packageManager
    return pm.queryIntentActivities(i, 0)
        .map { AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
        .filter { it.pkg != ctx.packageName }
        .sortedBy { it.label.lowercase() }
}

fun findActivity(ctx: Context): android.app.Activity? {
    var c: Context? = ctx
    while (c is android.content.ContextWrapper) {
        if (c is android.app.Activity) return c
        c = c.baseContext
    }
    return null
}

fun openApp(ctx: Context, pkg: String): Boolean {
    val intent = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return false
    ctx.startActivity(intent)
    return true
}

// Background colour choices: colour to dark-theme flag.
val swatches = listOf(
    Color(0xFF16122A) to true, Color(0xFF000000) to true, Color(0xFF1F3A2E) to true,
    Color(0xFFF4EFE6) to false, Color(0xFFDDE7F0) to false
)

fun bgFile(ctx: Context) = File(ctx.filesDir, "bg.jpg")

fun loadBg(ctx: Context): ImageBitmap? =
    bgFile(ctx).takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() }

fun saveBg(ctx: Context, uri: Uri): Boolean {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 2000 || bounds.outHeight / sample > 2000) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bmp = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return false
        val rot = ctx.contentResolver.openInputStream(uri)?.use {
            androidx.exifinterface.media.ExifInterface(it).rotationDegrees
        } ?: 0
        if (rot != 0) {
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(rot.toFloat()) }, true)
        }
        bgFile(ctx).outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        true
    } catch (e: Exception) {
        false
    }
}

// Backup code: your folders, icons, notes, sketches and focus log, as text you can save and paste back after a reinstall.
// New codes start with KAUNCHER2: (compressed). Older KAUNCHER1: codes still restore.
fun makeBackup(prefs: SharedPreferences): String {
    val o = org.json.JSONObject()
    o.put("folders", org.json.JSONObject(prefs.getString("folders", "{}") ?: "{}"))
    o.put("icons", org.json.JSONObject(prefs.getString("folderIcons", "{}") ?: "{}"))
    o.put("notes", org.json.JSONArray(prefs.getString("notes", "[]") ?: "[]"))
    o.put("focusLog", org.json.JSONArray(prefs.getString("focusLog", "[]") ?: "[]"))
    o.put("pauseApps", org.json.JSONArray(prefs.getString("pauseApps", "[]") ?: "[]"))
    o.put("quietApps", org.json.JSONArray(prefs.getString("quietApps", "[]") ?: "[]"))
    o.put("intentions", org.json.JSONObject(prefs.getString("intentions", "{}") ?: "{}"))
    o.put("appLimits", org.json.JSONObject(prefs.getString("appLimits", "{}") ?: "{}"))
    val zipped = java.io.ByteArrayOutputStream()
    java.util.zip.GZIPOutputStream(zipped).use { it.write(o.toString().toByteArray(Charsets.UTF_8)) }
    val b = android.util.Base64.encodeToString(zipped.toByteArray(), android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
    return "KAUNCHER2:$b"
}

fun restoreBackup(prefs: SharedPreferences, code: String): Boolean {
    return try {
        val t = code.replace(Regex("\\s"), "")
        val json = when {
            t.startsWith("KAUNCHER2:") -> {
                val raw = android.util.Base64.decode(t.removePrefix("KAUNCHER2:"), android.util.Base64.URL_SAFE)
                String(java.util.zip.GZIPInputStream(java.io.ByteArrayInputStream(raw)).use { it.readBytes() }, Charsets.UTF_8)
            }
            t.startsWith("KAUNCHER1:") -> String(android.util.Base64.decode(t.removePrefix("KAUNCHER1:"), android.util.Base64.URL_SAFE), Charsets.UTF_8)
            else -> return false
        }
        val o = org.json.JSONObject(json)
        val folders = o.getJSONObject("folders").toString()
        val icons = o.getJSONObject("icons").toString()
        val e = prefs.edit().putString("folders", folders).putString("folderIcons", icons)
        if (o.has("notes")) e.putString("notes", o.getJSONArray("notes").toString())
        if (o.has("focusLog")) e.putString("focusLog", o.getJSONArray("focusLog").toString())
        if (o.has("pauseApps")) e.putString("pauseApps", o.getJSONArray("pauseApps").toString())
        if (o.has("quietApps")) e.putString("quietApps", o.getJSONArray("quietApps").toString())
        if (o.has("intentions")) e.putString("intentions", o.getJSONObject("intentions").toString())
        if (o.has("appLimits")) e.putString("appLimits", o.getJSONObject("appLimits").toString())
        e.apply()
        true
    } catch (e: Exception) { false }
}

fun loadFolders(prefs: SharedPreferences): Map<String, List<String>> {
    val out = LinkedHashMap<String, List<String>>()
    try {
        val o = org.json.JSONObject(prefs.getString("folders", "{}") ?: "{}")
        for (k in o.keys()) {
            val a = o.getJSONArray(k)
            out[k] = List(a.length()) { a.getString(it) }
        }
    } catch (e: Exception) { }
    return out
}

fun saveFoldersPrefs(prefs: SharedPreferences, m: Map<String, List<String>>) {
    val o = org.json.JSONObject()
    for ((k, v) in m) o.put(k, org.json.JSONArray(v))
    prefs.edit().putString("folders", o.toString()).apply()
}

val glyphs = listOf("●", "▲", "■", "★", "♥", "Hidden")

fun loadIcons(prefs: SharedPreferences): Map<String, String> {
    val out = LinkedHashMap<String, String>()
    try {
        val o = org.json.JSONObject(prefs.getString("folderIcons", "{}") ?: "{}")
        for (k in o.keys()) out[k] = o.getString(k)
    } catch (e: Exception) { }
    return out
}

fun saveIcons(prefs: SharedPreferences, m: Map<String, String>) {
    val o = org.json.JSONObject()
    for ((k, v) in m) o.put(k, v)
    prefs.edit().putString("folderIcons", o.toString()).apply()
}

val artWords = listOf(
    "art", "draw", "paint", "sketch", "illustrat", "design", "poem", "poetry", "song", "lyric", "music",
    "compos", "melod", "guitar", "piano", "sing", "writ", "story", "stories", "novel", "book", "script",
    "film", "video", "photo", "edit", "animat", "craft", "sculpt", "potter", "danc", "logo", "poster",
    "cover", "podcast", "record", "beat", "cartoon", "comic", "calligraph", "knit", "sew", "carv",
    "color", "colour", "canvas", "mural", "fashion"
)

// True when the text mentions something creative or art related.
fun looksArtistic(text: String): Boolean {
    val words = text.lowercase().split(Regex("[^a-z]+")).filter { it.isNotEmpty() }
    return words.any { w -> artWords.any { w.startsWith(it) } }
}

// Searches YouTube (app if installed, otherwise the browser).
fun searchYouTube(ctx: Context, q: String) {
    val t = q.trim()
    if (t.isEmpty()) return
    try {
        ctx.startActivity(Intent(Intent.ACTION_SEARCH).setPackage("com.google.android.youtube").putExtra("query", t))
    } catch (e: Exception) {
        try {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(t))))
        } catch (e2: Exception) { }
    }
}

// Opens the phone's default notes app (Android 14+), or a known notes app as a fallback.
fun openNotes(ctx: Context): Boolean {
    // Open the real notes app (so your notes are saved normally), then fall back to the system "create note" request.
    val known = listOf(
        "com.samsung.android.app.notes", "com.google.android.keep", "com.miui.notes",
        "com.oneplus.note", "com.coloros.note", "com.microsoft.office.onenote"
    )
    for (p in known) if (openApp(ctx, p)) return true
    try {
        ctx.startActivity(Intent("android.intent.action.CREATE_NOTE"))
        return true
    } catch (e: Exception) { }
    return false
}

// Opens the camera app itself (photos are saved to the gallery), not the "return one photo" request.
fun openCamera(ctx: Context): Boolean {
    try { ctx.startActivity(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)); return true } catch (e: Exception) { }
    try { ctx.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE)); return true } catch (e: Exception) { }
    return false
}

// Opens the voice recorder app itself (recordings are saved in the app).
fun openRecorder(ctx: Context): Boolean {
    val known = listOf(
        "com.sec.android.app.voicenote", "com.google.android.apps.recorder", "com.android.soundrecorder",
        "com.coloros.soundrecorder", "com.miui.soundrecorder"
    )
    for (p in known) if (openApp(ctx, p)) return true
    try { ctx.startActivity(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)); return true } catch (e: Exception) { }
    return false
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Launcher(ctx: Context) {
    val sysDark = isSystemInDarkTheme()
    val prefs = remember { ctx.getSharedPreferences("studio", Context.MODE_PRIVATE) }
    var photo by remember { mutableStateOf<ImageBitmap?>(loadBg(ctx)) }
    var swatch by remember { mutableIntStateOf(prefs.getInt("swatch", -1)) }
    var menu by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var kotepad by remember { mutableStateOf(false) }
    var focusLogOpen by remember { mutableStateOf(false) }
    var weeklyOpen by remember { mutableStateOf(false) }
    var extrasOpen by remember { mutableStateOf(false) }
    var intentionsOpen by remember { mutableStateOf(false) }
    var kotepadNew by remember { mutableStateOf(false) }       // Krite opens straight into a new note
    var recordAuto by remember { mutableStateOf(false) }       // holding Kord starts recording straight away
    var pauseNote by remember { mutableStateOf<String?>(null) }
    var capNudge by remember { mutableStateOf<String?>(null) }
    var resumeTick by remember { mutableIntStateOf(0) }
    val showProg = remember(extrasOpen) { prefs.getBoolean("showProgress", true) }
    val showInt = remember(extrasOpen) { prefs.getBoolean("showIntentions", true) }
    val showBs = remember(extrasOpen) { prefs.getBoolean("showNepaliDate", true) }
    var pauseFor by remember { mutableStateOf<AppItem?>(null) }
    var pauseSet by remember { mutableStateOf(loadSet(prefs, "pauseApps")) }
    var quietSet by remember { mutableStateOf(loadSet(prefs, "quietApps")) }
    var endCardMin by remember { mutableIntStateOf(0) }      // minutes of the session that just ended; 0 = no card
    var breakMin by remember { mutableIntStateOf(0) }        // minutes of the break now running; 0 = none
    var mojeekOpen by remember { mutableStateOf(!prefs.getBoolean("mojeekSeen", false)) }
    var captureOpen by remember { mutableStateOf(false) }
    var recordOpen by remember { mutableStateOf(false) }
    var ownMedia by remember { mutableStateOf(prefs.getBoolean("ownMedia", true)) }
    var kotepadSketch by remember { mutableStateOf(false) }
    var writeKotepad by remember { mutableStateOf(prefs.getBoolean("writeKotepad", true)) }
    var artMin by remember { mutableIntStateOf(prefs.getInt("artMin", 30)) }
    var lockMin by remember { mutableIntStateOf(prefs.getInt("lockMin", 60)) }
    var autoStart by remember { mutableStateOf(prefs.getBoolean("autoStart", true)) }
    var pinLock by remember { mutableStateOf(prefs.getBoolean("pinLock", true)) }
    var themeMode by remember { mutableIntStateOf(prefs.getInt("themeMode", 0)) }   // 0 auto, 1 light, 2 dark
    var h24 by remember { mutableStateOf(prefs.getBoolean("h24", false)) }
    var kTop by remember { mutableStateOf(prefs.getBoolean("kTop", false)) }
    var game by remember { mutableStateOf(false) }
    var chess by remember { mutableStateOf(false) }
    var kames by remember { mutableStateOf(false) }
    var bagh by remember { mutableStateOf(false) }
    var ludo by remember { mutableStateOf(false) }
    var chungi by remember { mutableStateOf(false) }
    var keshey by remember { mutableStateOf(false) }
    var folders by remember { mutableStateOf(loadFolders(prefs)) }
    var openFolder by remember { mutableStateOf<String?>(null) }
    var pickFor by remember { mutableStateOf<String?>(null) }
    var naming by remember { mutableStateOf(false) }
    var backupMode by remember { mutableIntStateOf(0) }   // 0 = closed, 1 = back up, 2 = restore
    var backupCode by remember { mutableStateOf("") }
    var restoreText by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var icons by remember { mutableStateOf(loadIcons(prefs)) }
    fun setIcon(name: String, g: String) { icons = icons + (name to g); saveIcons(prefs, icons) }
    fun saveFolders(m: Map<String, List<String>>) { folders = m; saveFoldersPrefs(prefs, m) }
    fun addToFolder(f: String, pkg: String) {
        val l = folders[f] ?: return
        if (pkg !in l) saveFolders(folders + (f to (l + pkg)))
    }
    val dark = when {
        photo != null -> true
        swatch >= 0 -> swatches[swatch].second
        themeMode == 1 -> false
        themeMode == 2 -> true
        else -> sysDark
    }
    val bg = if (photo == null && swatch >= 0) swatches[swatch].first
        else if (dark) Color(0xFF16122A) else Color(0xFFE4E0EE)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && saveBg(ctx, uri)) {
            photo = loadBg(ctx); swatch = -1; prefs.edit().putInt("swatch", -1).apply()
        }
    }
    fun pickSwatch(i: Int) {
        swatch = i; photo = null; bgFile(ctx).delete(); prefs.edit().putInt("swatch", i).apply(); menu = false
    }
    val surface = if (dark) Color(0xFF221C3D) else Color(0xFFF3F1F8)
    val ink = if (dark) Color(0xFFEDEAF8) else Color(0xFF201A33)
    val mute = if (dark) Color(0xFF9189B0) else Color(0xFF6B6483)
    val line = if (dark) Color(0xFF342D55) else Color(0xFFC9C3DB)
    val accent = if (dark) Color(0xFF9C8CFF) else Color(0xFF4A35FF)

    var focus by remember { mutableStateOf(prefs.getString("focus", "") ?: "") }
    // The session survives the activity being recreated: its end time is stored.
    val savedEnd = remember { prefs.getLong("endAt", 0L) }
    val resumed = remember { savedEnd > System.currentTimeMillis() }
    var endAt by remember { mutableStateOf(savedEnd) }
    var left by remember { mutableIntStateOf(if (resumed) ((savedEnd - System.currentTimeMillis()) / 1000).toInt() + 1 else 1500) }
    var running by remember { mutableStateOf(resumed) }
    var drawer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var apps by remember { mutableStateOf(loadApps(ctx)) }
    var appMenu by remember { mutableStateOf<AppItem?>(null) }
    // Opens an app, or first shows the "Still want to open it?" pause if you marked it.
    fun launchApp(pkg: String): Boolean {
        // A daily limit that is used up shows the pause screen every time. Otherwise only marked apps pause.
        val overLimit = appLimitReached(ctx, prefs, pkg)
        if (overLimit != null || pkg in pauseSet) {
            val a = apps.firstOrNull { it.pkg == pkg } ?: return openApp(ctx, pkg)
            if (overLimit != null) recordLimitHit(prefs, pkg)
            pauseNote = overLimit
            pauseFor = a
            return true
        }
        return openApp(ctx, pkg)
    }
    // Refresh the list whenever Kauncher comes back to the front (after an uninstall or install).
    DisposableEffect(Unit) {
        val act = findActivity(ctx) as? ComponentActivity
        val obs = androidx.lifecycle.LifecycleEventObserver { _, ev ->
            if (ev == androidx.lifecycle.Lifecycle.Event.ON_RESUME) { apps = loadApps(ctx); resumeTick++ }
        }
        act?.lifecycle?.addObserver(obs)
        onDispose { act?.lifecycle?.removeObserver(obs) }
    }
    var creating by remember {
        mutableStateOf(
            if (prefs.getString("creatingDay", "") == LocalDate.now().toString()) prefs.getString("creating", "") ?: "" else ""
        )
    }
    var locked by remember { mutableStateOf(resumed && prefs.getBoolean("sessLocked", false)) }
    var lastAuto by remember { mutableStateOf<String?>(creating) }
    fun startFocusFor(text: String) {
        val art = looksArtistic(text)
        left = (if (art) artMin else lockMin) * 60
        locked = !art
        lastAuto = text
        endAt = System.currentTimeMillis() + left * 1000L
        prefs.edit().putLong("endAt", endAt).putBoolean("sessLocked", locked).putInt("planned", if (art) artMin else lockMin).apply()
        running = true
    }
    // What you type while a session runs. It is saved to the focus log when the session ends.
    var sessNote by remember { mutableStateOf(if (resumed) prefs.getString("sessNote", "") ?: "" else "") }
    fun clearNote() { sessNote = ""; prefs.edit().putString("sessNote", "").apply() }
    fun clearSession() { prefs.edit().putLong("endAt", 0L).putBoolean("sessLocked", false).apply() }
    // When a session ends, the "What are you creating?" text is cleared.
    fun clearCreating() { creating = ""; lastAuto = ""; prefs.edit().putString("creating", "").apply() }
    fun onFocusTap() {
        if (running) {
            if (locked) Toast.makeText(ctx, "Locked until the session ends", Toast.LENGTH_SHORT).show()
            else { logFocus(prefs, creating, prefs.getInt("planned", 0) - (left + 59) / 60, false, focus, sessNote); refreshFocusWidgets(ctx); running = false; clearSession(); clearCreating(); clearNote() }
        } else startFocusFor(creating)
    }
    // Starts by itself only after you type what you are creating (never on an empty field or on open).
    LaunchedEffect(creating, running, autoStart) {
        if (!autoStart || running || creating.isBlank() || lastAuto == creating) return@LaunchedEffect
        delay(if (looksArtistic(creating)) 3_000L else 6_000L)
        startFocusFor(creating)
    }

    // While a locked (1 hour) session runs, pin the screen: Recents (|||) and Home stop working.
    LaunchedEffect(running, locked, pinLock) {
        val act = findActivity(ctx) ?: return@LaunchedEffect
        try {
            val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val pinned = am.lockTaskModeState != android.app.ActivityManager.LOCK_TASK_MODE_NONE
            if (running && locked && pinLock) { if (!pinned) act.startLockTask() }
            else if (pinned) act.stopLockTask()
        } catch (e: Exception) { }
    }

    DisposableEffect(running, locked, pinLock) {
        val act = findActivity(ctx) as? ComponentActivity
        val obs = androidx.lifecycle.LifecycleEventObserver { _, ev ->
            if (ev == androidx.lifecycle.Lifecycle.Event.ON_RESUME && running && locked && pinLock) {
                try {
                    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                    if (am.lockTaskModeState == android.app.ActivityManager.LOCK_TASK_MODE_NONE) act?.startLockTask()
                } catch (e: Exception) { }
            }
        }
        act?.lifecycle?.addObserver(obs)
        onDispose { act?.lifecycle?.removeObserver(obs) }
    }

    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(15_000) } }
    LaunchedEffect(running) {
        if (running) {
            while (true) {
                left = maxOf(0, ((endAt - System.currentTimeMillis() + 999) / 1000).toInt())
                if (left <= 0) break
                delay(500)
            }
            val doneMin = prefs.getInt("planned", 0)
            logFocus(prefs, creating, doneMin, true, focus, sessNote)
            refreshFocusWidgets(ctx)
            running = false; locked = false; clearSession(); clearCreating(); clearNote()
            focusDnd(ctx, prefs, false)
            if (chimeOn(prefs)) playChime(ctx)
            if (doneMin > 0 && (askMadeOn(prefs) || breakMinutes(prefs) > 0)) endCardMin = doneMin
            autoBackupIfDue(ctx, prefs)
        }
    }
    // Quiet background sound while a session runs (built in, no internet).
    DisposableEffect(running) {
        if (running) { FocusSound.start(focusSoundKind(prefs)); if (dndOn(prefs)) focusDnd(ctx, prefs, true) }
        onDispose { FocusSound.stop(); focusDnd(ctx, prefs, false) }
    }
    // If Kauncher was closed in the middle of a session, put Do Not Disturb back the way it was.
    LaunchedEffect(Unit) { if (!running) focusDnd(ctx, prefs, false) }
    // Gentle nudge when today's phone time passes the cap. Checked when Kauncher comes back and every few minutes.
    LaunchedEffect(resumeTick) {
        while (true) {
            capNudge = withContext(Dispatchers.IO) { try { phoneCapNudge(ctx) } catch (e: Exception) { null } }
            delay(5 * 60_000L)
        }
    }
    BackHandler(intentionsOpen) { intentionsOpen = false }
    BackHandler(openFolder != null) { openFolder = null }
    BackHandler(naming) { naming = false }
    BackHandler(settingsOpen) { settingsOpen = false }
    val mojeekMissing = apps.none { it.pkg == MOJEEK_PKG || it.label.contains("mojeek", ignoreCase = true) }
    val showMojeek = mojeekOpen && mojeekMissing && !running
    fun closeMojeek() { mojeekOpen = false; prefs.edit().putBoolean("mojeekSeen", true).apply() }
    BackHandler(showMojeek) { closeMojeek() }
    BackHandler(kotepad) { kotepad = false; kotepadNew = false }
    BackHandler(focusLogOpen) { focusLogOpen = false }
    BackHandler(weeklyOpen) { weeklyOpen = false }
    BackHandler(extrasOpen) { extrasOpen = false }
    BackHandler(pauseFor != null) { recordPause(prefs, false); pauseFor = null }
    BackHandler(endCardMin > 0) { endCardMin = 0 }
    BackHandler(breakMin > 0) { breakMin = 0 }
    LaunchedEffect(Unit) { catchUpWeeklyReport(ctx) }
    DisposableEffect(Unit) {
        val act = findActivity(ctx) as? ComponentActivity
        val obs = androidx.lifecycle.LifecycleEventObserver { _, ev ->
            if (ev == androidx.lifecycle.Lifecycle.Event.ON_STOP) { autoBackupIfDue(ctx, prefs); refreshFocusWidgets(ctx) }
        }
        act?.lifecycle?.addObserver(obs)
        onDispose { act?.lifecycle?.removeObserver(obs) }
    }
    LaunchedEffect(weeklySignal.intValue) { if (weeklySignal.intValue > 0 && !running) { settingsOpen = false; drawer = false; weeklyOpen = true } }
    BackHandler(captureOpen) { captureOpen = false }
    BackHandler(recordOpen) { recordOpen = false; recordAuto = false }
    BackHandler(backupMode != 0) { backupMode = 0 }
    BackHandler(drawer) { drawer = false; query = ""; pickFor = null }
    BackHandler(menu) { menu = false }
    BackHandler(appMenu != null) { appMenu = null }
    BackHandler(kames) { kames = false }
    BackHandler(game) { game = false; kames = true }
    BackHandler(chess) { chess = false; kames = true }
    BackHandler(bagh) { bagh = false; kames = true }
    BackHandler(ludo) { ludo = false; kames = true }
    BackHandler(chungi) { chungi = false; kames = true }
    BackHandler(keshey) { keshey = false; kames = true }
    // Back does nothing while a focus session is running, so it can never reset the session.
    BackHandler(running) { }

    // Home button: close folders, the app list, search, menus and games, and show the home screen.
    var seenHome by remember { mutableIntStateOf(homeSignal.intValue) }
    LaunchedEffect(homeSignal.intValue) {
        if (homeSignal.intValue != seenHome) {
            seenHome = homeSignal.intValue
            openFolder = null; pickFor = null; naming = false; appMenu = null; backupMode = 0; settingsOpen = false; kotepad = false; captureOpen = false; recordOpen = false; kames = false; bagh = false; ludo = false; chungi = false; keshey = false; game = false; chess = false; focusLogOpen = false; weeklyOpen = false; extrasOpen = false; pauseFor = null; intentionsOpen = false; recordAuto = false
            drawer = false; query = ""; menu = false; game = false; chess = false
        }
    }

    // Edit these package names to point the four tools at your favourite apps.
    val tools = listOf(
        Triple("Krite", "✎", ""),
        Triple("Ketch", "〰", ""),
        Triple("Kord", "●", ""),
        Triple("Kamera", "◉", ""),
        Triple("Khone", "✆", "")
    )
    fun runTool(name: String, pkg: String) {
        when (name) {
            "Kord" -> if (ownMedia) { recordAuto = false; recordOpen = true } else if (!openRecorder(ctx)) { query = "record"; drawer = true }
            "Kamera" -> if (ownMedia) captureOpen = true else if (!openCamera(ctx)) { query = "camera"; drawer = true }
            "Khone" -> ctx.startActivity(Intent(Intent.ACTION_DIAL))
            "Krite" -> if (writeKotepad) { kotepadSketch = false; kotepadNew = true; kotepad = true } else if (!openNotes(ctx)) { query = "note"; drawer = true }
            "Ketch" -> if (writeKotepad) { kotepadSketch = true; kotepadNew = false; kotepad = true } else if (!openNotes(ctx)) { query = "note"; drawer = true }
            else -> if (!openApp(ctx, pkg)) { query = name; drawer = true }
        }
    }

    Box(Modifier.fillMaxSize().background(bg)) {
    photo?.let {
        Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .45f)))
    }
    Box(
        Modifier.fillMaxSize().systemBarsPadding()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { if (!running) menu = true }) }
            .pointerInput(running) {
                var drag = 0f
                detectVerticalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = { if (drag < -150f && !running) drawer = true }
                ) { _, dy -> drag += dy }
            }
    ) {
        // In landscape the home screen scrolls so both fields stay reachable.
        BoxWithConstraints(Modifier.fillMaxSize()) {
        val land = maxWidth > maxHeight
        Column(
            (if (land) Modifier.fillMaxSize().verticalScroll(rememberScrollState()) else Modifier.fillMaxSize())
                .padding(horizontal = 26.dp, vertical = if (land) 12.dp else 28.dp)
        ) {
            Text(
                now.format(DateTimeFormatter.ofPattern(if (h24) "HH:mm" else "h:mm")),
                color = ink, fontSize = if (running || land) 30.sp else 64.sp, fontWeight = FontWeight.Light
            )
            Text(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), color = mute, fontSize = 15.sp)
            // Nepali (BS) date and the next festival, from the same data as Kalendar by Kasign.
            if (showBs) {
                val bsLine = remember(now.toLocalDate()) {
                    val d = now.toLocalDate()
                    val bs = NepaliDate.format(d)
                    val fest = NepaliDate.nextFestival(d)
                    if (bs == null) null else bs to fest
                }
                bsLine?.let { (bs, fest) ->
                    Text(bs, color = mute, fontSize = 15.sp)
                    if (fest != null && !running) Text("★ " + fest, color = accent, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            // Progress without opening anything: today's focus time and your streak.
            val stats = remember(now, running, endCardMin, extrasOpen) { focusStats(prefs) }
            if (showProg) Text(
                fmtDur(stats.first) + " focus today" + (if (stats.second > 0) "  ·  " + streakText(stats.second) else ""),
                color = mute, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp)
            )
            val intLine = remember(now, intentionsOpen) { intentionsLine(prefs) }
            if (showInt) Text(
                intLine, color = accent, fontSize = 14.sp,
                modifier = Modifier.clickable { intentionsOpen = true }.padding(top = 6.dp, bottom = 2.dp)
            )
            capNudge?.let { Text(it, color = accent, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }

            Column(if (land) Modifier.padding(vertical = 16.dp) else Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                BasicTextField(
                    value = focus,
                    onValueChange = { focus = it.take(60); prefs.edit().putString("focus", focus).apply() },
                    textStyle = TextStyle(color = ink, fontSize = 34.sp, fontWeight = FontWeight.SemiBold, lineHeight = 38.sp),
                    cursorBrush = SolidColor(accent),
                    singleLine = true,
                    readOnly = running,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { if (!running && focus.isNotBlank()) { searchYouTube(ctx, focus); focus = ""; prefs.edit().putString("focus", "").apply() }; focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Column {
                            Box(Modifier.padding(bottom = 14.dp)) {
                                if (focus.isEmpty()) Text("What are you learning today?", color = mute.copy(alpha = .55f), fontSize = 34.sp, fontWeight = FontWeight.SemiBold, lineHeight = 38.sp)
                                inner()
                            }
                            Box(Modifier.fillMaxWidth().height(1.5.dp).background(line))
                        }
                    }
                )
                Spacer(Modifier.height(20.dp))
                BasicTextField(
                    value = creating,
                    onValueChange = {
                        creating = it.take(40)
                        prefs.edit().putString("creating", creating).putString("creatingDay", LocalDate.now().toString()).apply()
                    },
                    readOnly = running,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); if (!running && creating.isNotBlank()) startFocusFor(creating) }),
                    textStyle = TextStyle(color = ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Column {
                            Box(Modifier.padding(bottom = 10.dp)) {
                                if (creating.isEmpty()) Text("What are you creating?", color = mute.copy(alpha = .55f), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                                inner()
                            }
                            Box(Modifier.fillMaxWidth().height(1.5.dp).background(line))
                        }
                    }
                )
                Text("Starts after you type what you are creating. Creative: 30 min. Anything else: 1 hour, locked.", color = mute, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                if (running) {
                    Spacer(Modifier.height(14.dp))
                    BasicTextField(
                        value = sessNote,
                        onValueChange = { sessNote = it.take(200); prefs.edit().putString("sessNote", sessNote).apply() },
                        textStyle = TextStyle(color = ink, fontSize = 17.sp),
                        cursorBrush = SolidColor(accent),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Column {
                                Box(Modifier.padding(bottom = 8.dp)) {
                                    if (sessNote.isEmpty()) Text("Type what you are doing. It is saved to your log.", color = mute.copy(alpha = .55f), fontSize = 17.sp)
                                    inner()
                                }
                                Box(Modifier.fillMaxWidth().height(1.5.dp).background(line))
                            }
                        }
                    )
                }
                Spacer(Modifier.height(22.dp))
                Row(
                    Modifier.clip(CircleShape).background(ink)
                        .clickable { onFocusTap() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (running) "%d:%02d".format(left / 60, left % 60) + (if (locked) "  ·  locked" else "")
                        else "Start focus · ${if (looksArtistic(creating)) artMin else lockMin} min",
                        color = bg, fontSize = 16.sp, fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().alpha(if (running) .12f else 1f).horizontalScroll(rememberScrollState()).padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                folders.keys.forEach { name ->
                    val g = icons[name] ?: "●"
                    val hidden = g == "Hidden"
                    Box(
                        Modifier.size(44.dp)
                            .then(if (hidden) Modifier else Modifier.border(1.5.dp, line, CircleShape))
                            .clip(CircleShape).clickable(enabled = !running) { openFolder = name },
                        contentAlignment = Alignment.Center
                    ) { if (!hidden) Text(g, color = ink, fontSize = 18.sp) }
                }
                Box(
                    Modifier.size(44.dp).clip(CircleShape).clickable(enabled = !running) { naming = true },
                    contentAlignment = Alignment.Center
                ) { Text("+", color = mute, fontSize = 22.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                tools.forEach { (name, glyph, pkg) ->
                    Column(
                        Modifier.alpha(if (running && name !in FOCUS_TOOLS) .12f else 1f)
                            .combinedClickable(
                                enabled = !running || name in FOCUS_TOOLS,
                                onClick = { runTool(name, pkg) },
                                onLongClick = if (name == "Kord" && ownMedia) ({ recordAuto = true; recordOpen = true }) else null
                            ).padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(46.dp).border(1.5.dp, line, CircleShape), contentAlignment = Alignment.Center) {
                            Text(glyph, color = ink, fontSize = 20.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(name, color = mute, fontSize = 13.sp)
                    }
                }
            }
            Text(
                "All apps", color = mute, fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).alpha(if (running) .12f else 1f)
                    .clickable(enabled = !running) { drawer = true }.padding(16.dp)
            )
        }
        }

        AnimatedVisibility(drawer, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
            val listState = rememberLazyListState()
            Column(Modifier.fillMaxSize().background(surface).padding(horizontal = 26.dp, vertical = 20.dp)) {
                Row(
                    Modifier.pointerInput(Unit) {
                        // Swipe down on the search bar to go back home.
                        var drag = 0f
                        detectVerticalDragGestures(
                            onDragStart = { drag = 0f },
                            onDragEnd = { if (drag > 150f) { drawer = false; query = ""; pickFor = null } }
                        ) { _, dy -> drag += dy }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        textStyle = TextStyle(color = ink, fontSize = 20.sp),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box(Modifier.padding(vertical = 10.dp)) {
                                if (query.isEmpty()) Text(if (pickFor != null) "Choose an app for ${pickFor}" else "Find an app", color = mute, fontSize = 20.sp)
                                inner()
                            }
                        }
                    )
                    Text("Close", color = mute, modifier = Modifier.clickable { drawer = false; query = ""; pickFor = null }.padding(8.dp))
                }
                Box(Modifier.fillMaxWidth().height(1.5.dp).background(line))
                LazyColumn(
                    Modifier.weight(1f).pointerInput(Unit) {
                        // Swipe down while the list is already at the top to go back home. This only watches the touch, so scrolling still works.
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                            var total = 0f
                            while (true) {
                                val ev = awaitPointerEvent(PointerEventPass.Initial)
                                val c = ev.changes.firstOrNull { it.id == down.id } ?: break
                                total += c.positionChange().y
                                if (!c.pressed) break
                            }
                            if (atTop && total > 200f) { drawer = false; query = ""; pickFor = null }
                        }
                    },
                    state = listState
                ) {
                    val q = query.trim()
                    val kalendarMissing = apps.none { it.label.contains("kalendar", ignoreCase = true) }
                    val kutoutMissing = apps.none { it.label.contains("kutout", ignoreCase = true) }
                    val entries = ArrayList<ListEntry>()
                    if (pickFor == null) {
                        if ("kames games chess tic-tac-toe bagh-chal chungi keshey ludo".contains(q, ignoreCase = true))
                            entries.add(ListEntry("Kames", "kames", "Tic-Tac-Toe, Chess, Bagh-chal, Chungi, Keshey, Ludo", null))
                        if ("kotepad notes notepad".contains(q, ignoreCase = true)) entries.add(ListEntry("Kotepad", "kotepad", "Your notes", null))
                        if (kalendarMissing && "kalendar by kasign".contains(q, ignoreCase = true))
                            entries.add(ListEntry("Kalendar", "kalendar", "by Kasign · tap to get the app", null))
                        if (kutoutMissing && "kutout background remover".contains(q, ignoreCase = true))
                            entries.add(ListEntry("Kutout", "kutout", "Free background remover · tap to get the app", null))
                        if (mojeekMissing && "mojeek search engine".contains(q, ignoreCase = true))
                            entries.add(ListEntry("Mojeek", "mojeek", "Get the alternative search engine", null))
                        if ("weekly report screen time".contains(q, ignoreCase = true))
                            entries.add(ListEntry("Weekly report", "weekly", "Your week on the phone", null))
                        if ("kettings settings".contains(q, ignoreCase = true)) entries.add(ListEntry("Kettings", "settings", "Kauncher and phone", null))
                        quickNoteOf(q)?.let { entries.add(ListEntry("Save note: " + it.take(40), "qnote:$it", "Saved in Kotepad", null)) }
                        quickTimerOf(q)?.let { entries.add(ListEntry("Start a $it min timer", "qtimer:$it", "Uses your phone's clock", null)) }
                    }
                    for (a in apps) if (a.label.contains(query, ignoreCase = true) && (query.isNotBlank() || a.pkg !in quietSet || pickFor != null)) entries.add(ListEntry(a.label, null, null, a))
                    // A to Z. With "Kauncher items on top" switched on, Kauncher's own rows come first (also A to Z).
                    val ordered = if (kTop) entries.sortedWith(compareBy({ it.special == null }, { it.label.lowercase() }))
                                  else entries.sortedBy { it.label.lowercase() }
                    items(ordered) { e ->
                        val app = e.app
                        if (app != null) {
                            Text(
                                app.label, color = ink, fontSize = 28.sp, fontWeight = FontWeight.Light,
                                modifier = Modifier.fillMaxWidth()
                                    .pointerInput(app.pkg) {
                                        detectTapGestures(
                                            onTap = {
                                                val pf = pickFor
                                                if (pf != null) { addToFolder(pf, app.pkg); drawer = false; query = ""; pickFor = null }
                                                else if (launchApp(app.pkg)) { drawer = false; query = "" }
                                            },
                                            onLongPress = { if (pickFor == null) appMenu = app }
                                        )
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        } else {
                            Column(
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        drawer = false; query = ""
                                        when (e.special) {
                                            "kames" -> kames = true
                                            "mojeek" -> openMojeek(ctx)
                                            "kotepad" -> { kotepadSketch = false; kotepadNew = false; kotepad = true }
                                            "settings" -> settingsOpen = true
                                            "weekly" -> weeklyOpen = true
                                            "kalendar" -> try {
                                                ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://kartproduction.github.io/kalendarbykasign/download.html")))
                                            } catch (ex: Exception) { }
                                            "kutout" -> try {
                                                ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://kartproduction.github.io/kutout/#get")))
                                            } catch (ex: Exception) { }
                                            else -> {
                                                val sp = e.special ?: ""
                                                if (sp.startsWith("qnote:")) { saveQuickNote(prefs, sp.removePrefix("qnote:")); Toast.makeText(ctx, "Saved to Kotepad", Toast.LENGTH_SHORT).show() }
                                                else if (sp.startsWith("qtimer:")) { if (!startPhoneTimer(ctx, sp.removePrefix("qtimer:").toIntOrNull() ?: 0)) Toast.makeText(ctx, "Could not start a timer", Toast.LENGTH_SHORT).show() }
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(e.label, color = ink, fontSize = 28.sp, fontWeight = FontWeight.Light)
                                if (e.sub != null) Text(e.sub, color = mute, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
        val am = appMenu
        if (am != null) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { appMenu = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.padding(32.dp).clip(RoundedCornerShape(24.dp)).background(surface)
                        .pointerInput(Unit) { detectTapGestures { } }.padding(24.dp)
                ) {
                    Text(am.label, color = ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(if (am.pkg in pauseSet) "Stop pausing before it opens" else "Pause before opening", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            pauseSet = if (am.pkg in pauseSet) pauseSet - am.pkg else pauseSet + am.pkg
                            saveSet(prefs, "pauseApps", pauseSet); appMenu = null
                        }.padding(vertical = 10.dp))
                    Text("Daily limit: " + hrsText(appLimitMin(prefs, am.pkg)), color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            saveAppLimits(prefs, loadAppLimits(prefs) + (am.pkg to nextOption(APP_LIMIT_OPTIONS, appLimitMin(prefs, am.pkg))))
                            appMenu = null
                        }.padding(vertical = 10.dp))
                    Text(if (am.pkg in quietSet) "Show in All apps again" else "Make quiet (hide until searched)", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            quietSet = if (am.pkg in quietSet) quietSet - am.pkg else quietSet + am.pkg
                            saveSet(prefs, "quietApps", quietSet); appMenu = null
                        }.padding(vertical = 10.dp))
                    Text("Uninstall", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable { uninstallApp(ctx, am.pkg); appMenu = null }.padding(vertical = 10.dp))
                    Text("App info", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable { appInfo(ctx, am.pkg); appMenu = null }.padding(vertical = 10.dp))
                    Text("Cancel", color = mute, fontSize = 18.sp,
                        modifier = Modifier.clickable { appMenu = null }.padding(vertical = 10.dp))
                }
            }
        }
        val fo = openFolder
        if (fo != null && !drawer && folders.containsKey(fo)) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { openFolder = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.padding(32.dp).clip(RoundedCornerShape(24.dp)).background(surface)
                        .pointerInput(Unit) { detectTapGestures { } }.padding(24.dp)
                ) {
                    Text(fo, color = ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        glyphs.forEach { g ->
                            val on = (icons[fo] ?: "●") == g
                            Text(
                                if (g == "Hidden") "Hide" else g,
                                color = if (on) accent else mute, fontSize = 20.sp,
                                modifier = Modifier.clickable { setIcon(fo, g) }.padding(end = 16.dp, top = 4.dp, bottom = 8.dp)
                            )
                        }
                    }
                    val members = folders[fo] ?: emptyList()
                    members.forEach { pkg ->
                        val label = apps.firstOrNull { it.pkg == pkg }?.label
                        if (label != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    label, color = ink, fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                        .clickable { if (launchApp(pkg)) openFolder = null }.padding(vertical = 10.dp)
                                )
                                Text(
                                    "✕", color = mute, fontSize = 16.sp,
                                    modifier = Modifier.clickable { saveFolders(folders + (fo to members.filter { it != pkg })) }.padding(10.dp)
                                )
                            }
                        }
                    }
                    Text(
                        "+ Add an app", color = accent, fontSize = 16.sp,
                        modifier = Modifier.clickable { pickFor = fo; drawer = true }.padding(vertical = 10.dp)
                    )
                    Text(
                        "Delete folder", color = mute, fontSize = 14.sp,
                        modifier = Modifier.clickable { saveFolders(folders - fo); openFolder = null }.padding(vertical = 10.dp)
                    )
                }
            }
        }
        if (naming) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { naming = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.padding(32.dp).clip(RoundedCornerShape(24.dp)).background(surface)
                        .pointerInput(Unit) { detectTapGestures { } }.padding(24.dp)
                ) {
                    Text("New folder", color = ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    BasicTextField(
                        value = newName, onValueChange = { newName = it.take(20) }, singleLine = true,
                        textStyle = TextStyle(color = ink, fontSize = 20.sp),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        decorationBox = { inner ->
                            Box {
                                if (newName.isEmpty()) Text("Folder name", color = mute, fontSize = 20.sp)
                                inner()
                            }
                        }
                    )
                    Box(Modifier.fillMaxWidth().height(1.5.dp).background(line))
                    Text(
                        "Create", color = accent, fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            val n = newName.trim()
                            if (n.isNotEmpty() && !folders.containsKey(n)) {
                                saveFolders(folders + (n to emptyList<String>())); openFolder = n
                            }
                            naming = false; newName = ""
                        }.padding(top = 14.dp, bottom = 4.dp)
                    )
                }
            }
        }
        if (showMojeek) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { closeMojeek() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.padding(32.dp).clip(RoundedCornerShape(24.dp)).background(surface)
                        .pointerInput(Unit) { detectTapGestures { } }.padding(24.dp)
                ) {
                    Text("Try Mojeek", color = ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("The alternative search engine that puts the people who use it first.", color = mute, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Mojeek on Google Play", color = accent, fontSize = 15.sp,
                        modifier = Modifier.clickable { openMojeek(ctx); closeMojeek() }.padding(vertical = 6.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text(
                            "Get Mojeek", color = accent, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { openMojeek(ctx); closeMojeek() }.padding(vertical = 8.dp)
                        )
                        Text(
                            "Not now", color = mute, fontSize = 17.sp,
                            modifier = Modifier.clickable { closeMojeek() }.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
        if (settingsOpen && !running) SettingsScreen(
            ctx = ctx, ink = ink, mute = mute, surface = surface, line = line, accent = accent,
            artMin = artMin, lockMin = lockMin, autoStart = autoStart, pinLock = pinLock, themeMode = themeMode, h24 = h24, kTop = kTop, writeKotepad = writeKotepad, ownMedia = ownMedia,
            onArtMin = { artMin = it; prefs.edit().putInt("artMin", it).apply() },
            onLockMin = { lockMin = it; prefs.edit().putInt("lockMin", it).apply() },
            onAutoStart = { autoStart = it; prefs.edit().putBoolean("autoStart", it).apply() },
            onPinLock = { pinLock = it; prefs.edit().putBoolean("pinLock", it).apply() },
            onTheme = { themeMode = it; prefs.edit().putInt("themeMode", it).apply() },
            onH24 = { h24 = it; prefs.edit().putBoolean("h24", it).apply() },
            onKTop = { kTop = it; prefs.edit().putBoolean("kTop", it).apply() },
            onOwnMedia = { ownMedia = it; prefs.edit().putBoolean("ownMedia", it).apply() },
            onWriteKotepad = { writeKotepad = it; prefs.edit().putBoolean("writeKotepad", it).apply() },
            onBackup = { backupCode = makeBackup(prefs); backupMode = 1 },
            onRestore = { restoreText = ""; backupMode = 2 },
            focusSummary = focusSummary(prefs),
            onFocusLog = { focusLogOpen = true },
            onWeeklyReport = { weeklyOpen = true },
            onExtras = { extrasOpen = true },
            onClose = { settingsOpen = false }
        )
        if (extrasOpen && !running) ExtrasScreen(
            ctx = ctx, prefs = prefs, apps = apps, pauseSet = pauseSet, quietSet = quietSet,
            onPauseSet = { pauseSet = it; saveSet(prefs, "pauseApps", it) },
            onQuietSet = { quietSet = it; saveSet(prefs, "quietApps", it) },
            onRestoreCode = { restoreText = it; backupMode = 2; extrasOpen = false },
            ink = ink, mute = mute, surface = surface, line = line, accent = accent
        ) { extrasOpen = false }
        val pf = pauseFor
        if (pf != null) PauseScreen(pf.label, 10, ink, mute, surface, accent, note = pauseNote,
            intent = loadIntentions(prefs, LocalDate.now()).firstOrNull { !it.done }?.text,
            onOpen = { recordPause(prefs, true); pauseFor = null; openApp(ctx, pf.pkg) },
            onCancel = { recordPause(prefs, false); pauseFor = null })
        if (endCardMin > 0) SessionEndCard(
            minutes = endCardMin, askMade = askMadeOn(prefs), breakMin = breakMinutes(prefs),
            ink = ink, mute = mute, surface = surface, line = line, accent = accent,
            onSave = { addToLastFocusNote(prefs, it); endCardMin = 0 },
            onBreak = { addToLastFocusNote(prefs, it); breakMin = breakMinutes(prefs); endCardMin = 0 },
            onClose = { endCardMin = 0 }
        )
        if (breakMin > 0) BreakScreen(ctx, breakMin, chimeOn(prefs), ink, mute, surface, accent) { breakMin = 0 }
        if (weeklyOpen && !running) WeeklyReportScreen(ctx = ctx, ink = ink, mute = mute, surface = surface, line = line, accent = accent) { weeklyOpen = false }
        if (focusLogOpen) FocusLogScreen(prefs = prefs, ink = ink, mute = mute, surface = surface, line = line, accent = accent) { focusLogOpen = false }
        if (backupMode != 0) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { backupMode = 0 },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.padding(32.dp).clip(RoundedCornerShape(24.dp)).background(surface)
                        .pointerInput(Unit) { detectTapGestures { } }.padding(24.dp)
                ) {
                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    if (backupMode == 1) {
                        Text("Back up folders", color = ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Save this code somewhere safe, such as a note or a message to yourself. After a reinstall, choose Restore folders and paste it.",
                            color = mute, fontSize = 14.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(backupCode, color = mute, fontSize = 11.sp, maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Text(
                                "Copy", color = accent, fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    cm.setPrimaryClip(android.content.ClipData.newPlainText("Kauncher backup", backupCode))
                                    Toast.makeText(ctx, "Backup code copied", Toast.LENGTH_SHORT).show()
                                }.padding(vertical = 10.dp)
                            )
                            Text(
                                "Share", color = accent, fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, backupCode) }
                                    try { ctx.startActivity(Intent.createChooser(send, "Save backup code")) } catch (e: Exception) { }
                                }.padding(vertical = 10.dp)
                            )
                        }
                    } else {
                        Text("Restore folders", color = ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Paste your backup code. This replaces your current folders, notes and focus log.", color = mute, fontSize = 14.sp)
                        BasicTextField(
                            value = restoreText, onValueChange = { restoreText = it }, singleLine = true,
                            textStyle = TextStyle(color = ink, fontSize = 16.sp), cursorBrush = SolidColor(accent),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            decorationBox = { inner ->
                                Box { if (restoreText.isEmpty()) Text("KAUNCHER2:…", color = mute, fontSize = 16.sp); inner() }
                            }
                        )
                        Box(Modifier.fillMaxWidth().height(1.5.dp).background(line))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Text(
                                "Paste", color = accent, fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    val t = cm.primaryClip?.getItemAt(0)?.coerceToText(ctx)?.toString() ?: ""
                                    if (t.isNotBlank()) restoreText = t
                                }.padding(top = 14.dp, bottom = 4.dp)
                            )
                            Text(
                                "Restore", color = accent, fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    if (restoreBackup(prefs, restoreText)) {
                                        folders = loadFolders(prefs); icons = loadIcons(prefs); openFolder = null
                                        backupMode = 0
                                        Toast.makeText(ctx, "Folders restored", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(ctx, "That code doesn’t look right", Toast.LENGTH_SHORT).show()
                                    }
                                }.padding(top = 14.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        if (captureOpen) CaptureScreen(ctx = ctx, ink = ink, mute = mute, surface = surface, line = line, accent = accent) { captureOpen = false }
        if (recordOpen) RecordScreen(ctx = ctx, ink = ink, mute = mute, surface = surface, line = line, accent = accent, autoStart = recordAuto) { recordOpen = false; recordAuto = false }
        if (kotepad) Kotepad(ctx = ctx, prefs = prefs, startSketch = kotepadSketch, startNew = kotepadNew, ink = ink, mute = mute, surface = surface, line = line, accent = accent) { kotepad = false; kotepadNew = false }
        if (intentionsOpen) IntentionsScreen(prefs = prefs, ink = ink, mute = mute, surface = surface, line = line, accent = accent) { intentionsOpen = false }
        if (kames && !running) Kames(ctx = ctx, ink = ink, mute = mute, surface = surface, line = line, accent = accent, onPick = { k ->
            kames = false
            when (k) { "ttt" -> game = true; "chess" -> chess = true; "bagh" -> bagh = true; "ludo" -> ludo = true; "chungi" -> chungi = true; "keshey" -> keshey = true; else -> { } }
        }) { kames = false }
        if (bagh && !running) BaghChal(ink = ink, mute = mute, surface = surface, line = line, accent = accent) { bagh = false; kames = true }
        if (ludo && !running) Ludo(ink = ink, mute = mute, surface = surface, line = line, accent = accent) { ludo = false; kames = true }
        if (keshey && !running) Keshey(ink = ink, mute = mute, surface = surface, line = line) { keshey = false; kames = true }
        if (chungi && !running) Chungi(ink = ink, mute = mute, surface = surface, line = line) { chungi = false; kames = true }
        if (chess && !running) Chess(surface = surface, ink = ink, mute = mute, line = line) { chess = false; kames = true }
        if (game && !running) TicTacToe(surface = surface, ink = ink, mute = mute, line = line) { game = false; kames = true }
        if (menu) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { menu = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.padding(32.dp).clip(RoundedCornerShape(24.dp)).background(surface)
                        .pointerInput(Unit) { detectTapGestures { } }.padding(24.dp)
                ) {
                    Text("Background", color = ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Choose a photo", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable { menu = false; picker.launch("image/*") }.padding(vertical = 10.dp)
                    )
                    if (photo != null) {
                        Text(
                            "Set as lock screen", color = ink, fontSize = 18.sp,
                            modifier = Modifier.clickable {
                                menu = false
                                val ok = try {
                                    val bmp = BitmapFactory.decodeFile(bgFile(ctx).path)
                                    WallpaperManager.getInstance(ctx).setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
                                    true
                                } catch (e: Exception) { false }
                                Toast.makeText(ctx, if (ok) "Lock screen updated" else "Couldn’t change the lock screen", Toast.LENGTH_SHORT).show()
                            }.padding(vertical = 10.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        swatches.forEachIndexed { i, sw ->
                            Box(
                                Modifier.padding(end = 12.dp).size(40.dp).clip(CircleShape).background(sw.first)
                                    .border(1.5.dp, line, CircleShape).clickable { pickSwatch(i) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Reset to default", color = mute, fontSize = 15.sp,
                        modifier = Modifier.clickable { pickSwatch(-1) }.padding(vertical = 10.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Kames", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable { menu = false; kames = true }.padding(vertical = 10.dp)
                    )
                    Text(
                        "Back up folders", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable { menu = false; backupCode = makeBackup(prefs); backupMode = 1 }.padding(vertical = 10.dp)
                    )
                    Text(
                        "Restore folders", color = ink, fontSize = 18.sp,
                        modifier = Modifier.clickable { menu = false; restoreText = ""; backupMode = 2 }.padding(vertical = 10.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Kauncher ${appVersion(ctx)} · © 2026 Kart Production. All rights reserved. Official download only: kartproduction.github.io/kauncher",
                        color = mute, fontSize = 11.sp, lineHeight = 15.sp
                    )
                }
            }
        }
    }
    }
}
