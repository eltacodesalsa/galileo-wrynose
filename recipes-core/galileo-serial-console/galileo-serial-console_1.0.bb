SUMMARY = "Consola serie robusta (agetty en modo linea local)"
DESCRIPTION = "Anade -L al getty de la consola serie para que ignorar el estado de \
portadora: sin esto, cerrar el puerto hace que agetty relance el shell en bucle."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://10-line-local.conf"
S = "${UNPACKDIR}"

do_install() {
    install -d ${D}${systemd_system_unitdir}/serial-getty@ttyS1.service.d
    install -m 0644 ${UNPACKDIR}/10-line-local.conf \
        ${D}${systemd_system_unitdir}/serial-getty@ttyS1.service.d/10-line-local.conf
}

FILES:${PN} = "${systemd_system_unitdir}/serial-getty@ttyS1.service.d"
