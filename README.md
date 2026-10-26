# galileoWrynose

A current Yocto image for the **Intel Galileo Gen 2** (Intel Quark X1000),
built on **Yocto 6.0.4 "wrynose"** with a real A/B boot chain.

The board is a Galileo Gen 2 — the firmware says so:
`DMI: Intel Corp. QUARK/GalileoGen2, BIOS 0x01010000 01/01/2014`.
Siemens' **SIMATIC IOT2000 is a re-brand of the same silicon**, and its BSP
(`meta-iot2000`) is used here as the starting point. The Yocto `MACHINE` is still
called `iot2000` because it inherits that machine definition (tune `i586-nlp-32`,
ACPI tables); it is a technical identifier, not the identity of the board.

> Community project, not a product. "Intel" and "Galileo" are trademarks of Intel
> Corporation and "SIMATIC"/"IOT2000" of Siemens AG; they are named here in their
> descriptive sense — to say which board this runs on and which BSP it derives
> from. Not produced, endorsed or supported by either company. Details and
> per-component credits in `NOTICE.md`.

## Status: 0.1 — A/B-ready, OTA not included

| Capability | State |
|---|---|
| Boots on hardware | ✅ |
| A/B with EFI Boot Guard 0.22 | ✅ both roots populated, manual switch verified |
| Automatic rollback (unconfirmed update reverts) | ✅ verified on hardware |
| Watchdog fed, survives >15 min | ✅ |
| Setup utility (`galileosetup`) | ✅ |
| **OTA (`swupdate`)** | ❌ binary installed but **built without `CONFIG_BOOTLOADER_EBG`** — see `RELEASE-CHECKLIST.md` |

## Hardware

| Board | Intel Galileo Gen 2 |
|---|---|
| SoC | Intel Quark X1000, 400 MHz, **no SSE/SSE2/CMOV**, non-PAE |
| RAM | 256 MB (186 MB usable) |
| Storage | SD card, GPT, 7 partitions |
| Console | `ttyS1` @ 115200 8N1 (DB9) |
| Watchdog | IE6xx, locked by firmware (`WDTLR=0x3`) |

## Partition layout

| # | Label | Size | Content |
|---|---|---|---|
| 1 | `efi` | 32 MB | EFI Boot Guard |
| 2 | `boot0` | 32 MB | kernel + env (revision 2, active) |
| 3 | `boot1` | 32 MB | kernel + env (revision 1, standby) |
| 4 | `platform0` | 2 GB | root filesystem A |
| 5 | `platform1` | 2 GB | root filesystem B (full copy) |
| 6 | `swap` | 512 MB | swap |
| 7 | `persistent` | 1 GB | mounted at `/data` |

Kernel 6.18.52, compiled with `-Wa,-momit-lock-prefix=yes` (Quark `LOCK` errata):
`objdump` reports **0** `lock` instructions in `vmlinux` and in all 588 modules.

## First boot

```
EFI Boot Guard v0.22
2 config partitions detected.
Config Revision: 2 · kernel: C:BOOT0:bzImage · root=PARTLABEL=platform0
...
Galileo Wrynose 6.0.4
galileo login:
```

* **Console (root, no password):** `root` — PAM `nullok_secure` allows an empty
  password on the local serial console only.
* **Network:** `eth0` defaults to **192.168.200.1/24** static (same convention as
  Siemens' image), so the board is always reachable with a direct cable.
  Change it with `galileosetup → Network configuration`.
* **SSH:** root login **by public key only** (`prohibit-password`), and the
  published image ships **no key at all** — a freshly flashed board has no remote
  access until you provision your own key from the serial console:

  ```sh
  # on the serial console, as root
  mkdir -p /root/.ssh && chmod 700 /root/.ssh
  echo "ssh-ed25519 AAAA...your-key user@host" >> /root/.ssh/authorized_keys
  chmod 600 /root/.ssh/authorized_keys
  ```

  Baking an administrator key into a distributed image would hand root access
  over every board running it, so the build only installs one when you set
  `GALILEO_ADMIN_KEY` in a local, unversioned `local.conf`.

## A/B semantics (measured, not assumed)

```
bg_setenv -p 1 -r 3 -s 1 ; reboot     # as an update would leave it
  -> boots root=PARTLABEL=platform1, ustate 2 (TESTING)
reboot without confirming
  -> boots root=PARTLABEL=platform0; boot1 becomes revision 0, ustate 3 (FAILED)
```

`galileo-confirm.service` runs once per boot and sets `ustate=0` **only if** the
system reached `running`/`degraded`, `galileo-network` is active and `eth0` has an
IPv4 address. If anything fails it does not confirm, and EFI Boot Guard reverts
by itself on the next boot.

## Building

`poky` no longer exists as a combined repository in 6.0; the tree is assembled
from three repositories plus the layers:

```
yocto-wrynose/
  oecore/  bitbake/  meta-yocto/         # Yocto 6.0.4
  meta-openembedded/                     # meta-oe, meta-python
  meta-efibootguard/                     # patched: per-partition kernel args
  meta-swupdate/
  galileo-wrynose/                       # this layer
```

```sh
cd yocto-wrynose
source oecore/oe-init-build-env build     # must NOT be piped: it sets BBPATH
MACHINE=iot2000 bitbake galileo-image
```

Requirements: `meta-openembedded` (meta-oe, meta-python), `meta-efibootguard`
branch `wrynose`, `meta-swupdate`. The patch to `meta-efibootguard` is in
`patches/efibootguard-boot-args-por-particion.patch` — it makes `args` define only
the root and keep the rest of the kernel command line (needed because
`console=ttyS1,115200n8` cannot pass through `--sourceparams`, which splits on
commas).

## Image contents

Base: systemd 259, `ifupdown` (networkd is masked), busybox.
Added for this board: `galileosetup`, `hw-smoke.sh`, `galileo-watchdog`,
`galileo-network`, `galileo-confirm`, `efibootguard-tools`, `net-tools`,
`iproute2`, `nano`, `less`, `bash`, `curl`, `jq`, `wget`, `ethtool`, `iw`,
`wireless-regdb`, `i2c-tools`, `can-utils`, `usbutils`, `pciutils`, `alsa-utils`,
`procps`, `psmisc`, `file`, `e2fsprogs`, `dosfstools`, `parted`, `sudo`,
`openssl-bin`, `screen`, `swupdate`.

Only one firmware file is shipped (`linux-firmware-iwlwifi-6000g2b-6`, for the
on-board Centrino 6235). That single change took the root filesystem from 1.8 GB
to **184 MB**.

## Documentation

| File | Contents |
|---|---|
| `RELEASE-CHECKLIST.md` | release gates, each with the measured output |
| `BOOT-ARCHITECTURE-NOTES.md` | boot chain, A/B layout and OTA design notes |
| `A-B-FLASHING-AND-TESTING.md` | how to flash the image and test both roots |
| `PORT-TO-WRYNOSE.md` | what it took to port the machine to Yocto 6.0 |
| `PATCHES.md` | patches applied to third-party layers and why |
| `GALILEOSETUP-STATUS.md` | scope and state of the on-screen setup tool |
| `NOTICE.md` | credits, licenses, trademark notice, GPL/binary redistribution |
| `SECURITY.md` | how to report a vulnerability, and the deliberate design decisions |
| `patches/` | local patches applied to third-party layers |

## License

The layer is MIT. Derived work keeps the licenses of `meta-iot2000` (Siemens),
`meta-efibootguard`, `meta-swupdate` and OpenEmbedded/Yocto. No code from Intel or
Siemens is redistributed as if it were original.
