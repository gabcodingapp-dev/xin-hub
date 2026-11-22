package official.nixhub.assistant.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import official.nixhub.assistant.Xin
import official.nixhub.assistant.data.Catalog
import official.nixhub.assistant.data.Injector
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.data.LoadState
import official.nixhub.assistant.data.RootStorage
import official.nixhub.assistant.data.SafStorage
import official.nixhub.assistant.data.Storage

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
            storage = RootStorage(RootStorage.ASSETS)
            message = "Connected · root"
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
        progress = Injector.Progress(running = true, label = "downloading")
        scope.launch {
            val result = Xin.injector.apply(target, job) { progress = it }
            progress = result
            if (result.error != null) {
                message = result.error
            } else {
                message = "${job.title} applied · ${result.done} files"
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
        progress = Injector.Progress(running = true, label = "restoring")
        scope.launch {
            val result = Xin.injector.restore(target, jobId) { progress = it }
            progress = result
            message = result.error ?: "Restored original files"
        }
    }

    fun restoreAll() {
        val target = storage
        if (target == null) {
            message = "Connect the game folder first"
            return
        }
        if (progress.running) return
        progress = Injector.Progress(running = true, label = "restoring everything")
        scope.launch {
            val result = Xin.injector.restoreAll(target) { progress = it }
            progress = result
            message = result.error ?: "Every backup restored"
        }
    }

    fun dismissProgress() {
        if (!progress.running) progress = Injector.Progress()
    }

    fun say(text: String) {
        message = text
    }
}
