SUMMARY = "Alimentador del watchdog de hardware para la Galileo / IOT2000"
DESCRIPTION = "Unidad systemd que hace ping al /dev/watchdog. Sin ella la placa se \
reinicia a los 60 s porque EFI Boot Guard arma el watchdog y no queda nadie que lo alimente."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://galileo-watchdog.service file://README"
S = "${UNPACKDIR}"

inherit systemd
SYSTEMD_SERVICE:${PN} = "galileo-watchdog.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} += "busybox"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/galileo-watchdog.service ${D}${systemd_system_unitdir}/
    install -d ${D}${datadir}/doc/galileo-watchdog
    install -m 0644 ${UNPACKDIR}/README ${D}${datadir}/doc/galileo-watchdog/README
}
