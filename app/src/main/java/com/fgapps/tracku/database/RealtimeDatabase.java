package com.fgapps.tracku.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.fgapps.tracku.activity.LoginActivity;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.MapsActivity;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.helper.Notification;
import com.fgapps.tracku.helper.Utils;
import com.fgapps.tracku.model.Contact;
import com.fgapps.tracku.service.LocationService;
import com.fgapps.tracku.sqlite.SQLDefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by (Engenharia) Felipe on 27/03/2018.
 */

public class RealtimeDatabase{

    private static DatabaseReference mDb;
    private static ValueEventListener responseListener;
    private static ValueEventListener locationListener;
    private static ValueEventListener requestListener;


    public RealtimeDatabase() {
        this.getInstance();
    }

    public static DatabaseReference getInstance(){
        if(mDb == null)
            mDb = FirebaseDatabase.getInstance().getReference();
        return mDb;
    }

    public void addUser(String name, String phone, String uid){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(phone);
        ref.child(Constants.UID).setValue(uid);
        ref.child(Constants.NAME).setValue(name);
        ref.child(Constants.PHONE).setValue(phone);
        ref.child(Constants.LOCATION).setValue("Desconhecida");

        ref.child(Constants.TIME).setValue(Utils.getFormattedDateTime());
    }

    public void sendLocation(String location, String database, String userphone){
        DatabaseReference ref = mDb.child(database).child(userphone);
        ref.child(Constants.LOCATION).setValue(location);
        ref.child(Constants.TIME).setValue(Utils.getFormattedDateTime());
    }

