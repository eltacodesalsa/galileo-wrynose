SUMMARY = "Image A/B para la Galileo / SIMATIC IOT2000"
DESCRIPTION = "Imagen con\n \
 * arranque A/B por EFI Boot Guard (dos particiones de configuracion boot0/boot1)\n \
 * dos ranuras de raiz (platform0/platform1) para actualizacion con rollback\n \
 * galileosetup y hw-smoke.sh\n \
 * sin connman: dejaba /etc/resolv.conf vacio en cada arranque\n \
 * zram en vez de swap sobre la tarjeta SD"

LICENSE = "MIT"

inherit core-image

# A/B: el wks define efi + boot0 + boot1 + platform0 + platform1 + swap + persistent
WKS_FILE = "iot2000-ab.wks"
IMAGE_FSTYPES = "wic.bz2 ext4"

IMAGE_INSTALL:append = " galileo-setup hw-smoke galileo-watchdog galileo-network"
# bg_printenv / bg_setenv: sin esto no se puede inspeccionar ni cambiar de
# entorno A/B desde Linux (venian en la imagen del vendor, faltaban en la nuestra).
IMAGE_INSTALL:append = " efibootguard-tools"
# swupdate: la receta existia en meta-swupdate pero nunca se instalo, asi que el
# OTA del proyecto no estaba entregado (no habia binario ni config en la placa).
IMAGE_INSTALL:append = " swupdate"
# En 6.0 linux-firmware ya no se divide en linux-firmware-iwlwifi-*: es un solo paquete.
# Solo el firmware de NUESTRA tarjeta (Centrino 6235 -> iwlwifi-6000g2b-6).
# 'linux-firmware' completo metia 1.54 GB en /usr/lib/firmware (medido con du en
# la placa: 1.611.428 KB de 1.8 GB de raiz). Con el paquete especifico la raiz
# baja a ~250 MB, que es lo razonable para una imagen de 2 GB con A/B.
IMAGE_INSTALL:append = " kernel-modules linux-firmware-iwlwifi-6000g2b-6"

# connman vaciaba /etc/resolv.conf (symlink a /var/run/resolv.conf) en cada
# arranque y la placa se quedaba sin DNS. Un solo gestor: ifupdown + udhcpc.
IMAGE_INSTALL:remove = "connman connman-client"

# zram: pendiente (en 6.0 no existe el paquete "zram"; ver zram-tools/zram-generator)

# espacio para herramientas de diagnostico
IMAGE_FEATURES:append = " ssh-server-openssh"

BAD_RECOMMENDATIONS:append = " connman"

# El plugin wic de efibootguard necesita bg_setenv/bg_printenv en el sysroot
# nativo. meta-efibootguard lo pone en wic-tools via DEPENDS, pero en 6.0 eso no
# basta: se declara explicitamente en do_image_wic.
do_image_wic[depends] += "efibootguard-native:do_populate_sysroot"
# el wks pide initrd=acpi-upgrades.cpio: hay que construirlo ANTES
do_image_wic[depends] += "acpi-upgrades:do_deploy"


# Login por consola: por defecto OE deja root con password '*' (cuenta bloqueada),
# por eso en el login pide Password y no hay ninguna valida.
# Se habilita root con password vacia y se documenta que hay que cambiarla con
# 'galileosetup -> Change Password' antes de exponer la placa en una red.
IMAGE_FEATURES:append = " empty-root-password allow-empty-password allow-root-login"

# Herramientas de red clasicas: busybox en OE 6.0 ya NO trae ifconfig (deprecado,
# solo deja 'ip'). net-tools devuelve ifconfig/route/netstat, e iproute2 trae el
# 'ip' completo (el applet de busybox no soporta 'ip -br a').
IMAGE_INSTALL:append = " net-tools iproute2"


