SUMMARY = "Red gobernada por ifupdown (galileosetup escribe /etc/network/interfaces)"
DESCRIPTION = "systemd-networkd ignora /etc/network/interfaces, asi que lo que \
configuraba galileosetup no tenia efecto. Esta unidad corre 'ifup -a' al arrancar \
y la imagen deshabilita systemd-networkd."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://galileo-network.service file://interfaces"
S = "${UNPACKDIR}"

inherit systemd
SYSTEMD_SERVICE:${PN} = "galileo-network.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} += "ifupdown"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/galileo-network.service ${D}${systemd_system_unitdir}/
    install -d ${D}${sysconfdir}/network
    install -m 0644 ${UNPACKDIR}/interfaces ${D}${sysconfdir}/network/interfaces
}
