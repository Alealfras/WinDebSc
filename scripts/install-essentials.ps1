# ==============================================================================
# Script: install-essentials.ps1
# Descripcion: Instalador desatendido de aplicaciones esenciales mediante winget
#              (Windows Package Manager) desde repositorios oficiales verificados.
# ==============================================================================

param (
    [string]$Packages = ""
)

Write-Output "Iniciando modulo de instalacion de aplicaciones esenciales (winget)..."

# Comprobacion de disponibilidad de winget en el sistema
$wingetCmd = Get-Command "winget" -ErrorAction SilentlyContinue
if (-not $wingetCmd) {
    Write-Output "Error: 'winget' (Windows Package Manager) no esta disponible en este sistema."
    Write-Output "Asegurate de tener instalado 'App Installer' desde Microsoft Store o Windows 10/11 actualizado."
    exit 1
}

# Catalogo interactivo por categorias si no se pasaron parametros desde la GUI
$catalog = @(
    # Navegadores y Comunicacion
    @{ Category = "Navegadores y Comunicacion"; Id = "Mozilla.Firefox"; Name = "Mozilla Firefox" },
    @{ Category = "Navegadores y Comunicacion"; Id = "Brave.Brave"; Name = "Brave Browser" },
    @{ Category = "Navegadores y Comunicacion"; Id = "Discord.Discord"; Name = "Discord" },
    # Multimedia y Audio
    @{ Category = "Multimedia y Audio"; Id = "VideoLAN.VLC"; Name = "VLC Media Player" },
    @{ Category = "Multimedia y Audio"; Id = "OBSProject.OBSStudio"; Name = "OBS Studio" },
    @{ Category = "Multimedia y Audio"; Id = "VB-Audio.Voicemeeter"; Name = "Voicemeeter" },
    # Gaming y Launchers
    @{ Category = "Gaming y Launchers"; Id = "Valve.Steam"; Name = "Steam" },
    @{ Category = "Gaming y Launchers"; Id = "EpicGames.EpicGamesLauncher"; Name = "Epic Games Launcher" },
    # Hardware y Monitorizacion
    @{ Category = "Hardware y Monitorizacion"; Id = "REALiX.HWiNFO"; Name = "HWiNFO" },
    @{ Category = "Hardware y Monitorizacion"; Id = "Guru3D.Afterburner"; Name = "MSI Afterburner" },
    @{ Category = "Hardware y Monitorizacion"; Id = "CPUID.CPU-Z"; Name = "CPU-Z" },
    @{ Category = "Hardware y Monitorizacion"; Id = "TechPowerUp.GPU-Z"; Name = "GPU-Z" },
    # Utilidades y Desarrollo
    @{ Category = "Utilidades y Desarrollo"; Id = "7zip.7zip"; Name = "7-Zip" },
    @{ Category = "Utilidades y Desarrollo"; Id = "Notepad++.Notepad++"; Name = "Notepad++" },
    @{ Category = "Utilidades y Desarrollo"; Id = "voidtools.Everything"; Name = "Everything" },
    @{ Category = "Utilidades y Desarrollo"; Id = "ShareX.ShareX"; Name = "ShareX" },
    @{ Category = "Utilidades y Desarrollo"; Id = "Microsoft.PowerToys"; Name = "Microsoft PowerToys" },
    @{ Category = "Utilidades y Desarrollo"; Id = "Git.Git"; Name = "Git" },
    @{ Category = "Utilidades y Desarrollo"; Id = "Microsoft.VisualStudioCode"; Name = "Visual Studio Code" }
)

$selectedIds = @()

if ([string]::IsNullOrWhiteSpace($Packages)) {
    Write-Output ""
    Write-Output "Aplicaciones disponibles por categoria:"
    $lastCat = ""
    for ($i = 0; $i -lt $catalog.Count; $i++) {
        if ($catalog[$i].Category -ne $lastCat) {
            $lastCat = $catalog[$i].Category
            Write-Output ""
            Write-Output "--- $lastCat ---"
        }
        Write-Output "$($i + 1). $($catalog[$i].Name) [$($catalog[$i].Id)]"
    }
    Write-Output ""
    Write-Output "0. Cancelar"
    $input = Read-Host "Introduce los numeros separados por coma (ej. 1,3,7) o 'all' para todas"

    if ($input -eq "0" -or [string]::IsNullOrWhiteSpace($input)) {
        Write-Output "Instalacion cancelada."
        exit 0
    } elseif ($input.Trim().ToLower() -eq "all") {
        $selectedIds = $catalog | ForEach-Object { $_.Id }
    } else {
        $indices = $input -split "," | ForEach-Object { $_.Trim() }
        foreach ($idx in $indices) {
            if ($idx -match "^\d+$") {
                $num = [int]$idx - 1
                if ($num -ge 0 -and $num -lt $catalog.Count) {
                    $selectedIds += $catalog[$num].Id
                }
            }
        }
    }
} else {
    $selectedIds = $Packages -split "," | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }
}

if ($selectedIds.Count -eq 0) {
    Write-Output "No se selecciono ninguna aplicacion valida."
    exit 0
}

Write-Output "Se instalaran $($selectedIds.Count) aplicaciones seleccionadas..."
Write-Output "--------------------------------------------------"

$total = $selectedIds.Count
$current = 0

foreach ($pkgId in $selectedIds) {
    $current++
    Write-Output "[$current/$total] Instalando $pkgId..."
    try {
        winget install --id $pkgId --exact --accept-package-agreements --accept-source-agreements --silent
        if ($LASTEXITCODE -eq 0) {
            Write-Output "Completado: $pkgId instalado correctamente."
        } else {
            Write-Output "Aviso: $pkgId finalizo con codigo $LASTEXITCODE (puede que ya estuviera instalado)."
        }
    } catch {
        Write-Output "Error al intentar instalar $pkgId : $_"
    }
    Write-Output ""
}

Write-Output "--------------------------------------------------"
Write-Output "Proceso de instalacion de aplicaciones esenciales finalizado."
