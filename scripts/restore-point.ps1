# ==============================================================================
# Script: restore-point.ps1
# Descripcion: Crea un punto de restauracion del sistema antes de aplicar debloat.
# ==============================================================================

Write-Output "Iniciando modulo de seguridad: Punto de Restauracion..."

# 1. Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para crear un punto de restauracion."
    Write-Output "Ejecuta la aplicacion o terminal como Administrador."
    exit 1
}

try {
    # 2. Habilitar la proteccion del sistema en el disco principal si estuviera desactivada
    Write-Output "Comprobando estado de proteccion del sistema en unidad C:..."
    Enable-ComputerRestore -Drive "C:\" -ErrorAction SilentlyContinue

    # 3. Creacion del punto de restauracion
    Write-Output "Creando punto de restauracion: 'WinDebSc_PreDebloat'..."
    Checkpoint-Computer -Description "WinDebSc_PreDebloat" -RestorePointType "MODIFY_SETTINGS" -ErrorAction Stop
    
    Write-Output "Punto de restauracion creado con exito."
    exit 0
}
catch {
    Write-Output "No se pudo crear el punto de restauracion: $($_.Exception.Message)"
    exit 2
}
