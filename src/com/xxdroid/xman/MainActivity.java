package com.xxdroid.xman;

import android.app.Activity;
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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity {

    private String usbmuxd = "usbmuxdd";
    private String ideviceid = "ideviceid";
    private String ideviceinfo = "ideviceinfo";

    private TextView textView;
    private Handler handler;

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
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnInstall:{
                LogUtils.d("binary install");
                RootTools.installBinary(this, R.raw.usbmuxdd, usbmuxd);
                RootTools.installBinary(this, R.raw.ideviceid, ideviceid);
                RootTools.installBinary(this, R.raw.ideviceinfo, ideviceinfo);
                break;
            }
            case R.id.btnRun:{
                if(RootTools.hasBinary(this, usbmuxd)){
                    LogUtils.d("binary found");
//                    RootTools.runBinary(this, usbmuxd, "");

                    Command command = new Command(0,
                            "export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/data/data/com.xxdroid.xman/lib",
                            "/data/data/com.xxdroid.xman/files/usbmuxdd",
                            "/data/data/com.xxdroid.xman/files/ideviceid -l",
                            "/data/data/com.xxdroid.xman/files/ideviceinfo",
                            "ps | grep usb"){
                        @Override
                        public void commandOutput(int i, String s) {
                            LogUtils.d("commandOutput");
                            LogUtils.d(s);
                        }

                        @Override
                        public void commandTerminated(int i, String s) {
                            LogUtils.d("commandTerminated");
                            LogUtils.d(s);
                        }

                        @Override
                        public void commandCompleted(int i, int i2) {
                            LogUtils.d("commandCompleted");

                            Bundle bundle = new Bundle();
                            bundle.putString("msg", "commandCompleted");
                            Message msg = new Message();
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }

                        @Override
                        public void output(int id, String line){
                            LogUtils.d("output");

                            Bundle bundle = new Bundle();
                            bundle.putString("msg", line);
                            Message msg = new Message();
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    };

                    try {
                        RootTools.getShell(true).add(command);
                    } catch (TimeoutException e) {
                        LogUtils.e("TimeoutException", e);
                    } catch (IOException e) {
                        LogUtils.e("IOException", e);
                    } catch (RootDeniedException e) {
                        LogUtils.e("RootDeniedException", e);;
                    }

                }else{
                    LogUtils.d("usbmuxd no found");
                }
                break;
            }
            case R.id.btnGetinfo:{
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
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    LogUtils.e("InterruptedException", e);
                }
                manager.stopDetection();
                break;
            }
        }



    }
}
