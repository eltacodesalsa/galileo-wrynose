# Notices, credits and licensing

## What this project is

`galileo-wrynose` is a community Yocto layer and image for the **Intel Galileo
Gen 2** single-board computer (Intel Quark X1000 SoC). It exists so that this
discontinued board can run a current, maintained Linux instead of the 2014 vendor
image.

## Trademarks

"Intel" and "Galileo" are trademarks of Intel Corporation. "SIMATIC", "IOT2000"
and "IOT2040" are trademarks of Siemens AG.

They are used here **descriptively**, to state which hardware this software runs
on and which BSP it derives from — the same way a spare-parts catalogue names the
car a part fits. This project is **not** produced, endorsed, sponsored or
supported by Intel or Siemens, and it is not a product: it is a configuration
layer plus a build recipe, published for the community.

## Upstream work this is based on

| Component | Origin | License |
|---|---|---|
| Machine definition, tune, ACPI tables, `.wks` layout | Siemens `meta-iot2000` (`meta-iot2000-bsp`) | MIT, © Siemens AG |
| Yocto / OpenEmbedded core (`oecore`, `bitbake`, `meta-yocto`) | Yocto Project | MIT-style + per-recipe licenses |
| `meta-openembedded` | OpenEmbedded | MIT-style + per-recipe licenses |
| `meta-efibootguard` | Siemens | MIT, © Siemens AG |
| `meta-swupdate` | Stefano Babic and contributors | GPL-2.0-only |
| `acpi-upgrades` recipe and `ssdt-fixes.asl` | ported from Siemens `meta-iot2000` | MIT, © Siemens AG |
| Linux kernel 6.18.52 | kernel.org / Yocto `linux-yocto` | GPL-2.0-only |
| `efibootguard` 0.22 | Siemens | GPL-2.0-or-later |
| Intel firmware strings (`SIMATIC IOT2000` DMI values) | Intel firmware, not redistributed here | n/a |

Files derived from `meta-iot2000` keep their original copyright headers and
license text. Nothing from Intel or Siemens is redistributed as if it were
original work.

## License of this layer

MIT — see `LICENSE`. Copyright (c) 2026 JHC.

Patches to the Linux kernel are GPL-2.0-only, like the kernel itself.

## Binary redistribution (the image)

The published `.wic.bz2` image contains GPL-licensed software (the kernel,
`systemd`, `busybox`, `swupdate`, and others). When distributing that image, the
corresponding source must be obtainable. For each release this project provides:

- `license.manifest` — the license of every package in the image;
- the Yocto `downloads` mirror, so the exact upstream sources can be rebuilt
  from `kas-galileo.yml`;
- `SHA256SUMS` for the released artifacts.

No proprietary firmware is included in the repository. The single firmware file
in the image (`iwlwifi-6000g2b-6.ucode`, for the on-board Centrino 6235) is
installed by Yocto from the `linux-firmware` package and carries Intel's
redistribution license, reproduced in the image under
`/usr/lib/firmware/LICENCE.iwlwifi_firmware`.

## Hardware identification

The board this image was developed and tested on reports, in its firmware:

```
DMI: Intel Corp. QUARK/GalileoGen2, BIOS 0x01010000 01/01/2014
```

The Yocto `MACHINE` is still called `iot2000`, because that machine definition
(inherited from the Siemens BSP) carries the `i586-nlp` tune, the overrides and
the ACPI table selection this board needs. It is a technical identifier, not a
claim about the brand of the hardware.
