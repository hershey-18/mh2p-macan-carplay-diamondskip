#!/bin/ksh

SKIPJAR=/mnt/app/eso/hmi/lsd/jars/diamondskip.jar

DRIVE=/fs/usb0_0
for d in /fs/usb0_0 /fs/usb1_0 /fs/sda0 /fs/sdb0; do
    if [[ -f "$d/failsafe.sh" ]]; then
        DRIVE=$d
        break
    fi
done

{
    echo "---- DiamondSkip fail-safe ran ----"
    date
    [[ ! -e "/mnt/app" ]] && mount -t qnx6 /dev/mnanda0t177.1 /mnt/app
    mount -uw /mnt/app
    if [[ -f "$SKIPJAR" ]]; then
        rm -f "$SKIPJAR"
        sync
        echo "Removed DiamondSkip jar"
    else
        echo "DiamondSkip jar not present"
    fi
    echo "Mod jars now:"
    ls -l /mnt/app/eso/hmi/lsd/jars
    sync
    echo "---- fail-safe done ----"
} >> "$DRIVE/failsafe_log.txt" 2>&1
