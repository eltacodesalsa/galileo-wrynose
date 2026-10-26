SUMMARY = "Identidad visible de la placa: hostname y banner del login"
DESCRIPTION = "Cambia lo que se VE en el login. No toca el firmware ni los strings \
DMI/ACPI ('SIMATIC IOT2000'), de los que dependen ethernet, serie y GPIO."
LICENSE = "MIT"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

# OJO: en base-files 6.0 el hostname NO se toma de un archivo en SRC_URI, sino de
# la variable 'hostname' (por defecto ${MACHINE} = iot2000). El archivo hostname
# que enviabamos antes no lo leia nadie: el login seguia diciendo iot2000.
hostname = "galileo"

# El banner SI se instala desde SRC_URI (file://issue): nuestra copia gana porque
# FILESEXTRAPATHS la pone primero.
SRC_URI:append = " file://issue"
