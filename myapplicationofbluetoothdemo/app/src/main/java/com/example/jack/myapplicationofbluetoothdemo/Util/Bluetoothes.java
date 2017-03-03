package com.example.jack.myapplicationofbluetoothdemo.Util;

import java.io.Serializable;

/**
 * Created by Jack on 2016/10/27.
 */
public class Bluetoothes implements Serializable {

    private String name;
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return name + "-" + address;
    }
}
