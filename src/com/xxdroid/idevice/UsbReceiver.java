package com.xxdroid.idevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.lidroid.xutils.util.LogUtils;
import com.xxdroid.xman.HexDump;

/**
 * Created by olunxchen on 2014.
 */
public class UsbReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e("onReceive UsbReceiver-------------------------------------");
        Toast.makeText(context, "onReceive UsbReceiver", Toast.LENGTH_LONG).show();

        UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        for (UsbDevice device : usbManager.getDeviceList().values()){
            String title = String.format("Vendor %s Product %s",
                    HexDump.toHexString((short) device.getVendorId()),
                    HexDump.toHexString((short) device.getProductId()));

            LogUtils.e("found usb device: " + title);
        }
    }

}
