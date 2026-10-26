SUMMARY = "Kernel LTS moderno para la Galileo / SIMATIC IOT2000"
DESCRIPTION = "Añade los fragmentos de configuración del hardware de la placa y \
la bandera que elimina el prefijo LOCK (errata del Quark X1000) al kernel de \
linux-yocto. No redefine la máquina ni toca los strings DMI."
LICENSE = "MIT"

# Se aplica sobre el kernel que traiga la release (linux-yocto de wrynose).
# No se pinea versión aquí a propósito: la capa sigue el kernel de la release.
require recipes-kernel/linux/linux-yocto.inc

FILESEXTRAPATHS:prepend := "${THISDIR}/files:${THISDIR}:"

SRC_URI:append = " \
    file://defconfig \
    file://galileo-hw.cfg \
    file://galileo-wdt.cfg \
    file://0001-ie6xx_wdt-manejar-un-watchdog-bloqueado-por-el-firm.patch \
"

# ---------------------------------------------------------------------------
# ERRATA DEL QUARK X1000 — CRÍTICO
# Si el prefijo LOCK coincide con un page fault, la instrucción se re-ejecuta.
# El kernel usa 'lock addl' como barrera de memoria cuando el CPU no tiene SSE2,
# que es exactamente el caso de esta placa. El tune i586-nlp protege el userland
# pero NO el kernel: hay que pasárselo aquí.
#
# El criterio de aceptación NO es esta línea, es el objdump sobre el artefacto:
#   objdump -d vmlinux | grep -cE '\block\b'                      -> 0
#   objdump -d arch/x86/boot/compressed/vmlinux | grep -cE '\block\b'  -> 0
# En CI y por release. Un grep en la receta es documentación, no prueba.
# ---------------------------------------------------------------------------
KCFLAGS:append = " -Wa,-momit-lock-prefix=yes"
KAFLAGS:append = " -Wa,-momit-lock-prefix=yes"

COMPATIBLE_MACHINE = "iot2000"

# linux-yocto exige un BSP o un defconfig: usamos el .config ya PROBADO en la placa
# (kernel 6.12.112: iwlwifi iwldvm/iwlmvm integrados, audio+UVC, watchdog ahora).
# KCONFIG_MODE=alldefconfig parte de el y resuelve lo que cambie en 6.18.
KCONFIG_MODE = "--alldefconfig"

# El Quark X1000 tiene la errata del prefijo LOCK: hay que compilar con
# -Wa,-momit-lock-prefix=yes. Definir KCFLAGS/KAFLAGS NO basta: no se exportan al
# make del kernel, y el resultado era un vmlinux con 73 instrucciones 'lock' y 218
# en los .ko (medido con objdump, que es el criterio de aceptacion).
# Se pasan por KERNEL_EXTRA_ARGS, que la clase del kernel si anade a la linea de make.
KERNEL_EXTRA_ARGS:append = " KCFLAGS=-Wa,-momit-lock-prefix=yes KAFLAGS=-Wa,-momit-lock-prefix=yes"
