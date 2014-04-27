export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/data/data/com.xxdroid.xman/lib/
export PATH=$PATH:/data/data/com.xxdroid.xman/files/

ideviceid -l

ideviceinfo -x -q com.apple.disk_usage
com.apple.mobile.battery
com.apple.mobile.data_sync
com.apple.mobile.backup
com.apple.mobile.sync_data_class
com.apple.mobile.wireless_lockdown

idevicediagnostics diagnostics

ideviceinstaller -l -o xml