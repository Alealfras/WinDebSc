# ==============================================================================
# Script: optimize-network.ps1
# Descripcion: Deshabilita la distribucion P2P de actualizaciones (Delivery Optimization),
#              elimina el limite de ancho de banda QoS y optimiza la pila de red TCP/IP.
# ==============================================================================

Write-Output "Iniciando optimizacion de red y ancho de banda..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para modificar la configuracion de red."
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
# 1. DESACTIVAR OPTIMIZACION DE ENTREGA P2P (DELIVERY OPTIMIZATION / DoSvc)
# ------------------------------------------------------------------------------
Write-Output "Desactivando subida P2P de actualizaciones a Internet (Delivery Optimization)..."

# Detener y deshabilitar el servicio DoSvc
if (Get-Service -Name "DoSvc" -ErrorAction SilentlyContinue) {
    Stop-Service -Name "DoSvc" -Force -ErrorAction SilentlyContinue
    Set-Service -Name "DoSvc" -StartupType Disabled -ErrorAction SilentlyContinue
    Write-Output "Servicio DoSvc detenido y deshabilitado."
}

# Directiva para forzar modo descarga HTTP exclusivo sin intercambio P2P (DODownloadMode = 0)
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\DeliveryOptimization" -Name "DODownloadMode" -Value 0
Set-RegistryValue -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\DeliveryOptimization" -Name "SystemSettingsDownloadMode" -Value 0

Write-Output "Distribucion P2P de actualizaciones desactivada."

# ------------------------------------------------------------------------------
# 2. ELIMINAR RESERVA DE ANCHO DE BANDA DE QOS (PSCHED)
# ------------------------------------------------------------------------------
Write-Output "Eliminando limitacion de ancho de banda reservado por QoS..."

# Por defecto, Windows puede reservar hasta un 20% del ancho de banda para trafico del sistema
Set-RegistryValue -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Psched" -Name "NonBestEffortLimit" -Value 0

Write-Output "Reserva de paquetes QoS eliminada (100% de ancho de banda disponible)."

# ------------------------------------------------------------------------------
# 3. DESACTIVAR TUNELES OBSOLETOS DE TRANSICION IPV6 (REDUCCION DE TIMEOUTS DNS)
# ------------------------------------------------------------------------------
Write-Output "Desactivando protocolos de tunelizacion IPv6 heredados (Teredo, ISATAP, 6to4)..."

try {
    netsh interface teredo set state disabled | Out-Null
    netsh interface isatap set state disabled | Out-Null
    netsh interface 6to4 set state disabled | Out-Null
    Write-Output "Tuneles de transicion obsoletos desactivados."
} catch {
    Write-Output "Aviso: No se pudieron modificar algunos estados de interfaces netsh."
}

# ------------------------------------------------------------------------------
# 4. OPTIMIZACION DE LA PILA TCP/IP Y ESCALADO (RSS)
# ------------------------------------------------------------------------------
Write-Output "Optimizando parametros de la pila TCP/IP..."

try {
    # Habilitar Receive-Side Scaling (RSS) para distribuir la carga de red entre nucleos de CPU
    netsh int tcp set global rss=enabled | Out-Null

    # Nivel de autoajuste de ventana TCP a normal
    netsh int tcp set global autotuninglevel=normal | Out-Null

    Write-Output "Pila TCP/IP configurada con autoajuste y escalado multinucleo (RSS)."
} catch {
    Write-Output "Aviso: No se pudieron aplicar ciertos parametros globales de TCP."
}

# ------------------------------------------------------------------------------
# 5. DESACTIVAR WI-FI SENSE (COMPARTICION AUTOMATICA DE REDES)
# ------------------------------------------------------------------------------
Write-Output "Desactivando directivas de Wi-Fi Sense..."

Set-RegistryValue -Path "HKLM:\SOFTWARE\Microsoft\PolicyManager\default\WiFi\AllowWiFiHotSpotReporting" -Name "value" -Value 0
Set-RegistryValue -Path "HKLM:\SOFTWARE\Microsoft\PolicyManager\default\WiFi\AllowAutoConnectToWiFiSenseHotspots" -Name "value" -Value 0

# ------------------------------------------------------------------------------
# 6. VACIADO DE CACHE DNS LOCAL
# ------------------------------------------------------------------------------
Write-Output "Purgando cache de resolucion DNS local..."
try {
    Clear-DnsClientCache
    Write-Output "Cache DNS vaciada correctamente."
} catch {
    ipconfig /flushdns | Out-Null
    Write-Output "Cache DNS vaciada mediante ipconfig."
}

Write-Output "--------------------------------------------------"
Write-Output "Optimizacion de red completada con exito."
