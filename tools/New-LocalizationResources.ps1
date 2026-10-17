# Converts the Android string resources into .NET .resx files for Known.Core.
#
# Android keeps the source of truth in res/values*/strings.xml. Rather than maintaining a second
# translation set by hand, this regenerates the .resx files from those files. Run it after any
# Android string change:
#
#   .\tools\New-LocalizationResources.ps1
#
param(
    [string]$AndroidRes = "Android\mobile\src\main\res",
    [string]$Overlay = "tools\winui-strings.xml",
    [string]$OutputDir = "Winui3\src\Known.Core\Localization"
)

$ErrorActionPreference = 'Stop'

# The neutral file is the default language, the rest become satellite assemblies.
#
# Note the Chinese pair: Android's generic values-zh folder holds Traditional Chinese in this
# project, while values-zh-rCN holds Simplified. The names below follow the script actually stored
# in each folder rather than the folder name.
$locales = @(
    @{ Source = 'values';          Target = 'AppStrings.resx';          Language = 'en-US' },
    @{ Source = 'values-en-rGB';   Target = 'AppStrings.en-GB.resx';    Language = 'en-GB' },
    @{ Source = 'values-zh-rCN';   Target = 'AppStrings.zh-Hans.resx';  Language = 'zh-Hans' },
    @{ Source = 'values-zh';       Target = 'AppStrings.zh-Hant.resx';  Language = 'zh-Hant' }
)

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

function Escape-Xml([string]$text) {
    if ($null -eq $text) { return '' }
    $text.Replace('&', '&amp;').Replace('<', '&lt;').Replace('>', '&gt;').Replace('"', '&quot;')
}

# Android's resource compiler understands backslash escapes, which XML does not: a string such as
# "Couldn\'t" is stored with the backslash intact and unescaped only by aapt. Reading the attribute
# with XmlDocument therefore hands back the literal backslash, which then reaches the interface as
# "Couldn\'t". Only the escapes this project actually uses are handled.
function Unescape-Android([string]$text) {
    if ($null -eq $text) { return '' }
    return $text.Replace("\'", "'").Replace('\"', '"').Replace('\n', "`n").Replace('\t', "`t")
}

foreach ($locale in $locales) {
    $sourcePath = Join-Path $AndroidRes "$($locale.Source)\strings.xml"

    if (-not (Test-Path $sourcePath)) {
        throw "Missing source file: $sourcePath"
    }

    [xml]$document = Get-Content -Raw -LiteralPath $sourcePath

    $entries = [ordered]@{}
    $skipped = 0

    foreach ($node in $document.resources.string) {
        $name = $node.name

        if (-not $name) { continue }

        # Strings flagged translatable="false" are brand names such as README and only belong in
        # the neutral file, otherwise each satellite would have to repeat them
        if ($node.GetAttribute('translatable') -eq 'false' -and $locale.Source -ne 'values') {
            $skipped++
            continue
        }

        $value = if ($node -is [System.Xml.XmlCDataSection]) { $node.Value } else { $node.InnerText }
        $entries[$name] = Unescape-Android $value
    }

    # Desktop only labels, which the Android resources have no equivalent for
    $overlaid = 0

    if (Test-Path $Overlay) {
        [xml]$overlayDocument = Get-Content -Raw -LiteralPath $Overlay

        # Collected separately before anything is applied. A bare element carries the neutral text and
        # a tagged one carries a translation of it, and they sit next to each other in the document.
        # Applying them as they were read would let the neutral one always win, because it is written
        # first and the translation would then find the key already present.
        $overlayNeutral = @{}
        $overlayTranslated = @{}

        foreach ($node in $overlayDocument.strings.string) {
            $name = $node.name

            if (-not $name) { continue }

            $nodeLocale = $node.GetAttribute('locale')

            if ($nodeLocale -eq '') {
                $overlayNeutral[$name] = $node.InnerText
                continue
            }

            if (-not $overlayTranslated.ContainsKey($name)) { $overlayTranslated[$name] = @{} }

            $overlayTranslated[$name][$nodeLocale] = $node.InnerText
        }

        foreach ($name in $overlayNeutral.Keys) {
            $entries[$name] = $overlayNeutral[$name]
        }

        foreach ($name in $overlayTranslated.Keys) {
            # The outer key is the string name, so the language is looked up one level in
            if ($overlayTranslated[$name].ContainsKey($locale.Language)) {
                $entries[$name] = $overlayTranslated[$name][$locale.Language]
                $overlaid++
            }
        }
    }

    $lines = @(
        '<?xml version="1.0" encoding="utf-8"?>',
        '<root>',
        '  <resheader name="resmimetype">',
        '    <value>text/microsoft-resx</value>',
        '  </resheader>',
        '  <resheader name="version">',
        '    <value>2.0</value>',
        '  </resheader>',
        '  <resheader name="reader">',
        '    <value>System.Resources.ResXResourceReader, System.Windows.Forms, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089</value>',
        '  </resheader>',
        '  <resheader name="writer">',
        '    <value>System.Resources.ResXResourceWriter, System.Windows.Forms, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089</value>',
        '  </resheader>'
    )

    # Two space indents, which is what the script has always written. The checked in files were
    # last touched by Visual Studio, which reformats them, so a regenerated file will always show a
    # large whitespace diff until it is opened and saved there too
    foreach ($name in $entries.Keys) {
        $lines += "  <data name=`"$name`" xml:space=`"preserve`">"
        $lines += "    <value>$(Escape-Xml $entries[$name])</value>"
        $lines += '  </data>'
    }

    $lines += '</root>'
    $lines += ''

    $targetPath = Join-Path $OutputDir $locale.Target
    # CRLF, to match the rest of the working tree
    [System.IO.File]::WriteAllText($targetPath, ($lines -join "`r`n"), [System.Text.UTF8Encoding]::new($false))

    Write-Host ("{0,-24} {1,4} strings ({2} skipped, {3} overlaid) -> {4}" -f `
        $locale.Target, $entries.Count, $skipped, $overlaid, $locale.Language)
}
