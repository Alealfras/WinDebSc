# ==============================================================================
# Script: clean-temp-cache.ps1
# Descripcion: Limpieza profunda de archivos temporales, informes de error (WER),
#              cache residual de Windows Update y papelera de reciclaje.
# ==============================================================================

Write-Output "Iniciando mantenimiento: Limpieza profunda de temporales y cache..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para limpiar carpetas del sistema."
    Write-Output "Ejecuta la aplicacion como Administrador."
    exit 1
}

# Helper para eliminacion segura tolerante a archivos en uso (bloqueados por procesos activos)
function Purge-FolderSafely {
    param (
        [string]$Path,
        [string]$Description
    )

    if (Test-Path $Path) {
        Write-Output "Procesando $Description..."
        $count = 0
        Get-ChildItem -Path $Path -Force -ErrorAction SilentlyContinue | ForEach-Object {
            try {
                Remove-Item -Path $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
                $count++
            } catch {
                # Omitir archivos bloqueados por el kernel o programas en ejecucion
            }
        }
        Write-Output "Elementos procesados en $Description: $count"
    } else {
        Write-Output "Ruta no encontrada (omitiendo): $Path"
    }
}

# ------------------------------------------------------------------------------
# 1. TEMPORALES DE USUARIO Y DEL SISTEMA
# ------------------------------------------------------------------------------
# Temporales de la cuenta de usuario (%TEMP%)
Purge-FolderSafely -Path $env:TEMP -Description "Temporales de Usuario (%TEMP%)"

# Temporales de nivel de sistema (C:\Windows\Temp)
Purge-FolderSafely -Path "$env:SystemRoot\Temp" -Description "Temporales del Sistema (Windows\Temp)"

# ------------------------------------------------------------------------------
# 2. CACHE RESIDUAL DE WINDOWS UPDATE (SoftwareDistribution\Download)
# ------------------------------------------------------------------------------
# Se detiene momentaneamente el servicio de actualizacion para desbloquear la carpeta
$updateService = "wuauserv"
$serviceRunning = $false

if (Get-Service -Name $updateService -ErrorAction SilentlyContinue) {
    $status = (Get-Service -Name $updateService).Status
    if ($status -eq "Running") {
        Write-Output "Pausando servicio Windows Update temporalmente..."
        Stop-Service -Name $updateService -Force -ErrorAction SilentlyContinue
        $serviceRunning = $true
    }
}

Purge-FolderSafely -Path "$env:SystemRoot\SoftwareDistribution\Download" -Description "Cache descargada de Windows Update"

# Restaurar el servicio si estaba en ejecucion previamente
if ($serviceRunning) {
    Write-Output "Reanudando servicio Windows Update..."
    Start-Service -Name $updateService -ErrorAction SilentlyContinue
}

# ------------------------------------------------------------------------------
# 3. INFORMES DE ERROR Y VOLCADOS DE DIAGNOSTICO (WER)
# ------------------------------------------------------------------------------
$werArchive = "$env:ProgramData\Microsoft\Windows\WER\ReportArchive"
$werQueue = "$env:ProgramData\Microsoft\Windows\WER\ReportQueue"
$werTemp = "$env:ProgramData\Microsoft\Windows\WER\Temp"

Purge-FolderSafely -Path $werArchive -Description "Archivos de informes de error (WER ReportArchive)"
Purge-FolderSafely -Path $werQueue -Description "Cola de informes de diagnostico (WER ReportQueue)"
Purge-FolderSafely -Path $werTemp -Description "Temporales de informes de error (WER Temp)"

# ------------------------------------------------------------------------------
# 4. VOLCADOS DE MEMORIA POR PANTALLAZOS AZULES (MINIDUMPS)
# ------------------------------------------------------------------------------
$minidumpsPath = "$env:SystemRoot\Minidump"
Purge-FolderSafely -Path $minidumpsPath -Description "Volcados de depuracion (Minidumps)"

# ------------------------------------------------------------------------------
# 5. VACIADO DE PAPELERA DE RECICLAJE
# ------------------------------------------------------------------------------
Write-Output "Vaciando papelera de reciclaje en todas las unidades..."
try {
    Clear-RecycleBin -Force -ErrorAction SilentlyContinue
    Write-Output "Papelera de reciclaje vaciada con exito."
} catch {
    Write-Output "No se pudo vaciar la papelera o ya se encontraba vacia."
}

Write-Output "--------------------------------------------------"
Write-Output "Mantenimiento y liberacion de espacio finalizados."
