package com.xxdroid.idevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.lidroid.xutils.util.LogUtils;

/**
 * Created by olunxchen on 2014.
 */
public class UsbDetachReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e("onReceive UsbReceiver-------------------------------------");
        Toast.makeText(context, "xMan UsbDetachReceiver", Toast.LENGTH_LONG).show();
    }

}
