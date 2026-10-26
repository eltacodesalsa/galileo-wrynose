# galileo-wrynose

A current Yocto image for the Intel Galileo Gen 2 (Intel Quark X1000), built on Yocto 6.0.4 "wrynose" with a real A/B boot chain.

The board is QUARK/GalileoGen2, BIOS 0x01010000 01/01/2014. 
Siemens' SIMATIC IOT2000 is a re-brand of the same silicon, and its BSP (meta-iot2000) is used here as the starting point. 
The Yocto MACHINE is still called iot2000 because it inherits that machine definition (tune i586-nlp-32, ACPI tables); it is a technical identifier, not the identity of the board.

> Community project, not a product. "Intel" and "Galileo" are trademarks of Intel
> Corporation and "SIMATIC"/"IOT2000" of Siemens AG; they are named here in their
> descriptive sense — to say which board this runs on and which BSP it derives
> from. Not produced, endorsed or supported by either company. Details and
> per-component credits in `NOTICE.md`.

## Status: 0.1 — A/B-ready

| Capability | State |
|---|---|
| Boots on hardware | ✅ |
| A/B with EFI Boot Guard 0.22 | ✅ both roots populated, manual switch verified |
| Automatic rollback (unconfirmed update reverts) | ✅ verified on hardware |
| Watchdog fed, survives >15 min | ✅ |
| Setup utility (`galileosetup`) | ✅ |

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

## First boot & Provisioning

```
EFI Boot Guard v0.22
2 config partitions detected.
Config Revision: 2 · kernel: C:BOOT0:bzImage · root=PARTLABEL=platform0
...
Galileo Wrynose 6.0.4
galileo login:
```

- **Console & Network Defaults:** 
  - Local serial console (`ttyS1`) allows an empty password via PAM `nullok_secure` for `root`.
  - Network interface `eth0` defaults to **192.168.200.1/24** static (matching Siemens' convention) for immediate out-of-the-box cable connection.
- **Credentials & Expiration:**
  - Default user/password: `root` / `galileo`.
  - **The password expires on first login**, forcing you to choose a new one immediately. Once changed, `galileo` stops working.

> ⚠️ **Zero-touch provisioning (Optional):**
> Prefer keys or a custom password from the start without typing on the console? Place one of these files in the **FAT boot partition** (accessible as a removable drive from your PC) before booting for the first time:
> 
> | File | Effect |
> |---|---|
> | `galileo-password.txt` | Sets that password for root and enables SSH password login; the file is deleted automatically afterwards. |
> | `galileo-keys.txt` | Installs one public key per line and disables SSH password login; the file is deleted automatically afterwards. |

## Setting it up: `galileosetup`

A menu-driven tool on the board, written in POSIX shell with no dependencies — no Python, no `mraa`, no `snack`. Run it from the serial console or over SSH:

```sh
galileosetup
```

| Menu | What it does |
|---|---|
| **Hostname** | changes the name the board announces on the network |
| **Password** | sets the root password (asks twice, minimum 4 characters) |
| **Timezone** | continent → country → zone, listing only zones present on the system |
| **Wired network** | static or DHCP on `eth0`; applies the change immediately, no reboot |
| **WiFi network** | scans, asks for the passphrase, writes the config, brings the link up |
| **Show status** | kernel, uptime, addresses, disk, watchdog, loaded modules |
| **Hardware test** | runs `hw-smoke.sh` and prints PASS/FAIL/SKIP per subsystem |

It reads and writes plain configuration files (`/etc/hostname`, `/etc/network/interfaces`, `/etc/wpa_supplicant.conf`), so anything it does can also be done by hand — it is a convenience, not a daemon, and nothing depends on it at boot.

## A/B semantics (measured, not assumed)

```bash
bg_setenv -p 1 -r 3 -s 1 ; reboot      # as an update would leave it
# -> boots root=PARTLABEL=platform1, ustate 2 (TESTING)
reboot without confirming
# -> boots root=PARTLABEL=platform0; boot1 becomes revision 0, ustate 3 (FAILED)
```

`galileo-confirm.service` runs once per boot and sets `ustate=0` **only if** the system reached `running`/`degraded`, `galileo-network` is active and `eth0` has an IPv4 address. If anything fails it does not confirm, and EFI Boot Guard reverts by itself on the next boot.

## Building

`poky` no longer exists as a combined repository in 6.0; the tree is assembled from three repositories plus the layers:

```
yocto-wrynose/
  oecore/  bitbake/  meta-yocto/            # Yocto 6.0.4
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

Requirements: `meta-openembedded` (meta-oe, meta-python), `meta-efibootguard` branch `wrynose`, `meta-swupdate`. The patch to `meta-efibootguard` is in `patches/efibootguard-boot-args-por-particion.patch` — it makes `args` define only the root and keep the rest of the kernel command line (needed because `console=ttyS1,115200n8` cannot pass through `--sourceparams`, which splits on commas).

## Image contents

Base: systemd 259, `ifupdown` (networkd is masked), busybox.
Added for this board: `galileosetup`, `hw-smoke.sh`, `galileo-watchdog`, `galileo-network`, `galileo-confirm`, `efibootguard-tools`, `net-tools`, `iproute2`, `nano`, `less`, `bash`, `curl`, `jq`, `wget`, `ethtool`, `iw`, `wireless-regdb`, `i2c-tools`, `can-utils`, `usbutils`, `pciutils`, `alsa-utils`, `procps`, `psmisc`, `file`, `e2fsprogs`, `dosfstools`, `parted`, `sudo`, `openssl-bin`, `screen`, `swupdate`.

Only one firmware file is shipped (`linux-firmware-iwlwifi-6000g2b-6`, for the on-board Centrino 6235). That single change took the root filesystem from 1.8 GB to **184 MB**.

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

The layer is MIT. Derived work keeps the licenses of `meta-iot2000` (Siemens), `meta-efibootguard`, `meta-swupdate` and OpenEmbedded/Yocto. No code from Intel or Siemens is redistributed as if it were original.
