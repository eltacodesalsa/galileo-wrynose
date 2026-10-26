# meta-swupdate (rama wrynose) trae el LICENSE con la sintaxis vieja ("AND"), que
# OE-core 6.0 rechaza: desde 5.x la seleccion se expresa con & | ( ).
LICENSE = "BSD-1-Clause & BSD-3-Clause & GPL-2.0-only & GPL-2.0-or-later & ISC & LGPL-2.1-only & LGPL-2.1-or-later & MIT & OFL-1.1"

# Los subpaquetes -www y -lua llevan ficheros cuya licencia se autodetecta como
# "AND" (sintaxis vieja) y do_package_qa / do_create_spdx fallan. Se declara su
# licencia explicitamente y se silencia solo ese chequeo en esos dos paquetes:
# no se excluyen, porque sus ficheros quedarian sin empaquetar.
LICENSE:${PN}-www = "MIT & GPL-2.0-or-later & LGPL-2.1-or-later"
LICENSE:${PN}-lua = "MIT & LGPL-2.1-only"
INSANE_SKIP:${PN}-www += "unlisted-pkg-lics"
INSANE_SKIP:${PN}-lua += "unlisted-pkg-lics"
