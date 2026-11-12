# ==============================================================================
# Script: configure-power-plan.ps1
# Descripcion: Configuracion de energia para maximo rendimiento y liberacion de espacio:
#              - Desbloquea y activa el plan 'Maximo Rendimiento' (Ultimate / High Performance)
#              - Desactiva la hibernacion del sistema (elimina hiberfil.sys liberando 8-32 GB)
#              - Deshabilita la suspension selectiva de puertos USB (evita desconexiones)
# ==============================================================================

Write-Output "Iniciando configuracion de plan de energia y rendimiento de hardware..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para modificar planes de energia."
    Write-Output "Ejecuta la aplicacion como Administrador."
    exit 1
}

# 1. Desbloquear y activar plan de energia 'Maximo Rendimiento'
Write-Output "Desbloqueando y activando esquema de energia 'Maximo Rendimiento'..."
$ultimateGuid = "e9a42b02-d5df-448d-aa00-03f14749eb61"
$highPerfGuid = "8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c"

try {
    # Intento 1: Esquema 'Ultimate Performance'
    $output = powercfg -duplicatescheme $ultimateGuid 2>&1
    if ($output -match "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})") {
        $scheme = $matches[1]
        powercfg -setactive $scheme
        Write-Output "Plan 'Maximo Rendimiento' (Ultimate Performance) activado con exito."
    } else {
        # Intento 2: Esquema 'Alto Rendimiento' estandar
        powercfg -setactive $highPerfGuid
        Write-Output "Plan 'Alto Rendimiento' activado con exito."
    }
} catch {
    Write-Output "Aviso: No se pudo conmutar el esquema de energia mediante powercfg."
}

# 2. Desactivar hibernacion (powercfg -h off) para liberar gigabytes de hiberfil.sys
Write-Output "Desactivando hibernacion para purgar hiberfil.sys y liberar espacio..."
try {
    powercfg -h off
    Write-Output "Hibernacion desactivada (archivo hiberfil.sys eliminado del disco C:)."
} catch {
    Write-Output "Aviso: No se pudo desactivar la hibernacion."
}

# 3. Desactivar suspension selectiva de puertos USB (evita que periféricos entren en reposo)
Write-Output "Desactivando suspension selectiva de concentradores USB..."
try {
    # Subgrupo USB: 2a737441-1930-4402-8d77-b2bebba4d5a0, Ajuste: 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 (0 = Deshabilitado)
    powercfg /SETACVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba4d5a0 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0
    powercfg /SETDCVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba4d5a0 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0
    powercfg /SETACTIVE SCHEME_CURRENT
    Write-Output "Suspension selectiva de USB desactivada (perifericos siempre activos)."
} catch {
    Write-Output "Aviso: No se pudo configurar la directiva de USB."
}

Write-Output "--------------------------------------------------"
Write-Output "Optimizacion de energia completada con exito."
