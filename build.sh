#!/bin/sh
# Usage: ./build.sh /path/to/lsd.jar
set -e
LSD="$1"
[ -f "$LSD" ] || { echo "Usage: ./build.sh /path/to/lsd.jar (dumped from your own car)"; exit 1; }
rm -rf out diamondskip.jar
mkdir out
javac --release 8 -g:none -cp "$LSD" -d out src/de/audi/app/terminalmode/hmi/*.java
find out -name '*.class' -exec python3 tools/downgrade.py {} +
(cd out && jar cf ../diamondskip.jar de)
echo "Built diamondskip.jar"
