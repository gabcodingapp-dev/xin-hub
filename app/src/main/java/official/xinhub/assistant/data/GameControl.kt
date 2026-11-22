package official.xinhub.assistant.data

import android.content.Context
import android.content.Intent

/**
 * Game lifecycle around asset writes. MLBB caches imported unity3d bundles in memory, so a
 * successful write to disk is invisible until the game process is restarted.
 */
object GameControl {
    /** Packages queried by the reference MLBB companion app. */
    val PACKAGES = listOf(
        "com.mobile.legends",
        "com.mobile.legends.am",
        "com.mobile.legends.beta",
        "com.mobile.legends.bil",
        "com.mobile.legends.ft",
        "com.mobile.legends.in",
        "com.mobile.legends.lite",
        "com.mobile.legends.mi",
        "com.mobile.legends.us",
        "com.mobile.legends.usa",
        "com.mobilelegends.hwag",
        "com.mobilelegends.taptest",
        "com.moonton.mlbb.cn",
        "com.hhgame.mlbbvn",
        "com.vng.mlbbvn",
        "com.dfjz.moba",
        "com.dfjz.moba.mi",
        "com.mobiin.gp",
    )

    fun installed(ctx: Context): List<String> = PACKAGES.filter { pkg ->
        try {
            ctx.packageManager.getPackageInfo(pkg, 0) != null
        } catch (t: Throwable) {
            false
        }
    }

    /** Force-stop every installed variant. Returns false when no shell route is available. */
    fun stop(storage: Storage, ctx: Context): Boolean {
        val packages = installed(ctx)
        if (packages.isEmpty()) return false
        val runner = storage.shell ?: return false
        val cmd = packages.joinToString("; ") { "am force-stop $it" }
        val result = runner(cmd) ?: return false
        return result.first == 0
    }

    /** Launch the first installed variant that exposes a launcher activity. */
    fun launch(ctx: Context): Boolean {
        for (pkg in installed(ctx)) {
            val intent = try {
                ctx.packageManager.getLaunchIntentForPackage(pkg)
            } catch (t: Throwable) {
                null
            } ?: continue
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                ctx.startActivity(intent)
                return true
            } catch (t: Throwable) {
                // Try the next installed variant.
            }
        }
        return false
    }
}
