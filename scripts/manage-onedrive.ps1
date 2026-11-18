# ==============================================================================
# Script: manage-onedrive.ps1
# Descripcion: Gestion modular de Microsoft OneDrive:
#              1) Debloat (Desactiva inicio automatico, sincronizacion y avisos)
#              2) Desinstalacion completa y ocultacion en el Explorador de Archivos
#              3) Reinstalacion oficial limpia
# ==============================================================================

param (
    [ValidateSet("Debloat", "Uninstall", "Reinstall")]
    [string]$Action = ""
)

Write-Output "Iniciando modulo de gestion de Microsoft OneDrive..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para gestionar OneDrive."
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

if ([string]::IsNullOrWhiteSpace($Action)) {
    Write-Output ""
    Write-Output "Selecciona una accion para Microsoft OneDrive:"
    Write-Output "1. Debloat / Desactivar inicio automatico y sincronizacion"
    Write-Output "2. Desinstalar OneDrive completamente y quitar del Explorador"
    Write-Output "3. Reinstalar Microsoft OneDrive"
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

$oneDriveClsid = "HKCR:\CLSID\{018D5C66-4533-4307-9B53-224DE2ED1FE6}"
$oneDriveClsidWow = "HKCR:\Wow6432Node\CLSID\{018D5C66-4533-4307-9B53-224DE2ED1FE6}"

# Asegurar acceso a HKCR si no esta montado en la sesion
if (-not (Test-Path "HKCR:")) {
    New-PSDrive -Name HKCR -PSProvider Registry -Root HKEY_CLASSES_ROOT -ErrorAction SilentlyContinue | Out-Null
}

# ==============================================================================
# ACCION 1: DEBLOAT (DESACTIVAR INICIO AUTOMATICO Y DIRECTIVAS)
# ==============================================================================
if ($Action -eq "Debloat") {
    Write-Output "Deteniendo procesos activos de OneDrive..."
    Stop-Process -Name "OneDrive" -Force -ErrorAction SilentlyContinue

    Write-Output "Eliminando arranque automatico de OneDrive al iniciar sesion..."
    Remove-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Run" -Name "OneDrive" -ErrorAction SilentlyContinue

    Write-Output "Aplicando directiva de bloqueo de sincronizacion automatica..."
    Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\OneDrive" -Name "DisableFileSyncNGSC" -Value 1

    Write-Output "Debloat de OneDrive completado con exito."
}

# ==============================================================================
# ACCION 2: DESINSTALACION COMPLETA Y LIMPIEZA DEL EXPLORADOR
# ==============================================================================
elseif ($Action -eq "Uninstall") {
    Write-Output "Deteniendo procesos de OneDrive..."
    Stop-Process -Name "OneDrive" -Force -ErrorAction SilentlyContinue

    # Localizar OneDriveSetup.exe en SysWOW64 (64-bit) o System32 (32-bit)
    $setup64 = "$env:SystemRoot\SysWOW64\OneDriveSetup.exe"
    $setup32 = "$env:SystemRoot\System32\OneDriveSetup.exe"
    $setupExe = $null

    if (Test-Path $setup64) {
        $setupExe = $setup64
    } elseif (Test-Path $setup32) {
        $setupExe = $setup32
    }

    if ($setupExe) {
        Write-Output "Ejecutando desinstalador nativo: $setupExe /uninstall..."
        Start-Process -FilePath $setupExe -ArgumentList "/uninstall" -Wait -ErrorAction SilentlyContinue
        Write-Output "Desinstalador finalizado."
    } else {
        Write-Output "Aviso: No se encontro OneDriveSetup.exe (es posible que ya estuviera desinstalado)."
    }

    # Limpiar arranque y directivas
    Remove-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Run" -Name "OneDrive" -ErrorAction SilentlyContinue
    Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\OneDrive" -Name "DisableFileSyncNGSC" -Value 1

    # Ocultar icono de OneDrive de la barra lateral del Explorador de Windows
    Write-Output "Ocultando OneDrive del panel de navegacion del Explorador..."
    Set-RegistryValue -Path $oneDriveClsid -Name "System.IsPinnedToNameSpaceTree" -Value 0
    Set-RegistryValue -Path $oneDriveClsidWow -Name "System.IsPinnedToNameSpaceTree" -Value 0

    # Limpiar carpetas residuales de programa (no toca archivos personales si existen)
    $residualPaths = @(
        "$env:LocalAppData\Microsoft\OneDrive",
        "$env:ProgramData\Microsoft OneDrive"
    )
    foreach ($p in $residualPaths) {
        if (Test-Path $p) {
            Remove-Item -Path $p -Recurse -Force -ErrorAction SilentlyContinue
        }
    }

    Write-Output "Desinstalacion de OneDrive y limpieza del Explorador completadas."
}

# ==============================================================================
# ACCION 3: REINSTALACION OFICIAL LIMPIA
# ==============================================================================
elseif ($Action -eq "Reinstall") {
    Write-Output "Restaurando directivas y preparando reinstalacion de OneDrive..."

    Remove-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\OneDrive" -Name "DisableFileSyncNGSC" -ErrorAction SilentlyContinue
    Set-RegistryValue -Path $oneDriveClsid -Name "System.IsPinnedToNameSpaceTree" -Value 1
    Set-RegistryValue -Path $oneDriveClsidWow -Name "System.IsPinnedToNameSpaceTree" -Value 1

    $setup64 = "$env:SystemRoot\SysWOW64\OneDriveSetup.exe"
    $setup32 = "$env:SystemRoot\System32\OneDriveSetup.exe"

    if (Test-Path $setup64) {
        Write-Output "Ejecutando instalador integrado: $setup64..."
        Start-Process -FilePath $setup64 -Wait -ErrorAction SilentlyContinue
        Write-Output "OneDrive reinstalado correctamente."
    } elseif (Test-Path $setup32) {
        Write-Output "Ejecutando instalador integrado: $setup32..."
        Start-Process -FilePath $setup32 -Wait -ErrorAction SilentlyContinue
        Write-Output "OneDrive reinstalado correctamente."
    } elseif (Get-Command "winget" -ErrorAction SilentlyContinue) {
        Write-Output "Instalando OneDrive mediante winget..."
        winget install --id "Microsoft.OneDrive" --exact --accept-package-agreements --accept-source-agreements --silent
        Write-Output "Instalacion mediante winget finalizada."
    } else {
        Write-Output "Error: No se encontro OneDriveSetup.exe ni winget para reinstalar."
    }
}

Write-Output "--------------------------------------------------"
Write-Output "Operacion sobre Microsoft OneDrive finalizada."
