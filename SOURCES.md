# Source code availability — galileoWrynose 0.1.0-rc1

This image contains free software distributed under the GNU General Public
License (v2 and v3), the LGPL, the MIT license and others. The GPL requires that
whoever distributes a binary be able to provide the corresponding source code.
This document states how, and where each component comes from.

## 1. Written offer (GPL-2.0 §3(b), GPL-3.0 §6(b))

> The binary image `galileo-wrynose-0.1.0-rc1.wic.bz2` is distributed together
> with this written offer, valid for **three years** from the date of
> distribution.
>
> Any third party may request the complete corresponding source code for any
> component contained in this image, and it will be provided on a durable medium
> or by download, at no more than the cost of physically performing the
> distribution.
>
> Requests: open an issue at
> https://github.com/eltacodesalsa/galileo-wrynose/issues titled "source request",
> or contact the maintainer through their GitHub profile.
>
> Components built from the recipes in this repository are already available here.
> Components taken from upstream are identified below with their exact source file
> and SHA256, so they can be fetched from upstream or from us.

Date of distribution: 2026-10-26
Maintainer: JHC (github.com/eltacodesalsa)

## 2. Components from this repository

Original recipes and scripts; their source **is** this repository:

- `galileo-confirm` 1.0
- `galileo-network` 1.0
- `galileo-provision` 1.0
- `galileo-serial-console` 1.0
- `galileo-setup` 1.0
- `galileo-ssh-key` 1.0
- `galileo-watchdog` 1.0
- `hw-smoke` 1.0

## 3. Components obtained from upstream tarballs

Recipe, version, source file, size and SHA256. The file name is the one Yocto
stores in its download mirror; fetching it upstream and checking the hash
reproduces the exact source that was compiled.

