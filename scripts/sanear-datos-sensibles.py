#!/usr/bin/env python3
"""sanear-datos-sensibles.py — quita datos de infraestructura antes de publicar.

Uso:
    ./sanear-datos-sensibles.py --check  <rutas...>   # solo informa (sale !=0 si hay algo)
    ./sanear-datos-sensibles.py --fix    <rutas...>   # reescribe con marcadores

OJO con --fix: pásale SOLO documentación (informes, README, notas). Si le pasas un
directorio de código operativo, le rompe las IPs y las rutas a los scripts que sí
las necesitan para funcionar.

Por qué existe: los informes de trabajo acaban en un repo público o se pasan a
terceros (Claude, DeepSeek...). Llevan IPs privadas, IPs de Tailscale, rutas
/home/<usuario>, hostnames reales, SSIDs y a veces contraseñas. Publicar eso es
regalar el mapa de la red a cualquiera.

Diseño: los marcadores son ESTABLES (<IP-PRIVADA>, <IP-TAILSCALE>, <HOME>...), así
que el documento sigue siendo legible y útil después de sanearlo.
"""
import re
import sys
import pathlib

# Direcciones que SI aparecen en el repo publicado y no son datos de Jose:
# son valores por defecto documentados del producto del que partimos.
ALLOW = {
    "192.168.200.1",                      # IP de fabrica documentada por Siemens
    "/home/root",                         # home de root dentro de la imagen
    "linux-watchdog@vger.kernel.org",     # lista publica del kernel
    "serial-getty@ttyS1",                 # sintaxis de unidad systemd, no un correo
}

# Reglas GENERICAS: describen formas de dato, no la red de nadie.
REGLAS = [
    ("<IP-TAILSCALE>", re.compile(r'\b100\.(?:6[4-9]|[7-9]\d|1[01]\d|12[0-7])\.\d{1,3}\.\d{1,3}\b')),
    ("<IP-PRIVADA>",   re.compile(r'\b(?:192\.168|10\.\d{1,3}|172\.(?:1[6-9]|2\d|3[01]))\.\d{1,3}\.\d{1,3}\b')),
    ("<HOME>",         re.compile(r'/home/[a-z][a-z0-9_-]{1,20}\b')),
    ("<CORREO>",       re.compile(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.(?!service\b|d\b|socket\b|target\b)[A-Za-z]{2,}\b')),
    ("<MAC>",          re.compile(r'\b(?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}\b')),
    ("<TOKEN>",        re.compile(r'\b(?:gh[pousr]_|Yz_|sk-)[A-Za-z0-9_\-]{16,}')),
    ("<SSH-PRIVADA>",  re.compile(r'-----BEGIN [A-Z ]*PRIVATE KEY-----')),
]

# Los terminos PROPIOS (nombres de equipos, usuarios, SSID, contrasenas) NO se
# escriben aqui: se leen de un archivo local que no se versiona, para que
# publicar esta herramienta no publique el mapa de la red de nadie.
#   Formato: una palabra o frase por linea; '#' comenta.
LOCAL = pathlib.Path.home() / ".config" / "sanear-local.txt"
if LOCAL.is_file():
    for _linea in LOCAL.read_text(errors="ignore").splitlines():
        _t = _linea.strip()
        if not _t or _t.startswith("#"):
            continue
        REGLAS.append((f"<PROPIO:{_t[:12]}>", re.compile(re.escape(_t), re.IGNORECASE)))

EXCL_DIRS = ("/.git/", "/venv/", "/.venv/", "/node_modules/", "/build/", "/.pio/", "/.hermes/cache/")
EXCL_NAMES = ("credenciales.md", "sanear-datos-sensibles.py")  # el cofre y esta herramienta
EXT = {".md", ".sh", ".yml", ".yaml", ".cfg", ".conf", ".txt", ".json", ".py",
       ".service", ".bb", ".bbappend", ".inc", ".scc", ".html"}


def archivos(rutas):
    for r in rutas:
        p = pathlib.Path(r).expanduser()
        if p.is_file():
            yield p
            continue
        for f in p.rglob("*"):
            s = str(f)
            if not f.is_file() or f.name in EXCL_NAMES:
                continue
            if any(x in s for x in EXCL_DIRS) or f.suffix.lower() not in EXT:
                continue
            if f.stat().st_size > 3_000_000:
                continue
            yield f


def main():
    if len(sys.argv) < 3 or sys.argv[1] not in ("--check", "--fix"):
        print(__doc__)
        return 2
    modo, rutas = sys.argv[1], sys.argv[2:]
    total, tocados = 0, 0
    for f in archivos(rutas):
        try:
            txt = f.read_text(errors="ignore")
        except Exception:
            continue
        nuevo, hits = txt, {}
        for marcador, rx in REGLAS:
            # los valores de la allowlist (defaults documentados del producto)
            # no se sustituyen ni cuentan como dato sensible
            def _rep(m, _mk=marcador):
                return m.group(0) if m.group(0) in ALLOW else _mk
            n = sum(1 for m in rx.finditer(nuevo) if m.group(0) not in ALLOW)
            if n:
                hits[marcador] = n
            nuevo = rx.sub(_rep, nuevo)
        if not hits:
            continue
        tocados += 1
        total += sum(hits.values())
        rel = str(f).replace(str(pathlib.Path.home()), "~")
        print(f"{rel}")
        for m, n in hits.items():
            print(f"    {m}: {n}")
        if modo == "--fix":
            f.write_text(nuevo)
    print(f"\n{'ARCHIVOS TOCADOS' if modo=='--fix' else 'ARCHIVOS CON DATOS'}: {tocados} "
          f"· ocurrencias: {total}")
    if modo == "--check" and total:
        print("FALLA: hay datos sensibles. Ejecuta --fix o revisa a mano.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
