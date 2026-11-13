# DiamondSkip – make the Porsche diamond button skip tracks in Apple CarPlay

On 2019–2021 Porsche Macans with PCM 5, the diamond button on the steering wheel can be set to *Next track*. That works for the radio, USB and Bluetooth audio, but does nothing in Apple CarPlay. If you listen to Spotify, Apple Music or podcasts through CarPlay, the button is dead. The usual answers are "buy a hardware module" or "buy a 2022+ car".

DiamondSkip is a free software fix that you install from an SD card in about 15 minutes. Once it's installed, pressing the diamond button while CarPlay audio is playing skips to the next track, just like pressing next on your phone.

- **No hardware and no dealer visit.** It adds one small file to the PCM and changes nothing else.
- **Fully reversible.** Uninstalling deletes that one file, and there's an emergency fail-safe card too.
- **Everything is in one download.** It includes LawPaul's free [MH2p SD ModKit](https://github.com/LawPaul/MH2p_SD_ModKit), the tool that lets the PCM install mods from an SD card. You don't need to download it separately.
- **Free for personal use.** Licensed CC BY-NC-SA 4.0, no commercial use (see [license](#credits-and-license)).
- **Works with other popular mods.** Tested alongside [LawPaul's full screen CarPlay mod](https://github.com/LawPaul/MH2p_CarPlay_FullScreen) and [fifthBro's cluster navigation mod](https://github.com/fifthBro/mh2p-cluster).

> ⚠️ **This changes software in your car and was tested on one car. You use it at your own risk.** Read [Will it work on my car?](#will-it-work-on-my-car) and [Is it safe?](#is-it-safe) before you start.

**[⬇️ Download the latest release](https://github.com/hershey-18/mh2p-macan-carplay-diamondskip/releases/latest)** – then follow [Install step by step](#install-step-by-step).

If this saved you buying a module, tips are appreciated: [Buy Me a Coffee](https://buymeacoffee.com/hershey18) ☕

---

## Contents

- [Will it work on my car?](#will-it-work-on-my-car)
- [Install step by step](#install-step-by-step)
- [Uninstall](#uninstall)
- [If something goes wrong: the fail-safe card](#if-something-goes-wrong-the-fail-safe-card)
- [Troubleshooting](#troubleshooting)
- [Is it safe?](#is-it-safe)
- [Limitations](#limitations)
- [For the technically curious](#for-the-technically-curious)
- [Helping with other cars and software versions](#helping-with-other-cars-and-software-versions)
- [Credits and license](#credits-and-license)

---

## Will it work on my car?

It has been tested on:

| | |
|---|---|
| **Car** | 2020 Porsche Macan S, US |
| **Infotainment** | PCM 5 (the system is also known as MH2p) |
| **PCM software version** | `MH2p_US_PO416_P2656` |
| **Phone connection** | Wireless CarPlay, Spotify |
| **Other mods installed** | LawPaul full screen CarPlay, fifthBro cluster navigation v1105 |

**You don't need to look up your software version first.** The installer checks it for you. If your car is on a different version, the installer stops without changing anything, and its log says so. If that happens, please see [Helping with other cars and software versions](#helping-with-other-cars-and-software-versions).

---

## Install step by step

### What you need

- **One SD card or USB stick, 32 GB or smaller.** Larger ones are harder to format correctly. Everywhere below, "card" means whichever you're using.
- **Optional: a second card or USB stick** to use as an emergency fail-safe card you keep in the glovebox (see step 3). One is enough to install the mod.
- **A Mac or Windows computer** with an SD card reader or USB port.
- **About 15 minutes in the car** with the engine running.

> **Never put the fail-safe file on the install card.** The fail-safe removes the mod every time the PCM starts with it inserted, so it would undo the install. If you only have one card, skip the fail-safe for now – you can always reformat your card and make one later if you ever need it.

### Step 1: Download the files

1. Open the **[latest release](https://github.com/hershey-18/mh2p-macan-carplay-diamondskip/releases/latest)**.
2. Under **Assets** at the bottom of the page, download **DiamondSkip_Install.zip**. If you're making a fail-safe card, also download **DiamondSkip_FailSafe.zip**.

### Step 2: Format the card

If you're making a fail-safe card too, do this for both. **Formatting erases everything on the card.**

**On a Mac:**
1. Insert the card and open **Disk Utility** (press Cmd+Space, type *Disk Utility*, press Enter).
2. In the **View** menu, choose **Show All Devices**.
3. In the left sidebar, click the card's top-level entry (the device, not the indented volume under it).
4. Click **Erase** and set:
   - **Name:** anything simple, for example `PORSCHE`
   - **Format:** MS-DOS (FAT)
   - **Scheme:** Master Boot Record
5. Click **Erase**, then **Done**.

If Disk Utility shows an error with an SD card, check the small lock switch on its side is slid towards the contacts (unlocked) and try again.

**On Windows:**
1. Insert the card, open **File Explorer**, and right-click the card.
2. Choose **Format**, set **File system** to **FAT32**, and click **Start**.

### Step 3 (optional): Make a fail-safe card

Skip this if you only have one card.

1. Open **DiamondSkip_FailSafe.zip** (double-click on a Mac; on Windows, right-click and choose **Extract All**).
2. Copy **failsafe.sh** onto your second card. It should be the only file on it.
3. Label it "FAIL-SAFE" and keep it in the glovebox. You'll probably never need it.

### Step 4: Make the install card

1. Open **DiamondSkip_Install.zip** the same way.
2. Open the folder it creates, select everything inside it (Cmd+A on a Mac, Ctrl+A on Windows), and copy it onto your card (not the fail-safe card).
3. Check the card. Its top level should show the folders **Data**, **Logs**, **Meta** and **Mods** and two README/LICENSE files. If you see a single folder named *DiamondSkip_Install* instead, open it and move its contents up to the top level of the card.

### Step 5 (Mac only): Remove hidden Mac files

Macs quietly add hidden files to SD cards and USB sticks that confuse the PCM. Windows users can skip this step.

1. Open **Terminal** (press Cmd+Space, type *Terminal*, press Enter).
2. Type `dot_clean -m ` (with a space at the end), then drag the card's icon from the Finder sidebar into the Terminal window and press Enter.
3. Type `sudo find ` (with a space), drag the card in again, then type ` -name '._*' -delete` and press Enter. Enter your Mac password when asked. Nothing appears while you type it – that's normal.
4. Eject the card in Finder.

If you made a fail-safe card, do the same for it. Some "Operation not permitted" messages about `.Trashes` or `.Spotlight-V100` are harmless and can be ignored.

### Step 6: Install in the car

1. Start the car (the **engine must be running**, not just the ignition) and wait for the PCM to finish starting up.
2. Insert the **install card** into the SD slot or USB port (in the centre console or glovebox, depending on your car). If it's an SD card, make sure its lock switch is unlocked.
3. Within about 10 seconds the PCM restarts and the install runs by itself. It takes a few minutes. You'll see a black **MH2p ModKit** screen, and the PCM may restart a few times and show some red engineering pages. **That's normal – don't touch anything and don't turn the car off.**
4. When the screen says **"Please remove update media"**, remove the card.
5. The PCM restarts into its normal screen with the mod installed.

### Step 7: Set up the button and test it

1. In the PCM settings, set the diamond button to *Next track*. The setting is three menus deep: **◇ button operation** → **PCM Functions** → **Next track**.
2. Connect CarPlay, play something in Spotify or Apple Music, and press the diamond button. The track should skip.

### Step 8 (optional): Check the install log

Put the install card back in your computer and open **Logs/DiamondSkip.log**. A successful install ends with:

```
OK: installed /mnt/app/eso/hmi/lsd/jars/diamondskip.jar
```

If it says **REFUSED** instead, nothing was changed – see [Troubleshooting](#troubleshooting).

---

## Uninstall

1. Download **DiamondSkip_Uninstall.zip** from the **[latest release](https://github.com/hershey-18/mh2p-macan-carplay-diamondskip/releases/latest)**.
2. Format a card, copy the files onto it and (on a Mac) remove the hidden files, exactly as in steps 2, 4 and 5 above.
3. In the car, follow step 6 again. When the PCM restarts, the mod is gone.

The log on the card, **Logs/DiamondSkip.log**, should end with `OK: DiamondSkip removed`.

---

## If something goes wrong: the fail-safe card

If the PCM ever misbehaves after installing (for example, CarPlay won't start) and you can't run the uninstall card, use the fail-safe card:

If you didn't make a fail-safe card during the install, make one now on any card or USB stick (steps 2, 3 and 5 above).

1. With the car running, insert the **fail-safe card**.
2. Restart the PCM by holding the volume/power knob for about 10 seconds, until the screen goes black and the Porsche logo appears.
3. **Restart it a second time the same way.** The fail-safe runs very early during start-up, so the second restart makes sure the PCM starts cleanly without the mod.
4. Remove the card.

The fail-safe deletes the mod's file and writes what it did to **failsafe_log.txt** on the card.

---

## Troubleshooting

**Nothing happens when I insert the card, or the PCM says "update not supported".**
The card isn't set up quite right. Check that it's formatted as FAT32 / MS-DOS (FAT), that **Data**, **Meta** and **Mods** are at the top level of the card (not inside another folder), that the lock switch is unlocked, and on a Mac that you removed the hidden files in step 5.

**The PCM says "Can't start update because BEM level is set to LEVEL 1".**
The engine needs to be running, not just the ignition.

**An error number appears partway through the install.**
Reformat the card and copy the files again, or try a different card. The card may be faulty.

**The log says REFUSED.**
Your car is on a PCM software version that hasn't been tested yet, so the installer stopped without changing anything. The log's **Release:** line shows your version. Please [open an issue](../../issues) with it.

**The install worked but the button still doesn't skip.**
- Check the diamond button is set to *Next track* (**◇ button operation** → **PCM Functions** → **Next track**).
- Make sure CarPlay is the audio source that's playing (not radio or Bluetooth).
- Restart the PCM once by holding the volume/power knob for about 10 seconds.
- Check **Logs/DiamondSkip.log** on the install card ends with `OK: installed`.

**CarPlay won't start after installing.**
Remove the mod with the uninstall card, or with the [fail-safe card](#if-something-goes-wrong-the-fail-safe-card) if you can't. Then please [open an issue](../../issues).

---

## Is it safe?

The goal from day one was that nothing this mod does could "brick" the car. Here's what it touches and why it's low risk.

- **It only adds one small file** (about 9 KB) to the PCM. It doesn't edit, replace or delete any Porsche files, and it doesn't touch the instrument cluster, the car's control units or how the PCM starts up.
- **Removing that file puts everything back** exactly as it was. That's what the uninstall and fail-safe cards do.
- **It uses the same method as other well-known PCM mods.** The PCM already loads extra files from that folder at start-up, which is how the full screen CarPlay and cluster navigation mods work too.
- **The installer is careful.** It stops without changing anything if your PCM software version isn't the tested one, if a file is missing, or if the copy fails.
- **The code is careful too.** If anything goes wrong inside the mod, it's caught and logged rather than passed on to Porsche's software, and CarPlay carries on as if the mod weren't there.

**Worst realistic case:** CarPlay misbehaves (for example, it doesn't start). You'd remove the mod with the uninstall or fail-safe card. The radio, cameras and the car's own functions aren't involved.

That said, this is unofficial software. It may come up in a warranty conversation with a dealer, and a dealer software update may remove it. **Use at your own risk.**

---

## Limitations

- **Skip forward only.** The diamond button set to *Next track* only sends "next". There's no way to go back a track with it.
- **No press-and-hold to fast-forward.**
- **One PCM software version so far.** Only `MH2p_US_PO416_P2656` is enabled. Others are blocked by the installer until someone tests them.
- **Dealer updates.** A PCM software update at the dealer may remove the mod or change the code it relies on. Don't reinstall it on a new software version until that version has been checked.
- **Wired CarPlay, other apps and other models** haven't been tested. The mod works on the CarPlay session itself rather than the connection, so wired CarPlay and other CarPlay audio apps should behave the same.

---

## For the technically curious

You don't need anything below this point to install the mod.

### What the mod actually does

#### The problem in one paragraph

On these cars the diamond button isn't read by the PCM directly. The steering wheel talks to the **instrument cluster**, and the cluster sends an audio "skip" command to the PCM over BAP (the bus protocol VW Group uses between the cluster and the head unit). Inside the PCM, a component decides which audio source should receive that command based on what's currently playing. For radio, USB and Bluetooth there's a handler registered. **For CarPlay, the slot for that handler is empty** – nobody ever registered one on this firmware – so the PCM logs `no active audio service listener`, tells the cluster the command failed, and nothing happens.

#### The fix

DiamondSkip registers the missing handler. When the cluster's skip command arrives while CarPlay is the active source, the handler presses and releases "next" on the CarPlay session – the same internal call the PCM uses when you press next in its own CarPlay controls – and then tells the cluster the skip succeeded.

#### The two classes

**`DiamondCarPlaySkip`** is the handler itself. It implements the two cluster-audio listener interfaces Porsche's code expects.

- **Skip:** when the cluster asks for a skip, it works out the direction from the sign of the count (the diamond button set to *Next track* always sends forward), clamps the number of steps between one and five, and for each step sends a press followed by a release to the CarPlay player. It then looks up the cluster's CarPlay audio service and reports the result, so the cluster isn't left waiting.
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

#### What you'll see in the logs

If you capture the PCM system log, a working install looks like this on every press:

```
[DiamondCarPlaySkip] registered CarPlay cluster skip handler
[DiamondCarPlaySkip] cluster skip received, count=1
[DiamondCarPlaySkip] sent CarPlay skip forward
-> [CombiBAPServiceTerminalMode.skipResult] true
[DiamondCarPlaySkip] reported skip result: success
```

On the test car, 14 out of 14 presses over a drive skipped the track, each taking between about 2 and 30 milliseconds from the cluster's command to the reply.

### Building from source

You need a JDK (11 or newer, so `--release 8` works), Python 3, and the PCM's `lsd.jar` **from your own car** – Porsche's code isn't included in this repo. The [ModKit](https://github.com/LawPaul/MH2p_SD_ModKit) can copy it to a USB stick.

```
./build.sh /path/to/lsd.jar
```

The script compiles the two classes against your `lsd.jar`, runs every class file through `tools/downgrade.py`, and packs the result into `diamondskip.jar`. Building from this source with the PCM's `lsd.jar` produces class files byte-for-byte identical to the released jar.

Then put `diamondskip.jar` in `Mods/DiamondSkip/Update/` alongside `install.sh` and `uninstall.sh` on a ModKit card.

#### Repo layout

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

## Helping with other cars and software versions

If your install log says REFUSED, or you have a PCM 5 car on a different software version and you're comfortable testing, please [open an issue](../../issues) with:

- Model, model year and region.
- Your PCM software version (for example `MH2p_US_PO416_P2656`), from the **Release:** line of **Logs/DiamondSkip.log**.
- Whether the diamond button works for Bluetooth audio but not CarPlay.

The code it hooks into has to be checked against each firmware's `lsd.jar` before the installer's version check is widened. Please don't edit the version check yourself.

---

## Credits and license

- **[LawPaul](https://github.com/LawPaul)** for the [MH2p SD ModKit](https://github.com/LawPaul/MH2p_SD_ModKit), which makes this mod (and most PCM 5 mods) possible, and for the full screen CarPlay mod. The ModKit is licensed **CC BY-NC-SA 4.0**. The release zips include it unmodified with its license, and they may not be sold.
- **[fifthBro](https://github.com/fifthBro)** for [mh2p-cluster](https://github.com/fifthBro/mh2p-cluster), whose work on the CarPlay internals was a useful reference.
- Everyone on Rennlist, Macanforum and r/Macan who documented the problem.

DiamondSkip is Copyright © 2026 hershey-18 and is licensed under [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 (CC BY-NC-SA 4.0)](LICENSE) – the same license as the ModKit. You're free to use, share and modify it for **non-commercial** purposes, as long as you credit this project and release any modified version under the same license. **Selling it, charging to install it, or bundling it into a paid product is not allowed** without permission. If you're interested in commercial use, open an issue to get in touch. Porsche, Macan and PCM are trademarks of Dr. Ing. h.c. F. Porsche AG. This project is not affiliated with or endorsed by Porsche, Audi or Volkswagen Group.

**No warranty.** This software is provided as is. You are responsible for anything you install on your car.
