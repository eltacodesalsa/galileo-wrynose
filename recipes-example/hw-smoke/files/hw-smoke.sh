#!/bin/sh
# hw-smoke.sh — prueba funcional de hardware de la Galileo / SIMATIC IOT2000.
#
# Se ejecuta EN LA PLACA. Salida PASS/FAIL por subsistema y código de salida != 0
# si algo falla. Requisito para publicar una release: "registrado" no es "funcional".
#
# Lo que NO puede comprobar este script (se hace en el build/CI):
#   - LOCK:  objdump -d vmlinux | grep -cE '\block\b'   -> debe ser 0
#   - SSE:   objdump -d arch/x86/boot/compressed/vmlinux | grep -c %xmm -> 0
# (en el artefacto, no en la .config: la config no prueba nada)

FAIL=0
pass() { printf "  [PASS] %s\n" "$1"; }
fail() { printf "  [FAIL] %s\n" "$1"; FAIL=$((FAIL+1)); }
info() { printf "  [info] %s\n" "$1"; }
chk()  { if [ "$2" = "$3" ]; then pass "$1"; else fail "$1 (obtenido: $2, esperado: $3)"; fi; }

echo "=================================================================="
echo " hw-smoke galileo-wrynose — $(date 2>/dev/null)"
echo " kernel: $(uname -r)   uptime: $(cut -d. -f1 /proc/uptime 2>/dev/null)s"
echo "=================================================================="

echo "--- sistema ---"
[ -f /etc/os-release ] && info "$(grep -E '^PRETTY_NAME' /etc/os-release | cut -d= -f2- | tr -d '\"')"
chk "raíz montada r/w" "$(grep -c ' / .* rw' /proc/mounts)" "1"
mem=$(free -m 2>/dev/null | awk '/^Mem:/{print $7}')
[ -n "$mem" ] && info "RAM disponible: ${mem} MB"
errs=$(dmesg 2>/dev/null | grep -c -iE "Oops|panic|BUG:|unable to handle")
chk "sin errores de kernel en dmesg" "$errs" "0"

echo "--- red ---"
for i in eth0 wlan0; do
    ip=$(ip -4 -o addr show $i 2>/dev/null | awk '{print $4}')
    if [ -n "$ip" ]; then pass "$i con IP ($ip)"; else fail "$i sin IPv4"; fi
done
if ping -c 1 -W 3 8.8.8.8 >/dev/null 2>&1; then pass "ping a 8.8.8.8"; else fail "sin salida a internet"; fi
if nslookup api.deepseek.com >/dev/null 2>&1 || getent hosts api.deepseek.com >/dev/null 2>&1; then
    pass "DNS resuelve"
else
    fail "DNS no resuelve"
    # Causa vista en el BSP del vendor: connman escribe un fe80:: sin scope en
    # /etc/resolv.conf y pisa lo que puso udhcpc. Un fe80::1 a secas no resuelve.
    grep -q '^nameserver fe80' /etc/resolv.conf 2>/dev/null && \
        info "resolv.conf tiene un link-local sin scope (lo escribió connman, no udhcpc)"
fi

echo "--- watchdog ---"
chk "/dev/watchdog existe" "$([ -c /dev/watchdog ] && echo si || echo no)" "si"
wd=$(ls /sys/class/watchdog/ 2>/dev/null | head -1)
[ -n "$wd" ] && info "watchdog sysfs: $wd  state=$(cat /sys/class/watchdog/$wd/state 2>/dev/null)" \
              || info "watchdog sysfs ausente (falta CONFIG_WATCHDOG_SYSFS=y)"
if [ -n "$wd" ]; then
    t=$(cat /sys/class/watchdog/$wd/timeout 2>/dev/null)
    if [ -z "$t" ]; then
        info "sin atributos en sysfs: falta CONFIG_WATCHDOG_SYSFS=y en el kernel"
    elif [ "$t" -ge 10 ] 2>/dev/null; then
        pass "timeout configurado (${t}s)"
    else
        fail "timeout fuera de rango (${t}s)"
    fi
fi
# El demonio debe tener el fd abierto: con 'ps' no basta (busybox da falsos negativos).
holder=""
for p in /proc/[0-9]*; do
    ls -l "$p/fd" 2>/dev/null | grep -q watchdog && holder="${p#/proc/}"
done
[ -n "$holder" ] && pass "alguien alimenta /dev/watchdog (pid $holder)" \
                 || fail "nadie tiene /dev/watchdog abierto: la placa se reiniciará"

echo "--- I2C / GPIO / ADC del header ---"
chk "/dev/i2c-0 existe" "$([ -c /dev/i2c-0 ] && echo si || echo no)" "si"
n=$(ls /sys/class/gpio/ 2>/dev/null | grep -c gpiochip)
[ "$n" -ge 3 ] 2>/dev/null && pass "$n gpiochip registrados" || fail "sólo $n gpiochip"
if [ -d /sys/bus/spi/devices/spi-INT3495:00 ]; then
    drv=$(readlink /sys/bus/spi/devices/spi-INT3495:00/driver 2>/dev/null | sed 's|.*/||')
    chk "ADC del header con driver" "$drv" "adc108s102"
    raw=$(cat /sys/bus/iio/devices/iio:device0/in_voltage0_raw 2>/dev/null)
    [ -n "$raw" ] && pass "ADC lee in_voltage0_raw ($raw)" || fail "ADC no lee in_voltage0_raw"
    info "comparar contra tensión conocida: 0 V, 3.3 V y un punto medio con divisor"
else
    fail "no hay dispositivo SPI-INT3495 (ADC de las entradas analógicas)"
fi

echo "--- serie / SPI ---"
n=$(ls /dev/ttyS* 2>/dev/null | wc -l)
[ "$n" -ge 2 ] && pass "$n puertos serie" || fail "sólo $n puertos serie"
if ls /dev/spidev* >/dev/null 2>&1; then
    pass "spidev presente: $(ls /dev/spidev* | tr '\n' ' ')"
else
    info "spidev AUSENTE — pendiente (módulo del vendor o SSDT por ACPI)"
fi

echo "--- audio y cámara USB ---"
if [ -d /dev/snd ]; then
    cards=$(cat /proc/asound/cards 2>/dev/null | grep -c '^\s*[0-9]')
    [ "$cards" -ge 1 ] 2>/dev/null && pass "$cards tarjeta(s) de sonido" \
        || info "sin tarjetas de sonido (¿dispositivo USB conectado?)"
else
    fail "/dev/snd no existe"
fi
if ls /dev/video* >/dev/null 2>&1; then
    pass "cámara: $(ls /dev/video* | tr '\n' ' ')"
else
    info "sin /dev/video* (¿cámara USB conectada?)"
fi

echo "--- memoria / swap ---"
sw=$(awk '/^SwapTotal:/{print $2}' /proc/meminfo 2>/dev/null)
info "swap total: ${sw:-0} kB"
[ -d /sys/block/zram0 ] && info "zram presente" || info "zram ausente (recomendado en vez de swap en SD)"

echo "=================================================================="
if [ "$FAIL" -eq 0 ]; then
    echo " RESULTADO: TODO PASS"
    exit 0
else
    echo " RESULTADO: $FAIL FALLO(S)"
    exit 1
fi
