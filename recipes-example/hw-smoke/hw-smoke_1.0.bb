SUMMARY = "Pruebas de humo del hardware de la Galileo"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI = "file://hw-smoke.sh"
S = "${UNPACKDIR}"
do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hw-smoke.sh ${D}${bindir}/hw-smoke.sh
}
