# ==============================================================================
# Script: disable-bing-copilot.ps1
# Descripcion: Deshabilita la integracion de Bing en el menu Inicio, Windows Copilot,
#              los widgets de noticias e intereses y sugerencias publicitarias.
# ==============================================================================

Write-Output "Iniciando optimizacion: Desactivacion de Bing, Copilot y Widgets..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para modificar estas directivas."
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
# 1. DESACTIVAR WINDOWS COPILOT (WINDOWS 10 Y 11)
# ------------------------------------------------------------------------------
Write-Output "Desactivando Windows Copilot..."

# Directiva de grupo para apagar Copilot
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\WindowsCopilot" -Name "TurnOffWindowsCopilot" -Value 1
Set-RegistryValue -Path "HKCU:\Software\Policies\Microsoft\Windows\WindowsCopilot" -Name "TurnOffWindowsCopilot" -Value 1

# Ocultar boton de Copilot de la barra de tareas en el perfil de usuario
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "ShowCopilotButton" -Value 0

Write-Output "Windows Copilot deshabilitado y boton ocultado."

# ------------------------------------------------------------------------------
# 2. DESACTIVAR BUSQUEDA WEB DE BING EN EL MENU INICIO
# ------------------------------------------------------------------------------
Write-Output "Desactivando busquedas web y sugerencias de Bing en el menu Inicio..."

# Busqueda local pura sin llamadas externas a Bing
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Search" -Name "BingSearchEnabled" -Value 0
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Search" -Name "DisableSearchBoxSuggestions" -Value 1
Set-RegistryValue -Path "HKCU:\Software\Policies\Microsoft\Windows\Explorer" -Name "DisableSearchBoxSuggestions" -Value 1

# Directivas del sistema para busqueda
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Windows Search" -Name "DisableWebSearch" -Value 1
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Windows Search" -Name "ConnectedSearchUseWeb" -Value 0
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Windows Search" -Name "AllowSearchToUseLocation" -Value 0

Write-Output "Busqueda de Bing desactivada (menu Inicio configurado en modo local)."

# ------------------------------------------------------------------------------
# 3. DESACTIVAR WIDGETS Y FEEDS DE NOTICIAS DE LA BARRA DE TAREAS
# ------------------------------------------------------------------------------
Write-Output "Desactivando panel de Widgets y Noticias de la barra de tareas..."

# Windows 11: Deshabilitar servicio y panel de widgets (Dsh)
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Dsh" -Name "AllowNewsAndInterests" -Value 0
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "TaskbarDa" -Value 0

# Windows 10: Noticias e intereses en la barra de tareas
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Windows Feeds" -Name "EnableFeeds" -Value 0
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Feeds" -Name "ShellFeedsTaskbarViewMode" -Value 2

Write-Output "Widgets y Noticias deshabilitados (liberando procesos WebView2 en segundo plano)."

# ------------------------------------------------------------------------------
# 4. DESACTIVAR SUGERENCIAS PUBLICITARIAS Y CONTENIDO PROMOCIONADO
# ------------------------------------------------------------------------------
Write-Output "Desactivando contenido promocionado y sugerencias del sistema..."

$contentDeliveryPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\ContentDeliveryManager"
Set-RegistryValue -Path $contentDeliveryPath -Name "SystemPaneSuggestionsEnabled" -Value 0
Set-RegistryValue -Path $contentDeliveryPath -Name "SubscribedContent-338388Enabled" -Value 0
Set-RegistryValue -Path $contentDeliveryPath -Name "SubscribedContent-338389Enabled" -Value 0
Set-RegistryValue -Path $contentDeliveryPath -Name "SubscribedContent-353696Enabled" -Value 0
Set-RegistryValue -Path $contentDeliveryPath -Name "SubscribedContent-353698Enabled" -Value 0
Set-RegistryValue -Path $contentDeliveryPath -Name "SoftLandingEnabled" -Value 0
Set-RegistryValue -Path $contentDeliveryPath -Name "RotatingLockScreenEnabled" -Value 0

Write-Output "Sugerencias publicitarias y telemetria de contenido desactivadas."
Write-Output "Optimizacion completada con exito."
