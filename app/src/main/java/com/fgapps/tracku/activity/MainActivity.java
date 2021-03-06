package com.fgapps.tracku.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fgapps.tracku.adapter.ListAdapter;
import com.fgapps.tracku.adapter.ListClick;
import com.fgapps.tracku.R;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.database.StorageDatabase;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.helper.Notification;
import com.fgapps.tracku.listener.MainListener;
import com.fgapps.tracku.model.Contact;
import com.fgapps.tracku.service.DatabaseService;
import com.fgapps.tracku.service.LocationService;
import com.fgapps.tracku.service.SaveLoadService;
import com.fgapps.tracku.service.SyncDatabases;
import com.fgapps.tracku.sqlite.SQLDefs;

import java.util.ArrayList;
import java.util.HashMap;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Activity currentActivity;

    private static ArrayList<Contact> contacts;
    private static HashMap<Integer,CardView> selected;
    private static boolean isSharing;

    private FloatingActionButton btnStop;
    private ImageView btnEditName;
    private ImageView btnDelete;
    private TextView txtText1;
    private TextView txtText2;

    private RealtimeDatabase rtdb;
    private ListAdapter adapter;
    private Intent locationIntent;
    private Intent databaseIntent;
    private Thread t;

    @Override
    @SuppressLint("UseSparseArrays")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentActivity = this;

        MainListener listener = new MainListener(this);

        contacts = new ArrayList<>();
        selected = new HashMap<>();

        RecyclerView rcyView = findViewById(R.id.recycler_id);
        rcyView.addOnItemTouchListener(new ListClick(getApplicationContext(), rcyView, listener));

        adapter = new ListAdapter(contacts);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rcyView.setLayoutManager(layoutManager);
        rcyView.setItemAnimator(new DefaultItemAnimator());
        rcyView.setAdapter(adapter);

        txtText1 = findViewById(R.id.text1_id);
        txtText2 = findViewById(R.id.text2_id);

        FloatingActionButton btnAdd = findViewById(R.id.fabAdd_id);
        btnAdd.setOnClickListener(listener);

        btnStop = findViewById(R.id.fabStop_id);
        btnStop.setOnClickListener(listener);

        btnEditName = findViewById(R.id.editName_id);
        btnEditName.setOnClickListener(listener);

        btnDelete = findViewById(R.id.delete_id);
        btnDelete.setOnClickListener(listener);

        ImageView btnSettings = findViewById(R.id.settings_id);
        btnSettings.setOnClickListener(listener);

        ImageView btnLogoff = findViewById(R.id.logoff_id);
        btnLogoff.setOnClickListener(listener);

        rtdb = new RealtimeDatabase();

        SyncDatabases sync = new SyncDatabases();
        sync.start();

        t = new Thread(activityControl);
        t.start();
    }

    public void getContactsFromDb(boolean justLoad) {
        contacts.clear();
        SQLiteDatabase db = LoginActivity.getDb_initializer().getReadableDatabase();
        Cursor c = SQLDefs.select(db, Constants.CONTACT, null, null, null, null);
        if (c.getCount() > 0){
            txtText1.setVisibility(View.INVISIBLE);
            txtText2.setVisibility(View.INVISIBLE);
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_NAME));
                String phone = c.getString(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_PHONE));
                String location = c.getString(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_LOCATION));
                String time = c.getString(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_TIME));
                String uid = c.getString(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_UID));
                int status = c.getInt(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_STATUS));
                Contact contact = new Contact(uid, name, phone, location, time, status);
                contacts.add(contact);
            }
        }else {
            txtText1.setVisibility(View.VISIBLE);
            txtText2.setVisibility(View.VISIBLE);
        }
        if(SyncDatabases.isOnline())
            if(!justLoad){
                Dialogs.showLoadingDialog("Sincronizando","Sincronizando contatos online...",false);
                StorageDatabase sd = new StorageDatabase();
                for(Contact contact : contacts) sd.downloadPhoto(contact.getPhone());
                rtdb.syncContacts();
            }
        c.close();
    }

    //THREAD

    Runnable activityControl = new Runnable() {
        @Override
        public void run() {
            while(MainActivity.currentActivity instanceof MainActivity){
                if(SyncDatabases.isOnline()) {
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                    rtdb.isSharingLocation();
                    runOnUiThread(() -> {
                        if(isSharing) btnStop.setVisibility(View.VISIBLE);
                        else btnStop.setVisibility(View.INVISIBLE);

                    });
                }else{
                    btnStop.setVisibility(View.INVISIBLE);
                }
                sleep(980);
            }
        }
    };

    //OVERRIDE

    @Override
    protected void onResume() {
        currentActivity = this;
        startServices();
        getContactsFromDb(false);
        new Handler().postDelayed(LocationService::isGpsEnabled,3000);
        adapter.notifyDataSetChanged();
        if(t.getState() == Thread.State.TERMINATED){
            t = new Thread(activityControl);
            t.start();
        }
        Notification.dismissAll();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //Nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SaveLoadService sls = SaveLoadService.getInstance(this);
        if(!sls.getConfigService()){
            stopService(new Intent(this, LocationService.class));
            unbindService(locationConnection);
        }
        Dialogs.dismissLoadingDialog(false);
    }

    //GETTERS AND SETTERS

    public static ArrayList<Contact> getContacts() {
        return contacts;
    }

    public static Contact getContact(String phone){
        for (Contact c:contacts) {
            if(c.getPhone().equals(phone)) return c;
        }
        return null;
    }

    public static HashMap<Integer, CardView> getSelected() {
        return selected;
    }

    public ListAdapter getAdapter() {
        return adapter;
    }

    public ImageView getBtnDelete() {
        return btnDelete;
    }

    public ImageView getBtnEditName() {
        return btnEditName;
    }

    public static void isSharing(boolean b){
        isSharing = b;
    }

    //SERVICE BINDER
    private ServiceConnection locationConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //LocationService.LocationBinder binder = (LocationService.LocationBinder)service; not used yet
            //LocationService locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private ServiceConnection databaseConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //DatabaseService.DatabaseBinder binder = (DatabaseService.DatabaseBinder) service; not used yet
            //DatabaseService databaseService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void startServices(){
        if(databaseIntent==null)
            databaseIntent = new Intent(this, DatabaseService.class);
        bindService(databaseIntent, databaseConnection, Context.BIND_AUTO_CREATE);
        startService(databaseIntent);

        if(locationIntent==null)
            locationIntent = new Intent(this, LocationService.class);
        bindService(locationIntent, locationConnection, Context.BIND_AUTO_CREATE);
        startService(locationIntent);
    }
}