# ---------------------------------------------------------------------------
# Paquetes elegidos a partir de la lista REAL de la imagen de Siemens (1554
# paquetes, obtenida de /var/lib/opkg/status de la .wic del vendor). De ahi se
# tomaron solo los que aportan algo a ESTA placa; no se copio la lista entera.
#
# Red y diagnostico:
#   ethtool          estado del enlace, velocidad, duplex
#   iw               WiFi (la placa lleva Centrino 6235)
#   wireless-regdb   CRITICO: el arranque mostraba 'regulatory.db failed'; sin
#                    esto el WiFi no configura canales legales
#   curl             HTTPS, y ca-certificates para validar
#   screen           consola serie persistente
# Hardware de la placa:
#   i2c-tools        bus I2C (el log muestra pca953x y el EEPROM at24)
#   can-utils        la placa expone CAN (log: 'controller area network core')
#   usbutils         lsusb
#   pciutils         lspci
#   alsa-utils       audio USB (aplay, alsamixer)
# Sistema:
#   procps           top, free y un 'ps' completo (el de busybox trunca nombres)
#   psmisc           killall, pstree
#   file             identificar binarios
#   e2fsprogs        e2fsck: imprescindible para revisar la SD tras las pruebas
#   dosfstools       fsck de la particion de arranque vfat
#   parted           gestionar particiones
#   bc, jq, tree, unzip, less, nano, bash, openssl-bin, sudo
#
# NO se incluyen: gcc, make, perl, gdb, sqlite3, cronie, minicom (screen cubre),
# bash-completion ni los tzdata-* sueltos: peso sin uso en un appliance.
IMAGE_INSTALL:append = " ethtool iw wireless-regdb curl ca-certificates screen"
IMAGE_INSTALL:append = " i2c-tools can-utils usbutils pciutils alsa-utils"
IMAGE_INSTALL:append = " procps psmisc file e2fsprogs dosfstools parted"
IMAGE_INSTALL:append = " bc jq tree unzip nano less bash openssl-bin sudo"


# systemd-networkd vuelve a habilitarse si solo se borran los enlaces de 'wants'
# (los presets los recrean). Se ENMASCARA con /dev/null, que es definitivo.
quitar_networkd() {
    install -d ${IMAGE_ROOTFS}${sysconfdir}/systemd/system
    for u in systemd-networkd.service systemd-networkd.socket systemd-networkd-wait-online.service; do
        rm -f ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/multi-user.target.wants/$u
        rm -f ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/sockets.target.wants/$u
        ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/$u
    done
    install -d ${IMAGE_ROOTFS}${sysconfdir}/systemd/system-preset
    install -m 0644 ${THISDIR}/files/60-galileo.preset ${IMAGE_ROOTFS}${sysconfdir}/systemd/system-preset/60-galileo.preset
    install -d ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/multi-user.target.wants
    ln -sf ${systemd_system_unitdir}/galileo-network.service ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/multi-user.target.wants/galileo-network.service
}
ROOTFS_POSTPROCESS_COMMAND += "quitar_networkd; "

# platform1 se genera con "--source rootfs" en el .wks: wic crea su propio
# ext4 con el contenido de la raiz. Antes se dejaba vacia (solo formateada) y el
# respaldo A/B hacia panic con "No working init found"; copiarla con dd despues
# del build era fragil y dependia de offsets.

# --- r16: cierre de compuertas de release -----------------------------------
# Confirma el arranque ante EFI Boot Guard (sin esto, cada actualizacion buena
# se revertiria sola al siguiente reinicio: medido en la placa).
IMAGE_INSTALL:append = " galileo-confirm"
# Seguridad: root por SSH SOLO con clave; contrasena vacia unicamente en consola.
IMAGE_INSTALL:append = " galileo-ssh-key"
# Consola serie con agetty -L (evita el bucle de prompts al cerrar el puerto).
IMAGE_INSTALL:append = " galileo-serial-console"
IMAGE_FEATURES:remove = "allow-root-login allow-empty-password"
# Los artefactos publicados deben decir galileo, no iot2000 (el MACHINE no cambia).
IMAGE_BASENAME = "galileo-wrynose"
