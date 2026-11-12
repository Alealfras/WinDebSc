# Contexto del Proyecto: WinDebSc (Windows Debloating & Automation Suite)

> **Instrucción para el Agente:** Lee este documento atentamente al retomar la sesión. Contiene las reglas operativas obligatorias acordadas con el usuario, el estado técnico exacto del repositorio y la hoja de ruta inmediata.

---

## 1. Visión General del Proyecto
**WinDebSc** es una suite de optimización, privacidad y eliminación de software innecesario (*debloating*) para Windows (10/11), desarrollada en **Java** como orquestador modular y respaldada por scripts especializados en **PowerShell**. El proyecto incluye una memoria académica rigurosa en **LaTeX** (`docs/main.tex`).

---

## 2. Reglas de Trabajo y Restricciones Obligatorias

### Regla 1: Estructura y Orden en la Documentación LaTeX (`docs/main.tex`)
* Conceptualmente, la **interfaz gráfica (GUI)** representa la última capa arquitectónica del proyecto (se implementó antes únicamente con fines de prototipado y validación).
* **Cada nuevo script o funcionalidad que se desarrolle DEBE documentarse en `docs/main.tex` ANTES de la sección de la GUI.** La sección de la GUI debe mantenerse siempre como el último apartado funcional antes de las pruebas y conclusiones.

### Regla 2: Política de Git y Control de Versiones (Sin pushes automáticos)
* **PROHIBIDO hacer `git push` o micro-commits automáticos sin validación previa.**
* Tras completar una tarea o conjunto de cambios, debes presentar un resumen claro de lo realizado y **solicitar el feedback del usuario**.
* Solo se procede a hacer commit y push una vez que el usuario ha revisado, pulido y aprobado expresamente el resultado.

### Regla 3: Enfoque de Diseño y Funcionalidad
* El foco actual es **100% funcionalidad, robustez y seguridad** de los scripts y del orquestador en Java.
* El pulido estético final de la interfaz o posibles migraciones visuales se realizarán como **último paso del proyecto**, sin alterar la funcionalidad subyacente. *(Nota: No plasmar menciones sobre rediseños futuros en la memoria del repositorio).*
* **Archivos temporales de desarrollo:** El script `run.sh` se incluye de forma provisional para permitir la ejecución y validación visual rápida de la interfaz en macOS/Linux durante la fase de desarrollo. **Debe eliminarse del repositorio al finalizar el proyecto antes del empaquetado final.**

---

## 3. Estado Actual de la Implementación

### A. Scripts PowerShell (`scripts/`)

#### 1. Módulos de Debloat y Optimización:
1. **`restore-point.ps1`**: Comprueba y activa `SystemRestore` en la unidad del sistema y genera un punto de restauración previo.
2. **`disable-telemetry.ps1`**: Modifica registros de políticas (`AllowTelemetry = 0`, `DiagTrack`), deshabilita servicios (`diagtrack`, `dmwappushservice`) y detiene tareas programadas de telemetría y CEIP.
3. **`remove-bloatware.ps1`**: Purgado dual de paquetes AppX (espacio de usuario y aprovisionados con `Get-AppxProvisionedPackage`), implementando una **lista blanca de seguridad** estricta para salvaguardar el Shell (`ShellExperienceHost`, `StartMenu`), autenticación (`CloudExperienceHost`, `AccountsControl`) y runtimes esenciales (`VCLibs`, `UI.Xaml`).
4. **`optimize-services.ps1`**: Detección y desactivación controlada (`Set-Service -StartupType Disabled`) de servicios prescindibles (`Fax`, `RetailDemo`, `MapsBroker`, `wisvc`, `WerSvc`, `RemoteRegistry`, `WdiServiceHost`).
5. **`disable-bing-copilot.ps1`**: Desactivación de Windows Copilot, búsqueda web con Bing en el menú Inicio (modo local puro), paneles de Widgets / Noticias (`Dsh` y WebView2 en segundo plano) y sugerencias comerciales.
6. **`clean-temp-cache.ps1`**: Saneamiento y mantenimiento de almacenamiento: purga segura de temporales de usuario y sistema, detención orquestada de `wuauserv` para vaciar caché acumulada de Windows Update, purga de informes WER y minidumps, y vaciado de la papelera en todas las unidades.
7. **`optimize-network.ps1`**: Desactivación de subida P2P en Delivery Optimization (`DoSvc`), eliminación de reserva del 20% de ancho de banda QoS (`NonBestEffortLimit = 0`), activación de escalado multinúcleo RSS en TCP/IP, desmantelamiento de túneles obsoletos (Teredo, ISATAP, 6to4) y purga de caché DNS.
8. **`manage-edge.ps1`**: Gestión modular paramétrica (`-Action <Debloat|Uninstall|Reinstall>`) de Microsoft Edge y restricción global de aplicaciones en segundo plano. Permite optimizar el navegador desactivando Startup Boost y procesos residentes, forzar su desinstalación bloqueando reinstalaciones automáticas por Windows Update, o reinstalarlo limpiamente desde fuentes oficiales.
9. **`optimize-performance-visuals.ps1`**: Optimización de latencia cinemática, DWM y gaming: desactivación de Windows Game DVR y capturas cíclicas en SSD, erradicación de la aceleración artificial del ratón (respuesta lineal 1:1), eliminación del retardo de menús contextuales (`MenuShowDelay = 0`) y supresión de avisos molestos de Sticky Keys.

