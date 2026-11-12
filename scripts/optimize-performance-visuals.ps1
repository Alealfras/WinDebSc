# ==============================================================================
# Script: optimize-performance-visuals.ps1
# Descripcion: Optimizacion de latencia, rendimiento grafico y gaming:
#              1) Desactivacion de Windows Game DVR y captura en bucle
#              2) Desactivacion de aceleracion del raton (Respuesta lineal 1:1)
#              3) Reduccion de latencia en DWM y eliminacion de animaciones superfluas
#              4) Supresion de atajos molestos de accesibilidad (Sticky Keys)
# ==============================================================================

Write-Output "Iniciando optimizacion de rendimiento visual, latencia y gaming..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para aplicar directivas del sistema."
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

# ------------------------------------------------------------------------------
# 1. DESACTIVAR WINDOWS GAME DVR Y CAPTURA EN SEGUNDO PLANO
# ------------------------------------------------------------------------------
Write-Output "Desactivando Windows Game DVR y grabacion en bucle de video..."

# Desactivar grabacion de fondo en GameConfigStore y GameDVR
Set-RegistryValue -Path "HKCU:\System\GameConfigStore" -Name "GameDVR_Enabled" -Value 0
Set-RegistryValue -Path "HKCU:\System\GameConfigStore" -Name "GameDVR_FSEBehaviorMode" -Value 2
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\GameDVR" -Name "AppCaptureEnabled" -Value 0

# Directiva global para bloquear Game DVR
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\GameDVR" -Name "AllowGameDVR" -Value 0

Write-Output "Game DVR deshabilitado (eliminando escrituras continuas en SSD y micro-stuttering)."

# ------------------------------------------------------------------------------
# 2. DESACTIVAR ACELERACION ARTIFICIAL DEL RATON (RESPUESTA LINEAL 1:1)
# ------------------------------------------------------------------------------
Write-Output "Configurando puntero del raton en respuesta lineal 1:1 (sin aceleracion)..."

Set-RegistryValue -Path "HKCU:\Control Panel\Mouse" -Name "MouseSpeed" -Value "0" -Type "String"
Set-RegistryValue -Path "HKCU:\Control Panel\Mouse" -Name "MouseThreshold1" -Value "0" -Type "String"
Set-RegistryValue -Path "HKCU:\Control Panel\Mouse" -Name "MouseThreshold2" -Value "0" -Type "String"

Write-Output "Aceleracion de puntero desactivada (Enhanced Pointer Precision apagado)."

# ------------------------------------------------------------------------------
# 3. OPTIMIZACION DEL ADMINISTRADOR DE VENTANAS (DWM) Y RETARDO DE MENUS
# ------------------------------------------------------------------------------
Write-Output "Ajustando efectos visuales del DWM para maximo rendimiento..."

# Eliminar el retraso artificial por omision (400ms) al desplegar menus contextuales
Set-RegistryValue -Path "HKCU:\Control Panel\Desktop" -Name "MenuShowDelay" -Value "0" -Type "String"

# Perfil de efectos visuales a mejor rendimiento
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\VisualEffects" -Name "VisualFXSetting" -Value 2

# Desactivar sombras bajo ventanas y cursores en el DWM
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\DWM" -Name "EnableAeroPeek" -Value 0
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\DWM" -Name "AlwaysHibernateThumbnails" -Value 0

Write-Output "Retardo de menus eliminado y carga grafica del DWM reducida."

# ------------------------------------------------------------------------------
# 4. DESACTIVAR DIALOGOS ACCIDENTALES DE ACCESIBILIDAD (STICKY KEYS)
# ------------------------------------------------------------------------------
Write-Output "Desactivando avisos emergentes de Sticky Keys (tecla Shift x5)..."

Set-RegistryValue -Path "HKCU:\Control Panel\Accessibility\StickyKeys" -Name "Flags" -Value "506" -Type "String"
Set-RegistryValue -Path "HKCU:\Control Panel\Accessibility\Keyboard Response" -Name "Flags" -Value "122" -Type "String"
Set-RegistryValue -Path "HKCU:\Control Panel\Accessibility\ToggleKeys" -Name "Flags" -Value "58" -Type "String"

Write-Output "Bloqueo de avisos accidentales de Sticky Keys aplicado."
Write-Output "--------------------------------------------------"
Write-Output "Optimizacion de rendimiento visual y gaming completada con exito."
