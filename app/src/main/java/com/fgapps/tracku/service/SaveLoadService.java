package com.fgapps.tracku.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.fgapps.tracku.helper.Constants;

/**
 * Created by (Engenharia) Felipe on 26/03/2018.
 */

public class SaveLoadService {

    @SuppressLint("StaticFieldLeak")
    private static SaveLoadService slService;
    private Context mContext;

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

    public void saveDeletedList(String phone){
        initUserFile();
        String del = mShPref.getString(Constants.DEL, "");

        mShEditor = mShPref.edit();

        if(del.equals("")) del +=phone;
        else del += "&"+phone;
        mShEditor.putString(Constants.DEL, del);

        mShEditor.apply();
    }

    String[] loadDeletedList(){
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
        mShEditor.apply();
    }

    public void saveConfigService(boolean b){
        initUserFile();

        mShEditor = mShPref.edit();
        mShEditor.putBoolean(Constants.CONFIG_SERVICE, b);
        mShEditor.apply();
    }

    public boolean getConfigService(){
        initUserFile();

        return mShPref.getBoolean(Constants.CONFIG_SERVICE, false);
    }

    public void saveConfigMap(int b){
        initUserFile();

        mShEditor = mShPref.edit();
        mShEditor.putInt(Constants.MAP_TYPE, b);
        mShEditor.apply();
    }

    public int getConfigMap(){
        initUserFile();

        return mShPref.getInt(Constants.MAP_TYPE, 1);
    }

    public void saveDistConfig(int d){
        initUserFile();

        mShEditor = mShPref.edit();
        mShEditor.putInt(Constants.DISTANCE, d);
        mShEditor.apply();
    }

    public int getDistConfig(){
        initUserFile();

        return mShPref.getInt(Constants.DISTANCE, 2500);
    }
}
