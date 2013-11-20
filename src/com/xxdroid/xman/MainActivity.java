package com.xxdroid.xman;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.util.LogUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.xxdroid.idevice.DeviceDetector;
import com.xxdroid.idevice.DeviceInfo;
import com.xxdroid.idevice.DeviceManagerService;
import com.xxdroid.idevice.UsbReceiver;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity {

    private String busybox = "busybox";
    private String usbmuxd = "usbmuxdd";
    private String ideviceid = "ideviceid";
    private String ideviceinfo = "ideviceinfo";

    private TextView textView;
    private Handler handler;

    private String appPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        textView = (TextView)findViewById(R.id.textView);
        textView.setText("onCreate \n");

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                textView.setText(textView.getText() + " " + bundle.getString("msg") + " \n");
            }
        };

        appPath = getApplicationInfo().dataDir;
        textView.setText("appPath: " + appPath + "\n");

        LogUtils.e("new UsbReceiver-------------------------------------");
        UsbReceiver usbReceiver = new UsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnInstall:{
                installBin();
                textView.setText(textView.getText() + " binary installed" + " \n");
                LogUtils.d("binary installed");
                break;
            }
            case R.id.btnUsbmuxd:{
                Command command = new XCommand(0,
                        exportLib(),
                        getBinPath(usbmuxd),
                        getBinPath(busybox) + " ps | " + getBinPath(busybox) + " grep usbmuxd");
                runCommand(command);
                break;
            }
            case R.id.btnIdeviceid:{
                Command command = new XCommand(0,
                        exportLib(),
                        getBinPath(ideviceid + " -l")
                );
                runCommand(command);
                break;
            }
            case R.id.btnIdeviceinfo:{
                Command command = new XCommand(0,
                        exportLib(),
                        getBinPath(ideviceinfo)
                );
                runCommand(command);
                break;
            }
        }

    }

    class XCommand extends Command{
        public XCommand(int id, String... command) {
            super(id, command);
        }

        @Override
        public void commandOutput(int i, String s) {
            LogUtils.d("commandOutput");
            sendText("commandOutput");
            sendText(s);
        }

        @Override
        public void commandTerminated(int i, String s) {
            LogUtils.d("commandTerminated");
            sendText("commandTerminated");
            sendText(s);
        }

        @Override
        public void commandCompleted(int i, int i2) {
            LogUtils.d("commandCompleted");
            sendText("commandCompleted");
        }

        @Override
        public void output(int id, String line){
            LogUtils.d("output");
            sendText(line);
        }
    }

    private void sendText(String value){
        Bundle bundle = new Bundle();
        bundle.putString("msg", value + "\n");
        Message msg = new Message();
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void installBin(){
        RootTools.installBinary(this, R.raw.usbmuxdd, usbmuxd);
        RootTools.installBinary(this, R.raw.busybox_armv7, busybox);
        RootTools.installBinary(this, R.raw.ideviceid, ideviceid);
        RootTools.installBinary(this, R.raw.ideviceinfo, ideviceinfo);
    }

    private String exportLib(){
        String result = "export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + appPath + "/lib";
        LogUtils.d(result);
        return result;
    }

    private String getBinPath(String bin){
        String path = appPath + "/files/" + bin;
        LogUtils.d(path);
        return path;
    }

    private void runCommand(Command command){
        try {
            RootTools.getShell(true).add(command);
        } catch (TimeoutException e) {
            LogUtils.e("TimeoutException", e);
        } catch (IOException e) {
            LogUtils.e("IOException", e);
        } catch (RootDeniedException e) {
            LogUtils.e("RootDeniedException", e);;
        }
    }
}
