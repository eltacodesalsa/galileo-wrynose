# Patch log — galileo-wrynose

Classification method (audit 2026-10-22): **reverse dry-run** against the clean tree.
`UPSTREAM` = the reverse applies cleanly (it is already there); `APLICA` (applies) = candidate to
evaluate; `CONFLICTO` (conflict) = needs a rebase, or is already there with changes (decided by
searching for unique symbols from the patch).

> `CONFLICTO` does **not** mean "discard". And `APLICA` does **not** mean "needed": it is validated
> with `tests/hw-smoke.sh`, not with `dmesg`.

## Patches owned by this layer

### 0001-ie6xx_wdt-manejar-un-watchdog-bloqueado-por-el-firm

**Status:** written and verified on 6.12.112 (build #10), pending rebase to 6.18.
**Reason:** the UEFI firmware arms the watchdog at 60 s and leaves it with `WDT_LOCK`. Two independent
conditions, and both must be resolved **before** `watchdog_register_device()`:

| Condition | Effect | Action |
|---|---|---|
| `WDT_ENABLE` | the hardware is already running | `set_bit(WDOG_HW_RUNNING)` so the core feeds it |
| `WDT_LOCK` | it cannot be stopped | `watchdog_set_nowayout(nowayout \|\| locked)`, **per device** |

**Consequence of getting it wrong** (measured, build #8): if `WDOG_HW_RUNNING` is set *after*
registering, the core does not start its pre-userspace handler and never feeds the watchdog. If the
core also believes it can stop it (`watchdog_stop()` does `clear_bit(HW_RUNNING)` before calling the
driver and the driver returns an error), **every ping becomes a silent no-op** and the board enters a
reboot loop every ~60 s with no error at all in the log.

```c
	wdtlr = inb(ie6xx_wdt_data.sch_wdtba + WDTLR);
	if (wdtlr & WDT_LOCK)
		dev_warn(&pdev->dev, "Watchdog Timer is Locked (Reg=0x%x)\n", wdtlr);

	if (wdtlr & WDT_ENABLE) {
		set_bit(WDOG_HW_RUNNING, &ie6xx_wdt_dev.status);
		dev_info(&pdev->dev,
			 "watchdog ya en marcha (WDTLR=0x%x); el core lo alimenta hasta que userspace lo abra\n",
			 wdtlr);
	}
	watchdog_set_nowayout(&ie6xx_wdt_dev, nowayout || (wdtlr & WDT_LOCK));

	ie6xx_wdt_debugfs_init();
	ret = watchdog_register_device(&ie6xx_wdt_dev);
```
And `ie6xx_wdt_stop()` returning `-EBUSY` instead of `-1`.

**Drop criterion:** when `ie6xx_wdt` in mainline sets `WDOG_HW_RUNNING` and honors `WDT_LOCK` by
itself. Checked with the reverse dry-run.

**Upstream candidate:** yes, `linux-watchdog@vger.kernel.org`.

---

## Patches inherited from the Siemens BSP (`meta-iot2000-bsp/recipes-kernel/linux/patches/`)

Classified against 6.12.112 (2026-10-22). **Repeat against 6.18 before deciding.**

| Patches | Reverse dry-run | Reading |
|---|---|---|
| 0001-0006 `stmmac-pci` | CONFLICTO | Ethernet works without them → very likely upstream (with changes). Confirm by symbols. |
| 0007 `serial-8250_exar` | **UPSTREAM** | Already there. |
| 0008 `mfd-intel_quark_i2c_gpio` | **UPSTREAM** | Already there. |
| 0009-0013 `gpio-sch` | CONFLICTO | GPIO works without them. Confirm by symbols whether the set is complete. |
| 0014 `iot2000-hack DSDT` | CONFLICTO | `acpi-upgrades-iot2000.cpio` **does apply tables** on 6.12 (`dmesg \| grep "table upgrade"` → 1), so it may be unnecessary. Review before porting it. |
| **0015** `pca9685 gpio-base legacy` | **APLICA** | Strong candidate: the Arduino header exposes PWM/legacy base. Validate with `hw-smoke.sh` (PWM). |
| 0016-0018 `pca953x` | CONFLICTO | Header behavior (base/drive). Validate with `hw-smoke.sh`. |
| 0019-0032 `random` backports | CONFLICTO | **Obsolete** (they were for 4.x). Discarded. |
| rt-0001/rt-0002 | not evaluated | PREEMPT_RT is mainline configuration now: re-evaluate in a later phase. |

**Tool:** `scripts/clasificar-parches.sh` (pending) — runs the reverse dry-run and searches the tree
for the symbols added by each patch, to tell "upstream with changes" from "a real rebase".
