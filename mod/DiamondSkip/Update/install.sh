#!/bin/ksh

JARS=/mnt/app/eso/hmi/lsd/jars
LSD=/mnt/app/eso/hmi/lsd/lsd.sh
TARGET=$JARS/diamondskip.jar
OPTIN="$MOD_PATH/../allow_untested.txt"

echo "DiamondSkip install: diamond button skips tracks in CarPlay"
echo "Release: $RELEASE_VERSION"

if [ "$OEM" != "PO" ]; then
    echo "REFUSED: DiamondSkip is only for Porsche PCM 5. Nothing changed."
    exit 0
fi

if [ "$OEM$TYPE" = "PO416" ] && [ "$SOFTWARE_VERSION" = "2656" ]; then
    echo "Tested software version PO416 P2656."
elif [[ -f "$OPTIN" ]]; then
    echo "WARNING: UNTESTED software version $OEM$TYPE P$SOFTWARE_VERSION."
    echo "WARNING: installing because allow_untested.txt is present. Use at your own risk."
    echo "WARNING: if CarPlay misbehaves, remove the mod with the uninstall or fail-safe card."
else
    echo "REFUSED: DiamondSkip has only been tested on PO416 P2656. Nothing changed."
    echo "To install anyway at your own risk, add an empty file named allow_untested.txt"
    echo "to the Mods/DiamondSkip folder on the card and run the install again."
    exit 0
fi

if [[ ! -f "$MOD_PATH/diamondskip.jar" ]]; then
    echo "REFUSED: diamondskip.jar is missing from the drive. Nothing changed."
    exit 0
fi

[[ ! -e "/mnt/app" ]] && mount -t qnx6 /dev/mnanda0t177.1 /mnt/app
mount -uw /mnt/app

if [[ ! -d "$JARS" ]]; then
    echo "REFUSED: $JARS does not exist. Nothing changed."
    exit 0
fi

if [[ -f "$LSD" ]] && [[ -z "$(sed -n '/\/jars/p' "$LSD")" ]]; then
    echo "REFUSED: this PCM software does not load mod jars from $JARS. Nothing changed."
    exit 0
fi

echo "Mod jars before install:"
ls -l "$JARS"

cp -f "$MOD_PATH/diamondskip.jar" "$TARGET"
chmod 644 "$TARGET"
sync

if [[ -s "$TARGET" ]]; then
    echo "OK: installed $TARGET"
    ls -l "$MOD_PATH/diamondskip.jar" "$TARGET"
else
    echo "ERROR: copy failed, removing partial file"
    rm -f "$TARGET"
fi

echo "Mod jars after install:"
ls -l "$JARS"
echo "DiamondSkip install finished"
