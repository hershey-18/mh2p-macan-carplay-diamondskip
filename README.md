# DiamondSkip – make the Porsche diamond button skip tracks in Apple CarPlay

On pre-2022 Porsche Macans with PCM 5, the steering wheel "diamond" (joker) button can be set to *Next*. That works for the radio, USB and Bluetooth audio, but does nothing at all in Apple CarPlay – so if you listen to Spotify, Apple Music or anything else through CarPlay, the button is dead. This is a long-standing complaint on Rennlist, Macanforum and r/Macan, and the usual answer is "buy a hardware module" or "buy a 2022+ car."

DiamondSkip is a small software mod, installed from an SD card with [LawPaul's MH2p SD ModKit](https://github.com/LawPaul/MH2p_SD_ModKit), that fixes it. Press the diamond button while CarPlay audio is playing and the track skips forward, exactly like pressing next on the phone.

- **No hardware, no coding of control units, no PIWIS.** It adds one small file to the PCM and changes nothing else.
- **Fully reversible.** Uninstalling deletes that one file. A fail-safe card is provided too.
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
- [How we figured it out](#how-we-figured-it-out)
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
| **Tested with** | Spotify over wired CarPlay, diamond button set to *Next* |
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
6. Make sure the diamond button is set to *Next* in the PCM settings, connect CarPlay, play something and press it.

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

**`DiamondCarPlaySkip`** is the handler itself. It implements the two cluster-audio listener interfaces Porsche's code expects (more on why it implements two in the [investigation](#how-we-figured-it-out)).

- **Skip:** when the cluster asks for a skip, it works out the direction from the sign of the count (the diamond button set to *Next* always sends forward), clamps the number of steps between one and five, and for each step sends a press followed by a release to the CarPlay player. It then looks up the cluster's CarPlay audio service and reports the result, so the cluster isn't left waiting.
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

## How we figured it out

This took several rounds of reading Porsche's code and capturing logs from the car. Here's the path, including the dead ends, in case it helps anyone doing something similar.

### 1. "Porsche fixed this in 2022"

Forum posts say the diamond button works with CarPlay on 2022+ Macans. Those cars have the newer PCM 6 hardware, so that "fix" isn't something you can install on an older car. It did suggest the problem was software rather than wiring, though: the button clearly reaches the PCM, because it works for every other audio source.

### 2. Getting the code off the car

PCM 5's UI is a Java application. Using the ModKit, a small script copied the UI's main jar and the existing mod jars from the PCM to a USB stick. Decompiling it showed the CarPlay component, the joker key handling, and the cluster audio plumbing.

### 3. The obvious suspect didn't fire

The CarPlay component already contains `TMSkipOverJokerkey`, Porsche's own "joker key skips CarPlay tracks" feature. That looked like the answer – maybe it was just switched off. But that class listens to the *head unit's* joker key service, and on the Macan the diamond button lives on the steering wheel and goes through the cluster. Logging confirmed it: that code never ran when the button was pressed.

### 4. Turning up the logging

To see what *did* happen, a temporary diagnostic mod (DiamondDiag) raised the log level of the relevant components by editing the logging line in `lsd.sh`, after backing it up. It also started a background capture of the QNX system log to a second SD card on each boot. It was built to be undone completely: the uninstall restores the backup and removes the capture.

With that in place, the button was pressed with three sources:
- Spotify over CarPlay (didn't work)
- YouTube Music over CarPlay (didn't work)
- Bluetooth audio (worked)

The logs from the three were then compared.

### 5. Finding the gap

The logs and the code together showed the real path:
1. The cluster sends a BAP "dedicated audio control" skip.
2. The PCM's BAP audio handler asks: *which listener handles the currently active source?*
3. For Bluetooth it finds the media listener.
4. For CarPlay ("smartphone integration") it asks the cluster audio module for its CarPlay listener – which is always empty.
5. It logs `no active audio service listener` and tells the cluster the command failed.

Porsche wrote the plumbing for a CarPlay listener, but on this firmware nothing ever registers one.

### 6. The first fix was wrong (and testing caught it)

The first version registered the new handler under the CarPlay listener's interface name. Before it ever went near the car, a separate review of the code found the problem. The component that fills those listener slots (`ServiceManagerAudio`) only watches for seven specific service names – tuner, media, online media, TV and tone listeners, plus two others – and **the CarPlay listener isn't one of them.** Registering under that name would have been silently ignored.

The way through came from how that component sorts what it finds. When a matching service appears, it checks what *kind* of object it is, and it checks for the CarPlay listener type before the tone listener type. So the handler implements **both** interfaces and is registered under the **tone listener** name, which the component does watch. When it's picked up, the CarPlay check matches first and it goes into the CarPlay slot. The real tone listener's slot is never touched.

Rather than trust that reading of the code, Porsche's actual `ServiceManagerAudio` class was run on a desktop Java VM with mock services, using the code dumped from this car. Adding the handler put it in the CarPlay slot and left the tone slot alone. Removing it cleared only the CarPlay slot.

### 7. Answering the cluster properly

The cluster expects a reply to its requests. A handler that silently ignores them could leave the cluster waiting or in an odd state. So every request the interfaces define gets either the real action (skip), a proper "can't do that" or status reply (seek, source switch), or an echo (source state, list preference). A desktop test harness checked that each request produces the expected calls back to the cluster service.

### 8. Building for a 2010s Java VM

The PCM runs IBM's J9 VM, which expects old Java 1.4-format class files. The code is compiled with a modern JDK targeting Java 8, against the jar from the car, and then run through a small script (`tools/downgrade.py`). The script rewrites the class files to the older version and strips the metadata the old VM doesn't understand. It refuses to process anything that uses features the old format can't represent.

The source sticks to what Java 1.4 can do: no generics, no autoboxing, no `StringBuilder` and no `+` string joining (`StringBuffer` instead), and classes are looked up by name rather than with `.class` literals.

### 9. The car test

Installed on the car, the handler registered at boot, and every press of the diamond button skipped the Spotify track and reported success to the cluster. The diagnostic logging was then removed, leaving only the one jar.

---

## Limitations

- **Forward only, in practice.** The diamond button set to *Next* only sends "skip forward". The handler supports backward if something sends it, but the button can't.
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

DiamondSkip's own code (everything in this repo except the bundled ModKit in the release zips) is released under the [MIT license](LICENSE). Porsche, Macan and PCM are trademarks of Dr. Ing. h.c. F. Porsche AG. This project is not affiliated with or endorsed by Porsche, Audi or Volkswagen Group.

**No warranty.** This software is provided as is. You are responsible for anything you install on your car.
