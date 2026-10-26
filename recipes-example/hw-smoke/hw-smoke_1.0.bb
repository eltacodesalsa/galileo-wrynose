SUMMARY = "Pruebas de humo del hardware de la Galileo"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
# Una sola fuente de verdad: el script vive en tests/hw-smoke.sh.
# Antes habia DOS copias y el arreglo del [SKIP] de wlan0 solo llego a una;
# la imagen instalaba la otra y reportaba un FAIL falso.
FILESEXTRAPATHS:prepend := "${THISDIR}/../../tests:"
SRC_URI = "file://hw-smoke.sh"
S = "${UNPACKDIR}"
do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hw-smoke.sh ${D}${bindir}/hw-smoke.sh
}
