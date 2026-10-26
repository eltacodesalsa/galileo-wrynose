SUMMARY = "Confirma el arranque ante EFI Boot Guard tras comprobar salud"
DESCRIPTION = "Servicio oneshot que pone ustate a 0 solo si el sistema arranco bien. \
Si no, EFI Boot Guard revierte al entorno anterior."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://galileo-confirm file://galileo-confirm.service file://README"
S = "${UNPACKDIR}"

inherit systemd
SYSTEMD_SERVICE:${PN} = "galileo-confirm.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
RDEPENDS:${PN} += "efibootguard-tools"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/galileo-confirm ${D}${bindir}/galileo-confirm
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/galileo-confirm.service ${D}${systemd_system_unitdir}/
    install -d ${D}${datadir}/doc/galileo-confirm
    install -m 0644 ${UNPACKDIR}/README ${D}${datadir}/doc/galileo-confirm/README
}
