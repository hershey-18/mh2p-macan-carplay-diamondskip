# DiamondSkip – make the Porsche diamond button skip tracks in Apple CarPlay

On pre-2022 Porsche Macans with PCM 5, the steering wheel "diamond" (joker) button can be set to *Next Track*. That works for the radio, USB and Bluetooth audio, but does nothing at all in Apple CarPlay – so if you listen to Spotify, Apple Music or anything else through CarPlay, the button is dead. This is a long-standing complaint on Rennlist, Macanforum and r/Macan, and the usual answer is "buy a hardware module" or "buy a 2022+ car."

DiamondSkip is a small software mod, installed from an SD card with [LawPaul's MH2p SD ModKit](https://github.com/LawPaul/MH2p_SD_ModKit), that fixes it. Press the diamond button while CarPlay audio is playing and the track skips forward, exactly like pressing next on the phone.

- **No hardware, no coding of control units, no PIWIS.** It adds one small file to the PCM and changes nothing else.
- **Fully reversible.** Uninstalling deletes that one file. A fail-safe card is provided too.
- **Free for personal use.** Licensed CC BY-NC-SA 4.0 – no commercial use (see [license](#credits-and-license)).
- **Plays nicely with other mods.** Tested alongside [LawPaul's full screen CarPlay mod](https://github.com/LawPaul/MH2p_CarPlay_FullScreen) and [fifthBro's cluster navigation mod](https://github.com/fifthBro/mh2p-cluster).

> ⚠️ **Read the [Safety](#safety) and [Compatibility](#compatibility) sections before installing.** This modifies software in your car. It was built and tested on one car. You use it at your own risk.

If this saved you buying a module, tips are appreciated: [GitHub Sponsors](https://github.com/sponsors/hershey-18) · [Buy Me a Coffee](https://buymeacoffee.com/YOUR-USERNAME)

---

## Contents

- [Compatibility](#compatibility)
- [Install](#install)
- [Uninstall](#uninstall)
- [Safety](#safety)
- [What the mod actually does](#what-the-mod-actually-does)
- [Limitations](#limitations)
- [Building from source](#building-from-source)
- [Helping with other cars and firmware](#helping-with-other-cars-and-firmware)
- [Credits and license](#credits-and-license)

---

## Compatibility

| | |
|---|---|
| **Tested car** | 2020 Porsche Macan S (95B.2), US |
| **Head unit** | PCM 5 / MH2p |
| **Firmware** | `MH2p_US_PO416_P2656` |
| **Tested with** | Spotify over wired CarPlay, diamond button set to *Next Track* |
| **Tested alongside** | LawPaul full screen CarPlay, fifthBro mh2p-cluster v1105 |

The installer **checks the firmware version and refuses to install on anything other than PO416 / 2656.** If your car is on different firmware, nothing is changed and the ModKit log says so. See [Helping with other cars and firmware](#helping-with-other-cars-and-firmware) if you'd like to help extend support.

To find your firmware version, check the ModKit log after any ModKit run, or look under the PCM's software version screen.

---

## Install

You need an SD card (or USB stick) formatted **FAT32 / MS-DOS (FAT)** with a Master Boot Record partition map. The armrest SD slot works.

1. Download `DiamondSkip_Install.zip` from [Releases](../../releases).
2. Unzip it and copy **everything inside** to the top level of the card. The card's root should contain the folders `Data`, `Logs`, `Meta` and `Mods`, not a single folder named after the zip.
3. **On a Mac**, remove the hidden files macOS adds, which can confuse the installer. Replace `CARD` with your card's name:
   ```
   dot_clean -m /Volumes/CARD
   sudo find /Volumes/CARD -name '._*' -delete
   ```
   Then eject the card.
4. With the car on and the PCM fully booted, insert the card. The ModKit runs automatically. Wait until it tells you to remove the media.
5. Remove the card and restart the PCM (hold the volume/power knob for about 10 seconds until the Porsche logo appears).
6. Make sure the diamond button is set to *Next Track* in the PCM settings, connect CarPlay, play something and press it.

The card's `Logs` folder will contain a log of the install. A successful one ends with `OK: installed /mnt/app/eso/hmi/lsd/jars/diamondskip.jar`.

---

## Uninstall

1. Download `DiamondSkip_Uninstall.zip` from [Releases](../../releases). It is the same mod with an `uninstall.txt` marker, which tells the ModKit to run the uninstall script instead.
2. Copy it to a card the same way as the install, insert it, wait for it to finish, and restart the PCM.

The log should end with `OK: DiamondSkip removed`.

### Fail-safe

`DiamondSkip_FailSafe.zip` contains a `failsafe.sh` that the ModKit runs very early in boot – before the Porsche UI starts – whenever a card containing it is inserted. It deletes `diamondskip.jar` and writes what it did to `failsafe_log.txt` on the card. It is your escape hatch if the PCM UI were ever unusable after installing.

- Prepare this card **before** you install, and keep it in the glovebox.
- The ModKit only looks for `failsafe.sh` on `/fs/sda0`, `/fs/sdb0` and `/fs/usb0_0`. The armrest SD slot shows up as `/fs/usb0_0`.
- Because the fail-safe runs at the same time as the UI is starting, insert the card and **restart the PCM twice**.

---

## Safety

The goal from day one was "nothing we do can brick the car." Here's exactly what the mod touches and why it's low risk.

**What changes on the PCM:** one new file, `/mnt/app/eso/hmi/lsd/jars/diamondskip.jar` (about 9 KB). No Porsche file is edited, replaced or deleted. No control unit coding, no changes to the instrument cluster, no changes to the boot process.

**Why that's enough:** the PCM's UI start-up script (`lsd.sh`) already loads every `.jar` in that folder ahead of Porsche's own code. This is the same mechanism the full screen and cluster mods use. Deleting the file puts the PCM back exactly as it was.

**Guards in the installer:**
- It refuses to run unless the firmware is exactly PO416 / 2656.
- It refuses to run if the jar is missing from the card or the target folder doesn't exist.
- It verifies the copy and removes a partial file if the copy failed.
- It logs the folder contents before and after.

**Guards in the code:**
- The mod's own logic is wrapped so that any error is caught and logged rather than thrown into Porsche's code.
- If registering the handler fails for any reason, the CarPlay component carries on exactly as it would without the mod.
- The skip count is capped, so a malformed message can't send a burst of skips.

**Worst realistic case:** the CarPlay part of the UI misbehaves (for example, CarPlay doesn't start). Remove the mod with the uninstall card, or with the fail-safe card if you can't reach the UI. The PCM's core system, radio, cameras and car functions are not involved.

That said: this is unofficial software, it may affect your warranty conversation with a dealer, and a dealer software update may remove it (or change the code it depends on). **Use at your own risk.**

---

## What the mod actually does

### The problem in one paragraph

On these cars the diamond button isn't read by the PCM directly. The steering wheel talks to the **instrument cluster**, and the cluster sends an audio "skip" command to the PCM over BAP (the bus protocol VW Group uses between the cluster and the head unit). Inside the PCM, a component decides which audio source should receive that command based on what's currently playing. For radio, USB and Bluetooth there's a handler registered. **For CarPlay, the slot for that handler is empty** – nobody ever registered one on this firmware – so the PCM logs `no active audio service listener`, tells the cluster the command failed, and nothing happens.

### The fix

DiamondSkip registers the missing handler. When the cluster's skip command arrives while CarPlay is the active source, the handler presses and releases "next" on the CarPlay session – the same internal call the PCM uses when you press next in its own CarPlay controls – and then tells the cluster the skip succeeded.

### The two classes

**`DiamondCarPlaySkip`** is the handler itself. It implements the two cluster-audio listener interfaces Porsche's code expects.

- **Skip:** when the cluster asks for a skip, it works out the direction from the sign of the count (the diamond button set to *Next Track* always sends forward), clamps the number of steps between one and five, and for each step sends a press followed by a release to the CarPlay player. It then looks up the cluster's CarPlay audio service and reports the result, so the cluster isn't left waiting.
- **Other requests:** the same interfaces include other requests the cluster might send, such as seeking, changing source or selecting list entries. CarPlay doesn't support those through this path, so the handler gives the cluster a proper answer instead of silence:
  - Seek requests get a "seek stopped" status (sent shortly after the request, so the cluster sees the request acknowledged and then finished).
  - Source-switch requests get a "not possible" result.
  - Source-state and list preferences are echoed back.
  - Everything else is logged and ignored.
- **Logging and lookups:** all of this is logged with a `[DiamondCarPlaySkip]` prefix to the PCM's normal CarPlay log channel. Every lookup of a cluster service is released afterwards.

**`TMSkipOverJokerkey`** is the hook that gets the handler running. Porsche ships a class with this name in the CarPlay component. Its job is to make the *head unit's* joker key skip CarPlay tracks. It's created at start-up and is handed a reference to the CarPlay player, which is exactly what the handler needs.

Our jar provides a replacement class with the same name, constructor and fields. Because mod jars load ahead of Porsche's code, ours is the one used. When the CarPlay component starts it:

1. Does everything Porsche's original did: it creates Porsche's own original listener class (by name, from Porsche's code) and attaches it to the joker key service exactly as before. So that feature behaves exactly as it did.
2. Then creates a `DiamondCarPlaySkip` and registers it with the PCM's service registry, inside a catch-all so a failure can't affect the rest of start-up.

When the component shuts down, it closes the joker key tracker and unregisters the handler.

### What you'll see in the logs

If you capture the PCM system log, a working install looks like this on every press:

```
[DiamondCarPlaySkip] registered CarPlay cluster skip handler
[DiamondCarPlaySkip] cluster skip received, count=1
[DiamondCarPlaySkip] sent CarPlay skip forward
-> [CombiBAPServiceTerminalMode.skipResult] true
[DiamondCarPlaySkip] reported skip result: success
```

On the test car, 14 out of 14 presses over a drive skipped the track, each taking between about 2 and 30 milliseconds from the cluster's command to the reply.

---

## Limitations

- **Forward only, in practice.** The diamond button set to *Next Track* only sends "skip forward". The handler supports backward if something sends it, but the button can't.
- **No press-and-hold seek.** If the cluster asks to seek, the mod tells it seeking has stopped rather than scrubbing through the track.
- **One firmware version.** Only `MH2p_US_PO416_P2656` is enabled. Others are blocked by the installer until someone tests them.
- **Dealer updates.** A PCM software update at the dealer may remove the mod or change the code it relies on. Don't reinstall on new firmware until it has been checked.
- **Wireless CarPlay, other apps and other models** are untested, though there's no obvious reason other CarPlay audio apps would behave differently.

---

## Building from source

You need a JDK (11 or newer, so `--release 8` works), Python 3, and the PCM's `lsd.jar` **from your own car** – Porsche's code isn't included in this repo. The [ModKit](https://github.com/LawPaul/MH2p_SD_ModKit) can copy it to a USB stick.

```
./build.sh /path/to/lsd.jar
```

The script compiles the two classes against your `lsd.jar`, runs every class file through `tools/downgrade.py`, and packs the result into `diamondskip.jar`. Building from this source with the PCM's `lsd.jar` produces class files byte-for-byte identical to the released jar.

Then put `diamondskip.jar` in `Mods/DiamondSkip/Update/` alongside `install.sh` and `uninstall.sh` on a ModKit card.

### Repo layout

- `src/` – the two Java classes.
- `build.sh` – the build script above.
- `tools/downgrade.py` – the class-file rewriter for the PCM's Java VM.
- `mod/DiamondSkip/Update/` – the ModKit `install.sh` and `uninstall.sh`.
- `failsafe/` – the fail-safe script.
- `test/harness/` – the mock-service test that checks every cluster request gets the right reply.
- `test/real-module/` – the test that runs Porsche's real `ServiceManagerAudio` against the handler.

Both tests need `lsd.jar` from your own car on the classpath. `.gitignore` blocks jars and class files so Porsche's code can't be committed by accident.

Release zips bundle the ModKit (see [Credits and license](#credits-and-license)).

---

## Helping with other cars and firmware

If you have a PCM 5 car on a different firmware version and you're comfortable testing, please open an issue with:

- Model, model year and region.
- Your full firmware string (for example `MH2p_US_PO416_P2656`), from a ModKit log.
- Whether the diamond button works for Bluetooth audio but not CarPlay.

The code it hooks into has to be checked against each firmware's `lsd.jar` before the installer's version check is widened. Please don't edit the version check yourself.

---

## Credits and license

- **[LawPaul](https://github.com/LawPaul)** for the [MH2p SD ModKit](https://github.com/LawPaul/MH2p_SD_ModKit), which makes this mod (and most PCM 5 mods) possible, and for the full screen CarPlay mod. The ModKit is licensed **CC BY-NC-SA 4.0**. The release zips include it unmodified with its license, and they may not be sold.
- **[fifthBro](https://github.com/fifthBro)** for [mh2p-cluster](https://github.com/fifthBro/mh2p-cluster), whose work on the CarPlay internals was a useful reference.
- Everyone on Rennlist, Macanforum and r/Macan who documented the problem.

DiamondSkip is Copyright © 2026 hershey-18 and is licensed under [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 (CC BY-NC-SA 4.0)](LICENSE) – the same license as the ModKit. You're free to use, share and modify it for **non-commercial** purposes, as long as you credit this project and release any modified version under the same license. **Selling it, charging to install it, or bundling it into a paid product is not allowed** without permission. If you're interested in commercial use, open an issue to get in touch. Porsche, Macan and PCM are trademarks of Dr. Ing. h.c. F. Porsche AG. This project is not affiliated with or endorsed by Porsche, Audi or Volkswagen Group.

**No warranty.** This software is provided as is. You are responsible for anything you install on your car.
