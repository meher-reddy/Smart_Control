package com.squirrelkey.yourcompanyname;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class deviceadapter extends RecyclerView.Adapter<deviceadapter.ViewHolder>{
    public List<deviceitem> devicelist;

    private OnDeviceClickListener monDeviceCLickListener;

    public deviceadapter(List<deviceitem> list, OnDeviceClickListener onDeviceClickListener) {
        this.devicelist = list;
        this.monDeviceCLickListener = onDeviceClickListener;
    }

    @NonNull
    @Override
    public deviceadapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new ViewHolder(view, monDeviceCLickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull deviceadapter.ViewHolder holder, int position) {
        String DeviceName = devicelist.get(position).getDeviceName();

        holder.desc.setText(DeviceName);


    }

    @Override
    public int getItemCount() {
        return devicelist.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        private View mView;
        private TextView desc;
        OnDeviceClickListener onDeviceClickListener;
        public ViewHolder(View itemView, OnDeviceClickListener onDeviceClickListener) {
            super(itemView);
            mView = itemView;
            desc = mView.findViewById(R.id.devicename);
            this.onDeviceClickListener = onDeviceClickListener;
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onDeviceClickListener.onDeviceClick(getAdapterPosition());
        }
    }


    public  interface OnDeviceClickListener{
        void onDeviceClick(int position);
    }
}
