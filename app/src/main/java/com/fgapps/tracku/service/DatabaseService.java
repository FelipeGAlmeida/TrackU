package com.fgapps.tracku.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.sqlite.SQLDatabase;
import com.fgapps.tracku.sqlite.SQLDefs;

public class DatabaseService extends Service {

    private static DatabaseService databaseService;

    private RealtimeDatabase rtdb;
    private static boolean running = false;
    private String userphone;

    public DatabaseService() {
        this.databaseService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;

        if (userphone != null){
            if (SyncDatabases.isOnline()) {
                rtdb = new RealtimeDatabase();
                rtdb.checkRequests(userphone);
            }
        }else{
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        userphone = getUserphoneFromDB();

        if(SaveLoadService.getInstance(this).getConfigService()) {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
        }
    }

    private String getUserphoneFromDB(){
        SQLDatabase db_initializer = new SQLDatabase(this);
        SQLiteDatabase db = db_initializer.getReadableDatabase();
        String[] fields = {
                SQLDefs.User_Table.COLUMN_PHONE
        };
        Cursor c = SQLDefs.select(db,Constants.USER,fields, null, null, null);
        c.moveToFirst();
        if(c.getCount()>0) {
            return c.getString(c.getColumnIndexOrThrow(SQLDefs.User_Table.COLUMN_PHONE));
        }
        c.close();
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean isRunning() {
        return running;
    }

    public static DatabaseService getDatabaseService() {
        return databaseService;
    }
}
