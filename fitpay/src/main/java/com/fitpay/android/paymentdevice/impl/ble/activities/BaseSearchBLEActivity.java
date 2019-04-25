package com.fitpay.android.paymentdevice.impl.ble.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.fitpay.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to select available BLE device
 */

//TODO should this be removed?   see if used in sample client
public abstract class BaseSearchBLEActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanCallback mScanCallback;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private Handler mHandler;

    protected List<BluetoothDevice> mDevicesList;

    private boolean scanning = false;

    public abstract void initViews();

    public abstract void onNewDevice(BluetoothDevice device);

    public abstract void onSearchBegin();

    public abstract void onSearchEnd();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mDevicesList = new ArrayList<>();

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.fp_error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
    }


    @Override
    public void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device. If Bluetooth is not
        // currently enabled,
        // fire an intent to display a dialog asking the user to grant
        // permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        scanLeDevice(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        scanLeDevice(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanForDevices(final boolean enable) {
        if (enable) {

            if (mLEScanner == null) {

                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

                filters = new ArrayList<>();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();

                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        addDevice(result.getDevice());
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                    }
                };
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(false);
                }
            }, SCAN_PERIOD);

            mLEScanner.startScan(filters, settings, mScanCallback);

        } else if (mLEScanner != null) {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    private void scanForDevicesOld(final boolean enable) {
        if (enable) {

            if (mLeScanCallback == null) {
                mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                        addDevice(device);
                    }
                };
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(false);
                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(null, mLeScanCallback);

        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    protected void scanLeDevice(final boolean enable) {

        if (enable) {
            if (!scanning) {
                scanning = true;
                onSearchBegin();
            }
        } else {
            if (scanning) {
                scanning = false;
                onSearchEnd();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanForDevices(enable);
        } else {
            scanForDevicesOld(enable);
        }
    }

    private void addDevice(final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mDevicesList.contains(device)) {
                    mDevicesList.add(device);

                    onNewDevice(device);
                }
            }
        });
    }
}
