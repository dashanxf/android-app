package com.example.dashanxf.test_0;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Binder;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dashanxf on 2016/8/21.
 */
public class BluetoothService extends Service implements Serializable{

    protected  static final String TAG = "LifeixDesign-A2DP";

    private static AudioManager mAudioManager;
    private static BluetoothAdapter mBtAdapter;
    private static BluetoothA2dp mA2dpService;

    private int ring_state = 0;

    private String mBLEDeviceAddress;
    private String mDeviceAddress;

    private BeaconManager beaconManager;

    private static BluetoothLeService mBluetoothLeService;

    private static boolean mConnected = false;
    private boolean service_connected = false;
    private static BluetoothGattCharacteristic characteristicTX;
    private static BluetoothGattCharacteristic characteristicRX;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private static HashMap<String, String> attributes = new HashMap();

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String HM_10_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
    static {
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "HM 10 Serial");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HM_RX_TX,"RX/TX data");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private void writeObject(java.io.ObjectOutputStream stream){
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mBLEDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public boolean connect(final String Device_address,final String BLE_Device_address) {
        mBLEDeviceAddress = BLE_Device_address;
        mDeviceAddress = Device_address;
        return true;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
        }
    };

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, this.lookup(uuid, unknownServiceString));

            // If the service exists for HM 10 Serial, say so.
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }
        String a = Integer.toString(ring_state);
        final byte[] tx = a.getBytes();
        Log.d("bluetooth","trying to make change");
        Log.d("writing tx value","write value is" + tx.toString());
        if(mConnected) {
            characteristicTX.setValue(tx);
            Log.d("bluetooth","making change");
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
        }

    }

    public boolean initialize(){
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mAudioManager == null || mBtAdapter == null) return false;
        mBtAdapter.getProfileProxy(this, mA2dpListener, BluetoothProfile.A2DP);
        return true;
    }

    /** a2dp service listener, invoked when connected with a2dp device
     *
     */
    private ServiceListener mA2dpListener = new ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile a2dp) {
            Log.d(TAG, "a2dp service connected. profile = " + profile);
            if (profile == BluetoothProfile.A2DP){
                mA2dpService = (BluetoothA2dp) a2dp;
                try {
                    Method connect = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
                    connect.invoke(mA2dpService,findBondedDeviceByName(mBtAdapter,mDeviceAddress));
                } catch (NoSuchMethodException ex) {
                    Log.e("TAG:Connect", "Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
                } catch (InvocationTargetException ex) {
                    Log.e(TAG, "Unable to invoke connect(BluetoothDevice) method on proxy. " + ex.toString());
                } catch (IllegalAccessException ex) {
                    Log.e(TAG, "Illegal Access! " + ex.toString());
                }
                if (mAudioManager.isBluetoothA2dpOn()){
                } else {
                    Log.d(TAG, "bluetooth a2dp is not on while service connected");
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        mBtAdapter.closeProfileProxy(BluetoothProfile.A2DP,mA2dpService);
        if(service_connected) {
            unregisterReceiver(mGattUpdateReceiver);
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    public void playMusic(){
        Log.d("playMusic","playMusic invoked");
        PackageManager manager = this.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage("com.miui.player");
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(i);
    }

    private static BluetoothDevice findBondedDeviceByName (BluetoothAdapter adapter, String address) {
        if (adapter.getBondedDevices() != null) {
            for (BluetoothDevice device : adapter.getBondedDevices()) {
                Log.v(TAG, String.format("Device with address %s.", device.getAddress()));
                if (address.matches(device.getAddress())) {
                    Log.v(TAG, String.format("Found device with name %s and address %s.", device.getName(), device.getAddress()));
                    return device;
                }
            }
        }
        Log.w(TAG, String.format("Unable to find device with address %s.", address));
        return null;
    }

    protected void connectBLE(int i){
        Log.d("BLEconnector","connecting to ble device");
        if(BluetoothLeService.available) {
            BluetoothLeService.available = false;
            if (service_connected) {
                unbindService(mServiceConnection);
                unregisterReceiver(mGattUpdateReceiver);
                service_connected = false;
            }
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            service_connected = true;
            ring_state = i;
        }
    }
}
