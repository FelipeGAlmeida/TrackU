package com.fgapps.tracku.helper;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.LoginActivity;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.MapsActivity;
import com.fgapps.tracku.database.Authorization;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.model.Contact;
import com.fgapps.tracku.service.SyncDatabases;
import com.fgapps.tracku.sqlite.SQLDefs;

/**
 * Created by (Engenharia) Felipe on 28/03/2018.
 */

public class Dialogs {

    private static ProgressDialog progress;

    public static void showLoadingDialog(String title, String message,
                                         boolean cancelable){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            progress = new ProgressDialog(MainActivity.currentActivity);
            progress.setTitle(title);
            progress.setMessage(message);
            progress.setCancelable(cancelable);
            progress.show();
        }
    }

    public static void dismissLoadingDialog(Boolean finish_activity){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
            if (finish_activity) MainActivity.currentActivity.finish();
        }else{
            progress = null;
        }
    }

    public static void showPermissionDialog(){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Permissão negada");
            builder.setCancelable(false);
            builder.setMessage("Para utilizar o aplicativo é necessário aceitar as permissões.");
            builder.setPositiveButton("Ok, entendi", (dialogInterface, i) -> MainActivity.currentActivity.finish());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static void showNumberFormatDialog(final String prefix, final String number, final boolean isLogin){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Número incorreto");
            builder.setCancelable(false);
            builder.setMessage("Você pode ter digitado o número errado ou esquecido o \"9\" extra do número." +
                    " Por favor, revise o número:\n " + number.substring(0, 4) + "-" + number.substring(4, 8));
            builder.setPositiveButton("Esqueci do 9 extra", (dialogInterface, i) -> {
                String n = prefix + "9" + number;

                if (isLogin) {
                    LoginActivity.USERPHONE = n;

                    Dialogs.showLoadingDialog("Enviando SMS", "aguarde um instante...", false);
                    new Authorization(MainActivity.currentActivity).verifyUserPhoneNumber(n);
                } else {
                    Dialogs.showLoadingDialog("Aguarde", "Adicionando novo contato...", false);

                    RealtimeDatabase rtdb = new RealtimeDatabase();
                    rtdb.addContact(n, true);
                }
            });
            builder.setNegativeButton("Digitei errado", (dialogInterface, i) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static void showAskLocationDialog(final String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Obter localização do contato");
            builder.setCancelable(false);
            builder.setMessage(Utils.getNameFromPhone(phone) + " não permitiu à você acessar sua localização, deseja perdir essa permissão?");
            builder.setPositiveButton("Não", (dialogInterface, i) -> {
                if (MainActivity.currentActivity instanceof MapsActivity) {
                    Intent intent = new Intent(MainActivity.currentActivity, MainActivity.class);
                    MainActivity.currentActivity.startActivity(intent);
                }
            });
            builder.setNegativeButton("Sim", (dialogInterface, i) -> {
                RealtimeDatabase rtdb = new RealtimeDatabase();
                rtdb.askLocationUser(phone);
                showLoadingDialog("Aguardando permissão",
                        "Espere até que seu contato dê a permissão...",
                        true);
                new Handler().postDelayed(() -> {
                    if (Utils.wppInstalled())
                        showWppDialog(phone);
                    else showMessageDialog(phone);
                }, 2300);
                SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
                SQLDefs.requestPhone(db, phone);
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static void showEndLocationDialog(String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Permissão finalizada");
            builder.setCancelable(false);
            builder.setMessage("O usuário " + Utils.getNameFromPhone(phone) + " parou o compartilhamento de localização com você.");
            builder.setPositiveButton("Ok, entendi", (dialogInterface, i) -> {
                if (MainActivity.currentActivity instanceof MapsActivity) {
                    Intent intent = new Intent(MainActivity.currentActivity, MainActivity.class);
                    MainActivity.currentActivity.startActivity(intent);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static void showDeniedLocationDialog(String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Permissão negada");
            builder.setCancelable(false);
            builder.setMessage("O usuário " + Utils.getNameFromPhone(phone) + " negou o compartilhamento de localização com você.");
            builder.setPositiveButton("Ok, entendi", (dialogInterface, i) -> {
                if(!(MainActivity.currentActivity instanceof MainActivity)) {
                    Intent intent = new Intent(MainActivity.currentActivity, MainActivity.class);
                    MainActivity.currentActivity.startActivity(intent);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            Notification.dismissAll();
        }else{
            Notification.sendDeniedNotification(MainActivity.currentActivity, phone);
        }
    }

    public static void showAllowedLocationDialog(final String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Permissão aceita");
            builder.setCancelable(false);
            if(MainActivity.currentActivity instanceof MapsActivity){
                builder.setMessage("O usuário " + Utils.getNameFromPhone(phone) + " aceitou o compartilhamento de localização com você.\n");
                builder.setPositiveButton("Ok, Entendi", (dialogInterface, i) -> { });
            }else {
                builder.setMessage("O usuário " + Utils.getNameFromPhone(phone) + " aceitou o compartilhamento de localização com você.\n" +
                        "Deseja visualiza-lo no mapa agora?");
                builder.setPositiveButton("Não", (dialogInterface, i) -> { });
                builder.setNegativeButton("Sim", (dialogInterface, i) -> {
                    Intent intent = new Intent(MainActivity.currentActivity, MapsActivity.class);
                    intent.putExtra(Constants.NAME, Utils.getNameFromPhone(phone));
                    intent.putExtra(Constants.PHONE, phone);
                    MainActivity.currentActivity.startActivity(intent);
                });
            }
            AlertDialog dialog = builder.create();
            dialog.show();
            Notification.dismissAll();
        }else{
            Notification.sendAllowedNotification(MainActivity.currentActivity, phone);
        }
    }

    public static void showRequestLocationDialog(final String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Permitir acesso a localização");
            builder.setCancelable(false);
            builder.setMessage("O usuário " + Utils.getNameFromPhone(phone) + " pediu permissão para localizar você, deseja permitir?");
            builder.setPositiveButton("Não", (dialogInterface, i) -> {
                if (SyncDatabases.isOnline()) {
                    RealtimeDatabase rtdb = new RealtimeDatabase();
                    rtdb.denyLocation(phone);
                }
            });
            builder.setNegativeButton("Sim", (dialogInterface, i) -> {
                if (SyncDatabases.isOnline()) {
                    RealtimeDatabase rtdb = new RealtimeDatabase();
                    rtdb.allowLocation(phone);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            Notification.dismissAll();
        }
    }

    private static void showWppDialog(final String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Enviar mensagem pelo WhatsApp");
            builder.setCancelable(false);
            builder.setMessage("Para garantir que " + Utils.getNameFromPhone(phone) + " veja seu pedido, " +
                    "recomenda-se enviar uma mensagem avisando-o. Gostaria de enviar agora?");
            builder.setPositiveButton("Não", (dialogInterface, i) -> { });
            builder.setNegativeButton("Sim", (dialogInterface, i) -> Utils.sendWppMessage(phone));
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static void showWppDialog2(final String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Enviar mensagem pelo WhatsApp");
            builder.setCancelable(false);
            builder.setMessage(Utils.getNameFromPhone(phone) + " não possui o aplicativo TrackU instalado, " +
                    "Gostaria de enviar um convite para que instale o aplicativo?");
            builder.setPositiveButton("Não", (dialogInterface, i) -> { });
            builder.setNegativeButton("Sim", (dialogInterface, i) -> Utils.sendWppMessage2(phone));
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private static void showMessageDialog(final String phone){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("Avise o contato");
            builder.setCancelable(false);
            builder.setMessage("Para garantir que " + Utils.getNameFromPhone(phone) + " veja seu pedido, " +
                    "recomenda-se avisa-lo, então enviar uma mensagem ou ligar pode agilizar o processo.");
            builder.setPositiveButton("Ok, entendi", (dialogInterface, i) -> { });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static void showEditDialog(String name, final String phone){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);

        TextView title = new TextView(MainActivity.currentActivity);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(MainActivity.currentActivity.getResources().getString(R.string.change_name));
        builder.setCustomTitle(title);

        LinearLayout ll = new LinearLayout(MainActivity.currentActivity);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(60, 0, 60, 0);

        TextView message1 = new TextView(MainActivity.currentActivity);
        message1.setGravity(Gravity.CENTER_HORIZONTAL);
        message1.setText(MainActivity.currentActivity.getResources().getString(R.string.new_contact_name));
        ll.addView(message1);

        final EditText input = new EditText(MainActivity.currentActivity);
        input.setText(name);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        input.setTypeface(null, Typeface.BOLD);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(input, layoutParams);

        builder.setView(ll);

        builder.setPositiveButton("Salvar", (dialog, which) -> saveNewName(phone, input.getText().toString()));
        builder.setNeutralButton("Redefinir", (dialog, which) -> {
            if(SyncDatabases.isOnline())
                saveNewName(phone, "");
            else
                Toast.makeText(MainActivity.currentActivity, "Você precisa de internet para realizar esta ação", Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> { });

        builder.show();

    }

    private static void saveNewName(String phone, String newName){
        SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Constants.NAME, newName);
        SQLDefs.update(db, Constants.CONTACT, cv, Constants.PHONE, new String[]{phone});
        Contact c =  MainActivity.getContact(phone);
        if(c != null && newName != null) c.setName(newName);
    }

    public static void showGpsDialog(){
        if(MainActivity.currentActivity.getWindow().getDecorView().isShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.currentActivity);
            builder.setTitle("GPS desabilitado");
            builder.setCancelable(false);
            builder.setMessage("Não se esqueça de ligar o GPS para compartilhar sua localização.\n  " +
                    "Deseja ligar agora?");
            builder.setNegativeButton("Sim", (dialogInterface, i) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MainActivity.currentActivity.startActivity(intent);
            });
            builder.setPositiveButton("Não", (dialogInterface, i) -> { });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

}
