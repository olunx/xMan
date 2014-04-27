package com.xxdroid.xman.helper;

import android.content.Context;
import android.util.Log;
import com.dd.plist.NSDictionary;
import com.lidroid.xutils.util.LogUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.xxdroid.idevice.*;
import com.xxdroid.xman.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.Exception;
import java.util.concurrent.TimeoutException;

/**
 * Created by lunzii on 2014/4/26.
 */
public class IDeviceHelper {

    public String busybox = "busybox";
    public String fusermount = "fusermount";
    public String idevicecrashreport = "idevicecrashreport";
    public String idevicedate = "idevicedate";
    public String idevicediagnostics = "idevicediagnostics";
    public String ideviceid = "ideviceid";
    public String ideviceinfo = "ideviceinfo";
    public String ideviceinstaller = "ideviceinstaller";
    public String idevicename = "idevicename";
    public String idevicepair = "idevicepair";
    public String idevicescreenshot = "idevicescreenshot";
    public String idevicesyslog = "idevicesyslog";
    public String ifuse = "ifuse";
    public String usbmuxdd = "usbmuxdd";;

    private static IDeviceHelper helper = null;

    public static IDeviceHelper getInstance(){
        if(helper == null){
            helper = new IDeviceHelper();
        }
        return helper;
    }

    public void destory(){
        helper = null;
    }

    private void installBinary(Context context){
        RootTools.installBinary(context, R.raw.fusermount, fusermount);
        RootTools.installBinary(context, R.raw.idevicecrashreport, idevicecrashreport);
        RootTools.installBinary(context, R.raw.idevicedate, idevicedate);
        RootTools.installBinary(context, R.raw.idevicediagnostics, idevicediagnostics);
        RootTools.installBinary(context, R.raw.ideviceid, ideviceid);
        RootTools.installBinary(context, R.raw.ideviceinfo, ideviceinfo);
        RootTools.installBinary(context, R.raw.ideviceinstaller, ideviceinstaller);
        RootTools.installBinary(context, R.raw.idevicename, idevicename);
        RootTools.installBinary(context, R.raw.idevicepair, idevicepair);
        RootTools.installBinary(context, R.raw.idevicescreenshot, idevicescreenshot);
        RootTools.installBinary(context, R.raw.idevicesyslog, idevicesyslog);
        RootTools.installBinary(context, R.raw.ifuse, ifuse);
        RootTools.installBinary(context, R.raw.usbmuxdd, usbmuxdd);
    }

    public void initUsbMuxd(final Context context){
        if(!RootTools.isProcessRunning(usbmuxdd)){
            IDeviceHelper.getInstance().installBinary(context);

            CommandCapture command = new CommandCapture(0,
                    exportLib(context),
                    getBinPath(context, usbmuxdd + " -v")){
                @Override
                protected void output(int id, String line) {
                    super.output(id, line);
                    LogUtils.e(line);
                }
            };
            runCommand(command);
        }else{
            LogUtils.e("usbmuxd is running...");
        }
    }

    public StringBuffer getDeviceId(final Context context){
        final StringBuffer sb = new StringBuffer();
        CommandCapture command = new CommandCapture(0,
                exportLib(context),
                getBinPath(context, ideviceid + " -l")){
            @Override
            protected void output(int id, String line) {
                super.output(id, line);
                sb.append(line);
                sb.append("\n");
            }
        };
        runCommand(command);
        LogUtils.e(sb.toString());
        return sb;
    }

    public StringBuffer getDeviceInfo(final Context context){
        final StringBuffer sb = new StringBuffer();
        CommandCapture command = new CommandCapture(0,
                exportLib(context),
                getBinPath(context, ideviceinfo + "")){
            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                LogUtils.e(sb.toString());
//                DeviceInfo info = new DeviceInfo(sb.toString());
//                LogUtils.e(info.toString());
//                try{
//                    byte[] xml = sb.toString().getBytes("UTF-8");
//                    NSDictionary rootDict = (NSDictionary) MyParser.parse(xml);
//                    LogUtils.e(rootDict.objectForKey("BatteryIsCharging").toString());
//                    LogUtils.e(rootDict.objectForKey("ExternalConnected").toString());
//                }catch (UnsupportedEncodingException e){
//                    LogUtils.e(Log.getStackTraceString(e));
//                }catch (Exception e){
//                    LogUtils.e(Log.getStackTraceString(e));
//                }
            }

            @Override
            protected void output(int id, String line) {
                super.output(id, line);
                sb.append(line);
                sb.append("\n");
            }
        };
        runCommand(command);
        return sb;
    }

    private void commandWait(Command cmd) {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; //7 tries, 6350 msec

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!cmd.isFinished()){
            LogUtils.e("Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

    private String exportLib(Context context){
        String result = "export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + context.getApplicationInfo().nativeLibraryDir;
        return result;
    }

    private String getBinPath(Context context, String bin){
        String result = context.getApplicationInfo().dataDir + "/files/" + bin;
        return result;
    }

    private void runCommand(Command command){
        try {
            RootTools.getShell(true).add(command);
        } catch (TimeoutException e) {
            LogUtils.e("TimeoutException");
        } catch (IOException e) {
            LogUtils.e("IOException");
        } catch (RootDeniedException e) {
            LogUtils.e("RootDeniedException");;
        }
    }
}
