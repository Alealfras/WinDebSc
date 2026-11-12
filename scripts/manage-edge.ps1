# ==============================================================================
# Script: manage-edge.ps1
# Descripcion: Gestion modular de Microsoft Edge y aplicaciones en segundo plano:
#              1) Debloat / Optimizacion (Startup Boost, telemetria y apps de fondo)
#              2) Desinstalacion completa y bloqueo de reinstalacion automatica
#              3) Reinstalacion oficial limpia
# ==============================================================================

param (
    [ValidateSet("Debloat", "Uninstall", "Reinstall")]
    [string]$Action = ""
)

Write-Output "Iniciando modulo de gestion de Microsoft Edge y Apps en segundo plano..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para gestionar Microsoft Edge."
    Write-Output "Ejecuta la aplicacion como Administrador."
    exit 1
}

# Helper para crear o actualizar valores en el Registro de forma segura
function Set-RegistryValue {
    param (
        [string]$Path,
        [string]$Name,
        [object]$Value,
        [string]$Type = "DWord"
    )
    if (-not (Test-Path $Path)) {
        New-Item -Path $Path -Force -ErrorAction SilentlyContinue | Out-Null
    }
    Set-ItemProperty -Path $Path -Name $Name -Value $Value -Type $Type -Force -ErrorAction SilentlyContinue | Out-Null
}

# Si no se paso parametro por linea de comandos, solicitar interactivamente
if ([string]::IsNullOrWhiteSpace($Action)) {
    Write-Output ""
    Write-Output "Selecciona una accion para Microsoft Edge:"
    Write-Output "1. Debloat / Optimizar (Desactivar procesos residentes, inicio rapido y telemetria)"
    Write-Output "2. Desinstalar Microsoft Edge y bloquear reinstalacion automatica"
    Write-Output "3. Reinstalar Microsoft Edge (Descarga oficial limpia)"
    Write-Output "0. Cancelar"
    $choice = Read-Host "Elige una opcion (0-3)"

    switch ($choice) {
        "1" { $Action = "Debloat" }
        "2" { $Action = "Uninstall" }
        "3" { $Action = "Reinstall" }
        default {
            Write-Output "Operacion cancelada."
            exit 0
        }
    }
}

Write-Output "Accion seleccionada: $Action"
Write-Output "--------------------------------------------------"

# ==============================================================================
# ACCION 1: DEBLOAT Y OPTIMIZACION DE PROCESOS RESIDENTES
# ==============================================================================
if ($Action -eq "Debloat") {
    Write-Output "Aplicando directivas de debloat sobre Microsoft Edge..."

    # 1. Directivas de Edge en HKLM y HKCU
    $edgePolicies = @(
        "HKLM:\SOFTWARE\Policies\Microsoft\Edge",
        "HKCU:\Software\Policies\Microsoft\Edge"
    )

    foreach ($path in $edgePolicies) {
        # Desactivar Startup Boost (aceleracion de inicio)
        Set-RegistryValue -Path $path -Name "StartupBoostEnabled" -Value 0
        # No permitir extensiones y procesos en segundo plano al cerrar
        Set-RegistryValue -Path $path -Name "BackgroundModeEnabled" -Value 0
        # Desactivar reporte de telemetria y diagnostico
        Set-RegistryValue -Path $path -Name "MetricsReportingEnabled" -Value 0
        Set-RegistryValue -Path $path -Name "SendSiteInfoToImproveServices" -Value 0
        # Desactivar barra lateral innecesaria y asistente de compras
        Set-RegistryValue -Path $path -Name "HubsSidebarEnabled" -Value 0
        Set-RegistryValue -Path $path -Name "EdgeShoppingAssistantEnabled" -Value 0
        # Desactivar sincronizacion publicitaria
        Set-RegistryValue -Path $path -Name "PersonalizationReportingEnabled" -Value 0
    }

    # 2. Desactivar ejecucion de aplicaciones en segundo plano globales de Windows
    Write-Output "Restringiendo ejecucion de aplicaciones globales en segundo plano..."
    Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\BackgroundAccessApplications" -Name "GlobalUserDisabled" -Value 1
    Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\AppPrivacy" -Name "LetAppsRunInBackground" -Value 2

    # 3. Detener procesos huérfanos de Edge en ejecucion
    Write-Output "Deteniendo instancias en segundo plano de msedge.exe..."
    Stop-Process -Name "msedge" -Force -ErrorAction SilentlyContinue

    Write-Output "Debloat de Edge y restriccion de aplicaciones en segundo plano completados con exito."
}

