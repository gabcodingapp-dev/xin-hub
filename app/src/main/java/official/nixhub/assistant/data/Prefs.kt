package official.nixhub.assistant.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("xin_hub", Context.MODE_PRIVATE)

    val applied = mutableStateOf(loadApplied())

    var storageUri: String?
        get() = sp.getString("storage_uri", null)
        set(value) {
            sp.edit().putString("storage_uri", value).apply()
            storageRevision.value = storageRevision.value + 1
        }

    var useRoot: Boolean
        get() = sp.getBoolean("use_root", false)
        set(value) {
            sp.edit().putBoolean("use_root", value).apply()
            storageRevision.value = storageRevision.value + 1
        }

    var useShizuku: Boolean
        get() = sp.getBoolean("use_shizuku", false)
        set(value) {
            sp.edit().putBoolean("use_shizuku", value).apply()
            storageRevision.value = storageRevision.value + 1
        }

    var firstRun: Boolean
        get() = sp.getBoolean("first_run", true)
        set(value) = sp.edit().putBoolean("first_run", value).apply()

    val storageRevision = mutableStateOf(0)

    fun isApplied(id: String): Boolean = applied.value.any { it.jobId == id }

    fun record(record: AppliedRecord) {
        val next = applied.value.filterNot { it.jobId == record.jobId } + record
        applied.value = next
        persist(next)
    }

    fun forget(jobId: String) {
        val next = applied.value.filterNot { it.jobId == jobId }
        applied.value = next
        persist(next)
    }

    fun clearApplied() {
        applied.value = emptyList()
        sp.edit().remove("applied").apply()
    }

    private fun persist(list: List<AppliedRecord>) {
        val arr = JSONArray()
        for (r in list) {
            arr.put(
                JSONObject()
                    .put("jobId", r.jobId)
                    .put("title", r.title)
                    .put("subtitle", r.subtitle)
                    .put("kind", r.kind)
                    .put("files", r.files)
                    .put("at", r.at)
            )
        }
        sp.edit().putString("applied", arr.toString()).apply()
    }

    private fun loadApplied(): List<AppliedRecord> {
        val raw = sp.getString("applied", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                AppliedRecord(
                    jobId = o.optString("jobId"),
                    title = o.optString("title"),
                    subtitle = o.optString("subtitle"),
                    kind = o.optString("kind"),
                    files = o.optInt("files"),
                    at = o.optLong("at"),
                )
            }
        } catch (t: Throwable) {
            emptyList()
        }
    }
}
