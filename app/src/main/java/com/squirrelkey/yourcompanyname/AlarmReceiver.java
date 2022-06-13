package com.squirrelkey.yourcompanyname;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;


public class AlarmReceiver extends BroadcastReceiver {

    Context context;
    Intent intent;
    private UUID mDeviceUUID;
    private BluetoothDevice mDevice;
    private static final String TAG = "BlueTest5-Controlling";
    private BluetoothSocket mBTSocket;
    String macaddress;
    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;
    int loop = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        macaddress = intent.getStringExtra("Address");
        mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        Log.d("TESTING UUID", "UUID: " + mDeviceUUID);
        Log.d("TESTING MAC", "MACADD"+macaddress);
        ConnectBT connectBT = new ConnectBT();
        connectBT.execute();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macaddress);
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(context, "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();
                if(loop<5) {
                    loop++;
                    Log.d(TAG, "onPostExecute: RETRYING>>>>>");
                    new ConnectBT().execute();
                }

            } else {
                msg("Connected to device");
                if(intent.getBooleanExtra("open", false)) {
                    sendtext("0");
                    Toast.makeText(context, "Curtains Opening", Toast.LENGTH_LONG).show();
                }
                else {
                    sendtext("3");
                    Toast.makeText(context, "Curtains Closing", Toast.LENGTH_LONG).show();
                }
                CountDownTimer c = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        try {
                            mBTSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mIsBluetoothConnected = true;
                    }
                };
                c.start();

            }
        }

    }


    void sendtext(String text){
        try {
            mBTSocket.getOutputStream().write(text.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void msg(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }








}
