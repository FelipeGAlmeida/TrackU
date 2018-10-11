package com.fgapps.tracku.helper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.service.DatabaseService;
import com.fgapps.tracku.sqlite.SQLDatabase;
import com.fgapps.tracku.sqlite.SQLDefs;

import java.net.URLEncoder;
import java.util.Calendar;

/**
 * Created by (Engenharia) Felipe on 28/03/2018.
 */

public class Utils {

    public static String getFormattedDateTime(){
        Calendar calendar = Calendar.getInstance();

        String time;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        time = formatField(day)+"/"+formatField(month)+"/"+year
                +" às "+formatField(hour)+":"+formatField(minute);
        return time;
    }

    private static String formatField(int field){
        if(field<10) return "0"+field;
        else return ""+field;
    }

    public static String unAcent(String s){
        s = s.replaceAll("[èéêë]","e");
        s = s.replaceAll("[ûùú]","u");
        s = s.replaceAll("[ïîí]","i");
        s = s.replaceAll("[àâãá]","a");
        s = s.replaceAll("[ô,ó]","o");

        s = s.replaceAll("[ÈÉÊË]","E");
        s = s.replaceAll("[ÛÙÚ]","U");
        s = s.replaceAll("[ÏÎÍ]","I");
        s = s.replaceAll("[ÀÂÃÁ]","A");
        s = s.replaceAll("ÔÓ","O");
        return s;
    }

    static String getNameFromPhone(String phone){
        SQLDatabase db_initializer = new SQLDatabase(DatabaseService.getDatabaseService());
        SQLiteDatabase db = db_initializer.getReadableDatabase();
        String[] fields = {
                SQLDefs.Contact_Table.COLUMN_NAME
        };
        Cursor c = SQLDefs.select(db,Constants.CONTACT,fields, Constants.PHONE, new String[]{phone}, null);
        c.moveToFirst();
        if(c.getCount()>0) {
            return c.getString(c.getColumnIndexOrThrow(SQLDefs.Contact_Table.COLUMN_NAME));
        }
        c.close();
        return phone;
    }

    static void sendWppMessage(String phone){
        try {
            PackageManager packageManager = MainActivity.currentActivity.getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);

            String url = "https://api.whatsapp.com/send?phone="+ phone +
                    "&text=" + URLEncoder.encode("Pedi que compartilhasse sua localização comigo via " +
                    "TrackU, Clique abaixo ou entre no aplicativo agora para ver meu pedido.\n\n" +
                    "https://tracku.com/request\n" +
                    "_(se perguntado, selecione o *TrackU*)_\n\n" +
                    "Caso não possua o aplicativo, faça download pelo link abaixo:\n" +
                    "https://tracku-fgapps.firebaseapp.com/\n\n" +
                    "Obrigado.", "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                MainActivity.currentActivity.startActivity(i);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    static void sendWppMessage2(String phone){
        try {
            PackageManager packageManager = MainActivity.currentActivity.getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);

            String url = "https://api.whatsapp.com/send?phone="+ phone +
                    "&text=" + URLEncoder.encode("Olá, gostaria de te adicionar como contato no *TrackU*.\n\n" +
                    "Por favor faça download e instale o aplicativo pelo link abaixo:\n" +
                    "https://tracku-fgapps.firebaseapp.com/\n\n" +
                    "Obrigado.", "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                MainActivity.currentActivity.startActivity(i);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    static boolean wppInstalled() {
        PackageManager pm = MainActivity.currentActivity.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}
