package official.nixhub.assistant

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import official.nixhub.assistant.data.CatalogStore
import official.nixhub.assistant.data.Injector
import official.nixhub.assistant.data.Prefs

object Xin {
    lateinit var app: Context
        private set

    lateinit var prefs: Prefs
        private set

    lateinit var catalog: CatalogStore
        private set

    lateinit var injector: Injector
        private set

    lateinit var images: ImageLoader
        private set

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        app = context.applicationContext
        prefs = Prefs(app)
        catalog = CatalogStore(app)
        injector = Injector(app, prefs)
        images = ImageLoader.Builder(app)
            .memoryCache { MemoryCache.Builder(app).maxSizePercent(0.18).build() }
            .diskCache {
                DiskCache.Builder()
                    .directory(app.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(80L * 1024 * 1024)
                    .build()
            }
            .crossfade(false)
            .respectCacheHeaders(true)
            .build()
    }
}

class XinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Xin.init(this)
    }
}
