# XIN HUB

Android skin, effect and lobby injector for Mobile Legends. Built as a native Kotlin + Compose
app, signed with the repo keystore, assembled by GitHub Actions on every push.

```
package   official.nixhub.assistant
version   1.0.0 (1)
minSdk    26
target    35
```

## What it does

| Surface | Behaviour |
|---|---|
| Hero skins | 1550 catalogued, 743 with a live script. Search, hero filter, tag filter, ready-only filter. |
| Effects | Recalls, avatar borders, emotes, respawns, trails, eliminations, radiant kits, flying vehicles. |
| Loading intros | 487 animated intros with previews, split by category. |
| Lobby sounds | 9 tracks, streamed preview, one-tap apply. |
| Collections | 88 release tags from the asset catalog. |
| Library | Every applied item is listed with a per-item restore and a restore-all. |
| Guides | Setup walkthrough with screenshots. |
| Catalog sync | Bundled snapshot loads instantly, remote manifest refreshes in the background. |

## Storage backends

- **SAF tree** — pick `Android/data/com.mobile.legends/files`. Persistable grant, no root.
- **Root** — `su` shell writes straight into `dragon2017/assets`.

Both are behind the `Storage` interface, so the injector code is identical for either.

## Injection model

A script archive is a tree rooted at `dragon2017/assets` (`Art/...`, `Document/...`,
`Interface/...`). Before any file is overwritten, the original is copied into
`filesDir/backups/<jobId>/<relative path>`. Restore walks that tree and writes it back, so it
never depends on the game redownloading assets.

## Build

```bash
gradle :app:assembleDebug
gradle :app:assembleRelease
```

CI does the same on `ubuntu-latest` with JDK 17 and Gradle 8.9. Tag `v1.0.1` to publish a
release with the APKs attached.

## Signing

`keystore/xin-release.jks`, alias `xinhub`, store and key password `xinhub2026`. Keep this file
in the repo so every build upgrades over the last one. Lose it and updates require a reinstall.

## Assets

Catalog and art live in a separate repo: `gabcodingapp-dev/xin-assets`. The app reads
`manifest.json` and the files under `catalog/`. A snapshot of that catalog is bundled in
`app/src/main/assets/catalog` so first launch works offline.

## Credits

Developed by Gab.
