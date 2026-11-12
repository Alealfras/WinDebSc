# ==============================================================================
# Script: configure-theme-dark.ps1
# Descripcion: Alterna el esquema de color del sistema (Modo Oscuro / Modo Claro):
#              - AppsUseLightTheme (aplicaciones UWP y explorador)
#              - SystemUsesLightTheme (barra de tareas, menu Inicio y centro de notificaciones)
# ==============================================================================

param (
    [ValidateSet("Dark", "Light")]
    [string]$Mode = "Dark"
)

Write-Output "Iniciando conmutacion de tema de interfaz: $Mode..."

$themePath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize"

if (-not (Test-Path $themePath)) {
    New-Item -Path $themePath -Force -ErrorAction SilentlyContinue | Out-Null
}

if ($Mode -eq "Dark") {
    Write-Output "Aplicando Modo Oscuro global..."
    Set-ItemProperty -Path $themePath -Name "AppsUseLightTheme" -Value 0 -Type DWord -Force | Out-Null
    Set-ItemProperty -Path $themePath -Name "SystemUsesLightTheme" -Value 0 -Type DWord -Force | Out-Null
    Write-Output "Modo Oscuro activado correctamente."
} else {
    Write-Output "Aplicando Modo Claro global..."
    Set-ItemProperty -Path $themePath -Name "AppsUseLightTheme" -Value 1 -Type DWord -Force | Out-Null
    Set-ItemProperty -Path $themePath -Name "SystemUsesLightTheme" -Value 1 -Type DWord -Force | Out-Null
    Write-Output "Modo Claro activado correctamente."
}

Write-Output "--------------------------------------------------"
Write-Output "Configuracion de tema finalizada con exito."
