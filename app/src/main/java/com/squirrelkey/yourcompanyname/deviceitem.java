package com.squirrelkey.yourcompanyname;

public class deviceitem {
    public String DeviceName;
    public String DeviceAdress;


    public deviceitem(){

    }

    public deviceitem(String DeviceName, String DeviceAddress) {
        this.DeviceName = DeviceName;
        this.DeviceAdress = DeviceAddress;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String DeviceName) {
        this.DeviceName = DeviceName;
    }

    public String getDeviceAdress() {
        return DeviceAdress;
    }

    public void setDeviceAdress(String DeviceAdress) {
        this.DeviceAdress = DeviceAdress;
    }

}
