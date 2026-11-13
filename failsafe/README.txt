DiamondSkip fail-safe card.
Copy failsafe.sh to the top level of an empty FAT32 card. If the card is inserted when the PCM starts, the ModKit runs this script early in boot and it deletes the DiamondSkip jar. Restart the PCM twice with the card inserted.
It writes what it did to failsafe_log.txt on the card.
