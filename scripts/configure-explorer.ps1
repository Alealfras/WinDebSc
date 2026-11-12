# ==============================================================================
# Script: configure-explorer.ps1
# Descripcion: Configura el Explorador de Windows para maximizar productividad y seguridad:
#              - Muestra extensiones de archivo conocidas (previene malware camuflado)
#              - Muestra carpetas y archivos ocultos
#              - Configura apertura directa en "Este equipo" en lugar de Acceso Rapido
#              - Restaura el menu contextual clasico completo en Windows 11
#              - Desactiva historial de archivos recientes en el explorador
# ==============================================================================

Write-Output "Iniciando configuracion de productividad del Explorador de Windows..."

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

$advancedPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced"

# 1. Mostrar extensiones de archivos para mayor seguridad
Write-Output "Habilitando visualizacion de extensiones de archivo..."
Set-RegistryValue -Path $advancedPath -Name "HideFileExt" -Value 0

# 2. Mostrar archivos y carpetas ocultos
Write-Output "Habilitando visualizacion de archivos y carpetas ocultas..."
Set-RegistryValue -Path $advancedPath -Name "Hidden" -Value 1

# 3. Abrir el Explorador por defecto en 'Este equipo' (LaunchTo = 1) en vez de 'Inicio/Acceso Rapido' (2)
Write-Output "Configurando apertura del Explorador en 'Este equipo'..."
Set-RegistryValue -Path $advancedPath -Name "LaunchTo" -Value 1

# 4. Desactivar historial de elementos y carpetas recientes en Acceso Rapido
Write-Output "Desactivando historial de archivos recientes en el Explorador..."
Set-RegistryValue -Path $advancedPath -Name "ShowRecent" -Value 0
Set-RegistryValue -Path $advancedPath -Name "ShowFrequent" -Value 0

# 5. Restaurar menu contextual clasico en Windows 11 (elimina 'Mostrar mas opciones')
Write-Output "Restaurando menu contextual clasico completo (Windows 11)..."
$win11MenuKey = "HKCU:\Software\Classes\CLSID\{86ca1aa0-34aa-4e8b-a509-50c905bae2a2}\InprocServer32"
if (-not (Test-Path $win11MenuKey)) {
    New-Item -Path $win11MenuKey -Force -ErrorAction SilentlyContinue | Out-Null
}
Set-ItemProperty -Path $win11MenuKey -Name "(default)" -Value "" -ErrorAction SilentlyContinue | Out-Null

# 6. Reiniciar de forma segura el proceso explorer.exe para aplicar los cambios
Write-Output "Reiniciando el explorador de Windows para aplicar los cambios visuales..."
Stop-Process -Name "explorer" -Force -ErrorAction SilentlyContinue

Write-Output "--------------------------------------------------"
Write-Output "Configuracion del Explorador de Windows completada con exito."
