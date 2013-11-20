package com.xxdroid.idevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.lidroid.xutils.util.LogUtils;

/**
 * Created by olunxchen on 2013.
 */
public class UsbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e("onReceive UsbReceiver-------------------------------------");
        Toast.makeText(context, "onReceive UsbReceiver", Toast.LENGTH_LONG).show();
    }
}