    public void addContact(final String phoneNumber, final boolean need_dismiss){
        DatabaseReference ref = mDb.child(Constants.DATABASE);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()){
                    if(messageSnapshot.getKey().equals(phoneNumber)) {
                        String uid = (String) messageSnapshot.child(Constants.UID).getValue();
                        String name = (String) messageSnapshot.child(Constants.NAME).getValue();
                        String phone = (String) messageSnapshot.child(Constants.PHONE).getValue();
                        String location = (String) messageSnapshot.child(Constants.LOCATION).getValue();
                        String time = (String) messageSnapshot.child(Constants.TIME).getValue();

                        Contact c = new Contact(uid, name, phone, location, time, 0);
                        MainActivity.getContacts().add(c);

                        SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(SQLDefs.Contact_Table.COLUMN_NAME, name);
                        values.put(SQLDefs.Contact_Table.COLUMN_PHONE, phone);
                        values.put(SQLDefs.Contact_Table.COLUMN_LOCATION, location);
                        values.put(SQLDefs.Contact_Table.COLUMN_TIME, time);
                        values.put(SQLDefs.Contact_Table.COLUMN_UID, uid);
                        SQLDefs.insert(db, Constants.CONTACT, values);

                        DatabaseReference ref0 = mDb.child(Constants.DATABASE).child(LoginActivity.USERPHONE).child(Constants.CONTACTS);
                        ref0.child(phone).setValue(0);
                    }
                }
                if(need_dismiss)
                    Dialogs.dismissLoadingDialog(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String a = "C:"+databaseError.getCode()+" M:"+databaseError.getMessage()+" D:"+databaseError.getDetails();
            }
        });
    }

    public void updateContact(final String phone) {
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(phone);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(Constants.NAME).getValue();
                String location = (String) dataSnapshot.child(Constants.LOCATION).getValue();
                String time = (String) dataSnapshot.child(Constants.TIME).getValue();
                Contact c = MainActivity.getContact(phone);
                if (c != null) {
                    SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase(); int status = 0;
                    String old_loc = "Desconhecida"; String old_time = "Desconhecido";
                    Cursor cursor = SQLDefs.select(db, Constants.CONTACT, null, Constants.PHONE, new String[]{phone},null);
                    cursor.moveToFirst();
                    if(cursor.getCount()>0) {
                        status = cursor.getInt((cursor.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_STATUS)));
                        old_loc = cursor.getString(cursor.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_LOCATION));
                        old_time = cursor.getString(cursor.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_TIME));
                    }
                    cursor.close();

                    if(status == 1) {
                        c.setLocation(location);
                        c.setTime(time);
                    }else{
                        location = old_loc;
                        time = old_time;
                    }

                    ContentValues cv = new ContentValues();
                    cv.put(Constants.LOCATION, location);
                    cv.put(Constants.TIME, time);
                    if(c.getName().equals("")) {
                        cv.put(Constants.NAME, name);
                        MainActivity.getContact(phone).setName(name);
                    }
                    SQLDefs.update(db, Constants.CONTACT, cv, Constants.PHONE, new String[]{phone});
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void syncContacts(){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(LoginActivity.USERPHONE).child(Constants.CONTACTS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String phone = messageSnapshot.getKey();
                    if(MainActivity.getContact(phone) == null) addContact(phone, false);
                    else updateContact(phone);
                }
                Dialogs.dismissLoadingDialog(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void askLocationUser(String phone){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(phone).child(Constants.CONTACTS);
        ref.child(LoginActivity.USERPHONE).setValue(2);
    }

    public void allowLocation(String phone){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(LoginActivity.USERPHONE).child(Constants.CONTACTS);
        ref.child(phone).setValue(1);
    }

    public void denyLocation(String phone){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(LoginActivity.USERPHONE).child(Constants.CONTACTS);
        ref.child(phone).setValue(0);
    }

    public void denyAllLocation(){
        final DatabaseReference ref = mDb.child(Constants.DATABASE).child(LoginActivity.USERPHONE).child(Constants.CONTACTS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    ref.child(messageSnapshot.getKey()).setValue(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkAllowed(final String phone){
        final DatabaseReference ref = mDb.child(Constants.DATABASE).child(phone).child(Constants.CONTACTS).child(LoginActivity.USERPHONE);
        if(responseListener != null){
            ref.removeEventListener(responseListener);
            responseListener = null;
        }
        responseListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long p = (Long)dataSnapshot.getValue(); int status = 0;
                SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
                Cursor c = SQLDefs.select(db, Constants.CONTACT, null, Constants.PHONE, new String[]{phone},null);
                c.moveToFirst();
                if(c.getCount()>0) {
                    status = c.getInt((c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_STATUS)));
                }
                c.close();
                if(p == 1){ //SIM
                    SQLDefs.allowPhone(db, phone);
                    if(status == 2)
                        Dialogs.showAllowedLocationDialog(phone);
                    startGetingLocation(phone);
                    Dialogs.dismissLoadingDialog(false);
                }else if(p == 0){ //NÃO
                    if(status == 0) { //NEGADO
                        MapsActivity.showAskAlertDialog();
                    }else {
                        SQLDefs.denyPhone(db, phone);
                        if(status == 2) { //NEGOU
                            Dialogs.showDeniedLocationDialog(phone);
                            ref.removeEventListener(responseListener);
                        }else { //REVOGOU
                            MapsActivity.showEndAlertDialog();
                            ref.removeEventListener(responseListener);
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.currentActivity, "O contato ainda não respondeu seu pedido, exibindo a última localização disponível",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkRequests(final String userphone){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(userphone).child(Constants.CONTACTS);
        if(requestListener != null){
            ref.removeEventListener(requestListener);
            requestListener = null;
        }
        requestListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String key = messageSnapshot.getKey();
                    Long r = (Long)messageSnapshot.getValue();
                    if (r == 2) {
                        Notification.sendIncommingNotification(LocationService.getLocationService(), key);
                        Dialogs.showRequestLocationDialog(key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void startGetingLocation(String phone){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(phone).child(Constants.LOCATION);
        if(locationListener != null){
            ref.removeEventListener(locationListener);
            locationListener = null;
        }
        locationListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String l = (String)dataSnapshot.getValue();
                String[] loc = l.split(",");
                if(loc.length>1)
                    MapsActivity.updateMap(loc[0], loc[1], false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void stopGettingLocation(String phone){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(phone).child(Constants.LOCATION);
        if(locationListener!=null) {
            ref.removeEventListener(locationListener);
            locationListener = null;
        }
    }

    public void isSharingLocation(){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(LoginActivity.USERPHONE).child(Constants.CONTACTS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Long r = (Long)messageSnapshot.getValue();
                    if (r == 1) {
                        MainActivity.isSharing(true);
                        return;
                    }
                }
                MainActivity.isSharing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteContact(final String phone){
        DatabaseReference ref = mDb.child(Constants.DATABASE).child(LoginActivity.USERPHONE).child(Constants.CONTACTS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()){
                    String key = messageSnapshot.getKey();
                    if(key.equals(phone))
                        messageSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
