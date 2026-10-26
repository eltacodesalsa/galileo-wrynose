SUMMARY = "ACPI table upgrades for the SIMATIC IOT2000 (initrd)"
DESCRIPTION = "Compiles the .asl tables with iasl and packs them as \
kernel/firmware/acpi/*.aml inside a cpio, which the kernel loads as ACPI \
overrides at boot. Ethernet, GPIO and the serial ports of this board depend on \
these tables: do not drop this recipe from the image."
# Ported from meta-iot2000-bsp (Siemens AG), MIT. See CREDITOS.md.
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit deploy

DEPENDS = "acpica-native"

FILESEXTRAPATHS:prepend := "${THISDIR}/tables:"
SRC_URI = "file://iot2000"

S = "${UNPACKDIR}"

do_compile() {
    rm -fr ${B}/acpi-upgrades
    install -d ${B}/acpi-upgrades/kernel/firmware/acpi
    for table in ${S}/*/*.asl; do
        [ -e "$table" ] || continue
        iasl -p ${B}/acpi-upgrades/kernel/firmware/acpi/$(basename $table) $table
    done
    cd ${B}/acpi-upgrades
    find kernel | cpio -H newc -o > ${B}/acpi-upgrades.cpio
}

do_deploy() {
    install -d ${DEPLOYDIR}
    cp ${B}/acpi-upgrades.cpio ${DEPLOYDIR}
}
addtask deploy after do_compile
