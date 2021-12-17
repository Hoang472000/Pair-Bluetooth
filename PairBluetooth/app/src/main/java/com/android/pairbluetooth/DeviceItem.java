package com.android.pairbluetooth;

public class DeviceItem {
    String nameDevice;
    String ip;

    public DeviceItem(String name, String address) {
        this.nameDevice = name;
        this.ip = address;
    }

    public String getNameDevice() {
        return nameDevice;
    }

    public void setNameDevice(String nameDevice) {
        this.nameDevice = nameDevice;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
