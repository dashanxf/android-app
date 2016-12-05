package com.example.dashanxf.test_0;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;


/**
 * Created by dashanxf on 2016/9/9.
 */
public class ObjectFragmentActivity extends FragmentActivity implements BeaconConsumer {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_BLE_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_BLE_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";

    CollectionPagerAdapter mCollectionPagerAdapter;

    private static final String TAG = "ObjectFragmentActivity";

    private String mDeviceAddress;
    private String mBLEDeviceName;
    private String mBLEDeviceAddress;

    AudioManager mAudioManager;
    BluetoothAdapter mBtAdapter;
    BluetoothA2dp mA2dpService;
    ViewPager mViewPager;

    private BluetoothService mBluetoothService;

    private BeaconManager beaconManager;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mBLEDeviceName = intent.getStringExtra(EXTRAS_BLE_DEVICE_NAME);
        mBLEDeviceAddress = intent.getStringExtra(EXTRAS_BLE_DEVICE_ADDRESS);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.bind(this);

        Intent BthServiceIntent = new Intent(this, BluetoothService.class);
        bindService(BthServiceIntent, mBthServiceConnection, BIND_AUTO_CREATE);
    }

    public void logToDisplay(final String line, final double dist) {
        runOnUiThread(new Runnable() {
            public void run() {
                ObjectFragment.editText0.setText(line);
                ObjectFragment.editText1.setText(String.format("%.2f",dist));
                ObjectFragment.editText2.setText("meters");
            }
        });
    }

    private final ServiceConnection mBthServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            if(mBluetoothService == null)
                Log.d("Service Connection","mBthService in service connection is null");
            else
                Log.d("Service Connection","mBthService in service connection is not null");
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothService.connect(mDeviceAddress,mBLEDeviceAddress);
            mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
            mCollectionPagerAdapter.setBluetoothService(mBluetoothService);
            mViewPager.setAdapter(mCollectionPagerAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothService = null;
        }
    };

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon firstBeacon = beacons.iterator().next();
                    double td = firstBeacon.getDistance();
                    if(td<0){
                        logToDisplay("Beacon is not working properly!",0);
                    }
                    else if(td>=0 && td<4){
                        logToDisplay("Beacon is very close. \nDistance is: ", td*0.75);
                    }
                    else if(td>=4 && td<8){
                        logToDisplay("Beacon is close. \nDistance is: ", (td-4)*0.5+3);
                    }
                    else if(td>=8 && td<15){
                        logToDisplay("Beacon is far from here. \nDistance is: ", (td-8)*0.25+5);
                    }
                    else if(td>=15){
                        logToDisplay("You might have lost the beacon.",20);
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, Identifier.parse("409"), Identifier.parse("409")));
        } catch (RemoteException e) {   }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        beaconManager.unbind(this);
        unbindService(mBthServiceConnection);
    }

}
