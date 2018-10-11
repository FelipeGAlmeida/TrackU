package com.fgapps.tracku.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.LoginActivity;

/**
 * Created by (Engenharia) Felipe on 09/04/2018.
 */

public class Notification {

    private static int id = 0;

    private static NotificationManager notificationManager;

    public static void sendIncommingNotification(Context c, String phone) {
        Intent intent = new Intent(c, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.PHONE, phone);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, id /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = c.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.app.Notification n = new android.app.Notification.Builder(c)
                        .setSmallIcon(R.drawable.logo_app)
                        .setContentTitle("Estão pedindo sua localização !")
                        .setContentText(Utils.getNameFromPhone(phone)+" está pedindo sua localização, entre na aplicação para permitir ou não")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .build();

        if(notificationManager == null)
            notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        if(notificationManager != null) {
            notificationManager.notify(id, n);
            id++;
        }
    }

    static void sendAllowedNotification(Context c, String phone) {
        Intent intent = new Intent(c, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.PHONE, phone);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, id /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = c.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.app.Notification n = new android.app.Notification.Builder(c)
                .setSmallIcon(R.drawable.logo_app)
                .setContentTitle("Pedido aceito !")
                .setContentText(Utils.getNameFromPhone(phone)+" permitiu o compartilhamento de localização, toque para localiza-lo")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .build();

        if(notificationManager == null)
            notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        if(notificationManager != null) {
            notificationManager.notify(id, n);
            id++;
        }
    }

    static void sendDeniedNotification(Context c, String phone) {
        Intent intent = new Intent(c, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.PHONE, phone);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, id /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = c.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.app.Notification n = new android.app.Notification.Builder(c)
                .setSmallIcon(R.drawable.logo_app)
                .setContentTitle("Pedido negado !")
                .setContentText(Utils.getNameFromPhone(phone)+" não permitiu o compartilhamento de localização")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .build();

        if(notificationManager == null)
            notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        if(notificationManager != null) {
            notificationManager.notify(id, n);
            id++;
        }
    }

    public static void dismissAll(){
        if(notificationManager != null) {
            notificationManager.cancelAll();
            id = 0;
        }
    }

    public static android.app.Notification getFixedNotification(Context c){
        Intent notificationIntent = new Intent(c, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 999, notificationIntent, 0);
        return new android.app.Notification.Builder(c)
                .setContentTitle("Compartilhando localização")
                .setContentText("Para parar, vá em Configurações")
                .setSmallIcon(R.drawable.logo_app)
                .setContentIntent(pendingIntent)
                .build();
    }

}
