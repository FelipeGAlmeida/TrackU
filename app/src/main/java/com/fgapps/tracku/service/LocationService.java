package com.fgapps.tracku.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.fgapps.tracku.activity.LoginActivity;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.MapsActivity;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.helper.Notification;
import com.fgapps.tracku.sqlite.SQLDatabase;
import com.fgapps.tracku.sqlite.SQLDefs;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {

    private final IBinder locationBinder = new LocationBinder();

    private static LocationService locationService;
    private static boolean running = false;
    private static String location;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private RealtimeDatabase rtdb;
    private Location prev_location;

    private String database;
    private String userphone;
    private float distance;

    public LocationService() {
        locationService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                String lt = Double.toString(locationResult.getLastLocation().getLatitude());
                String lg = Double.toString(locationResult.getLastLocation().getLongitude());
                location = lt+","+lg;

                if(MapsActivity.getMode() == 1){
                    MapsActivity.updateMap(lt, lg, true);
                }

                Location cur_loc = new Location("current");
                cur_loc.setLatitude(Double.parseDouble(lt));
                cur_loc.setLongitude(Double.parseDouble(lg));
                Log.v("LOCATION_SERVICE_DIST", "New value set to cur_loc");

                if(prev_location != null)
                    Log.v("LOCATION_SERVICE_DIST", cur_loc.distanceTo(prev_location)+">"+distance);

                if(prev_location != null && cur_loc.distanceTo(prev_location) > distance) {
                    if(prev_location != null)
                        Log.v("LOCATION_SERVICE_DIST", cur_loc.distanceTo(prev_location)+">"+distance);
                    sendCoordnates(cur_loc);
                }else{
                    if(prev_location == null) {
                        Log.v("LOCATION_SERVICE_DIST", "prev_location is NULL");
                        sendCoordnates(cur_loc);
                    }
                }

                SaveLoadService sls = SaveLoadService.getInstance(locationService);
                if(MainActivity.currentActivity == null){
                    if(!sls.getConfigService()){
                        locationService.stopForeground(true);
                        locationService.stopSelf();
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    }
                }else{
                    distance = sls.getDistConfig();
                }
            }
        };
        if(LoginActivity.checkGPSPermission(this))
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);


        startForeground(999,Notification.getFixedNotification(this));
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rtdb = new RealtimeDatabase();

        SaveLoadService sls = SaveLoadService.getInstance(locationService);
        distance = sls.getDistConfig();

        database = Constants.DATABASE;//"tracku-users";
        userphone = getUserphoneFromDB();
    }

    private void sendCoordnates(Location cur_loc){
        if (SyncDatabases.isOnline()) {
            if (rtdb == null) rtdb = new RealtimeDatabase();
            rtdb.sendLocation(location, database, userphone);
        }
        prev_location = cur_loc;
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

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public static boolean isGpsEnabled(){
        LocationManager lm;
        if(MainActivity.currentActivity != null) {
            lm = (LocationManager) MainActivity.currentActivity.getSystemService(MainActivity.currentActivity.LOCATION_SERVICE);
        }else{
            lm = (LocationManager) locationService.getSystemService(locationService.LOCATION_SERVICE);
        }
        boolean enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!enabled && MainActivity.currentActivity != null ){
            Dialogs.showGpsDialog();
        }
        return enabled;
    }

    public static String[] getSeparatedLocation(){
        if(location!=null) {
            String[] locAsStr = location.split(",");
            if (locAsStr.length > 1) {
                return locAsStr;
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SaveLoadService sls = SaveLoadService.getInstance(this);
        if(!sls.getConfigService()) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopForeground(true);
            stopSelf();
        }
        running = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return locationBinder;
    }

    public static LocationService getLocationService() {
        return locationService;
    }

    public static boolean isRunning() {
        return running;
    }

    public class LocationBinder extends Binder {
        public LocationService getService() { return LocationService.this; }
    }
}
