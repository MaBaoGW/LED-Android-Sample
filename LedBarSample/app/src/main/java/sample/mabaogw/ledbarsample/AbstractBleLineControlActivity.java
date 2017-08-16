/*
 * Copyright (C) 2017 MaBaoGW
 *
 * Source Link: https://github.com/MaBaoGW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.mabaogw.ledbarsample;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sample.mabaogw.ledbarsample.ble.BluetoothLeService;
import sample.mabaogw.ledbarsample.ble.SampleGattAttributes;

public abstract class AbstractBleLineControlActivity extends AppCompatActivity {
    private final static String TAG = AbstractBleLineControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static int BLE_MSG_SEND_INTERVAL = 40;
    public static int BLE_MSG_BUFFER_LEN = 10;

    protected String currDeviceName, currDeviceAddress;

    protected Activity activity;

    protected BluetoothLeService mBluetoothLeService;
    protected BluetoothGattCharacteristic characteristic;
    protected boolean mConnected = false, characteristicReady = false;
    protected ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    protected final String LIST_NAME = "NAME";
    protected final String LIST_UUID = "UUID";

    protected StringBuilder msgBuffer;

    protected byte[] startmsgBuffer;
    protected byte[] endmsgBuffer;
    protected byte[] datamsgBuffer;

    // Code to manage Service lifecycle.
    protected final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(currDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    protected final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAG, "ACTION_GATT_CONNECTED");
                mConnected = true;
                updateConnectionState(R.string.connected,mConnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "ACTION_GATT_DISCONNECTED");
                mConnected = false;
                updateConnectionState(R.string.disconnected,mConnected);
                if(mBluetoothLeService!=null)
                    mBluetoothLeService.connect(currDeviceAddress);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                Log.d(TAG, "uuid = " + uuid);
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                if (uuid.equals(SampleGattAttributes.LED_CONFIG)) {
                    characteristic = gattCharacteristic;
                    Log.d(TAG, "LED_CONFIG done~");
                    updateReadyState(R.string.ready);
                }
            }

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    protected static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        final Intent intent = getIntent();
        currDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        currDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(currDeviceAddress);
            Log.d(TAG, "result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    protected void updateConnectionState(final int resourceId, final boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    protected void updateReadyState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                characteristicReady = true;
            }
        });
    }

    public void wait_ble(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            // ignore
        }
    }

    protected void sendMessage(byte[] msgBytes) {
        mBluetoothLeService.writeCharacteristic(characteristic,msgBytes);
    }

    public class Upload1AsyncTask1 extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... args) {
            if (characteristicReady) {

                startmsgBuffer = new byte[] { (byte)0xb1, (byte)(args[0] & 0xFF), (byte)(args[1] & 0xFF), (byte)(args[2] & 0xFF), (byte)(args[3] & 0xFF), (byte)(args[4] & 0xFF), (byte)(args[5] & 0xFF), (byte)(args[6] & 0xFF)};
                //led_length1, led_length2, led_length3, led_length4, 特效
                sendMessage(startmsgBuffer);
                wait_ble(BLE_MSG_SEND_INTERVAL);

                byte[] buffer = new byte[BLE_MSG_BUFFER_LEN];

                Log.v(TAG, "effect = " + args[4] + " byte array length = " + hexStringToByteArray(msgBuffer.toString()).length + " " + msgBuffer.toString());

                int count = 0;

                for (int offset = 0; offset < hexStringToByteArray(msgBuffer.toString()).length; offset += BLE_MSG_BUFFER_LEN) {
                    System.arraycopy(hexStringToByteArray(msgBuffer.toString()), offset, buffer, 0, Math.min( BLE_MSG_BUFFER_LEN, hexStringToByteArray(msgBuffer.toString()).length - offset));

                    datamsgBuffer = new byte[] { (byte)0xc1, (byte)Integer.parseInt(String.valueOf(count))};

                    mBluetoothLeService.writeCharacteristic(characteristic,concatenateByteArrays(datamsgBuffer,buffer));
                    wait_ble(BLE_MSG_SEND_INTERVAL);
                    count++;
                }
                wait_ble(BLE_MSG_SEND_INTERVAL);

                endmsgBuffer = new byte[] { (byte)0xd1, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

                sendMessage(endmsgBuffer);
                wait_ble(BLE_MSG_SEND_INTERVAL);

            }
            return null;
        }
    }

    public class UploadAsyncTask2 extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... args) {
            if (characteristicReady) {

                startmsgBuffer = new byte[] { (byte)(args[0] & 0xFF), (byte)(args[1] & 0xFF), (byte)(args[2] & 0xFF), (byte)(args[3] & 0xFF), (byte)(args[4] & 0xFF), (byte)(args[5] & 0xFF)};
                sendMessage(startmsgBuffer);
                wait_ble(BLE_MSG_SEND_INTERVAL);
            }
            return null;
        }
    }

    public class Save2LEDAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {
            if (characteristicReady) {

                startmsgBuffer = new byte[] { (byte)0xe1, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
                sendMessage(startmsgBuffer);
                wait_ble(BLE_MSG_SEND_INTERVAL);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(AbstractBleLineControlActivity.this, getString(R.string.ledsavedone), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}