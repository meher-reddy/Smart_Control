package com.squirrelkey.yourcompanyname;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.content.ContentValues.TAG;

public class home extends AppCompatActivity {

    final Fragment rooms_frag = new rooms_frag();
    final Fragment settings_frag = new settings_frag();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = rooms_frag;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    Button bluetoothstatus;
    public static String DeviceName;
    public static String DeviceAddress;
    public static int Position=0;
    public static boolean delete = false;
    TextView fragheading;


    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothstatus.setClickable(true);
                        bluetoothstatus.setBackground(ContextCompat.getDrawable(home.this, R.drawable.bluetooth_disabled));
                        bluetoothstatus.setBackgroundTintList(ContextCompat.getColorStateList(home.this, R.color.white));
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        bluetoothstatus.setClickable(false);
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetoothstatus.setClickable(true);
                        bluetoothstatus.setBackground(ContextCompat.getDrawable(home.this, R.drawable.bluetooth));
                        bluetoothstatus.setBackgroundTintList(ContextCompat.getColorStateList(home.this, R.color.purple_500));
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












    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bluetoothstatus = findViewById(R.id.bluetoothstatus);
        fragheading = findViewById(R.id.saveddevicestext);

            DeviceName = getIntent().getStringExtra("DeviceName");
            DeviceAddress = getIntent().getStringExtra("DeviceAddress");
            Log.d(TAG, "PositionInHome "+getIntent().getIntExtra("Position", 5));
            Position = getIntent().getIntExtra("Position", 0);
            delete = getIntent().getBooleanExtra("delete", false);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.main_bottom_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.Frag_container, settings_frag, "2").hide(settings_frag).commit();
        fm.beginTransaction().add(R.id.Frag_container,rooms_frag, "1").commit();


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

    public void bluetoothstatus(View view){

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




    public void adddevicebutton(View view){

        startActivity(new Intent(this, DeviceList.class));

    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.rooms:
                    fm.beginTransaction().hide(active).show(rooms_frag).commit();
                    active = rooms_frag;
                    fragheading.setText("Saved Devices");
                    return true;

                case R.id.settings:
                    fm.beginTransaction().hide(active).show(settings_frag).commit();
                    active = settings_frag;
                    fragheading.setText("About");
                    return true;

            }
            return false;
        }
    };










    public void exitapp(View view){
        unregisterReceiver(mBroadcastReceiver1);
        ExitActivity.exit(this);

    }




    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver1);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(mBroadcastReceiver1);
        ExitActivity.exit(this);
    }
}