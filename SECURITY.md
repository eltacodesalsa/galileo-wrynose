# Security policy

## Reporting a vulnerability

Open a **private security advisory** on this repository
(`Security` -> `Advisories` -> `Report a vulnerability`), or contact the
maintainer directly through their GitHub profile. Please do not open a public
issue for anything that could let someone take over a board.

Expect an initial reply within a week. This is a hobby project with a single
maintainer, not a company with an SLA — but security reports are taken seriously
and credited unless you prefer otherwise.

## What is in scope

- The image: anything that grants remote access without the owner's own
  credentials, privilege escalation, or bypasses of the A/B rollback.
- `galileosetup` and the helper scripts: command injection, unsafe quoting,
  writing a secret to a world-readable location.
- The release artifacts: a `.wic.bz2` that does not match its published hash.

## Known design decisions (not vulnerabilities)

- **The image ships no SSH key.** SSH root login is key-only
  (`PermitRootLogin prohibit-password`) and password authentication is off, so a
  freshly flashed image has no remote access at all. Provision your own key from
  the serial console; this is deliberate.
- **The serial console accepts `root` with an empty password.** PAM
  `nullok_secure` restricts it to the local console, which is the physical
  recovery path. If you deploy a board somewhere physically accessible by
  others, set a console password or disable the getty.
- **No OTA yet.** `swupdate` is installed but built without the EFI Boot Guard
  backend, so there is no update mechanism in this release. The A/B layout and
  its rollback do work.
- **The Quark X1000 has no SSE/SSE2/CMOV.** Software built for a generic i686
  will crash with `Illegal instruction`. The image is built for `i586-nlp`.

## Supported versions

Only the latest release. Older images are not patched.