| Recipe | Version | Source file | Size | SHA256 |
|---|---|---|---|---|
| `alsa-lib` | 1.2.15.3 | `alsa-lib-1.2.15.3.tar.bz2` | 1 MB | `7b079d614d582cade7ab8db2364e65271d0877a37df8757ac4ac0c8970be861e` |
| `alsa-topology-conf` | 1.2.5.1 | `alsa-topology-conf-1.2.5.1.tar.bz2` | 0 MB | `f7c5bae1545abcd73824bc97f4e72c340e11abea188ba0f1c06f5e0ad776b179` |
| `alsa-ucm-conf` | 1.2.15.3 | `alsa-ucm-conf-1.2.15.3.tar.bz2` | 0 MB | `9f79e813c08fc86cfa46dd75c4fcda1a4a51b482db2607e1fcfaafb92f588a31` |
| `alsa-utils` | 1.2.15.2 | `alsa-utils-1.2.15.2.tar.bz2` | 1 MB | `7aaaafbfb01942113ec0c31e51f705910e81079205088ca2f8f137a3869e1a3a` |
| `avahi` | 0.8 | `avahi-0.8.tar.gz` | 1 MB | `060309d7a333d38d951bc27598c677af1796934dbd98e1024e7ad8de798fedda` |
| `base-passwd` | 3.6.8 | `base-passwd_3.6.8.tar.xz` | 0 MB | `fab3d0e6e8b641e116bda9bd5f7a7ed24482384c1513f6a369b506327fbc8dde` |
| `bash` | 5.3 | `bash-5.3.tar.gz` | 10 MB | `0d5cd86965f869a26cf64f4b71be7b96f90a3ba8b3d74e27e8e9d9d5550f31ba` |
| `bc` | 1.08.2 | `bc-1.08.2.tar.gz` | 0 MB | `ae470fec429775653e042015edc928d07c8c3b2fc59765172a330d3d87785f86` |
| `busybox` | 1.37.0 | `busybox-1.37.0.tar.bz2` | 2 MB | `3311dff32e746499f4df0d5df04d7eb396382d7e108bb9250e7b519b837043a4` |
| `ca-certificates` | 20260816 | `ca-certificates_20260816.tar.xz` | 0 MB | `d939bcdd0cb058712cf4175bac76997676eb8b68fe9473765e1b40fb3d5b186a` |
| `curl` | 8.19.0 | `curl-8.19.0.tar.xz` | 2 MB | `4eb41489790d19e190d7ac7e18e82857cdd68af8f4e66b292ced562d333f11df` |
| `dbus` | 1.16.2 | `dbus-1.16.2.tar.xz` | 1 MB | `0ba2a1a4b16afe7bceb2c07e9ce99a8c2c3508e5dec290dbb643384bd6beb7e2` |
| `dosfstools` | 4.2 | `dosfstools-4.2.tar.gz` | 0 MB | `64926eebf90092dca21b14259a5301b7b98e7b1943e8a201c7d726084809b527` |
| `ethtool` | 6.19 | `ethtool-6.19.tar.gz` | 0 MB | `1c1a2f9fc02670ab005fb3e5de16688852bd6cea7e15ebc6ec91d0278b00dd79` |
| `i2c-tools` | 4.4 | `i2c-tools-4.4.tar.gz` | 0 MB | `04d1e3b0cd88df8fb96e7709f374dd0b3561191b4c0363eaf873a074b8b7cb22` |
| `iproute2` | 6.19.0 | `iproute2-6.19.0.tar.xz` | 0 MB | `9332213d35480b647086a70c302de8568de83455a98774d35de216c4ce191006` |
| `iw` | 6.17 | `iw-6.17.tar.gz` | 0 MB | `aebdcfa3691aa5065a9b0d7def66c761c8b01a59dd81bc315d0305e05c3b04de` |
| `kbd` | 2.9.0 | `kbd-2.9.0.tar.xz` | 1 MB | `fb3197f17a99eb44d22a3a1a71f755f9622dd963e66acfdea1a45120951b02ed` |
| `kmod` | 34.2 | `kmod-34.2.tar.xz` | 0 MB | `5a5d5073070cc7e0c7a7a3c6ec2a0e1780850c8b47b3e3892226b93ffcb9cb54` |
| `less` | 692 | `less-692.tar.gz` | 0 MB | `61300f603798ecf1d7786570789f0ff3f5a1acf075a6fb9f756837d166e37d14` |
| `libcap` | 2.77 | `libcap-2.77.tar.xz` | 0 MB | `897bc18b44afc26c70e78cead3dbb31e154acc24bee085a5a09079a88dbf6f52` |
| `libxml2` | 2.15.4 | `libxml2-2.15.4.tar.xz` | 3 MB | `98087fd181d9070724f3fbc65c7377db03038eb92bd882374daff44940138821` |
| `linux-firmware` | 20260410 | `linux-firmware-20260410.tar.xz` | 590 MB | `b7812ed6d59f6b09ecceddaa0be842a7e82a79cc0e46ca60478a4ebf02f1e178` |
| `nano` | 9.0 | `nano-9.0.tar.xz` | 1 MB | `9f384374b496110a25b73ad5a5febb384783c6e3188b37063f677ac908013fde` |
| `netbase` | 6.5 | `netbase_6.5.tar.xz` | 0 MB | `9116047aebbaa1698934052d01c6e09b4c3aed643e93df63d2ddcbec243c26d1` |
| `openssh` | 10.3p1 | `openssh-10.3p1.tar.gz` | 1 MB | `56682a36bb92dcf4b4f016fd8ec8e74059b79a8de25c15d670d731e7d18e45f4` |
| `openssl` | 3.5.8 | `openssl-3.5.8.tar.gz` | 50 MB | `a8f84a39918ec6415ce765d9b429d313ba97b8143169c172e734b9514464f5b2` |
| `parted` | 3.6 | `parted-3.6.tar.xz` | 1 MB | `3b43dbe33cca0f9a18601ebab56b7852b128ec1a3df3a9b30ccde5e73359e612` |
| `pciutils` | 3.14.0 | `pciutils-3.14.0.tar.xz` | 0 MB | `e7713409882813991d2269d125e40dad1f54a019a52b78b3962941c1d4a6f86f` |
| `rpcbind` | 1.2.9 | `rpcbind-1.2.9.tar.bz2` | 0 MB | `ce5f1a87c566ef0b2897a28f50a75c1dc23fec413a46a7f4183423b6b6aa991b` |
| `screen` | 5.0.1 | `screen-5.0.1.tar.gz` | 0 MB | `2dae36f4db379ffcd14b691596ba6ec18ac3a9e22bc47ac239789ab58409869d` |
| `shadow` | 4.19.4 | `shadow-4.19.4.tar.gz` | 7 MB | `ba7e9aed1c5420203ac549583d6e6f485a819ac7505af9c7cd8c13b2013210b8` |
| `sudo` | 1.9.17p2 | `sudo-1.9.17p2.tar.gz` | 5 MB | `4a38a1ab3adb1199257edc2a7c4a2bd714665eb605b04368843b06dada2cfcfb` |
| `tree` | 2.3.2 | `tree-2.3.2.tgz` | 0 MB | `6b941dd6cbecfb4d3250700e4d08d8e0c251488981dd4868b90d744234300e21` |
| `usbutils` | 019 | `usbutils-019.tar.gz` | 0 MB | `347c52b65a6aeb22cfe3af8382192004f99725850e1dc5f705336e54cf6d59fc` |
| `util-linux` | 2.41.5 | `util-linux-2.41.5.tar.xz` | 9 MB | `f586e35d320ff537aab3ffeca37e9ecd482ccbe013590db4429a414d8aa6a728` |
| `wireless-regdb` | 2026.09.03 | `wireless-regdb-2026.09.03.tar.xz` | 0 MB | `b22e0901227b820cd1c280abe681a15b773a5103a5e10dc442e94ebb34cbf58d` |

