# Common configuration for Intel Quark (Galileo / SIMATIC IOT2000).
# Ported from meta-iot2000-bsp (Siemens AG), GPL-2.0. See CREDITOS.md.

DEFAULTTUNE ?= "i586-nlp-32"
# ojo: en BitBake una linea "require" NO admite comentario al final: el # se
# toma como parte del nombre de archivo. En 6.0 los tunes viven en x86/.
require conf/machine/include/x86/tune-i586-nlp.inc
require conf/machine/include/x86/x86-base.inc

# gnutls padlock code uses CMOV in assembly, invalid on Quark.
EXTRA_OECONF:append:pn-gnutls = " --disable-padlock"
EXTRA_OECONF:append:pn-openssl = " no-asm"
