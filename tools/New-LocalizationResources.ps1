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
        $entries[$name] = $value
    }

    # Desktop only labels, which the Android resources have no equivalent for
    $overlaid = 0

    if (Test-Path $Overlay) {
        [xml]$overlayDocument = Get-Content -Raw -LiteralPath $Overlay

        foreach ($node in $overlayDocument.strings.string) {
            $name = $node.name
            $nodeLocale = $node.GetAttribute('locale')

            if (-not $name) { continue }

            # A bare element is the neutral text; a tagged one only applies to that satellite and
            # falls back to the neutral value when the locale does not define it
            $applies = ($nodeLocale -eq '') -or ($nodeLocale -eq $locale.Language)

            if (-not $applies) { continue }

            if (($nodeLocale -eq '') -or (-not $entries.Contains($name))) {
                $entries[$name] = $node.InnerText

                if ($nodeLocale -ne '') { $overlaid++ }
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
