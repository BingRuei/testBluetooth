package com.app.ray.testbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static android.provider.ContactsContract.Intents.Insert.NAME;

public class MainActivity extends AppCompatActivity {

    //该UUID表示串口服务
    //请参考文章<a href="http://wiley.iteye.com/blog/1179417">http://wiley.iteye.com/blog/1179417</a>
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private Button btnSearch, btnDis, btnExit;
    private ToggleButton tbtnSwitch;
    private ListView lvBTDevices;
    private ArrayAdapter<String> adtDevices;
    private List<String> lstDevices = new ArrayList<String>();
    private BluetoothAdapter btAdapt;
    public static BluetoothSocket btSocket;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Button 设置
        btnSearch = (Button) this.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new ClickEvent());
        btnExit = (Button) this.findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new ClickEvent());
        btnDis = (Button) this.findViewById(R.id.btn_dis);
        btnDis.setOnClickListener(new ClickEvent());

        // ToogleButton设置
        tbtnSwitch = (ToggleButton) this.findViewById(R.id.tbtn_switch);
        tbtnSwitch.setOnClickListener(new ClickEvent());

        // ListView及其数据源 适配器
        lvBTDevices = (ListView) this.findViewById(R.id.lv_devices);
        adtDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lstDevices);
        lvBTDevices.setAdapter(adtDevices);
        lvBTDevices.setOnItemClickListener(new ItemClickEvent());

        btAdapt = BluetoothAdapter.getDefaultAdapter();// 初始化本机蓝牙功能

        // ========================================================
        // modified by wiley
        /*
         * if (btAdapt.getState() == BluetoothAdapter.STATE_OFF)// 读取蓝牙状态并显示
         * tbtnSwitch.setChecked(false); else if (btAdapt.getState() ==
         * BluetoothAdapter.STATE_ON) tbtnSwitch.setChecked(true);
         */
        if (btAdapt.isEnabled()) {
            tbtnSwitch.setChecked(false);
        } else {
            tbtnSwitch.setChecked(true);
        }
        // ============================================================
        // 注册Receiver来获取蓝牙设备相关的结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(searchDevices, intent);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private BroadcastReceiver searchDevices = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            Log.i("BlueToothTestActivity", "onReceive");
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();

            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                Log.e(keyName, String.valueOf(b.get(keyName)));
            }
            BluetoothDevice device = null;
            // 搜索设备时，取得设备的MAC地址
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Needing the permission "ACCESS_FINE_LOCATION"
                Log.i("BlueToothTestActivity", "ACTION_FOUND");
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.i("BlueToothTestActivity", "BOND_NONE");
                    String str = "未配对|" + device.getName() + "|" + device.getAddress();
                    if (lstDevices.indexOf(str) == -1)// 防止重复添加
                        lstDevices.add(str); // 获取设备名称和mac地址
                    adtDevices.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("BlueToothTestActivity", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("BlueToothTestActivity", "完成配对");
                        connect(device);//连接设备
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d("BlueToothTestActivity", "取消配对");
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(searchDevices);
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder().setName("Main Page")
                // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    class ItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (btAdapt.isDiscovering()) btAdapt.cancelDiscovery();
            String str = lstDevices.get(arg2);
            String[] values = str.split("\\|");
            String address = values[2];
            Log.e("address", values[2]);
            BluetoothDevice btDev = btAdapt.getRemoteDevice(address);
            try {
                Boolean returnValue = false;
                if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
                    //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    Log.d("BlueToothTestActivity", "开始配对");
                    returnValue = (Boolean) createBondMethod.invoke(btDev);

                } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
                    connect(btDev);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void connect(BluetoothDevice btDev) {
        UUID uuid = UUID.fromString(SPP_UUID);
        try {
            btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
            Log.d("BlueToothTestActivity", "开始连接...");
            btSocket.connect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_search:
                    // 搜索蓝牙设备，在BroadcastReceiver显示结果
                    if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {
                        /// / 如果蓝牙还没开启
                        Toast.makeText(MainActivity.this, "请先打开蓝牙", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (btAdapt.isDiscovering())
                        btAdapt.cancelDiscovery();
                    lstDevices.clear();
                    Object[] lstDevice = btAdapt.getBondedDevices().toArray();
                    for (int i = 0; i < lstDevice.length; i++) {
                        BluetoothDevice device = (BluetoothDevice) lstDevice[i];
                        String str = "已配对|" + device.getName() + "|" + device.getAddress();
                        lstDevices.add(str); // 获取设备名称和mac地址
                        adtDevices.notifyDataSetChanged();
                    }
                    setTitle("本机蓝牙地址：" + btAdapt.getAddress());
                    btAdapt.startDiscovery();
                    break;
                case R.id.tbtn_switch:
                    // 本机蓝牙启动/关闭
                    if (tbtnSwitch.isChecked() == false)
                        btAdapt.enable();
                    else if (tbtnSwitch.isChecked() == true)
                        btAdapt.disable();
                    break;
                case R.id.btn_dis:
                    // 本机可以被搜索
                    Intent discoverableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(
                            BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                    break;
                case R.id.btn_exit:
                    try {
                        if (btSocket != null)
                            btSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    MainActivity.this.finish();
                    break;
            }
        }

    }

    public class AcceptThread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = btAdapt.listenUsingRfcommWithServiceRecord(NAME, UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
//                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    public class ConnectThread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            btAdapt.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
//            manageMyConnectedSocket(mmSocket);
        }

        //    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
//        if (mmSocket.isConnected()) {
//            MyBluetoothService mConnectedThread = new MyBluetoothService();
//            mConnectedThread.start();
//        }
//    }
//
        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final String MESSAGE_READ = null;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Handler mHandler = new Handler();

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Integer.parseInt(MESSAGE_READ), bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main Activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main Activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}