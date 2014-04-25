package com.xxdroid.xman;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.lidroid.xutils.util.LogUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity {

    private UsbManager mUsbManager;
    private ListView mListView;
    private TextView mProgressBarTitle;
    private ProgressBar mProgressBar;

    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    refreshDeviceList();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    private List<UsbDevice> mEntries = new ArrayList<UsbDevice>();
    private ArrayAdapter<UsbDevice> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mListView = (ListView) findViewById(R.id.deviceList);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBarTitle = (TextView) findViewById(R.id.progressBarTitle);

        mAdapter = new ArrayAdapter<UsbDevice>(this, android.R.layout.simple_expandable_list_item_2, mEntries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TwoLineListItem row;
                if (convertView == null){
                    final LayoutInflater inflater =
                            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
                } else {
                    row = (TwoLineListItem) convertView;
                }

                final UsbDevice device = mEntries.get(position);
                final String title = String.format("Vendor 0x%s Product 0x%s",
                        HexDump.toHexString((short) device.getVendorId()),
                        HexDump.toHexString((short) device.getProductId()));
                row.getText1().setText(title);

                final String subtitle = DeviceUtils.getInstance().getDeviceDesc(device.getVendorId(), device.getProductId());
                row.getText2().setText(subtitle);

                return row;
            }

        };
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.d("Pressed item " + position);
                if (position >= mEntries.size()) {
                    LogUtils.w("Illegal position.");
                    return;
                }

                if(!RootTools.isProcessRunning("usbmuxdd")){
                    installBin();
                    XCommand usbmuxd = new XCommand(0,
                            exportLib(),
                            getBinPath("usbmuxdd -v")
                    );
                    runCommand(usbmuxd);
                }
;               XCommand ideviceId = new XCommand(0,
                    exportLib(),
                    getBinPath("ideviceid -l")
                );
                runCommand(ideviceId);
                XCommand ideviceInfo = new XCommand(0,
                        exportLib(),
                        getBinPath("ideviceinfo")
                );
                runCommand(ideviceInfo);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MESSAGE_REFRESH);
    }

    private void refreshDeviceList() {
        showProgressBar();

        new AsyncTask<Void, Void, List<UsbDevice>>() {
            @Override
            protected List<UsbDevice> doInBackground(Void... params) {
//                LogUtils.d("Refreshing device list ...");
                SystemClock.sleep(1000);
                final List<UsbDevice> result = new ArrayList<UsbDevice>();
                for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
                    result.add(device);
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<UsbDevice> result) {
                mEntries.clear();
                mEntries.addAll(result);
                mAdapter.notifyDataSetChanged();
                mProgressBarTitle.setText(
                        String.format("%s device(s) found",Integer.valueOf(mEntries.size())));
                hideProgressBar();
//                LogUtils.d("Done refreshing, " + mEntries.size() + " entries found.");
            }

        }.execute((Void) null);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarTitle.setText("刷新中...");
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }


    class XCommand extends Command{
        public XCommand(int id, String... command) {
            super(id, command);
        }

        @Override
        public void commandOutput(int i, String line) {
            LogUtils.d("commandOutput");
            LogUtils.d(line);
        }

        @Override
        public void commandTerminated(int i, String s) {
            LogUtils.d("commandTerminated");
        }

        @Override
        public void commandCompleted(int i, int i2) {
            LogUtils.d("commandCompleted");
        }

        @Override
        public void output(int id, String line){
            LogUtils.d("output");
            LogUtils.d(line);
        }
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

    private String exportLib(){
        String result = "export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + getApplicationInfo().nativeLibraryDir;
        LogUtils.d(result);
        return result;
    }

    private String getBinPath(String bin){
        String result = getApplicationInfo().dataDir + "/files/" + bin;
        LogUtils.d(result);
        return result;
    }

    private String busybox = "busybox";
    private String usbmuxd = "usbmuxdd";
    private String ideviceid = "ideviceid";
    private String ideviceinfo = "ideviceinfo";
    private void installBin(){
        RootTools.installBinary(this, R.raw.usbmuxdd, usbmuxd);
        RootTools.installBinary(this, R.raw.ideviceid, ideviceid);
        RootTools.installBinary(this, R.raw.ideviceinfo, ideviceinfo);
    }
}
