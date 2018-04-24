package com.fgapps.tracku.model;
/**
 * Created by (Engenharia) Felipe on 26/03/2018.
 */

public class Contact {

    private String uid;
    private String name;
    private String phone;
    private String location;
    private String time;
    private int status;

    public Contact(String uid, String name, String phone, String location, String time, int status) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.location = location;
        this.time = time;
        this.status = status;
    }

    //GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
