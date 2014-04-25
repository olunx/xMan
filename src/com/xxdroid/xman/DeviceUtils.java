package com.xxdroid.xman;

/**
 * Created by olunxchen on 2014-04-25.
 */
public class DeviceUtils {
    //    <!-- 0x05ac / 0x12a8: iPhone 5/5c/5s  -->
//    <usb-device vendor-id="1452" product-id="4776" />
//    <!-- 0x05ac / 0x12a0: iPhone 4s  -->
//    <usb-device vendor-id="1452" product-id="4768" />
    public static final int VENDOR_ID_APPLE = 1452;
    public static final int PRODUCT_ID_IPHONE_4S = 4768;
    public static final int PRODUCT_ID_IPHONE_5S = 4776;

    private static DeviceUtils deviceUtils;
    public static DeviceUtils getInstance(){
        if(deviceUtils == null){
            deviceUtils = new DeviceUtils();
        }
        return deviceUtils;
    }

    public String getDeviceDesc(int vendor, int product){
        String desc;
        switch (vendor){
            case VENDOR_ID_APPLE:
                switch (product){
                    case PRODUCT_ID_IPHONE_4S:
                        desc = "Apple iPhone 4s";
                        break;
                    case PRODUCT_ID_IPHONE_5S:
                        desc = "Apple iPhone 5s";
                        break;
                    default:
                        desc = "Apple Device";
                        break;
                }
                break;
            default:
                desc = "unknown";
                break;
        }
        return desc;
    }
}
