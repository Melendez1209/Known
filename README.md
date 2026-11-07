# Known

![Known logo](Logo.png)

**知学情 (Known)** is a free exam-score analysis app for students. It helps you record exams,
visualise your progress across subjects and stay motivated. The app is local first: your data
never leaves your device.

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Features

- **Exam history** — Record every exam with its date, subject scores and full marks. The history
  list offers three densities (list, compact and standard), a favourite filter, full-text search
  and multi-select with a marquee, so a whole term can be tidied in a couple of gestures.
- **Dashboard** — A trend chart and a per-subject bar chart summarise the exams you have already
  recorded. Each chart can switch between raw marks and a percentage, and the choice is remembered.
- **Detailed analysis** — Every exam has a pie chart of its subject split, a comparison chart that
  places it against the rest of your history, and a signed delta for each subject compared with
  its historical average.
- **Favourites and sharing** — Pin the exams you care about and share a summary or the full detail
  sheet through the Windows share sheet.
- **Local first** — Exams, settings and preferences live in a local database. There is no account
  and no telemetry. The only network request is the optional city lookup, which is described in
  the privacy policy.
- **Personalisation** — Follow the system theme or force light or dark, and switch the interface
  between English (United States), English (United Kingdom), 简体中文 and 繁體中文.

## Platforms

Known is a monorepo of independent apps that share no code. Each platform follows its own
official design language.

| Platform | Directory | Tech stack | Status |
| --- | --- | --- | --- |
| Windows | `Winui3/` | WinUI 3, Windows App SDK, EF Core, SkiaSharp | In development |
| Android | `Android/` | Kotlin, Jetpack Compose, Material 3, Room | In development |
| HarmonyOS | `HarmonyOS/` | ArkTS, DevEco Studio | In development |
| Web | `Web/` | Static HTML/CSS/JS, Vite | Live |

## Download

Grab the latest release from the [releases page](https://github.com/Melendez1209/Known/releases),
or visit the [project website](https://melendez1209.github.io/Known/) for the download links and
the changelog.

## Privacy

Known stores your exams in a local database on your device. There is no server, no analytics and
no third-party SDK that reads your data. The only feature that sends anything off the device is
the optional city lookup: when you ask the app to fill in your region, your approximate
coordinates are sent to a third-party geocoding service to be resolved into a city name, and are
never stored. The full policy is published at
<https://melendez1209.github.io/Known/privacy.html>.

## Build from source

Every platform has its own toolchain. The repository root contains the shared documentation;
`AGENTS.md` records the conventions and the commands for each app.

### Windows

Requirements: Windows 10 version 1809 or later, the .NET SDK, and the Windows App SDK.

```powershell
dotnet build Winui3\Known.slnx -c Debug -p:Platform=x64
dotnet test Winui3\tests\Known.Core.Tests\Known.Core.Tests.csproj
```

The app project requires an explicit platform because MSIX packaging cannot target `AnyCPU`.
`dotnet` is not always on `PATH`; on the development machine it lives at
`C:\Program Files\dotnet\dotnet.exe`.

### Android

Open `Android/` in Android Studio and build the `mobile` module, or use the Gradle wrapper:

```powershell
.\gradlew.bat :mobile:assembleDebug
.\gradlew.bat :mobile:testDebugUnitTest
```

### HarmonyOS

Open `HarmonyOS/` in DevEco Studio and build with hvigor. The DevEco toolchain is required.

### Web

The site is plain static HTML. Vite is only used as a dev server and a build tool.

```bash
npm run dev      # start the dev server
npm run build    # produce the static output
```

## Project structure

```
Known/
├── Android/      Kotlin + Jetpack Compose app (mobile and wear modules)
├── HarmonyOS/    ArkTS app for DevEco Studio
├── Winui3/       WinUI 3 desktop app, its Core library and the xUnit tests
├── Web/          Marketing site and privacy policy
├── AGENTS.md     Conventions and commands for every platform
├── CONTRIBUTING.md
└── LICENSE
```

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) first, and note that
the audience is mainly underage students: keep the content appropriate and follow the existing
code style (CRLF line endings, a blank line at the end of every file, and English comments with a
space after `//`).

## License

Known is released under the [MIT License](LICENSE).

