package com.fgapps.tracku.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fgapps.tracku.service.DatabaseService;

/**
 * Created by (Engenharia) Felipe on 10/04/2018.
 */

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent myIntent = new Intent(context, DatabaseService.class);
            context.startService(myIntent);
        }
    }
}
