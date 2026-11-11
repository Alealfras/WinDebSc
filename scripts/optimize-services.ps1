# ==============================================================================
# Script: optimize-services.ps1
# Descripcion: Deshabilita servicios innecesarios o heredados de Windows para
#              liberar memoria RAM y ciclos de CPU.
# ==============================================================================

Write-Output "Iniciando optimizacion de servicios del sistema..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para modificar servicios del sistema."
    Write-Output "Ejecuta la aplicacion como Administrador."
    exit 1
}

# Lista de servicios prescindibles en equipos modernos
# - Fax: Servicio de fax telefonico tradicional.
# - MapsBroker: Gestor de mapas descargados para aplicaciones UWP sin conexion.
# - RetailDemo: Modo demostracion de tienda (usado en escaparates comerciales).
# - wisvc: Servicio del programa Windows Insider (pruebas beta).
# - WerSvc: Servicio de informe de errores de Windows a Microsoft.
# - RemoteRegistry: Permite a usuarios remotos modificar el registro (riesgo de seguridad).
# - WdiServiceHost / WdiSystemHost: Diagnosticos heredados y recoleccion de trazas.
$servicesToOptimize = @(
    @{ Name = "Fax"; Description = "Servicio de Fax" },
    @{ Name = "MapsBroker"; Description = "Administrador de mapas descargados" },
    @{ Name = "RetailDemo"; Description = "Modo de demostracion comercial" },
    @{ Name = "wisvc"; Description = "Servicio de Windows Insider" },
    @{ Name = "WerSvc"; Description = "Informe de errores de Windows" },
    @{ Name = "RemoteRegistry"; Description = "Registro remoto (seguridad)" },
    @{ Name = "WdiServiceHost"; Description = "Host de servicio de diagnostico" },
    @{ Name = "WdiSystemHost"; Description = "Host de sistema de diagnostico" }
)

Write-Output "Analizando estado de los servicios seleccionados..."

foreach ($item in $servicesToOptimize) {
    $serviceName = $item.Name
    $serviceDesc = $item.Description

    $serviceObj = Get-Service -Name $serviceName -ErrorAction SilentlyContinue

    if ($serviceObj) {
        if ($serviceObj.Status -eq "Running") {
            Stop-Service -Name $serviceName -Force -ErrorAction SilentlyContinue
            Write-Output "Servicio detenido: $serviceDesc ($serviceName)"
        }
        if ($serviceObj.StartType -ne "Disabled") {
            Set-Service -Name $serviceName -StartupType Disabled -ErrorAction SilentlyContinue
            Write-Output "Servicio deshabilitado: $serviceDesc ($serviceName)"
        }
    }
}

Write-Output "Optimizacion de servicios completada con exito."
exit 0
