# ==============================================================================
# Script: build-exe.ps1
# Descripcion: Compila WinDebSc y genera el ejecutable nativo WinDebSc.exe
#              mediante jpackage (requiere JDK 17+ en Windows).
# ==============================================================================

$ErrorActionPreference = "Stop"

Write-Output "=== Compilando WinDebSc para Windows (.exe) ==="

# 1. Limpiar carpetas temporales de compilacion
$buildDir = "build-pkg"
$distDir = "dist"

if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
if (Test-Path $distDir)  { Remove-Item -Recurse -Force $distDir }

New-Item -ItemType Directory -Path "$buildDir\classes" | Out-Null
New-Item -ItemType Directory -Path "$buildDir\input" | Out-Null

# 2. Compilar clases Java con FlatLaf
Write-Output "[1/4] Compilando codigo fuente Java..."
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp "lib\*" -d "$buildDir\classes" $javaFiles

# 3. Empaquetar WinDebSc.jar con Main-Class
Write-Output "[2/4] Generando WinDebSc.jar..."
$manifestFile = "$buildDir\MANIFEST.MF"
"Main-Class: com.windebsc.Main`r`nClass-Path: flatlaf-3.8.jar`r`n" | Out-File -FilePath $manifestFile -Encoding ascii

jar cfm "$buildDir\input\WinDebSc.jar" $manifestFile -C "$buildDir\classes" .
Copy-Item "lib\flatlaf-3.8.jar" "$buildDir\input\"

# 4. Generar ejecutable nativo WinDebSc.exe con jpackage (app-image portable)
Write-Output "[3/4] Empaquetando WinDebSc.exe con jpackage..."
jpackage --type app-image `
    --name "WinDebSc" `
    --input "$buildDir\input" `
    --main-jar "WinDebSc.jar" `
    --main-class "com.windebsc.Main" `
    --dest $distDir `
    --app-version "1.0.0" `
    --vendor "Alealfras" `
    --description "Windows Debloating & Automation Suite" `
    --java-options "--enable-native-access=ALL-UNNAMED"

# 5. Copiar carpeta scripts/ junto a WinDebSc.exe
Write-Output "[4/4] Copiando scripts PowerShell al paquete..."
Copy-Item -Recurse "scripts" "$distDir\WinDebSc\scripts"

# 6. Intentar incrustar manifiesto UAC (requireAdministrator) si mt.exe (Windows SDK) esta disponible
$mtExe = Get-ChildItem "C:\Program Files (x86)\Windows Kits\10\bin\*\x64\mt.exe" -ErrorAction SilentlyContinue | Select-Object -Last 1
if ($mtExe) {
    Write-Output "Incrustando manifiesto UAC (Administrador) en WinDebSc.exe..."
    $uacManifest = "$buildDir\uac.manifest"
    @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<assembly xmlns="urn:schemas-microsoft-com:asm.v1" manifestVersion="1.0">
  <trustInfo xmlns="urn:schemas-microsoft-com:asm.v3">
    <security>
      <requestedPrivileges>
        <requestedExecutionLevel level="requireAdministrator" uiAccess="false"/>
      </requestedPrivileges>
    </security>
  </trustInfo>
</assembly>
"@ | Out-File -FilePath $uacManifest -Encoding utf8
    & $mtExe.FullName -manifest $uacManifest -outputresource:"$distDir\WinDebSc\WinDebSc.exe;#1"
} else {
    Write-Output "Aviso: mt.exe no encontrado. Si los scripts piden permisos, ejecuta WinDebSc.exe como Administrador."
}

# Limpiar carpeta temporal
Remove-Item -Recurse -Force $buildDir

Write-Output "=================================================="
Write-Output "¡Listo! Ejecutable generado en: $distDir\WinDebSc\WinDebSc.exe"
Write-Output "=================================================="
