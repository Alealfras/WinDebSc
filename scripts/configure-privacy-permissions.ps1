# ==============================================================================
# Script: configure-privacy-permissions.ps1
# Descripcion: Bastionado universal de permisos de privacidad y hardware en Windows:
#              - Bloqueo global de Camara
#              - Bloqueo global de Microfono
#              - Bloqueo global de Ubicacion / Geolocalizacion
#              - Bloqueo de acceso a Diagnosticos de Aplicaciones
#              - Restauracion de permisos por defecto
# ==============================================================================

param (
    [switch]$BlockCamera,
    [switch]$BlockMicrophone,
    [switch]$BlockLocation,
    [switch]$BlockDiagnostics,
    [switch]$ResetDefaults
)

Write-Output "Iniciando configuracion de proteccion de privacidad y permisos..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para modificar politicas globales de privacidad."
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

# Si no se especificaron parametros, mostrar menu interactivo en consola
if (-not $BlockCamera -and -not $BlockMicrophone -and -not $BlockLocation -and -not $BlockDiagnostics -and -not $ResetDefaults) {
    Write-Output ""
    Write-Output "Selecciona que bloqueos universales de privacidad deseas aplicar:"
    Write-Output "1. Bloquear acceso global a la Camara"
    Write-Output "2. Bloquear acceso global al Microfono"
    Write-Output "3. Bloquear acceso global a la Ubicacion / Geolocalizacion"
    Write-Output "4. Bloquear acceso a Diagnosticos de Aplicaciones"
    Write-Output "5. Aplicar TODOS los bloqueos universales"
    Write-Output "6. Restaurar todos los permisos por defecto (Permitir)"
    Write-Output "0. Cancelar"
    $opt = Read-Host "Elige una opcion (0-6)"

    switch ($opt) {
        "1" { $BlockCamera = $true }
        "2" { $BlockMicrophone = $true }
        "3" { $BlockLocation = $true }
        "4" { $BlockDiagnostics = $true }
        "5" {
            $BlockCamera = $true
            $BlockMicrophone = $true
            $BlockLocation = $true
            $BlockDiagnostics = $true
        }
        "6" { $ResetDefaults = $true }
        default {
            Write-Output "Operacion cancelada."
            exit 0
        }
    }
}

$consentHKLM = "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore"
$consentHKCU = "HKCU:\Software\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore"
$appPrivacyPolicy = "HKLM:\SOFTWARE\Policies\Microsoft\Windows\AppPrivacy"

if ($ResetDefaults) {
    Write-Output "Restaurando permisos globales de privacidad a sus valores por defecto (Allow)..."

    $capabilities = @("webcam", "microphone", "location", "appDiagnostics")
    foreach ($cap in $capabilities) {
        Set-RegistryValue -Path "$consentHKLM\$cap" -Name "Value" -Value "Allow" -Type "String"
        Set-RegistryValue -Path "$consentHKCU\$cap" -Name "Value" -Value "Allow" -Type "String"
    }

    $policyNames = @("LetAppsAccessCamera", "LetAppsAccessMicrophone", "LetAppsAccessLocation", "LetAppsGetDiagnosticInfo")
    foreach ($pol in $policyNames) {
        Remove-ItemProperty -Path $appPrivacyPolicy -Name $pol -ErrorAction SilentlyContinue
    }
    Remove-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\LocationAndSensors" -Name "DisableLocation" -ErrorAction SilentlyContinue

    Write-Output "Todos los permisos de privacidad restaurados correctamente."
} else {
    if ($BlockCamera) {
        Write-Output "Aplicando bloqueo universal de Camara..."
        Set-RegistryValue -Path "$consentHKLM\webcam" -Name "Value" -Value "Deny" -Type "String"
        Set-RegistryValue -Path "$consentHKCU\webcam" -Name "Value" -Value "Deny" -Type "String"
        # 2 = Force Deny en directiva AppPrivacy
        Set-RegistryValue -Path $appPrivacyPolicy -Name "LetAppsAccessCamera" -Value 2
        Write-Output "Acceso a la Camara bloqueado globalmente."
    }

    if ($BlockMicrophone) {
        Write-Output "Aplicando bloqueo universal de Microfono..."
        Set-RegistryValue -Path "$consentHKLM\microphone" -Name "Value" -Value "Deny" -Type "String"
        Set-RegistryValue -Path "$consentHKCU\microphone" -Name "Value" -Value "Deny" -Type "String"
        Set-RegistryValue -Path $appPrivacyPolicy -Name "LetAppsAccessMicrophone" -Value 2
        Write-Output "Acceso al Microfono bloqueado globalmente."
    }

    if ($BlockLocation) {
        Write-Output "Aplicando bloqueo universal de Ubicacion / Geolocalizacion..."
        Set-RegistryValue -Path "$consentHKLM\location" -Name "Value" -Value "Deny" -Type "String"
        Set-RegistryValue -Path "$consentHKCU\location" -Name "Value" -Value "Deny" -Type "String"
        Set-RegistryValue -Path $appPrivacyPolicy -Name "LetAppsAccessLocation" -Value 2
        Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\LocationAndSensors" -Name "DisableLocation" -Value 1
        Write-Output "Acceso a la Ubicacion bloqueado globalmente."
    }

    if ($BlockDiagnostics) {
        Write-Output "Aplicando bloqueo universal de Diagnosticos de Aplicaciones..."
        Set-RegistryValue -Path "$consentHKLM\appDiagnostics" -Name "Value" -Value "Deny" -Type "String"
        Set-RegistryValue -Path "$consentHKCU\appDiagnostics" -Name "Value" -Value "Deny" -Type "String"
        Set-RegistryValue -Path $appPrivacyPolicy -Name "LetAppsGetDiagnosticInfo" -Value 2
        Write-Output "Acceso a Diagnosticos de Aplicaciones bloqueado globalmente."
    }
}

Write-Output "--------------------------------------------------"
Write-Output "Configuracion de privacidad completada con exito."
