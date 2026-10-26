# RELEASE-CHECKLIST — galileoWrynose

The project's golden rule, learned the hard way: **defining something is not applying it**.
Every gate is verified against the **artifact**, never against the recipe:

| What is defined | Where it is actually checked |
|---|---|
| `hostname` in `base-files` | `/etc/hostname` inside the rootfs |
| systemd unit | the unit **file**, not the `wants` symlink |
| `KCFLAGS`/`KAFLAGS` | `objdump` on `vmlinux` and the `.ko` files |
| `IMAGE_INSTALL` | the binary exists in the rootfs |
| `--source rootfs` in the `.wks` | mount the partition and check `/sbin/init` |

No release (not even a pre-release) ships without this list green **and the output
attached** for every check.

---

## Blocking gates

### 1. Kernel without the `LOCK` prefix (Quark X1000 errata) — ✅ CLOSED
```sh
V=build/tmp/work/iot2000-oe-linux/linux-yocto/*/linux-iot2000-standard-build/vmlinux
objdump -d $V | grep -cE '\block\b'
find <build> -name '*.ko' -exec objdump -d {} \; | grep -cE '\block\b'
```
| Date | vmlinux | .ko | Note |
|---|---|---|---|
| before | **73** | **218** | `KCFLAGS:append` was decorative: it never reaches `make` |
| r14 | **0** | **0** | fixed with `KERNEL_EXTRA_ARGS:append = " KCFLAGS=… KAFLAGS=…"` |

### 2. No SSE / CMOV in anything that executes — ✅ CLOSED (with a caveat)
```sh
grep -m1 '^flags' /proc/cpuinfo     # en la placa
# → fpu tsc pae cx8   (NO hay cmov, sse ni sse2)
```
Static sweep of 381 ELF binaries in the rootfs (objdump):
| Library | Instructions | Does any executable link it? |
|---|---|---|
| `liblzma.so.5.8.2` | SSE | **No** (`xz` not installed, no binary links it) |
| `libstdc++.so.6.0.34` | CMOV | **No** (no C++ apps) |
| `libasound.so.2.0.0` | CMOV | Yes, but `aplay -l` runs without SIGILL |

**Caveat:** if a future app links `liblzma` or `libstdc++`
(xz, Python, anything in C++), **re-run this gate**. The risk today is
practically nil because those paths never execute.

### 3. A/B: both roots boot — ✅ CLOSED
Mount the `.wic` and check `p4` and `p5`:
| | platform0 | platform1 |
|---|---|---|
| contents | 184 MB | 184 MB |
| `/sbin/init` | ✓ | ✓ |
| `busybox` md5 | identical | identical |

### 4. A/B: manual switchover — ✅ CLOSED
`bg_setenv -p 1 -r 3` + reboot → `/proc/cmdline` = `root=PARTLABEL=platform1`;
restored to `-r 1` → back to `platform0`.

### 5. A/B: automatic rollback — ✅ CLOSED
```
bg_setenv -p 1 -r 3 -s 1 ; reboot
   → root=PARTLABEL=platform1 · boot1: revision 3 · ustate 2 (TESTING)
reboot SIN confirmar
   → root=PARTLABEL=platform0 · boot0: revision 2 ustate 0 (OK)
     boot1: revision 0 · ustate 3 (FAILED)
```
Conclusion: **EFI Boot Guard rolls back on its own**; `galileo-confirm.service` should only
call `bg_setenv --confirm` after checking health within the first boot in
`TESTING`.

### 6. Root filesystem diet — ✅ CLOSED
| | Before | Now |
|---|---|---|
| Root | 1.8 GB | **184 MB** |
| `/usr/lib/firmware` | 1.54 GB | 680 KB |
| `.wic.bz2` | 1.81 GB | **147 MB** |
Cause: full `linux-firmware` pulled in for a single file
(`linux-firmware-iwlwifi-6000g2b-6`). Claude's target: <500 MB → met.

### 7. `hw-smoke.sh` — ✅ with a caveat
`TODO PASS` with `[SKIP] wlan0 sin configurar`. The "no internet
access"/"DNS does not resolve" FAILs are **real** when the default route is missing.
Pending: **mutation test** (break watchdog/ADC/GPIO on purpose and watch it
FAIL) — two genuine FAILs do not prove the rest knows how to fail.

### 8. 20 cold boots — ✅ CLOSED
Run from an administration host on the same network as the board (no hops, no tunnels), 20 consecutive
reboots, ~2.5 min per cycle:

```
# resumen: 20 correctos, 0 con problemas de 20
```

Across all 20 boots: always `root=PARTLABEL=platform0`, `ustate 0 (OK)` in both
environments, `systemctl --failed` = 0, and stable revisions **2 / 1**.

**One anomaly observed, stated honestly:** on cycle 18 the `boot0` environment reported
`revision: 0` and on the following cycle it was back to `2` with no intervention. There was no
failed boot and no rollback (the system booted from `platform0` either way). The
most likely explanation is a **concurrent read** racing the environment rewrite that
the firmware performs on every boot (`bg_printenv` reading mid-write). It did not reproduce in the other 19
cycles. If it shows up again it must be investigated: a revision that changes by itself would signal an
involuntary rollback.

**Long soak (12–24 h of continuous uptime): still pending.** 20 boots do not
measure SD wear or memory leaks over hours.

### 9. Security — ⏳ PENDING (a decision, not a measurement)
Measured on the board:
```
/etc/securetty            → existe (root solo en console)
/etc/pam.d/common-auth    → nullok
sshd -T                   → permitrootlogin yes · permitemptypasswords yes
```
These come from `IMAGE_FEATURES` (`allow-root-login`, `allow-empty-password`,
`empty-root-password`). For release: drop the first two and provide keys;
keep `empty-root-password` for the console only.

---

## Scope items pending (they do not block 0.1 "A/B-ready")

- **swupdate with the EBG backend**: `strings /usr/bin/swupdate | grep -ic ebg` = 0.
  Missing `CONFIG_BOOTLOADER_EBG=y`, the default EBG interface, `efibootguard`
  in the dependencies, `/etc/swupdate.cfg`, and a `sw-description` with `by-partlabel`.
  Without that the OTA **does not exist**, even though A/B does.
- **`galileo-confirm.service`** (design already defined by gate 5).
- **ACPI tables**: Siemens' tables are designed for their firmware; classify them
  table by table (keep / drop / condition on DMI) and compare the
  Arduino mapping against the Galileo Gen 2 original.
- **`agetty -L`** (local line) so the `hupcl` loop does not affect users.
- **`kas-galileo.yml`** for reproducibility on another machine.
- **Read-only root + `persistent`**: decide before freezing paths.

---

## Hardware identity

**Intel Galileo Gen 2** (`DMI: Intel Corp. QUARK/GalileoGen2`, BIOS 0x01010000
01/01/2014). It is **not** a SIMATIC IOT2000: that is a re-brand of the same silicon,
and it was used as the starting point for the BSP (`meta-iot2000`). `MACHINE =
"iot2000"` is kept as the technical identifier.
