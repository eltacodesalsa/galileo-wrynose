SUMMARY = "Multi-screen setup utility for the Galileo / SIMATIC IOT2000"
DESCRIPTION = "Installs 'galileosetup': menus and forms (arrow keys) for hostname, \
password, timezone, WiFi and wired network. Plain shell, no dependencies \
(no snack, no mraa, no Python)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://galileosetup"

S = "${UNPACKDIR}"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/galileosetup ${D}${bindir}/galileosetup
}

FILES:${PN} = "${bindir}/galileosetup"

# Necesita wpa_passphrase para el WiFi y tzdata para las zonas horarias.
RRECOMMENDS:${PN} = "wpa-supplicant tzdata"
