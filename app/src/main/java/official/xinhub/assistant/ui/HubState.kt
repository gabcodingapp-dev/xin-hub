package official.xinhub.assistant.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.Catalog
import official.xinhub.assistant.data.GameControl
import official.xinhub.assistant.data.Injector
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.data.LoadState
import official.xinhub.assistant.data.RootStorage
import official.xinhub.assistant.data.SafStorage
import official.xinhub.assistant.data.ShizukuAccess
import official.xinhub.assistant.data.Storage

/** Single source of truth for the shell: catalog, storage binding, running jobs. */
class HubState(private val ctx: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var load by mutableStateOf<LoadState>(LoadState.Idle)
        private set

    var storage by mutableStateOf<Storage?>(null)
        private set

    var progress by mutableStateOf(Injector.Progress())
        private set

    var refreshing by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)

    var shizuku by mutableStateOf(ShizukuAccess.status(ctx))
        private set

    fun refreshShizuku() {
        shizuku = ShizukuAccess.status(ctx)
    }

    val catalog: Catalog get() = (load as? LoadState.Ready)?.catalog ?: Catalog.EMPTY

    fun boot() {
        scope.launch {
            if (load is LoadState.Idle) {
                load = LoadState.Loading
                load = Xin.catalog.load()
            }
            reconnect()
            refresh(silent = true)
        }
    }

    fun refresh(silent: Boolean = false) {
        if (refreshing) return
        refreshing = true
        scope.launch {
            val updated = Xin.catalog.refresh()
            val result = Xin.catalog.load()
            load = result
            refreshing = false
            if (!silent && updated) message = "Catalog updated"
            if (!silent && !updated) message = "Catalog already current"
        }
    }

    fun connectSaf(uri: Uri) {
        scope.launch {
            val opened = SafStorage.fromUri(ctx, uri)
            if (opened == null) {
                message = "Pick Android/data/com.mobile.legends/files"
                return@launch
            }
            if (!opened.writable) {
                message = "That folder is read-only. Pick it again."
                return@launch
            }
            Xin.prefs.storageUri = uri.toString()
            Xin.prefs.useRoot = false
            storage = opened
            message = "Connected · ${opened.label}"
        }
    }

    fun connectRoot() {
        scope.launch {
            val ok = RootStorage.available()
            if (!ok) {
                message = "No root shell on this device"
                return@launch
            }
            Xin.prefs.useRoot = true
            Xin.prefs.useShizuku = false
            storage = RootStorage(RootStorage.ASSETS)
            message = "Connected · root"
        }
    }

    fun connectShizuku() {
        refreshShizuku()
        if (!shizuku.installed) {
            message = "Install Shizuku first, then come back"
            return
        }
        if (!shizuku.running) {
            message = "Open Shizuku and start the service"
            return
        }
        if (!shizuku.granted) {
            ShizukuAccess.request()
            message = "Approve the Shizuku prompt"
            return
        }
        Xin.prefs.useShizuku = true
        Xin.prefs.useRoot = false
        Xin.prefs.storageUri = null
        storage = ShizukuAccess.assets()
        message = "Connected · shizuku"
    }

    fun onShizukuResult() {
        refreshShizuku()
        if (shizuku.granted) {
            Xin.prefs.useShizuku = true
            Xin.prefs.useRoot = false
            Xin.prefs.storageUri = null
            storage = ShizukuAccess.assets()
            message = "Shizuku access granted"
        } else {
            message = "Shizuku permission denied"
        }
    }

    fun importArchive(uri: Uri) {
        val target = storage
        if (target == null) {
            message = "Connect the game folder first"
            return
        }
        if (progress.running) return
        scope.launch {
            progress = Injector.Progress(running = true, label = "reading zip")
            val label = resolveName(uri)
            val job = Xin.injector.stageLocal(ctx, uri, label)
            if (job == null) {
                progress = Injector.Progress(error = "that file is not a readable zip", finished = true)
                return@launch
            }
            val stopped = GameControl.stop(target, ctx)
            val result = Xin.injector.apply(target, job) { progress = it }
            val relaunched = result.error == null && GameControl.launch(ctx)
            progress = result
            message = result.error ?: run {
                val tail = if (stopped && relaunched) "MLBB reopened" else "restart MLBB to see it"
                "${job.title} applied · ${result.done} files · $tail"
            }
        }
    }

    private fun resolveName(uri: Uri): String {
        val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
        return try {
            ctx.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(projection[0])
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            } ?: "imported skin"
        } catch (t: Throwable) {
            "imported skin"
        }
    }

    fun disconnect() {
        storage = null
        Xin.prefs.storageUri = null
        Xin.prefs.useRoot = false
        message = "Storage disconnected"
    }

    private suspend fun reconnect() {
        if (storage != null) return
        refreshShizuku()
        if (Xin.prefs.useShizuku) {
            if (ShizukuAccess.granted()) storage = ShizukuAccess.assets()
            return
        }
        val uri = Xin.prefs.storageUri
        if (Xin.prefs.useRoot) {
            storage = RootStorage(RootStorage.ASSETS)
            return
        }
        if (uri != null) {
            val parsed = runCatching { Uri.parse(uri) }.getOrNull()
            if (parsed != null) storage = SafStorage.fromUri(ctx, parsed)
        }
    }

    fun apply(job: Job) {
        val target = storage
        if (target == null) {
            message = "Connect the game folder first"
            return
        }
        if (progress.running) return
        progress = Injector.Progress(running = true, phase = "closing game", label = "closing MLBB")
        scope.launch {
            val mirrored = Xin.catalog.mirrorUrl(job.id)
            val source = if (mirrored != null) job.copy(script = mirrored) else job
            val stopped = GameControl.stop(target, ctx)
            val result = Xin.injector.apply(target, source) { progress = it }
            val relaunched = result.error == null && GameControl.launch(ctx)
            progress = result
            if (result.error != null) {
                message = "Failed · ${result.error}"
            } else {
                val tail = when {
                    stopped && relaunched -> "MLBB reopened"
                    stopped -> "open MLBB to see it"
                    else -> "force-close MLBB, then reopen"
                }
                message = "Success · ${job.title} applied · ${result.done} files · $tail"
            }
        }
    }

    fun restore(jobId: String) {
        val target = storage
        if (target == null) {
            message = "Connect the game folder first"
            return
        }
        if (progress.running) return
        progress = Injector.Progress(running = true, phase = "closing game", label = "closing MLBB")
        scope.launch {
            val stopped = GameControl.stop(target, ctx)
            val result = Xin.injector.restore(target, jobId) { progress = it }
            val relaunched = result.error == null && GameControl.launch(ctx)
            progress = result
            message = result.error?.let { "Failed · $it" } ?: run {
                val tail = if (stopped && relaunched) "MLBB reopened" else "restart MLBB to see it"
                "Success · Restored original files · $tail"
            }
        }
    }

    fun restoreAll() {
        val target = storage
        if (target == null) {
            message = "Connect the game folder first"
            return
        }
        if (progress.running) return
        progress = Injector.Progress(running = true, phase = "closing game", label = "closing MLBB")
        scope.launch {
            val stopped = GameControl.stop(target, ctx)
            val result = Xin.injector.restoreAll(target) { progress = it }
            val relaunched = result.error == null && GameControl.launch(ctx)
            progress = result
            message = result.error?.let { "Failed · $it" } ?: run {
                val tail = if (stopped && relaunched) "MLBB reopened" else "restart MLBB to see it"
                "Success · Every backup restored · $tail"
            }
        }
    }

    fun dismissProgress() {
        if (!progress.running) progress = Injector.Progress()
    }

    fun say(text: String) {
        message = text
    }
}
