package official.xinhub.assistant.data

object Config {
    const val OWNER = "gabcodingapp-dev"
    const val APP_REPO = "xin-hub"
    const val ASSETS_REPO = "xin-assets"
    const val ASSETS_BASE = "https://raw.githubusercontent.com/$OWNER/$ASSETS_REPO/main/"
    const val MANIFEST_URL = ASSETS_BASE + "manifest.json"
    const val PROJECT_URL = "https://github.com/$OWNER/$APP_REPO"
    const val ASSETS_URL = "https://github.com/$OWNER/$ASSETS_REPO"
    const val DEV = "Gab"
    const val PACKAGE = "official.xinhub.assistant"
    const val VERSION = "1.6.0"
    const val TG_CHANNEL = "t.me/toolsdropdaw"
    const val TG_OWNER = "@xvanngdxc"
    const val TG_CHANNEL_URL = "https://t.me/toolsdropdaw"
    const val TG_OWNER_URL = "https://t.me/xvanngdxc"

    /** Restoration-protection pair shipped with the reference injector. */
    const val PROTECT_ON = ASSETS_BASE + "restore/enable_protection.zip"
    const val PROTECT_OFF = ASSETS_BASE + "restore/restore_normal.zip"

    /** GB loading-screen restore kit mirrored into this repo's asset channel. */
    const val GB_RESTORE_IMAGE = ASSETS_BASE + "restore/restore_intro.jpg"
    const val GB_RESTORE_SCRIPT = ASSETS_BASE + "restore/restore_original_intro.zip"

    const val PROTECTION_JOB = "restore-protection-on"
    const val PROTECTION_OFF_JOB = "restore-protection-off"
}
