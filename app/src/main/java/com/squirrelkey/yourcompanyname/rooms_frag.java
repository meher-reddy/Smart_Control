package com.squirrelkey.yourcompanyname;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class rooms_frag extends Fragment implements deviceadapter.OnDeviceClickListener{

    public List<deviceitem> rv_list;
    private RecyclerView recyclerView;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final String PREFS_NAME  = "filename";
    String DeviceName="Null", DeviceAddress="Null";
    String userid;
    ProgressDialog progressDialog;

    public rooms_frag() {
        // Required empty public constructor
    }
    public static com.squirrelkey.yourcompanyname.rooms_frag newInstance(String param1, String param2) {
        com.squirrelkey.yourcompanyname.rooms_frag fragment = new com.squirrelkey.yourcompanyname.rooms_frag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
        userid = FirebaseAuth.getInstance().getUid();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.devicerv);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_list = new ArrayList<>();
        Context ctxx1 = getActivity();
        SharedPreferences sharedPreferencess1 = ctxx1.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferencess1.edit();
        if(sharedPreferencess1.getInt("Total", 9962)==9962){
            progressDialog = ProgressDialog.show(getContext(), "Hold on", "Syncing with cloud...");
            FirebaseDatabase.getInstance().getReference().child(userid).child("devices").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(String.valueOf(1)).exists()){
                        editor1.putInt("Total", Integer.parseInt(snapshot.child("total").getValue().toString()));
                        for(int i = 1;i<=Integer.parseInt(snapshot.child("total").getValue().toString());i++){
                            editor1.putString(i+"Name", snapshot.child(String.valueOf(i)).child("name").getValue().toString());
                            editor1.putString(i+"Address", snapshot.child(String.valueOf(i)).child("address").getValue().toString());
                            editor1.commit();
                            progressDialog.dismiss();
                        }
                    }
                    else{
                        editor1.putInt("Total", 0);
                        editor1.commit();
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "No devices found in your cloud account", Toast.LENGTH_LONG).show();
                    }
                    Context ctx = getActivity();
                    int totalVals;
                    SharedPreferences sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    totalVals = sharedPreferences.getInt("Total",9395);
                    Log.d(TAG, "TotalVals = "+totalVals);
                    if(totalVals!=9395) {
                        for (int i = 1; i <= totalVals; i++) {
                            rv_list.add(new deviceitem(sharedPreferences.getString(i + "Name", "Null"), sharedPreferences.getString(i + "Address", "Null")));
                        }
                    }
                    deviceadapter mAdapter = new deviceadapter(rv_list, rooms_frag.this);
                    recyclerView.setAdapter(mAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }






        if(home.delete){
            Context ctxx = getActivity();
            SharedPreferences sharedPreferencess = ctxx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferencess.edit();
            editor.putInt("Total", sharedPreferencess.getInt("Total", 0)-1);
            for(int i =home.Position;i<sharedPreferencess.getInt("Total", 0)-1;i++){
                editor.putString(i+"Name", sharedPreferencess.getString((i+1)+"Name", "Name"));
                editor.putString(i+"Address", sharedPreferencess.getString((i+1)+"Address", "address"));
                FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child(String.valueOf(i)).child("name").setValue(sharedPreferencess.getString((i+1)+"Name", "Name"));
                FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child(String.valueOf(i)).child("address").setValue(sharedPreferencess.getString((i+1)+"Address", "address"));
            }
            FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child(String.valueOf(sharedPreferencess.getInt("Total", 0))).removeValue();
            FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child("total").setValue(String.valueOf(sharedPreferencess.getInt("Total", 0)-1));
            editor.remove(sharedPreferencess.getInt("Total", 0)+"Name");
            editor.remove(sharedPreferencess.getInt("Total", 0 )+"Address");
            home.delete = false;
            editor.commit();
        }



        Context ctx = getActivity();
        int totalVals;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        totalVals = sharedPreferences.getInt("Total",9395);
        Log.d(TAG, "TotalVals = "+totalVals);
        if(totalVals!=9395) {
            for (int i = 1; i <= totalVals; i++) {
                rv_list.add(new deviceitem(sharedPreferences.getString(i + "Name", "Null"), sharedPreferences.getString(i + "Address", "Null")));
            }
        }



        if(home.DeviceAddress!=null) {
            boolean duplicate = false;
            DeviceAddress = home.DeviceAddress;
            DeviceName = home.DeviceName;
            for (int i = 0;i<rv_list.size();i++){
                if(rv_list.get(i).DeviceAdress.equals(DeviceAddress)){
                    duplicate = true;
                }
            }
            if(!duplicate) {
                rv_list.add(new deviceitem(DeviceName, DeviceAddress));
                Context ctxx = getActivity();
                SharedPreferences sharedPreferencess = ctxx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferencess.edit();
                editor.putInt("Total", rv_list.size());
                editor.putString(rv_list.size() + "Name", DeviceName);
                editor.putString(rv_list.size() + "Address", DeviceAddress);
                editor.commit();
                FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child("total").setValue(rv_list.size());
                FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child(String.valueOf(rv_list.size())).child("name").setValue(DeviceName);
                FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child(String.valueOf(rv_list.size())).child("address").setValue(DeviceAddress);
            }
            else Toast.makeText(getActivity(), "AlreadyExists", Toast.LENGTH_LONG).show();
        }

        deviceadapter mAdapter = new deviceadapter(rv_list, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        FirebaseDatabase.getInstance().getReference().child(userid).child("devices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Context ctxx = getActivity();
                    SharedPreferences sharedPreferencess = ctxx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    if (sharedPreferencess.getInt("Total", 9952) == 0) {
                        FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child("total").setValue(0);
                    } else {
                        for (int i = 1; i <= sharedPreferencess.getInt("Total", 0); i++) {
                            FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child(String.valueOf(i)).child("name").setValue(sharedPreferencess.getString(i + "Name", "Null"));
                            FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child(String.valueOf(i)).child("address").setValue(sharedPreferencess.getString(i + "Address", "Null"));
                        }
                        FirebaseDatabase.getInstance().getReference().child(userid).child("devices").child("total").setValue(sharedPreferencess.getInt("Total", 9952));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }





    @Override
    public void onDeviceClick(int position) {
        Intent i = new Intent(getActivity(), CurtainsControl.class);
        i.putExtra("DeviceAddress",rv_list.get(position).DeviceAdress);
        i.putExtra("DeviceName", rv_list.get(position).DeviceName);
        i.putExtra("Position", position);
        startActivity(i);
    }







    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}