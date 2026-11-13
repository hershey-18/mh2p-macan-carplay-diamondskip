#!/bin/ksh

JARS=/mnt/app/eso/hmi/lsd/jars
LSD=/mnt/app/eso/hmi/lsd/lsd.sh
TARGET=$JARS/diamondskip.jar

echo "DiamondSkip install: diamond button skips tracks in CarPlay"
echo "Release: $RELEASE_VERSION"

if [ "$OEM" != "PO" ]; then
    echo "REFUSED: DiamondSkip is only for Porsche PCM 5. Nothing changed."
    exit 0
fi

case "$OEM$TYPE P$SOFTWARE_VERSION" in
    "PO416 P2656"|"PO416 P2648")
        echo "Tested software version $OEM$TYPE P$SOFTWARE_VERSION."
        ;;
    *)
        echo "WARNING: UNTESTED software version $OEM$TYPE P$SOFTWARE_VERSION. Installing at your own risk."
        echo "WARNING: if CarPlay misbehaves, remove the mod with the uninstall or fail-safe card."
        ;;
esac

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
