package com.fgapps.tracku.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.helper.Constants;

/**
 * Created by (Engenharia) Felipe on 29/03/2018.
 */

public class SQLDefs {

    //Common
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String LIKE_MOD = " LIKE ?";

    //Queries
    public static final String SQL_CREATE_CONTACT =
            "CREATE TABLE IF NOT EXISTS " + Contact_Table.TABLE_NAME + " (" +
                    Contact_Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contact_Table.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    Contact_Table.COLUMN_PHONE + TEXT_TYPE + COMMA_SEP +
                    Contact_Table.COLUMN_LOCATION + TEXT_TYPE + COMMA_SEP +
                    Contact_Table.COLUMN_TIME + TEXT_TYPE + COMMA_SEP +
                    Contact_Table.COLUMN_CONTACTS + TEXT_TYPE + COMMA_SEP +
                    Contact_Table.COLUMN_UID + TEXT_TYPE + COMMA_SEP +
                    Contact_Table.COLUMN_STATUS + " INTEGER )";

    public static final String SQL_CREATE_USER =
            "CREATE TABLE IF NOT EXISTS " + User_Table.TABLE_NAME + " (" +
                    User_Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    User_Table.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    User_Table.COLUMN_PHONE + TEXT_TYPE + COMMA_SEP +
                    User_Table.COLUMN_UID + TEXT_TYPE + " )";

    public static final String SQL_DELETE_CONTACT =
            "DROP TABLE IF EXISTS " + Contact_Table.TABLE_NAME;

    public static final String SQL_DELETE_USER =
            "DROP TABLE IF EXISTS " + User_Table.TABLE_NAME;

    public static long insert(SQLiteDatabase db, String table, ContentValues values){
        return db.insert(table,null, values);
    }

    public static Cursor select(SQLiteDatabase db, String table, String[] fields, String where_field,
                    String[] where_match, String sortOrder){
        if(where_field!=null) where_field += " = ?";
        return db.query(
                table,                     // The table to query
                fields,                    // The columns to return
                where_field,               // The columns for the WHERE clause
                where_match,               // The values for the WHERE clause
                null,             // don't group the rows
                null,               // don't filter by row groups
                sortOrder                  // The sort order
        );
    }

    public static void update(SQLiteDatabase db, String table, ContentValues new_values,
                              String where_field, String[] where_match){
        where_field += LIKE_MOD;
        int count = db.update(
                table,
                new_values,
                where_field,
                where_match);
    }

    public static boolean deleteContact(SQLiteDatabase db, String delete_phone){
        String[] where_match = {delete_phone};
        String where_field = Contact_Table.COLUMN_PHONE + LIKE_MOD;
        return db.delete(Constants.CONTACT, where_field, where_match)>0 ;
    }

    public static void allowPhone(SQLiteDatabase db, String phone){
        ContentValues cv = new ContentValues();
        cv.put(Constants.STATUS, 1);
        update(db, Constants.CONTACT, cv, Constants.PHONE, new String[]{phone});
        MainActivity.getContact(phone).setStatus(1);
    }

    public static void requestPhone(SQLiteDatabase db, String phone){
        ContentValues cv = new ContentValues();
        cv.put(Constants.STATUS, 2);
        update(db, Constants.CONTACT, cv, Constants.PHONE, new String[]{phone});
        MainActivity.getContact(phone).setStatus(2);
    }

    public static void denyPhone(SQLiteDatabase db, String delete_phone){
        ContentValues cv = new ContentValues();
        cv.put(Constants.STATUS, 0);
        update(db, Constants.CONTACT, cv, Constants.PHONE, new String[]{delete_phone});
        MainActivity.getContact(delete_phone).setStatus(0);
    }

    public static void denyAllPhone(SQLiteDatabase db){
        ContentValues cv = new ContentValues();
        cv.put(Constants.STATUS, 0);
        update(db, Constants.CONTACT, cv, Constants.STATUS, new String[]{"1"});
    }
    
    public static class Contact_Table implements BaseColumns {
        public static final String TABLE_NAME = Constants.CONTACT;
        public static final String COLUMN_NAME = Constants.NAME;
        public static final String COLUMN_PHONE = Constants.PHONE;
        public static final String COLUMN_LOCATION = Constants.LOCATION;
        public static final String COLUMN_TIME = Constants.TIME;
        public static final String COLUMN_CONTACTS = Constants.CONTACTS;
        public static final String COLUMN_STATUS = Constants.STATUS;
        public static final String COLUMN_UID = Constants.UID;
    }

    public static class User_Table implements BaseColumns {
        public static final String TABLE_NAME = Constants.USER;
        public static final String COLUMN_NAME = Constants.NAME;
        public static final String COLUMN_PHONE = Constants.PHONE;
        public static final String COLUMN_UID = Constants.UID;
    }
}
