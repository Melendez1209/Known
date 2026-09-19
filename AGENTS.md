# AGENTS.md

Communicate with the user in Simplified Chinese, but, as a matter of open-source etiquette, write comments, commit messages, Markdown files and similar content in British English.

## Git operations (mandatory rule)

- **Every Git operation that changes state - staging, commit, push, pull, merge, rebase, branch, stash, reset, tag, cherry-pick, etc. - requires explicit user confirmation first.** Always ask before running any mutating git command; never commit or push on your own initiative.
- Reviewing past records is always allowed without confirmation: read-only commands such as `git log`, `git status`, `git diff`, `git show`, `git blame`.
- Active development branch for Android work is `dev_android`, not `main` (check `git status` - it may be ahead of `origin/dev_android`).
- Pushes to `main` containing changes under `Web/` trigger the GitHub Pages deploy workflow (`.github/workflows/static.yml`). Treat pushes to `main` with extra care.

## Repository layout (monorepo, single git repo at root)

- `Android/` - the main workspace: Gradle project with two modules, `mobile` (phone app, package `com.melendez.known`) and `wear` (Wear OS app). Most work happens here.
- `HarmonyOS/` - HarmonyOS app (hvigor build); `Web/` - static Vite site. Usually out of scope for Android work.

## Android specifics

- Build with the Gradle wrapper on Windows: `.\gradlew.bat`. Useful tasks: `:mobile:assembleDebug`, `:mobile:testDebugUnitTest`, `:wear:assembleDebug`.
- Run one unit test with `--tests`, e.g. `.\gradlew.bat :mobile:testDebugUnitTest --tests "com.melendez.known.util.SettingsCoreKtTest"`.
- Dependency versions live only in `gradle/libs.versions.toml` - add/update there, never inline.
- Toolchain: AGP `9.4.0-alpha08`, Kotlin `2.4.0`, Gradle `9.6.1`, JDK 17 toolchain, compileSdk 37 / targetSdk 36, minSdk 29 (mobile) / 30 (wear). "Stable" version suggestions will usually be wrong here; match existing versions.
- UI: Jetpack Compose + Material 3 (Material Expressive). `mobile` navigates with androidx Navigation3 (`androidx.navigation3`), not classic navigation-compose.
- `mobile` uses Room + KSP (`data/` package), Firebase (auth/firestore/messaging/analytics), AdMob, Coil.
- `mobile/google-services.json` is gitignored (root `.gitignore`); a clean clone will not build `mobile` until that file is restored.
- Secrets: `mobile/build.gradle.kts` embeds an API key via `buildConfigField` in both build types. Never add new secrets, and never log or echo existing ones.
- `local.properties` (SDK path) is gitignored - do not commit or reference it.
- No CI/lint gate for Android: verify changes with the unit test task and `assembleDebug`.
- In Compose, use `stringResource()` instead of `context.getString()` for querying string resources; `LocalContext.current` should only be used for Android framework APIs (e.g., `Toast`, `Intent`), not for resource queries.

## Style (see CONTRIBUTING.md for full spec)

- CRLF line endings in the working tree (repo stores LF via `.gitattributes` `text=auto`); end every file with a blank line.
- Run Android Studio's code cleanup on changed files before finishing.
- Compose calls with multi-line arguments place the trailing lambda on its own line after `) {` (see CONTRIBUTING.md examples).
- Comments: standard English, `//` with a space after the symbol, no trailing period.
