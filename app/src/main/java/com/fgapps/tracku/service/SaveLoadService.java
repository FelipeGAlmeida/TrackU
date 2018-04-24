package com.fgapps.tracku.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.fgapps.tracku.helper.Constants;

import java.util.HashMap;

/**
 * Created by (Engenharia) Felipe on 26/03/2018.
 */

public class SaveLoadService {

    private static SaveLoadService slService;
    private static Context mContext;

    private SharedPreferences mShPref;
    private SharedPreferences.Editor mShEditor;


    public SaveLoadService(Context context) {
        mContext = context;
        slService = this;
    }

    public static SaveLoadService getInstance(Context context){
        if(slService == null){
            slService = new SaveLoadService(context);
        }
        return slService;
    }

    private void initUserFile(){
        if(mShPref == null)
            mShPref = mContext.getSharedPreferences(Constants.USERDATA, Constants.SH_MODE);
    }

    public boolean saveDeletedList(String phone){
        initUserFile();
        String del = mShPref.getString(Constants.DEL, "");

        mShEditor = mShPref.edit();

        if(del.equals("")) del +=phone;
        else del += "&"+phone;
        mShEditor.putString(Constants.DEL, del);

        return mShEditor.commit();
    }

    public String[] loadDeletedList(){
        initUserFile();
        String del = mShPref.getString(Constants.DEL, "");
        if(!del.isEmpty()){
            String phones[] = del.split("&");
            clearDeletedList();
            return phones;
        }
        return null;
    }

    private void clearDeletedList(){
        mShEditor = mShPref.edit();
        mShEditor.remove(Constants.DEL);
        mShEditor.commit();
    }

    public boolean saveConfigService(boolean b){
        initUserFile();

        mShEditor = mShPref.edit();
        mShEditor.putBoolean(Constants.CONFIG_SERVICE, b);
        return mShEditor.commit();
    }

    public boolean getConfigService(){
        initUserFile();

        return mShPref.getBoolean(Constants.CONFIG_SERVICE, false);
    }

    public boolean saveConfigMap(int b){
        initUserFile();

        mShEditor = mShPref.edit();
        mShEditor.putInt(Constants.MAP_TYPE, b);
        return mShEditor.commit();
    }

    public int getConfigMap(){
        initUserFile();

        return mShPref.getInt(Constants.MAP_TYPE, 1);
    }
}
