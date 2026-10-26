# Port of the machine to Yocto 6.0 "wrynose"

> **Correction of a mistake of mine.** I previously wrote that "wrynose does not exist". That is false:
> **Yocto 6.0.4 is called `wrynose`** (official RELEASENOTES; `yocto-docs` has the `wrynose` and
> `wrynose-next` branches). My conclusion came from checking only the `poky` repos, where it does not
> appear.
>
> **Why it did not appear:** in 6.0 **`poky` stopped existing as a combined repository**. The release
> is assembled from separate components:
> ```
> bitbake   oecore   meta-yocto   meta-openembedded   meta-arm   meta-intel   …
> ```
> That release directory **does not include any `poky-*.tar.bz2`**. Looking for the `wrynose` branch in
> `git ls-remote …/poky` proves nothing about the release. Lesson: to verify a Yocto release, look at
> `downloads.yoctoproject.org/releases/yocto/<release>/`, not only at the `poky` branches.

## What is already set up and verified

```
~/yocto-wrynose/
├── oecore/          openembedded-core 6.0.4  (contiene meta/ y oe-init-build-env)
├── bitbake/         BitBake 2.12.1
├── meta-yocto/      meta-poky + meta-yocto-bsp
├── meta -> oecore/meta
├── meta-poky -> meta-yocto/meta-poky
├── meta-yocto-bsp -> meta-yocto/meta-yocto-bsp
├── galileo-wrynose/ nuestra capa
└── build/
```

Evidence:
```
$ bitbake --version
BitBake Build Tool Core version 2.12.1

$ bitbake-layers show-layers
core                /home/…/yocto-wrynose/oecore/meta           5
yocto               /home/…/yocto-wrynose/meta-poky              5
yoctobsp            /home/…/yocto-wrynose/meta-yocto-bsp         5
galileo-wrynose     /home/…/yocto-wrynose/galileo-wrynose        10
```

And the core confirms it unambiguously:
```
ERROR: Layer galileo-wrynose is not compatible with the core layer which only supports
these series: wrynose (layer is compatible with walnascar)
```
→ `LAYERSERIES_COMPAT_galileo-wrynose = "wrynose"` (which was the original value) is the correct one.

## Environment details that cost time

- `source oe-init-build-env` **must not go into a pipe** (`| tail`): it runs in a subshell and
  `BBPATH`/`BUILDDIR` are lost. Redirect it to a file and read that afterwards.
- A symlink `oe-init-build-env -> oecore/oe-init-build-env` does not work: the script computes
  `$OEROOT` from its own path and ends up looking for `scripts/oe-buildenv-internal` in the wrong
  place. Invoke the real file: `source oecore/oe-init-build-env build`.
- Host tools that bitbake requires and that were not installed: `chrpath`, `diffstat`, `gawk`, `lz4`
  (plus `zstd` and `cpio`).

## What is missing before baking: porting the `iot2000` machine

`meta-iot2000-bsp` declares compatibility only with *dunfell*, so it does not load on wrynose. It has
to be brought into our layer, **keeping Siemens' copyright and license**:

1. `conf/machine/iot2000.conf`
   - `DEFAULTTUNE = "i586-nlp-32"` (Quark X1000: `cx8 pae tsc`, **no** SSE/SSE2)
   - **Do not rename the DMI strings** (`SIMATIC IOT2000`): it breaks ethernet, serial and GPIO
   - `KCFLAGS`/`KAFLAGS` with `-Wa,-momit-lock-prefix=yes` (**not** `EXTRA_CFLAGS`, deprecated);
     the acceptance criterion is `objdump`, not a `grep`
2. the `wic` `.wks` (today: boot vfat 32 MB + root; **no A/B**: EFI Boot Guard reports
   `found: 1, but expected 2`)
3. `efibootguard` and `acpi-upgrades`: they are not in 6.0. **This is the piece with the most
   uncertainty.**
4. Kernel: `linux-yocto_%.bbappend` with `galileo-hw.cfg` and `galileo-wdt.cfg`. Decided **`=m`** for
   everything that is not needed to mount the root, with `linux-firmware-iwlwifi-6000g2b` in the image.
5. Image: **no `connman`** (it left `/etc/resolv.conf` empty), with `galileosetup` and `hw-smoke.sh`,
   and `zram` instead of swap on SD.
6. `bitbake galileo-image` and validate on the board with `hw-smoke.sh`.

## What cannot be promised
Full reproducibility: old mirrors with dead links. The deliverable to the community is
**the layer + an already built `.wic`**, not a "it builds and that's it".
