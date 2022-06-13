 package com.squirrelkey.yourcompanyname;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Controlling extends Activity {
    private static final String TAG = "BlueTest5-Controlling";
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    boolean authentication = true;
    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;

    String rstring="";
    String DeviceName;



    private BluetoothDevice mDevice;
    String macaddress;

    boolean deviceokay = false, passwordtrue = false;


    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlling);
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        macaddress = b.getString(DeviceList.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        Log.d(TAG, "UUID: " + mDeviceUUID);
        Log.d(TAG, "Ready");
    }

    void showdialog(boolean password){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(password) {
            builder.setTitle("Enter Password");
            builder.setMessage("Usually printed on back of the device");
        }
        else{
            builder.setTitle("Enter a Name");
            builder.setMessage("The Shorter The Sweeter");
        }
        final EditText input = new EditText(this);
        if(password)
            input.setHint("Password Maybe Case Sensitive!!!");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!password) {
                    DeviceName = input.getText().toString();
                    Intent intent = new Intent(Controlling.this, home.class);
                    intent.putExtra("DeviceAddress", macaddress);
                    intent.putExtra("DeviceName", DeviceName);
                    startActivity(intent);
                }
                else{
                    progressDialog = ProgressDialog.show(Controlling.this, "Hold on", "Connecting");
                    sendtext(input.getText().toString());
                    CountDownTimer c = new CountDownTimer(2500, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            if(passwordtrue){
                                showdialog(false);
                            }
                            else{
                                Toast.makeText(Controlling.this,"Wrong Password", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    };
                    c.start();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                progressDialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    void sendtext(String text){
        try {
            mBTSocket.getOutputStream().write(text.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;
            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);
                        rstring = rstring.concat(strInput);

                        Log.d(TAG, "Received String  "+rstring);
                        if(authentication){
                            if(rstring.equals("AutoCurtains")){
                                deviceokay = true;
                            }
                            if(rstring.contains("CorrectPassword")){
                                t.interrupt();
                                passwordtrue = true;
                            }
                        }
                        rstring = "";

                    }
                    Thread.sleep(500);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }


    }




    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning());
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(Controlling.this, "Hold on", "Connecting");
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
                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();
                finish();
                progressDialog.dismiss();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                sendtext("getDeviceName");
                mReadThread = new ReadInput();
                CountDownTimer c = new CountDownTimer(2500,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        progressDialog.dismiss();
                        if(deviceokay) {
                            showdialog(true);
                        }
                        else{
                            Toast.makeText(Controlling.this,"InCorrect Device. Please try again", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                };
                c.start();
                // Kick off input reader
            }

        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}