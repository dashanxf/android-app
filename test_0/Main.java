package com.example.dashanxf.test_0;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class Main extends Activity {

    Button btnScan;
    Button btnStart;

    boolean receiver_registered = false;

    private BluetoothAdapter mBthAdapter;
    private BluetoothLeScanner mBthLeScanner;
    private String device_name;
    private String device_addr;
    private String ble_device_name;
    private String ble_device_addr;
    private boolean audio_device_bond = false;
    private boolean ble_device_bond = false;

    private Handler mHandler;
    private boolean mScanning;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 15000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnScan = (Button)findViewById(R.id.button);
        btnStart = (Button)findViewById(R.id.start_button);
        mHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBthAdapter = bluetoothManager.getAdapter();
        mBthLeScanner = mBthAdapter.getBluetoothLeScanner();


        /*if does not have BLE*/
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        /*if no bluetooth device*/
        if (mBthAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth Device not available",Toast.LENGTH_LONG).show();
            finish();
        }

        /*if bluetooth not enabled*/
        else if(!mBthAdapter.isEnabled())
        {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (audio_device_bond && ble_device_bond){
                    audio_start();
                }
                else{
                    log_ToDisplay("Bluetooth devices are not ready yet. Please wait...",R.id.warning);
                }
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                boolean device_connected=false;
                log_ToDisplay("Searching for Bluetooth Audio Device...",R.id.monitoringText);
                Set<BluetoothDevice> pairedDevices = mBthAdapter.getBondedDevices();
                for(BluetoothDevice device : pairedDevices){
                    if(device.getName() != null)
                        if(device.getName().equals("MPOW")){
                            device_connected = true;
                            device_name = device.getName();
                            device_addr = device.getAddress();
                            log_ToDisplay("Bluetooth Audio Device found",R.id.monitoringText);
                        }
                }
                if(device_connected){
                    audio_device_bond=true;
                    if(ble_device_bond){
                        log_ToDisplay("Bluetooth devices are ready to use!",R.id.warning);
                    }
                    else{
                        scanLeDevice(true);
                    }
                }else{
                    mBthAdapter.cancelDiscovery();
                    registerReceiver(bReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
                    registerReceiver(bReceiver,new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
                    receiver_registered = true;
                    mBthAdapter.startDiscovery();
                    scanLeDevice(true);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBthAdapter.cancelDiscovery();
        if(receiver_registered) unregisterReceiver(bReceiver);
        scanLeDevice(false);
    }


    private void bth_pairing(BluetoothDevice device){
        try{
            Log.d("pairDevice()","Start Pairing...");
            device.createBond();
            device_name = device.getName();
            device_addr = device.getAddress();
            Log.d("pairDevice()","Bluetooth device paired");
        }catch (Exception e){
            Log.e("pairDevice()",e.getMessage());
        }
    }

    private void audio_start(){

        final Intent intent = new Intent(this,ObjectFragmentActivity.class);
        intent.putExtra(ObjectFragmentActivity.EXTRAS_DEVICE_NAME, device_name);
        intent.putExtra(ObjectFragmentActivity.EXTRAS_DEVICE_ADDRESS, device_addr);
        intent.putExtra(ObjectFragmentActivity.EXTRAS_BLE_DEVICE_NAME,ble_device_name);
        intent.putExtra(ObjectFragmentActivity.EXTRAS_BLE_DEVICE_ADDRESS,ble_device_addr);

        log_ToDisplay("",R.id.monitoringText);
        log_ToDisplay("",R.id.bleStatus);
        log_ToDisplay("",R.id.warning);

        ble_device_bond = false;

        if (mBthAdapter.isDiscovering()) mBthAdapter.cancelDiscovery();

        if (mScanning){
            scanLeDevice(false);
            mScanning = false;
        }
        startActivity(intent);
        finish();
    }

    private android.bluetooth.le.ScanCallback mLeScanCallback =
            new android.bluetooth.le.ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType,result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BluetoothDevice device;
                            device = result.getDevice();
                            if (device.getName() != null && device.getName().equals("DASHANXF")) {
                                ble_device_name = device.getName();
                                ble_device_addr = device.getAddress();
                                ble_device_bond = true;
                                log_ToDisplay("Tracker found", R.id.bleStatus);
                                if (audio_device_bond)
                                    log_ToDisplay("Bluetooth devices are ready to use!", R.id.warning);
                            }
                        }
                    });
                }
            };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBthLeScanner.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            log_ToDisplay("Searching for Tracker...",R.id.bleStatus);
            mBthLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBthLeScanner.stopScan(mLeScanCallback);
        }
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    if (device.getName().equals("MPOW")) {
                        log_ToDisplay("Bluetooth Audio Device found",R.id.monitoringText);
                        bth_pairing(device);
                    }
                }
            }
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()){
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("Bluetooth binding","pairing");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("Bluetooth binding","paired");
                        audio_device_bond = true;
                        if(ble_device_bond) log_ToDisplay("Bluetooth devices are ready to use!",R.id.warning);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void log_ToDisplay(final String line,final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView text = (TextView)Main.this
                        .findViewById(i);
                text.setText(line+"\n");
            }
        });
    }
}