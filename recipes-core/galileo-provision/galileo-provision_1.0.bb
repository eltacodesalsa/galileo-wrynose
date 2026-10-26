SUMMARY = "First-boot SSH credential provisioning from the FAT boot partition"
DESCRIPTION = "Reads galileo-keys.txt or galileo-password.txt from the EFI \
partition on first boot and installs the credential, then deletes the file. \
Without either file the image keeps no credentials and SSH stays closed."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://galileo-provision file://galileo-provision.service file://README"
S = "${UNPACKDIR}"

inherit systemd
SYSTEMD_SERVICE:${PN} = "galileo-provision.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
RDEPENDS:${PN} += "busybox shadow openssh-sshd"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/galileo-provision ${D}${bindir}/galileo-provision
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/galileo-provision.service ${D}${systemd_system_unitdir}/
    install -d ${D}${datadir}/doc/galileo-provision
    install -m 0644 ${UNPACKDIR}/README ${D}${datadir}/doc/galileo-provision/README
}

FILES:${PN} += "${bindir}/galileo-provision ${systemd_system_unitdir}/galileo-provision.service ${datadir}/doc/galileo-provision"
