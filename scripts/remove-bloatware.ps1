# ==============================================================================
# Script: remove-bloatware.ps1
# Descripcion: Desinstalacion profunda de aplicaciones UWP (incluyendo Tienda y apps accesorias),
#              protegiendo unicamente los componentes criticos del nucleo de Windows.
# ==============================================================================

Write-Output "Iniciando eliminacion profunda de aplicaciones modernas (UWP)..."

# Comprobacion de privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Output "Se requieren privilegios de Administrador para desinstalar paquetes de sistema."
    Write-Output "Ejecuta la aplicacion como Administrador."
    exit 1
}

# ------------------------------------------------------------------------------
# LISTA BLANCA DE COMPONENTES CRITICOS (NO TOCAR PARA EVITAR INESTABILIDAD)
# ------------------------------------------------------------------------------
# Estos componentes son dependencias del explorador de Windows, autenticacion o librerias base.
$protectedPackages = @(
    "Microsoft.Windows.ShellExperienceHost",
    "Microsoft.Windows.StartMenuExperienceHost",
    "Microsoft.Windows.Search",
    "Microsoft.Windows.SecureAssessmentBrowser",
    "Microsoft.Windows.CapturePicker",
    "Microsoft.Windows.CloudExperienceHost",
    "Microsoft.AAD.BrokerPlugin",
    "Microsoft.AccountsControl",
    "Microsoft.BioEnrollment",
    "Microsoft.Windows.ParentalControls",
    "Microsoft.Windows.XGpuEjectDialog",
    "Microsoft.Windows.Apprep.ChxApp",
    "Microsoft.VCLibs",
    "Microsoft.NET.Native.Framework",
    "Microsoft.NET.Native.Runtime",
    "Microsoft.UI.Xaml"
)

# Funcion auxiliar para comprobar si un paquete esta protegido
function Is-Protected($packageName) {
    foreach ($protected in $protectedPackages) {
        if ($packageName -like "*$protected*") {
            return $true
        }
    }
    return $false
}

# ------------------------------------------------------------------------------
# 1. ELIMINACION DE PAQUETES APROVISIONADOS (PLANTILLAS DEL SISTEMA)
# ------------------------------------------------------------------------------
Write-Output "Eliminando paquetes aprovisionados del sistema (para que no se reinstalen)..."

$provisionedApps = Get-AppxProvisionedPackage -Online -ErrorAction SilentlyContinue

foreach ($app in $provisionedApps) {
    $displayName = $app.DisplayName
    if (-not (Is-Protected $displayName)) {
        try {
            Remove-AppxProvisionedPackage -Online -PackageName $app.PackageName -ErrorAction SilentlyContinue | Out-Null
            Write-Output "Paquete aprovisionado eliminado: $displayName"
        }
        catch {
            # Ignorar si esta bloqueado por el kernel
        }
    }
}

# ------------------------------------------------------------------------------
# 2. ELIMINACION DE PAQUETES INSTALADOS (TODOS LOS USUARIOS)
# ------------------------------------------------------------------------------
Write-Output "Eliminando aplicaciones instaladas en los perfiles de usuario..."

$installedApps = Get-AppxPackage -AllUsers -ErrorAction SilentlyContinue

foreach ($app in $installedApps) {
    $name = $app.Name
    if (-not (Is-Protected $name) -and -not $app.IsFramework -and -not $app.NonRemovable) {
        try {
            Remove-AppxPackage -Package $app.PackageFullName -AllUsers -ErrorAction SilentlyContinue | Out-Null
            Write-Output "Aplicacion desinstalada: $name"
        }
        catch {
            # Ignorar si el sistema marca el paquete como no extraible
        }
    }
}

Write-Output "Eliminacion de aplicaciones UWP completada con exito."
exit 0
