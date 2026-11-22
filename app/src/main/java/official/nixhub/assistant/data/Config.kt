package official.nixhub.assistant.data

object Config {
    const val OWNER = "gabcodingapp-dev"
    const val APP_REPO = "xin-hub"
    const val ASSETS_REPO = "xin-assets"
    const val ASSETS_BASE = "https://raw.githubusercontent.com/$OWNER/$ASSETS_REPO/main/"
    const val MANIFEST_URL = ASSETS_BASE + "manifest.json"
    const val PROJECT_URL = "https://github.com/$OWNER/$APP_REPO"
    const val ASSETS_URL = "https://github.com/$OWNER/$ASSETS_REPO"
    const val DEV = "Gab"
    const val PACKAGE = "official.nixhub.assistant"
    const val VERSION = "1.0.0"
}