#### 2. Módulos de Configuración y Utilidades:
10. **`configure-explorer.ps1`**: Visibilidad obligatoria de extensiones de archivos conocidas (`HideFileExt = 0`), carpetas ocultas, apertura directa en "Este equipo", restauración del menú contextual clásico de Windows 11 sin 'Mostrar más opciones' y desactivación del historial reciente.
11. **`configure-power-plan.ps1`**: Activación del esquema oculto de Máximo Rendimiento (*Ultimate Performance*), desactivación de hibernación para purgar `hiberfil.sys` (recuperando de 8 a 32 GB de almacenamiento) y desactivación de suspensión selectiva de USB.
12. **`configure-windows-update.ps1`**: Control selectivo paramétrico (`-ExcludeDrivers`, `-NotifyOnly`, `-ResetDefaults`): impide que Windows Update sobreescriba controladores de GPU/chipset, activa el modo de notificación previa para evitar reinicios forzados en mitad del trabajo o restablece valores por omisión.
13. **`configure-theme-dark.ps1`**: Conmutación de tema visual global del sistema y aplicaciones (`-Mode Dark|Light`) manipulando `AppsUseLightTheme` y `SystemUsesLightTheme`.

### B. Aplicación Java (`src/com/windebsc/`)
* **`core/ScriptRunner.java`**: Ejecución de procesos PowerShell con paso dinámico de argumentos y captura de flujos en tiempo real.
* **`Main.java`**: Entrada dual: soporta modo consola estructurado en dos categorías y modo gráfico con `--gui`.
* **`ui/MainWindow.java`**: Interfaz gráfica en Swing organizada en **dos pestañas independientes** (`JTabbedPane`: *Debloat y Optimización* vs. *Configuración y Utilidades*), con cuadros modales interactivos para Edge y Windows Update, y un conmutador (*switch* `JToggleButton`) interactivo para alternar el Modo Oscuro en tiempo real.

### C. Memoria Académica (`docs/main.tex`)
* Redactada la base teórica completa de arquitectura, los 9 scripts de debloat y los 4 módulos de configuración y utilidades antes de la sección de la GUI.
* Secciones pendientes:
  * `Capítulo: Pruebas y Validación` (Pruebas en máquina virtual).
  * `Capítulo: Conclusiones y Trabajo Futuro`.

---

## 4. Próximos Pasos en la Hoja de Ruta
1. **Desarrollar nuevos scripts modulares** de optimización y debloat (por ejemplo: ajustes de privacidad adicionales, optimización de red/DNS, limpieza de temporales y cachés profundas de Windows).
2. **Integrar cada nuevo script** en el menú de consola (`Main.java`) y en la interfaz gráfica (`MainWindow.java`).
3. **Documentar cada script en `docs/main.tex`**, ubicando la explicación siempre **antes** de la sección de la GUI.
4. **Revisar con el usuario** cada bloque antes de comitear o subir a GitHub.
