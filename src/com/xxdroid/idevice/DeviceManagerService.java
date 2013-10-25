package com.xxdroid.idevice;

import android.util.Log;

import com.google.common.collect.Lists;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceManagerService {

    static {
        System.loadLibrary("iconv");
        System.loadLibrary("xml2");
        System.loadLibrary("plist");
        System.loadLibrary("usb");
        System.loadLibrary("usbmuxd");
        System.loadLibrary("crypto");
        System.loadLibrary("ssl");
        System.loadLibrary("imobiledevice");
        System.loadLibrary("zip");
        System.loadLibrary("idevice");
    }

    public native String[] getDeviceListNative();

    public native String getDeviceInfoNative(String uuid);

    private static String TAG = "DeviceManagerService";
    private final DeviceDetector detector;
    private Thread listeningThread;
    private final Map<String, DeviceInfo> deviceByUuid = new HashMap<String, DeviceInfo>();
    private volatile boolean run = true;
    private volatile boolean running = false;
    private static DeviceManagerService INSTANCE;


    public synchronized static DeviceManagerService create(DeviceDetector detector) {
        if (INSTANCE == null) {
            INSTANCE = new DeviceManagerService(detector);
        }
        return INSTANCE;
    }

    public synchronized static DeviceManagerService getInstance() {
        if (INSTANCE == null) {
            Log.e(TAG, "You need to create the instance passing a detector first.");
        }
        return INSTANCE;
    }

    private DeviceManagerService(DeviceDetector detector) {
        this.detector = detector;
    }

    private List<String> getDeviceList() {
        return Arrays.asList(getDeviceListNative());
    }

    private DeviceInfo getDeviceInfo(String uuid) {
        String xml = getDeviceInfoNative(uuid);
        try {
            xml = new String(xml.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new DeviceInfo(xml);
    }


    public synchronized void startDetection() {
        if (running) {
            Log.e(TAG, "already running. Only 1 instance allowed.");
        }
        listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                running = true;
                try {
                    while (run) {
                        List<String> connecteds = getDeviceList();
                        List<String> previouslyConnecteds = Lists.newArrayList(deviceByUuid.keySet());

                        for (String uuid : connecteds) {
                            if (!deviceByUuid.containsKey(uuid)) {
                                try {
                                    DeviceInfo di = getDeviceInfo(uuid);
                                    deviceByUuid.put(uuid, di);
                                    detector.onDeviceAdded(di);
                                } catch (Exception e) {
                                    System.err.println("cannot read info," + e.getMessage());
                                }
                            }
                            previouslyConnecteds.remove(uuid);
                        }

                        for (String uuid : previouslyConnecteds) {
                            DeviceInfo di = deviceByUuid.remove(uuid);
                            detector.onDeviceRemoved(di);
                        }
                    }

                } finally {
                    running = false;
                }
            }
        });
        listeningThread.start();
    }

    public void stopDetection() {
        run = false;
        while (running) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // ignore.
            }
            Log.e(TAG, "waiting for the listener thread to finish.");
        }
    }


    public static void main(String[] args) throws InterruptedException {
        DeviceDetector detector = new DeviceDetector() {
            @Override
            public void onDeviceAdded(DeviceInfo deviceInfo) {
                System.out.println(
                        "added " + deviceInfo.getDeviceName() + " running " + deviceInfo.getProductVersion());
                System.out.println(deviceInfo.toString());
            }

            @Override
            public void onDeviceRemoved(DeviceInfo deviceInfo) {
                System.out.println("device unplugged :" + deviceInfo.getDeviceName());
            }
        };
        DeviceManagerService manager = DeviceManagerService.create(detector);
        manager.startDetection();
        Thread.sleep(1000);
        manager.stopDetection();

        //DeviceManagerService manager2 = DeviceManagerService.create(detector);
        //manager2.startDetection();

    }
}


