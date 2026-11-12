# ==============================================================================
# Script: configure-windows-update.ps1
# Descripcion: Configuracion selectiva de directivas de Windows Update:
#              - Excluir controladores/drivers para evitar que sobreescriba GPUs/hardware
#              - Modo notificar antes de descargar e instalar (evita reinicios inesperados)
#              - Restaurar configuracion por defecto
# ==============================================================================

param (
    [switch]$ExcludeDrivers,
    [switch]$NotifyOnly,
    [switch]$ResetDefaults
)

Write-Output "Iniciando configuracion selectiva de Windows Update..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para modificar directivas de Windows Update."
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

$wuPolicyPath = "HKLM:\SOFTWARE\Policies\Microsoft\Windows\WindowsUpdate"
$auPolicyPath = "HKLM:\SOFTWARE\Policies\Microsoft\Windows\WindowsUpdate\AU"

# Si no se especificaron parametros, consultar interactivamente en consola
if (-not $ExcludeDrivers -and -not $NotifyOnly -and -not $ResetDefaults) {
    Write-Output ""
    Write-Output "Selecciona que configuraciones deseas aplicar:"
    Write-Output "1. Bloquear instalacion automatica de drivers en Windows Update"
    Write-Output "2. Modo notificar antes de descargar (evitar reinicios forzados)"
    Write-Output "3. Aplicar ambas optimizaciones (Recomendado)"
    Write-Output "4. Restaurar valores por defecto de Windows"
    Write-Output "0. Cancelar"
    $opt = Read-Host "Elige una opcion (0-4)"

    switch ($opt) {
        "1" { $ExcludeDrivers = $true }
        "2" { $NotifyOnly = $true }
        "3" { $ExcludeDrivers = $true; $NotifyOnly = $true }
        "4" { $ResetDefaults = $true }
        default {
            Write-Output "Operacion cancelada."
            exit 0
        }
    }
}

if ($ResetDefaults) {
    Write-Output "Restableciendo directivas de Windows Update a los valores por defecto..."
    Remove-ItemProperty -Path $wuPolicyPath -Name "ExcludeWUDriversInQualityUpdate" -ErrorAction SilentlyContinue
    Remove-ItemProperty -Path $auPolicyPath -Name "AUOptions" -ErrorAction SilentlyContinue
    Remove-ItemProperty -Path $auPolicyPath -Name "NoAutoRebootWithLoggedOnUsers" -ErrorAction SilentlyContinue
    Write-Output "Valores por defecto restablecidos."
} else {
    if ($ExcludeDrivers) {
        Write-Output "Configurando exclusion automatica de controladores en Windows Update..."
        Set-RegistryValue -Path $wuPolicyPath -Name "ExcludeWUDriversInQualityUpdate" -Value 1
        Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\DriverSearching" -Name "SearchOrderConfig" -Value 0
        Write-Output "Drivers excluidos (Windows Update no reemplazara tus drivers de GPU o chipset)."
    }

    if ($NotifyOnly) {
        Write-Output "Configurando modo 'Notificar antes de descargar e instalar'..."
        # AUOptions: 2 = Notificar para descarga y notificar para instalacion
        Set-RegistryValue -Path $auPolicyPath -Name "AUOptions" -Value 2
        Set-RegistryValue -Path $auPolicyPath -Name "NoAutoRebootWithLoggedOnUsers" -Value 1
        Write-Output "Modo de notificacion activado (reinicios sorpresa deshabilitados)."
    }
}

Write-Output "--------------------------------------------------"
Write-Output "Configuracion de Windows Update completada con exito."
