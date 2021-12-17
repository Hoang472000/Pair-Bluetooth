package com.android.pairbluetooth;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH = 1;
    private static final int REQUEST_COARSE_LOCATION = 1;
    private ListView listView;
    private Button button,scan;
    private ProgressBar progressBar;
    private ArrayList<DeviceItem> deviceItem;
    BluetoothHeadset bluetoothHeadset;
    BluetoothAdapter BTAdapter;
    private ArrayAdapter aAdapter;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private BluetoothService mBluetoothService;
    private String mAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        initBluetooth();

        deviceItem = new ArrayList<DeviceItem>();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BTAdapter==null){
                    Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
                }
                else{
                    Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
                    ArrayList list = new ArrayList();
                    if(pairedDevices.size()>0){
                        for(BluetoothDevice device: pairedDevices){
                            String devicename = device.getName();
                            String macAddress = device.getAddress();
                            list.add("Name: "+devicename+" Address: "+macAddress);
                            mAddress=macAddress;
                        }
                        listView = (ListView) findViewById(R.id.list_item);
                        aAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, list);
                        listView.setAdapter(aAdapter);
                    }
                    //list name device
                    String[] device = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        device[i] = String.valueOf(list.get(i));
                    }

                    new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Thiết bị khả dụng")
                    .setItems(device, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String namedevice = device[which];
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),"You select: " + namedevice,
                                    Toast.LENGTH_SHORT).show();
                            button.setText(device[which]);
                            sendMessage("4");
                        }
                    }).show();
                }
            }
        });

        /*Set<BluetoothDevice> pairDevice = BTAdapter.getBondedDevices();
        if(pairDevice.size()>0){
            for (BluetoothDevice device : pairDevice) {
                Log.d("HoangCV", "onCreate: "+device.getName());
                DeviceItem deviceItem1 = new DeviceItem(device.getName(), device.getAddress());
                button.setText(device.getName());
                deviceItem.add(deviceItem1);
            }
        }*/
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
                Log.d("HoangCV", "onClick:scan ");
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
        });

    }
    private void doDiscovery() {
        Log.d("HoangCV", "doDiscovery()");

        // Indicate scanning in the title
        setSupportProgressBarIndeterminateVisibility(true);
        setTitle("Scanning");

        // Turn on sub-title for new devices
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        Log.d("HoangCV", "doDiscovery: "+BTAdapter.isDiscovering());
        if (BTAdapter.isDiscovering()) {
            BTAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        BTAdapter.startDiscovery();
        progressBar.setVisibility(View.VISIBLE);
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("HoangCV", "onReceive:action "+action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Toast.makeText(context, deviceName+ " : "+deviceHardwareAddress, Toast.LENGTH_SHORT).show();
                Log.d("HoangCV", "onReceive: "+deviceName);
                progressBar.setVisibility(View.GONE);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                Log.d("HoangCV", "onReceive: finished");
            }
        }
    };

    protected void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //proceedDiscovery(); // --->
                } else {
                    //TODO re-request
                }
                break;
            }
        }
    }
        @Override
        protected void onDestroy () {
            super.onDestroy();
            unregisterReceiver(receiver);
        }

        public void initBluetooth () {
            // Phone does not support Bluetooth so let the user know and exit.
            if (BTAdapter == null) {
                new AlertDialog.Builder(this)
                        .setTitle("Not compatible")
                        .setMessage("Your phone does not support Bluetooth")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            if (!BTAdapter.isEnabled()) {
                Log.d("HoangCV", "initBluetooth: ");
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 1);
            }
        }

        private void init () {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
            listView = findViewById(R.id.list_item);
            button = findViewById(R.id.button);
            scan = findViewById(R.id.scan);
            checkLocationPermission();
        }
        private void showDialogDevice () {

        }
        @Override
        public void onStart () {
            super.onStart();
            if (!BTAdapter.isEnabled()) {
                Log.d("HoangCV", "onStart: 1");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                if (mBluetoothService == null) {
                    Log.d("HoangCV", "onStart: ");
                    mBluetoothService = new BluetoothService(this, mHandler);
                }
            }
        }

        @Override
        public synchronized void onResume () {
            super.onResume();
            if (mBluetoothService != null) {
                if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                    Log.d("HoangCV", "onResume: ");
                    mBluetoothService.start();
                }
            }
        }
        public void connect (View v){
            Intent enableBT = new Intent(BluetoothAdapter.EXTRA_CONNECTION_STATE);
            startActivityForResult(enableBT, REQUEST_CONNECT_DEVICE);
        }

        public void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            Log.d("HoangCV", "onActivityResult: 1" + requestCode);
            switch (requestCode) {
                case REQUEST_CONNECT_DEVICE:
                    // When DeviceListActivity returns with a device to connect
                    if (resultCode == Activity.RESULT_OK) {
                        // Get the device MAC address
                        //String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        // Get the BLuetoothDevice object
                        BluetoothDevice device = BTAdapter.getRemoteDevice(mAddress);
                        Log.d("HoangCV", "onActivityResult: " + device);

                        // Attempt to connect to the device
                        mBluetoothService.connect(device);
                    }
                    break;
                case REQUEST_ENABLE_BT:
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a chat session
                        sendMessage("4");
                    } else {
                        // User did not enable Bluetooth or an error occured
                        Toast.makeText(this, "Không thể kết nối với thiết bị", Toast.LENGTH_SHORT).show();
                        finish();
                    }
            }
        }
        private void sendMessage (String message){

            // Check that we're actually connected before trying anything
            Log.d("HoangCV", "sendMessage:1 " + mBluetoothService.getState());
            if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mBluetoothService.write(send);
                // Reset out string buffer to zero and clear the edit text field
            /*mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);*/
            }
        }

        // The Handler that gets information back from the BluetoothChatService
        private final Handler mHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        //mAdapter.notifyDataSetChanged();
                        //messageList.add(new androidRecyclerView.Message(counter++, writeMessage, "Me"));
                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        // mAdapter.notifyDataSetChanged();
                        // messageList.add(new androidRecyclerView.Message(counter++, readMessage, mConnectedDeviceName));
                        break;
                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        // mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                        //  Toast.makeText(getApplicationContext(), "Connected to "
                        //          + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_TOAST:
                        Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

    }