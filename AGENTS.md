# AGENTS.md

Monorepo for **Known** (知学情), an exam-score analysis app for students. Three independent platform apps that share no
code:

- `Web/` — marketing/landing site (Vite, plain HTML/CSS/JS, no framework). Most active work.
- `Android/` — Kotlin + Jetpack Compose (Material 3), Gradle Kotlin DSL, `mobile` + `wear` modules.
- `HarmonyOS/` — ArkTS app for DevEco Studio (hvigor build).

## Git workflow

- Default branch is `main`. Feature work happens on per-platform branches: `dev_web`, `dev_android`, `dev_harmony`,
  `dev_wear`. Currently checked out: `dev_web`.
- All Git commits require explicit human confirmation before running.
- Pushes to `main` touching `Web/**` trigger `.github/workflows/static.yml`, which deploys the **raw `Web/` directory**
  to GitHub Pages — there is no build step, so `src/`/`css/` edits go live as-is after the push. `vite build` output
  (`dist/`) is not deployed.

## Web (`Web/`)

Commands (run in `Web/`):

- `npm run dev` — Vite dev server. Vite is only a dev server/build tool here; the site itself is static HTML.
- `npm run build` / `npm run preview`
- No tests, no lint, no typecheck. Only Prettier for formatting (`.prettierrc`: single quotes, printWidth 80, tabWidth
  2).

Gotchas:

- `src/script.js` is the real UI logic (theme, language, particles, changelog timeline).
- `src/main.js` is leftover WebStorm counter boilerplate. Its top-level
  `setupCounter(document.getElementById('counter-value'))` throws at load because no `#counter-value` element exists,
  which also prevents its `DOMContentLoaded` handler from registering. Don't build on it.
- All UI strings are in `assets/values/translations.json` (locales `zh`, `zh-tw`, `en`, `en-GB`), applied via
  `data-i18n` attributes. Theme/lang dropdown state persists in `localStorage`.
- The changelog section is fetched live from the GitHub API (`Melendez1209/Known`: releases, then commits fallback) — it
  is not stored in the repo.
- `404.html` also loads `src/i18n.js` and shares the same translations file.

## Android (`Android/`)

- Jetpack Compose + Material 3, Gradle Kotlin DSL, modules `mobile` and `wear`. Build with `gradlew.bat`/`./gradlew`;
  developed in Android Studio.

## HarmonyOS (`HarmonyOS/`)

- DevEco Studio project (ArkTS, hvigor). Requires the DevEco toolchain.

## Project conventions (from `CONTRIBUTING.md`)

- Audience is minors — never add porn/violence/gambling/drugs content anywhere in the repo.
- Use **CRLF** line endings and leave a blank line at the end of every file.
- Code comments in standard English; a space before and after `//`, no trailing full stop.
- Android: run Android Studio's code cleanup on changed files before committing.
