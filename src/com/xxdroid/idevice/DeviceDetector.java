package com.xxdroid.idevice;

public interface DeviceDetector {

  public void onDeviceAdded(DeviceInfo deviceInfo);

  public void onDeviceRemoved(DeviceInfo deviceInfo);
}
