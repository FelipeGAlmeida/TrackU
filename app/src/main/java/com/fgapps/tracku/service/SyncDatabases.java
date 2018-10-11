package com.fgapps.tracku.service;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.model.Contact;

import java.util.ArrayList;

/**
 * Created by (Engenharia) Felipe on 02/04/2018.
 */

public class SyncDatabases extends Thread {

    public static ArrayList<Contact> contacts2Delete;

    public SyncDatabases() {
        contacts2Delete = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            if (isOnline()) {
                RealtimeDatabase rtdb = new RealtimeDatabase();
                if(!contacts2Delete.isEmpty()) {
                    for (Contact c : contacts2Delete) {
                        rtdb.deleteContact(c.getPhone());
                    }
                    contacts2Delete.clear();
                }
                String[] deletedList = SaveLoadService.getInstance(MainActivity.currentActivity).loadDeletedList();
                if (deletedList != null) {
                    for (String p : deletedList) {
                        rtdb.deleteContact(p);
                    }

                }
            }
            SystemClock.sleep(2500);
        }
    }

    public static boolean isOnline() {
        ConnectivityManager cm;
        if(MainActivity.currentActivity != null){
            cm =(ConnectivityManager) MainActivity.currentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        }else{
            cm =(ConnectivityManager) DatabaseService.getDatabaseService().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if(cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }
}
