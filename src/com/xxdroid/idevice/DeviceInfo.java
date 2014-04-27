package com.xxdroid.idevice;

import com.dd.plist.NSDictionary;

import java.lang.Exception;

public class DeviceInfo {

    private final String raw;
    private String buildVersion;
    private String bluetoothAddress;
    private String boardId;
    private String cpuArchitecture;
    private String chipID;
    private String deviceClass;
    private String deviceColor;
    private String productType;
    private String productVersion;
    private String uniqueDeviceID;
    private String wifiAddress;
    private String deviceName;
    private String firmwareVersion;
    private String hardwareModel;
    private String modelNumber;
    // array of int
    private String SupportedDeviceFamilies;

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public String getBoardId() {
        return boardId;
    }

    public String getCpuArchitecture() {
        return cpuArchitecture;
    }

    public String getChipID() {
        return chipID;
    }

    public String getDeviceClass() {
        return deviceClass;
    }

    public String getDeviceColor() {
        return deviceColor;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getHardwareModel() {
        return hardwareModel;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public String getProductType() {
        return productType;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getUniqueDeviceID() {
        return uniqueDeviceID;
    }

    public String getWifiAddress() {
        return wifiAddress;
    }


    public DeviceInfo(String xml) {
        this.raw = xml;
        try {
            parse();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot parse the device info xml " + e.getMessage(), e);
        }

    }

    private void parse() throws Exception {
        byte[] xml = raw.getBytes("UTF-8");
        NSDictionary rootDict = (NSDictionary) MyParser.parse(xml);
        buildVersion = rootDict.objectForKey("BuildVersion").toString();

        bluetoothAddress = get(rootDict, "BluetoothAddress");
        boardId = get(rootDict, "BoardId");
        cpuArchitecture = get(rootDict, "CPUArchitecture");
        chipID = get(rootDict, "ChipID");
        deviceClass = get(rootDict, "DeviceClass");
        deviceColor = get(rootDict, "DeviceColor");
        deviceName = get(rootDict, "DeviceName");
        firmwareVersion = get(rootDict, "FirmwareVersion");
        hardwareModel = get(rootDict, "HardwareModel");
        modelNumber = get(rootDict, "ModelNumber");
        productType = get(rootDict, "ProductType");
        productVersion = get(rootDict, "ProductVersion");
        uniqueDeviceID = get(rootDict, "UniqueDeviceID");
        wifiAddress = get(rootDict, "WiFiAddress");

    }

    private String get(NSDictionary rootDict, String key) {
        if (rootDict.objectForKey(key) != null) {
            return rootDict.objectForKey(key).toString();
        } else {
            System.out.println("key " + key + " is null.");
            return null;
        }
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("DeviceInfo");
        sb.append("{buildVersion='").append(buildVersion).append('\'');
        sb.append(", cpuArchitecture='").append(cpuArchitecture).append('\'');
        sb.append(", deviceClass='").append(deviceClass).append('\'');
        sb.append(", deviceName='").append(deviceName).append('\'');
        sb.append(", productType='").append(productType).append('\'');
        sb.append(", productVersion='").append(productVersion).append('\'');
        sb.append(", uniqueDeviceID='").append(uniqueDeviceID).append('\'');
        sb.append('}');
        return sb.toString();
    }


    public String getBuildVersion() {
        return buildVersion;
    }


}




