# Architecture query: boot and OTA on Yocto 6.0 (wrynose)

**Context:** port the Galileo / SIMATIC IOT2000 (Intel Quark X1000, x86 32-bit without SSE) from Siemens'
*dunfell* layer to **Yocto 6.0.4 "wrynose"**. The new layer (`galileo-wrynose`) already loads on the
6.0 core.

## Measured facts (not assumptions)

1. **`poky` no longer exists as a combined repo in 6.0.** The release is assembled from components:
   `bitbake` + `oecore` + `meta-yocto` (+ `meta-openembedded`, `meta-arm`, `meta-intel`, …).
   The `yocto-6.0.4` directory **includes** no `poky-*.tar.bz2`.
2. **The vendor tune still exists:** `oecore/meta/conf/machine/include/x86/tune-i586-nlp.inc`
   is present in 6.0 (`AVAILTUNES += "i586"`). That is, `DEFAULTTUNE = "i586-nlp-32"` is valid.
3. **`efibootguard` and `swupdate` do NOT appear** in release 6.0.4 (neither as a layer nor as a tarball).
   In dunfell they came from `meta-efibootguard` (2020-07) and `meta-swupdate` (2020-08), both from Siemens.
4. **The board has no A/B today.** EFI Boot Guard v0.7 reports on the console:
   `Warning, too few config partitions: found: 1, but expected 2.`
5. Our own 6.12.112 kernel is already validated on the board (iwlwifi WiFi, integrated audio/UVC, `LOCK`
   errata removed with `-Wa,-momit-lock-prefix=yes` verified by `objdump`, `ie6xx_wdt` watchdog with
   `WDOG_HW_RUNNING` before registering, and a real reboot at ~60 s when the daemon is killed).
6. `connman` leaves `/etc/resolv.conf` **empty** on every boot (it is a symlink to `/var/run/resolv.conf`).

## The decision being asked for

For **v0.1**, which boot/update architecture?

### Option A — simple wic, no A/B (recommended for 0.1)
- `wic` with a vfat boot partition + ext4 root, EFI boot via the firmware's `bootx64.efi`.
- No `efibootguard`, no `swupdate`, no A/B. Update = reflash the SD card.
- **Advantages:** does not depend on unported Siemens layers; ships on 6.0 with no boot R&D; low risk.
- **Cost:** no automatic rollback. OTA is deferred to 0.2.

### Option B — A/B with EFI Boot Guard + swupdate (the vendor's approach)
- Port `meta-efibootguard` and `meta-swupdate` from 2020 to 6.0, or look for current alternatives
  (`rauc`, upstream `swupdate`, `systemd-boot` + `systemd-sysupdate`).
- **Advantages:** real A/B, rollback if boot fails — what the project wants in the medium term.
- **Cost:** it is a research project of its own; two layers that are 6 years old on top of a
  Yocto that has already changed structure. High risk that v0.1 never reaches an image.

### Option C — A/B with `systemd-boot` + `systemd-sysupdate` (no Siemens)
- Native systemd A/B, no old layers. Requires systemd in the image (today the vendor image
  is sysvinit) and a UKI or kernel+initrd per slot.
- **Advantages:** maintainable going forward, everything on 6.0.
- **Cost:** changes both the boot manager and the init system; more work than A, less than B.

## Specific questions

1. A for 0.1 and B/C for 0.2, or do we assume the risk of B from the start?
2. If C: is there any reason not to use `systemd-sysupdate` with two root partitions and a UKI?
3. The Quark's `LOCK` errata: is `-Wa,-momit-lock-prefix=yes` in `KCFLAGS`/`KAFLAGS` plus the
   `objdump` check in the recipe enough, or should the build also be rejected if any
   `lock` shows up in the `vmlinux` disassembly (today that is a check external to the build)?
4. Should we keep `i586-nlp-32`, or is there a more suitable 6.0 tune for this Quark
   (`cx8 pae tsc`, without SSE/SSE2)?
