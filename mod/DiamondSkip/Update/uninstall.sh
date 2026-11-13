#!/bin/ksh

TARGET=/mnt/app/eso/hmi/lsd/jars/diamondskip.jar

echo "DiamondSkip uninstall"

[[ ! -e "/mnt/app" ]] && mount -t qnx6 /dev/mnanda0t177.1 /mnt/app
mount -uw /mnt/app

if [[ -f "$TARGET" ]]; then
    rm -f "$TARGET"
    sync
fi

if [[ -f "$TARGET" ]]; then
    echo "ERROR: $TARGET is still present"
else
    echo "OK: DiamondSkip removed"
fi

ls -l /mnt/app/eso/hmi/lsd/jars
echo "DiamondSkip uninstall finished"
