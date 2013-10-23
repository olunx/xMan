#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

SRC_DIR=$DIR/../libimobiledevice-android/android/libs/armeabi-v7a
LIBS_DIR=$DIR/libs/armeabi-v7a
BIN_DIR=$DIR/res/raw

mkdir -p $LIBS_DIR
mkdir -p $BIN_DIR

# Install so
echo "----------Install so--------------"
for S in libiconv.so libxml2.so libplist.so libplist++.so libusb.so libusbmuxd.so libcrypto.so libssl.so libimobiledevice.so libzip.so
do
  rm "$LIBS_DIR/$S"
  mv "$SRC_DIR/$S" $LIBS_DIR
done

# Install binary
echo "----------Install binary--------------"
for B in listdevs openssl usbmuxdd ideviceid ideviceinfo idevicedate idevicediagnostics idevicescreenshot idevicesyslog ideviceinstaller ifuse fusermount
do
  rm "$BIN_DIR/$B"
  mv "$SRC_DIR/$B" $BIN_DIR
done

echo "----------Install done--------------"