# ==============================================================================
# ACCION 2: DESINSTALACION COMPLETA Y BLOQUEO DE REINSTALACION
# ==============================================================================
elseif ($Action -eq "Uninstall") {
    Write-Output "Preparando desinstalacion forzada de Microsoft Edge..."

    # Detener procesos de Edge activos
    Stop-Process -Name "msedge" -Force -ErrorAction SilentlyContinue
    Stop-Process -Name "MicrosoftEdgeUpdate" -Force -ErrorAction SilentlyContinue

    # Localizar instalador setup.exe en Program Files (x86) o Program Files
    $setupPaths = @(
        "${env:ProgramFiles(x86)}\Microsoft\Edge\Application",
        "$env:ProgramFiles\Microsoft\Edge\Application"
    )

    $installerFound = $false

    foreach ($basePath in $setupPaths) {
        if (Test-Path $basePath) {
            $setups = Get-ChildItem -Path $basePath -Filter "setup.exe" -Recurse -ErrorAction SilentlyContinue
            foreach ($s in $setups) {
                Write-Output "Ejecutando desinstalador: $($s.FullName)..."
                Start-Process -FilePath $s.FullName -ArgumentList "--uninstall", "--system-level", "--verbose-logging", "--force-uninstall" -Wait -ErrorAction SilentlyContinue
                $installerFound = $true
                break
            }
        }
        if ($installerFound) { break }
    }

    if (-not $installerFound) {
        Write-Output "Aviso: No se encontro el desinstalador setup.exe (es posible que Edge ya este desinstalado)."
    } else {
        Write-Output "Proceso de desinstalacion de Edge finalizado."
    }

    # Bloquear que Windows Update lo reinstale automaticamente
    Write-Output "Bloqueando reinstalacion automatica por Windows Update..."
    Set-RegistryValue -Path "HKLM:\SOFTWARE\Microsoft\EdgeUpdate" -Name "DoNotUpdateToEdgeWithChromium" -Value 1

    # Deshabilitar servicios de actualizacion de Edge
    $edgeServices = @("edgeupdate", "edgeupdatem")
    foreach ($svc in $edgeServices) {
        if (Get-Service -Name $svc -ErrorAction SilentlyContinue) {
            Stop-Service -Name $svc -Force -ErrorAction SilentlyContinue
            Set-Service -Name $svc -StartupType Disabled -ErrorAction SilentlyContinue
            Write-Output "Servicio deshabilitado: $svc"
        }
    }

    # Eliminar accesos directos residuales
    $desktopEdge = "$env:PUBLIC\Desktop\Microsoft Edge.lnk"
    if (Test-Path $desktopEdge) {
        Remove-Item -Path $desktopEdge -Force -ErrorAction SilentlyContinue
    }

    Write-Output "Desinstalacion y bloqueo de Microsoft Edge completados con exito."
}

# ==============================================================================
# ACCION 3: REINSTALACION OFICIAL LIMPIA
# ==============================================================================
elseif ($Action -eq "Reinstall") {
    Write-Output "Iniciando reinstalacion oficial de Microsoft Edge..."

    # 1. Desbloquear directivas de actualizacion
    Remove-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\EdgeUpdate" -Name "DoNotUpdateToEdgeWithChromium" -ErrorAction SilentlyContinue
    
    # 2. Habilitar servicios de actualizacion
    $edgeServices = @("edgeupdate", "edgeupdatem")
    foreach ($svc in $edgeServices) {
        if (Get-Service -Name $svc -ErrorAction SilentlyContinue) {
            Set-Service -Name $svc -StartupType Manual -ErrorAction SilentlyContinue
        }
    }

    # 3. Intentar instalar primero con winget
    $wingetAvailable = Get-Command "winget" -ErrorAction SilentlyContinue
    $installed = $false

    if ($wingetAvailable) {
        Write-Output "Instalando Microsoft Edge a traves de Windows Package Manager (winget)..."
        try {
            winget install --id "Microsoft.Edge" --accept-package-agreements --accept-source-agreements --silent
            $installed = $true
            Write-Output "Microsoft Edge instalado correctamente mediante winget."
        } catch {
            Write-Output "Winget no pudo completar la instalacion. Probando descarga directa..."
        }
    }

    # Si winget no estaba disponible o fallo, descargar instalador oficial
    if (-not $installed) {
        $tempInstaller = "$env:TEMP\MicrosoftEdgeSetup.exe"
        $downloadUrl = "https://go.microsoft.com/fwlink/?linkid=2108834"

        Write-Output "Descargando instalador oficial de Microsoft Edge desde $downloadUrl..."
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 -bor [Net.SecurityProtocolType]::Tls13
            Invoke-WebRequest -Uri $downloadUrl -OutFile $tempInstaller -UseBasicParsing
            Write-Output "Ejecutando instalador oficial..."
            Start-Process -FilePath $tempInstaller -ArgumentList "/silent", "/install" -Wait
            Remove-Item -Path $tempInstaller -Force -ErrorAction SilentlyContinue
            Write-Output "Instalacion oficial de Microsoft Edge finalizada con exito."
        } catch {
            Write-Output "Error al descargar o ejecutar el instalador oficial: $_"
        }
    }
}

Write-Output "--------------------------------------------------"
Write-Output "Operacion sobre Microsoft Edge finalizada."