## 4. Components fetched from version control

These take their source from a git repository; the exact URL and revision are in
each recipe's `SRC_URI` and in `kas-galileo.yml`:

- `avahi-libnss-mdns` 0.15.1
- `base-files` 3.0.14
- `can-utils` 2025.01
- `e2fsprogs` 1.47.4
- `file` 5.47
- `glibc` 2.43+git
- `glibc-locale` 2.43+git
- `ifupdown` 0.8.45
- `jq` 1.8.1
- `libpam` 1.7.2
- `libpcre2` 10.47
- `libsamplerate0` 0.2.2
- `libseccomp` 2.6.0
- `linux-yocto` 6.18.52+git
- `ncurses` 6.6
- `net-tools` 2.10
- `opkg-utils` 0.7.0
- `os-release` 1.0
- `packagegroup-base` 1.0
- `packagegroup-core-boot` 1.0
- `packagegroup-core-ssh-openssh` 1.0
- `procps` 4.0.6
- `psmisc` 23.7
- `shadow-securetty` 4.6
- `shared-mime-info` 2.4
- `swupdate` 2026.05.1
- `systemd` 259.5
- `systemd-conf` 1.0
- `systemd-serialgetty` 1.0
- `tzdata` 2026c
- `unzip` 6.0
- `volatile-binds` 1.0
- `wpa-supplicant` 2.11

## 5. Rebuilding

```sh
cat kas-galileo.yml                 # the seven repositories and their revisions
kas build kas-galileo.yml           # or: source oe-init-build-env build && bitbake galileo-image
```

Honest note: the four base layers are pinned by **branch**, not by commit hash,
because the development tree was assembled from release tarballs, and a clean
`kas-container` build has not been compared byte for byte against the published
image. This does not affect the source-availability obligation: the sources listed
above are the ones actually compiled.

## 6. Intentionally not included

- **Build-only tooling** (gcc, qemu, llvm, cross toolchain): used to build the
  image, not distributed inside it, so their sources are outside the obligation
  for this binary.
- **Intel firmware** (`iwlwifi-6000g2b-6.ucode`): installed from `linux-firmware`
  under Intel's own redistribution license, reproduced in the image at
  `/usr/lib/firmware/LICENCE.iwlwifi_firmware`. Proprietary firmware, not GPL.
