# galileosetup — status

Configuration utility for the Galileo / SIMATIC IOT2000. Pure POSIX shell, no dependencies
(no `snack`, no `mraa`, no Python). It functionally replaces Siemens' `iot2000setup`, which
is kept untouched.

- Version: **0.2**  ·  File: `recipes-example/galileo-setup/files/galileosetup`
- Installed on the board as `/usr/bin/galileosetup` (755)
- The `iot2000setup → galileosetup` alias is only created if the vendor binary is **not** present

## What it does

### Main menu
```
1) OS Settings        → Hostname · Password · Timezone · Set Date and Time · Sync Clock (NTP)
2) Network configuration → Wifi (SSID/PW/TYPE/IP) · Wired (IP/SUBNET/GATEWAY, precarga el gw actual)
3) System status      → kernel, host, uptime, hora, zona, memoria, interfaces, SSIDs
4) Exit
```

### Time zone in three steps
`Continent → Country → Zone`, with fixed-height scrolling lists and an `[9/23]` counter.
Only zones that exist in `/usr/share/zoneinfo` are offered. If a country has just one, it is applied
without asking. The country→zones table is static (deriving it at runtime cost ~270 subprocesses and
took seconds on the Quark).

### Date and time
- `Set Date and Time`: form pre-filled with the current date/time; validates `YYYY-MM-DD` and
  `HH:MM:SS`; writes the RTC if `/dev/rtc0` exists.
- `Sync Clock (NTP)`: `ntpd -q` with `rdate` as fallback.

### CLI (everything verifiable without a TTY)
```
galileosetup --status
galileosetup --hostname NOMBRE
galileosetup --tz 'America/Tijuana' | --tz Mexico
galileosetup --tz-list Mexico
galileosetup --datetime '2026-10-22 21:58:30'
galileosetup --ntp
galileosetup --eth IP [MASCARA] [GATEWAY] | --wifi ...
galileosetup --serial MODE
galileosetup --version | --help
```

## Navigation
↑/↓ arrows (menus and forms), Enter (select / edit / accept), Tab (next field),
←/→ (OK/CANCEL), Esc (cancel), `q` (exits instantly, no Enter). Cursor hidden while running.

## Design decisions
- **No GPIO or PWM in 0.1**: `mraa` 2.1.0 assumes kernel 4.4's `gpiochip` numbering; on 6.12
  the chips are dynamic, so it would be lying.
- **Internal labels in ASCII**: `printf` pads by bytes, not by columns; accents inside
  the frame throw it off.
- **Box with one row of margin**: if it measures exactly the terminal height, the final
  line break scrolls and all absolute coordinates end up shifted (title and option 1 duplicated).
- **Bilingual**: English by default (community), Spanish with `GALILEOSETUP_LANG=es`.
- **Sandbox for tests**: `GALILEOSETUP_ETC=/tmp/...` does not touch `/etc`.

## Environment traps already solved (do not repeat)
| Symptom | Cause |
|---|---|
| Enter does not advance the menu | `$(dd ...)` **strips** the trailing `0x0a`: read it with `od` and compare in hex |
| The 2nd secret field comes out empty | each `stty` uses TCSAFLUSH and discards what was typed |
| Arrows write `^[[A` into the field | `read -r` in cooked mode; there is now a custom editor that discards ESC |
| Options get erased when moving | row off-by-one + positional parameters inside the function |
| Title and option 1 duplicated | box with exact height → 1-row scroll |
| Messages on top of the frame | painted on the last row, clearing with `ESC[K` |
| Frame leftovers on the right | notices with multibyte `↑/↓` and `printf %-80s` pads by bytes |

## Cause of the slow clock (finding)
`eth0` was configured **without a `gateway`**, so the board had no default route
(`Network is unreachable`): no internet means no NTP, and the clock stayed at the RTC value. Fixed with
`gateway 192.0.2.254` and IPv4 DNS in `resolv.conf`; `ntpd` applied **+10293 s**. The network form
now **pre-fills the current gateway** so it cannot be left empty by mistake.

## Pending
- Reproduce the utility inside the Yocto image (the recipe already installs the file; baking it in is pending).
- `hw-smoke.sh`: mutation test (break DNS/daemon/camera and check that the test **fails**).
- Decide `=m` vs `=y` for the modules, and whether to port or fork `iot2000-bsp`.
- Flash kernel build **#10** and measure the watchdog timeline.
