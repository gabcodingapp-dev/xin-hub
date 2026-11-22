package official.nixhub.assistant

import android.app.Application
import android.content.Context
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

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        app = context.applicationContext
        prefs = Prefs(app)
        catalog = CatalogStore(app)
        injector = Injector(app, prefs)
    }
}

class XinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Xin.init(this)
    }
}
