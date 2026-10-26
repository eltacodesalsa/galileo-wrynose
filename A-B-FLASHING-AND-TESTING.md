# Flashing the A/B image and testing it on the board

Procedure for when `bitbake galileo-image` finishes. None of this has been executed yet.

## 0. Before touching the SD card

```sh
# ¿qué dispositivo es la SD? (NO confundir con el disco del sistema)
lsblk -o NAME,SIZE,MODEL,TRAN
```
Confirm the size (~8 GB) and that `TRAN` is `usb` (card reader). If in doubt, **unplug the reader and list again**: the one that disappears is the right one.

## 1. Locating the artifact

```sh
ls -la ~/yocto-wrynose/build/tmp/deploy/images/iot2000/ | grep wic
```
Expected: `galileo-image-iot2000-<fecha>.rootfs.wic.bz2` plus a final `.wic.bz2` (~500-700 MB).

Check that the compression is healthy before writing:
```sh
bzip2 -t galileo-image-iot2000.wic.bz2 && echo OK
```

## 2. Writing

```sh
# SUSTITUIR sdX por el dispositivo confirmado en el paso 0
bzcat galileo-image-iot2000.wic.bz2 | sudo -A dd of=/dev/sdX bs=4M conv=fsync status=progress
sync
```
**Never** use a partition (`/dev/sdX1`): the whole disk is written.

## 3. Expected partition table (7)

```
efi        32 MB   EF00   (EFI System)
boot0      32 MB   0700   EFI Boot Guard, revision=2
boot1      32 MB   0700   EFI Boot Guard, revision=1  → esto es el A/B real
platform0  2 GB    ext4   raíz activa
platform1  2 GB    ext4   raíz de reserva
swap       512 MB
persistent 1 GB    ext4   /data
```
Verify after writing:
```sh
sudo -A fdisk -l /dev/sdX
```

## 4. First boot

Over the **serial console** (`/dev/ttyUSB0` from the FTDI at 115200, script `~/.hermes/scripts/consola-serie.py`) or a monitor.

What should appear, in order:
1. `EFI Boot Guard` **without** the `too few config partitions` warning (that was the failure of the old image)
2. `Loading Linux ...` from `boot0`
3. Kernel 6.18 (the linux-yocto one, not the 6.12 from the spike)
4. `iot2000 login:`

If it reboots every ~60 s: the watchdog bit again → check the `watchdog=60` in `sourceparams` and the `ie6xx_wdt` patch, exactly as in kernel #10.

## 5. Verifying that A/B really exists

With the EFI Boot Guard tools (`bg_printenv` / `bg_setenv`, shipped with the image's `efibootguard`):
```sh
bg_printenv                 # debe listar boot0 y boot1, con revisión y estado (GOOD/FALLBACK)
bg_printenv -c /dev/mmcblk0p2   # env de boot0
bg_printenv -c /dev/mmcblk0p3   # env de boot1
```
Success criterion: **two** sets of variables, each with its own revision (2 and 1).

## 6. Rollback test (the one that matters)

1. With the board up on `platform0`, mark the next boot towards `boot1`:
   ```sh
   bg_setenv -c /dev/mmcblk0p3 ...   # según la sintaxis de la versión 0.22
   reboot
   ```
2. It must boot and mount `platform1` as root (`findmnt /`).
3. **Destructive test** (the one that actually proves rollback): deliberately corrupt `platform0` and let the watchdog fire
   ```sh
   dd if=/dev/urandom of=/dev/mmcblk0p4 bs=1M count=8 conv=fsync   # platform0
   reboot
   ```
   Expected: it does not boot from `platform0`, EFI Boot Guard falls back to `boot1`/`platform1`, and the board comes back.

Without this test, "we have A/B" is a claim with no evidence behind it.

## 7. Updating with swupdate

Only after rollback works. It requires a signed `.swu`, or one whose `sw-description` points at `platform1`; the image's `meta-swupdate` provides the tools. It is tested **afterwards**: first a solid A/B, then OTA.

## Known risks

- **The SD card can be corrupted**: it doesn't matter, José said there is nothing important on the board.
- **A backup is worth making anyway**: partition 2 of the current SD card holds the `bzImage` from the original 4.4 (`bzImage-4.4.302-original`) — if we ever want to go back to the Siemens image, it is worth copying it before rewriting.
- The board's serial port is **`ttyS1` with RS232 levels** on the DB9: do not connect a bare TTL FTDI there.
