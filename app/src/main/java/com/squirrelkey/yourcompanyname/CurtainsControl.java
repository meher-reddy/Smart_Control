package com.squirrelkey.yourcompanyname;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageView;

public class CurtainsControl extends AppCompatActivity {

    private static final String TAG = "BlueTest5-Controlling";
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private CurtainsControl.ReadInput mReadThread = null;

    int cpos = 0, lastpos= 0;
    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    String rstring="";
    String DeviceName;
    TextView devicename;
    TextView opentime, closetime;
    EditText editDeviceName;
    ImageButton tickmark, deleteitem;
    int requestcode;

    SwitchMaterial opentoggle, closetoggle;

    ImageButton alarms;
    GifImageView gifView;

    private BluetoothDevice mDevice;
    String macaddress;

    SeekBar openchange;
    Bundle b;
    Button bluetoothstatus;

    ImageButton fullopen, fullclose, retryc;
    TextView fullopent, fullcloset, connectionstatus;


    boolean getposstatus = true;
    Intent intent;
    private ProgressDialog progressDialog;

    SharedPreferences.Editor editor;

    SharedPreferences sharedPreferencess;





    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothstatus.setClickable(true);
                        bluetoothstatus.setBackground(ContextCompat.getDrawable(CurtainsControl.this, R.drawable.bluetooth_disabled));
                        bluetoothstatus.setBackgroundTintList(ContextCompat.getColorStateList(CurtainsControl.this, R.color.white));
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        bluetoothstatus.setClickable(false);
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetoothstatus.setClickable(true);
                        bluetoothstatus.setBackground(ContextCompat.getDrawable(CurtainsControl.this, R.drawable.bluetooth));
                        bluetoothstatus.setBackgroundTintList(ContextCompat.getColorStateList(CurtainsControl.this, R.color.purple_500));
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        bluetoothstatus.setClickable(false);
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };









    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtains_control);


        intent = new Intent(CurtainsControl.this, home.class);
        sharedPreferencess = this.getSharedPreferences("TIME", Context.MODE_PRIVATE);
        editor = sharedPreferencess.edit();

        gifView = findViewById(R.id.gifview);
        devicename = findViewById(R.id.devicenamecontrol);
        editDeviceName = findViewById(R.id.editdevicenamecontrol);
        tickmark = findViewById(R.id.editname);
        bluetoothstatus = findViewById(R.id.bluestat);
        deleteitem = findViewById(R.id.deleteitem);
        fullclose = findViewById(R.id.fullclose);
        fullopen = findViewById(R.id.fullopen);
        fullopent = findViewById(R.id.fullopentitile);
        fullcloset = findViewById(R.id.fullclosetitile);
        retryc = findViewById(R.id.retrybutton);
        connectionstatus = findViewById(R.id.statustext);
        alarms = findViewById(R.id.alarms);

        Intent i = getIntent();
        b = i.getExtras();
        macaddress = b.getString(DeviceList.DEVICE_EXTRA);
        DeviceName = b.getString("DeviceName");
        devicename.setText(DeviceName);
        mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


        alarms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "JINGLE IS CLICKED...");
                alarms();
            }
        });

        retryc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });


        deleteitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tickmark.setVisibility(View.INVISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(CurtainsControl.this);

                builder.setTitle("Confirm");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        intent.putExtra("delete", true);
                        intent.putExtra("Position", getIntent().getIntExtra("Position", 0)+1);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        tickmark.setVisibility(View.VISIBLE);
                    }
                });
            }
        });


        devicename.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CurtainsControl.this);

                builder.setTitle("Change Password");
                builder.setMessage("Choose Something Strong");
                final EditText passwordinput = new EditText(CurtainsControl.this);
                passwordinput.setHint("Password");
                passwordinput.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(passwordinput);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        sendtext("changepass"+passwordinput.getText().toString());
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        tickmark.setVisibility(View.VISIBLE);
                    }
                });

                return true;
            }
        });

        openchange = findViewById(R.id.curtainsprogress);

        openchange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: "+progress+"lastprogress"+lastpos);

                if(progress==0){
                    sendtext(String.valueOf(0));
                    imagechange(lastpos, progress);
                }
                else if(progress==1) {
                    sendtext(String.valueOf(1));
                    imagechange(lastpos, progress);
                }
                else if(progress==2) {
                    sendtext(String.valueOf(2));
                    imagechange(lastpos, progress);
                }
                else if(progress==3) {
                    sendtext(String.valueOf(3));
                    imagechange(lastpos, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);
        if(mBluetoothAdapter!=null) {

            if (mBluetoothAdapter.isEnabled()) {
                bluetoothstatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bluetooth));
                bluetoothstatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.purple_500));
            }

        }

        else Toast.makeText(this, "bluetooth hardware not found", Toast.LENGTH_LONG).show();


    }



    void alarms(){



        Dialog alarmpop = new Dialog(this);
        alarmpop.setContentView(R.layout.alarms_popup);
        alarmpop.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alarmpop.setCancelable(true);
        alarmpop.show();
        opentime = alarmpop.findViewById(R.id.opentime);
        closetime = alarmpop.findViewById(R.id.closetime);
        if(sharedPreferencess.getInt("opentimehour", 1234)!=1234) {
            Log.d(TAG, "JINGLE OPEN TIME SHARED"+sharedPreferencess.getInt("opentimehour", 00)+":"+sharedPreferencess.getInt("opentimeminute", 00));
            opentime.setText(sharedPreferencess.getInt("opentimehour", 00) + ":" + sharedPreferencess.getInt("opentimeminute", 00));
        }
        if(sharedPreferencess.getInt("closetimehour", 4567)!=4567){
            Log.d(TAG, "JINGLE OPEN TIME SHARED"+sharedPreferencess.getInt("closetimehour", 00)+":"+sharedPreferencess.getInt("closetimeminute", 00));
            closetime.setText(sharedPreferencess.getInt("closetimehour", 00) + ":" + sharedPreferencess.getInt("closetimeminute", 00));
        }
        opentime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmSetter(true);
            }
        });

        closetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmSetter(false);
            }
        });

        opentoggle = alarmpop.findViewById(R.id.opentoggle);
        closetoggle = alarmpop.findViewById(R.id.closetoggle);

        if(sharedPreferencess.getBoolean("open", false)){
            opentoggle.setChecked(true);
        }
        if(sharedPreferencess.getBoolean("close", false)){
            closetoggle.setChecked(true);
        }
        opentoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = CurtainsControl.this;
                if(isChecked){

                    Calendar calendar = Calendar.getInstance();

                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                            sharedPreferencess.getInt("opentimehour", 0), sharedPreferencess.getInt("opentimeminute", 0), 0);

                    alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent2 = new Intent(context, AlarmReceiver.class);
                    intent2.putExtra("Address", macaddress);
                    intent2.putExtra("open", true);
                    String tempcode = (getIntent().getIntExtra("Position", 0)+1)+"00000";
                    requestcode = Integer.parseInt(tempcode);
                    alarmIntent = PendingIntent.getBroadcast(context, requestcode, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
                    editor.putBoolean("open", true);
                    editor.commit();
                }
                else{
                    editor.putBoolean("open", false);
                    editor.commit();
                    String tempcode = (getIntent().getIntExtra("Position", 0)+1)+"00000";
                    requestcode = Integer.parseInt(tempcode);
                    alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent2 = new Intent(context, AlarmReceiver.class);
                    alarmIntent = PendingIntent.getBroadcast(context, requestcode, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                    if(alarmIntent!=null)
                    alarmMgr.cancel(alarmIntent);
                }
            }
        });


        closetoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = CurtainsControl.this;
                if(isChecked){

                    Calendar calendar = Calendar.getInstance();

                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                            sharedPreferencess.getInt("closetimehour", 0), sharedPreferencess.getInt("closetimeminute", 0), 0);

                    alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent2 = new Intent(context, AlarmReceiver.class);
                    intent2.putExtra("Address", macaddress);
                    String tempcode = (getIntent().getIntExtra("Position", 0)+1)+"0000";
                    requestcode = Integer.parseInt(tempcode);
                    intent2.putExtra("open", false);
                    alarmIntent = PendingIntent.getBroadcast(context, requestcode, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
                    editor.putBoolean("close", true);
                    editor.commit();

                }
                else{
                    editor.putBoolean("close", false);
                    editor.commit();
                    String tempcode = (getIntent().getIntExtra("Position", 0)+1)+"0000";
                    requestcode = Integer.parseInt(tempcode);
                    alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent2 = new Intent(CurtainsControl.this, AlarmReceiver.class);
                    alarmIntent = PendingIntent.getBroadcast(CurtainsControl.this, requestcode, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                    if(alarmIntent!=null)
                    alarmMgr.cancel(alarmIntent);
                }
            }
        });

    }





    void alarmSetter(boolean open){



        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);


        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {



                if(open){
                    Log.d(TAG, "JINGLE OPEN TIME IN HOURS : "+timePicker.getCurrentHour()+" JINGLE OPEN TIME IN MINUTES : "+timePicker.getCurrentMinute());
                    opentime.setText(String.format(timePicker.getCurrentHour().toString())+":"+String.format(timePicker.getCurrentMinute().toString()));
                    SharedPreferences.Editor editor = sharedPreferencess.edit();
                    editor.putInt("opentimehour", timePicker.getCurrentHour());
                    editor.putInt("opentimeminute", timePicker.getCurrentMinute());
                    editor.putBoolean("open", false);
                    editor.commit();
                    if(opentoggle.isChecked()){
                        Context context = CurtainsControl.this;

                        Calendar calendar = Calendar.getInstance();

                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                                sharedPreferencess.getInt("opentimehour", 0), sharedPreferencess.getInt("opentimeminute", 0), 0);

                        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        Intent intent2 = new Intent(context, AlarmReceiver.class);
                        intent2.putExtra("Address", macaddress);
                        String tempcode = (getIntent().getIntExtra("Position", 0)+1)+"0000";
                        requestcode = Integer.parseInt(tempcode);
                        intent2.putExtra("open", false);
                        alarmIntent = PendingIntent.getBroadcast(context, requestcode, intent2, 0);
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
                        editor.putBoolean("close", true);
                        editor.commit();
                    }
                }
                else{
                    Log.d(TAG, "JINGLE CLOSE TIME IN HOURS : "+timePicker.getCurrentHour()+" JINGLE CLOSE TIME IN MINUTES : "+timePicker.getCurrentMinute());
                    closetime.setText(String.format(timePicker.getCurrentHour().toString())+":"+String.format(timePicker.getCurrentMinute().toString()));
                    SharedPreferences.Editor editor = sharedPreferencess.edit();
                    editor.putInt("closetimehour", timePicker.getCurrentHour());
                    editor.putInt("closetimeminute", timePicker.getCurrentMinute());
                    editor.putBoolean("close", true);
                    editor.commit();
                    if(closetoggle.isChecked()){
                        Context context = CurtainsControl.this;

                        Calendar calendar = Calendar.getInstance();

                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                                sharedPreferencess.getInt("closetimehour", 0), sharedPreferencess.getInt("closetimeminute", 0), 0);

                        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        Intent intent2 = new Intent(context, AlarmReceiver.class);
                        intent2.putExtra("Address", macaddress);
                        String tempcode = (getIntent().getIntExtra("Position", 0)+1)+"0000";
                        requestcode = Integer.parseInt(tempcode);
                        intent2.putExtra("open", false);
                        alarmIntent = PendingIntent.getBroadcast(context, requestcode, intent2, 0);
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
                        editor.putBoolean("close", true);
                        editor.commit();
                    }
                }
            }
        }, hour, minute, true );
        timePickerDialog.show();
    }







    public void bluetoothstatuscontrol(View view){
        if(mBluetoothAdapter!=null) {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                bluetoothstatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bluetooth_disabled));
                bluetoothstatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
            } else if(!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                bluetoothstatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bluetooth));
                bluetoothstatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.purple_500));
            }
        }
        else Toast.makeText(this, "bluetooth hardware not found", Toast.LENGTH_LONG).show();

    }


    public void edit(View view){
            tickmark.setVisibility(View.VISIBLE);
            editDeviceName.setText(devicename.getText().toString());
            devicename.setVisibility(View.INVISIBLE);
            editDeviceName.setVisibility(View.VISIBLE);
            connectionstatus.setVisibility(View.INVISIBLE);
            deleteitem.setVisibility(View.INVISIBLE);
    }

    public void editname(View view){
        tickmark.setVisibility(View.INVISIBLE);
        if(retryc.getVisibility()==View.VISIBLE)
            connectionstatus.setVisibility(View.VISIBLE);
        deleteitem.setVisibility(View.VISIBLE);
        devicename.setText(editDeviceName.getText().toString());
        editDeviceName.setVisibility(View.INVISIBLE);
        devicename.setVisibility(View.VISIBLE);
        DeviceName = devicename.getText().toString();
        intent.putExtra("DeviceName", DeviceName);
        intent.putExtra("Position", (getIntent().getIntExtra("Position", 0))+1);
    }





    public void fullopen(View view){
        sendtext(String.valueOf(0));
        imagechange(lastpos, 0);
    }
    public void fullclose(View view){
        sendtext(String.valueOf(3));
        imagechange(lastpos, 3);
    }

    void imagechange(int lastposl, int cpos){

        if(cpos == 3 && lastposl<cpos){
            gifView.setImageResource(R.drawable.c_3);
            openchange.setProgress(cpos);
        }
        if(cpos == 2 && lastposl<cpos){
            gifView.setImageResource(R.drawable.c_2);
            openchange.setProgress(cpos);
        }
        if(cpos==1&&lastposl<cpos){
            gifView.setImageResource(R.drawable.c_1);
        }
        if(cpos == 0 && lastposl>cpos){
            gifView.setImageResource(R.drawable.o_3);
            openchange.setProgress(cpos);
        }
        if(cpos == 1 && lastposl>cpos){
            gifView.setImageResource(R.drawable.o_2);
            openchange.setProgress(cpos);
        }
        if(cpos == 2 && lastposl>cpos){
            gifView.setImageResource(R.drawable.o_1);
        }
        lastpos = cpos;


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
                        if(getposstatus){
                            cpos = Integer.parseInt(rstring);
                            getposstatus = false;
                            imagechange(lastpos,cpos);
                        }
                        Log.d(TAG, "Received String  "+rstring);
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
                while (mReadThread.isRunning())
                    ; // Wait until it stops
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
            new CurtainsControl.DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new CurtainsControl.ConnectBT().execute();
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

            progressDialog = ProgressDialog.show(CurtainsControl.this, "Hold on", "Connecting");
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
                openchange.setVisibility(View.INVISIBLE);
                fullclose.setVisibility(View.INVISIBLE);
                fullopen.setVisibility(View.INVISIBLE);
                fullopent.setVisibility(View.INVISIBLE);
                fullcloset.setVisibility(View.INVISIBLE);
                retryc.setVisibility(View.VISIBLE);
                connectionstatus.setText("Connection Status : DisConnected");
                connectionstatus.setTextColor(getResources().getColor(R.color.error));
                connectionstatus.setVisibility(View.VISIBLE);


            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                sendtext("getPos");
                mReadThread = new CurtainsControl.ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void deviceback(View view){
        unregisterReceiver(mBroadcastReceiver1);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(mBroadcastReceiver1);
        startActivity(intent);
    }
}