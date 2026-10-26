SUMMARY = "Optional SSH public key provisioning (empty by default)"
DESCRIPTION = "Restricts sshd to key-only root login and installs an administrator \
key ONLY when GALILEO_ADMIN_KEY is set at build time. The published image ships \
NO key: whoever flashes it cannot log in by SSH until they provision their own \
key from the serial console. Baking a key into a distributed image would hand \
root access over every board running it."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://authorized_keys.example file://10-galileo-sshd.conf"
S = "${UNPACKDIR}"

# Clave publica del administrador. VACIA por defecto a proposito: si no se define,
# la imagen no lleva ninguna llave y el acceso por SSH queda cerrado hasta que el
# usuario provisione la suya desde la consola serie.
# Para desarrollo, definirla en un local.conf NO versionado:
#   GALILEO_ADMIN_KEY = "ssh-ed25519 AAAA... usuario@maquina"
GALILEO_ADMIN_KEY ??= ""

python do_install() {
    import os
    destdir = d.getVar('D')
    root_home = d.getVar('ROOT_HOME')
    unitdir = d.getVar('sysconfdir')

    # drop-in de sshd: root solo con clave, nunca contrasena vacia por red
    sshd_dir = os.path.join(destdir, unitdir.lstrip('/'), 'ssh', 'sshd_config.d')
    os.makedirs(sshd_dir, exist_ok=True)
    src = os.path.join(d.getVar('UNPACKDIR'), '10-galileo-sshd.conf')
    with open(src) as f, open(os.path.join(sshd_dir, '10-galileo-sshd.conf'), 'w') as o:
        o.write(f.read())

    clave = (d.getVar('GALILEO_ADMIN_KEY') or '').strip()
    if clave:
        ssh_dir = os.path.join(destdir, root_home.lstrip('/'), '.ssh')
        os.makedirs(ssh_dir, exist_ok=True)
        os.chmod(ssh_dir, 0o700)
        with open(os.path.join(ssh_dir, 'authorized_keys'), 'w') as f:
            f.write(clave + '\n')
        os.chmod(os.path.join(ssh_dir, 'authorized_keys'), 0o600)
        bb.warn("galileo-ssh-key: imagen CON llave de administrador (uso interno)")
    else:
        bb.note("galileo-ssh-key: imagen SIN llave (publicable)")
}

FILES:${PN} = "${ROOT_HOME}/.ssh ${sysconfdir}/ssh/sshd_config.d"
RDEPENDS:${PN} += "openssh-sshd"
