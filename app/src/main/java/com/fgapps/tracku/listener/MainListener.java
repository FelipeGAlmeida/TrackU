package com.fgapps.tracku.listener;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.ConfigActivity;
import com.fgapps.tracku.activity.ContactActivity;
import com.fgapps.tracku.activity.LoginActivity;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.MapsActivity;
import com.fgapps.tracku.database.Authorization;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.database.StorageDatabase;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.model.Contact;
import com.fgapps.tracku.helper.ListListener;
import com.fgapps.tracku.service.SaveLoadService;
import com.fgapps.tracku.service.SyncDatabases;
import com.fgapps.tracku.sqlite.SQLDefs;

import java.util.HashMap;

/**
 * Created by (Engenharia) Felipe on 27/03/2018.
 */

public class MainListener implements ListListener, View.OnClickListener {

    private MainActivity activity;
    private HashMap<Integer, Drawable> backs;


    @SuppressLint("UseSparseArrays")
    public MainListener(MainActivity activity) {
        this.activity = activity;
        this.backs = new HashMap<>();
    }

    @Override //Lista
    public void OnClick(View v, int pos) {
        if(MainActivity.getSelected().isEmpty()){

            String phone = ((TextView)v.findViewById(R.id.thephone_id)).getText().toString();
            String name = ((TextView)v.findViewById(R.id.name_id)).getText().toString();
            String location = ((TextView)v.findViewById(R.id.location_id)).getText().toString();

            Intent intent = new Intent(activity, MapsActivity.class);
            intent.putExtra(Constants.PHONE, phone);
            intent.putExtra(Constants.NAME, name);
            intent.putExtra(Constants.LOCATION, location);

            activity.startActivity(intent);

            selectionControl(false);
        }else if(!MainActivity.getSelected().containsKey(pos)) {
            backs.put(pos,v.findViewById(R.id.modelLayout_id).getBackground());
            v.findViewById(R.id.modelLayout_id).setBackgroundResource(R.drawable.list_selected_back);
            MainActivity.getSelected().put(pos,(CardView)v);
            buttonsControl();
        }else{
            v.findViewById(R.id.modelLayout_id).setBackground(backs.get(pos));
            MainActivity.getSelected().remove(pos);
            buttonsControl();
        }
    }

    @Override
    public void OnLongClick(View v, int pos) {
        if(!MainActivity.getSelected().containsKey(pos)) {
            backs.put(pos,v.findViewById(R.id.modelLayout_id).getBackground());
            v.findViewById(R.id.modelLayout_id).setBackgroundResource(R.drawable.list_selected_back);
            MainActivity.getSelected().put(pos,(CardView)v);
            buttonsControl();
        }else{
            v.findViewById(R.id.modelLayout_id).setBackground(backs.get(pos));
            MainActivity.getSelected().remove(pos);
            buttonsControl();
        }
    }

    @Override //Botões
    public void onClick(View view) {
        if(view.getId() == R.id.fabAdd_id){
            if(SyncDatabases.isOnline()) {
                Intent intent = new Intent(activity.getApplicationContext(), ContactActivity.class);
                selectionControl(false);
                activity.startActivity(intent);
            }else{
                Toast.makeText(activity, "Você precisa de internet para realizar esta ação", Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.delete_id) {
            if(MainActivity.getSelected().isEmpty()){
                Toast.makeText(activity, "Selecione ao menos um contato para deletar", Toast.LENGTH_LONG).show();
                return;
            }
            selectionControl(true);
            activity.getContactsFromDb(true);
            activity.getAdapter().notifyDataSetChanged();
        }else if(view.getId() == R.id.fabStop_id){
            if(SyncDatabases.isOnline()){
                RealtimeDatabase rtdb = new RealtimeDatabase();
                rtdb.denyAllLocation();
                SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
                SQLDefs.denyAllPhone(db);
                Toast.makeText(activity, "Sua localização está privada. Todas as permissões concedidas foram revogadas",
                        Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(activity, "Você precisa de internet para realizar esta ação", Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.logoff_id){
            Authorization.getFirebaseAuth().signOut();

            SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
            db.execSQL(SQLDefs.SQL_DELETE_USER);
            db.execSQL(SQLDefs.SQL_DELETE_CONTACT);

            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
        }else if(view.getId() == R.id.settings_id){
            Intent intent = new Intent(activity, ConfigActivity.class);
            activity.startActivity(intent);
        }else if(view.getId() == R.id.editName_id){
            CardView c = null;
            for (Integer i : MainActivity.getSelected().keySet())
                c = MainActivity.getSelected().get(i);
            if(c!=null) {
                String phone = ((TextView) c.findViewById(R.id.thephone_id)).getText().toString();
                String name = ((TextView) c.findViewById(R.id.name_id)).getText().toString();
                Dialogs.showEditDialog(name, phone);
            }
            selectionControl(false);
            buttonsControl();
        }
    }

    private void buttonsControl(){
        if(MainActivity.getSelected().isEmpty()){
            activity.getBtnEditName().setVisibility(View.INVISIBLE);
            activity.getBtnDelete().setVisibility(View.INVISIBLE);
        }else if(MainActivity.getSelected().size()>1){
            activity.getBtnEditName().setVisibility(View.INVISIBLE);
            activity.getBtnDelete().setVisibility(View.VISIBLE);
        }else{
            activity.getBtnEditName().setVisibility(View.VISIBLE);
            activity.getBtnDelete().setVisibility(View.VISIBLE);
        }
    }

    private void selectionControl(boolean delete){
        for (Integer i : MainActivity.getSelected().keySet()) {
            CardView c = MainActivity.getSelected().get(i);
            String phone = ((TextView)c.findViewById(R.id.thephone_id)).getText().toString();
            if(delete) {
                RealtimeDatabase rtdb = new RealtimeDatabase();
                Contact rmv = MainActivity.getContact(phone);
                MainActivity.getContacts().remove(rmv);
                if(SyncDatabases.isOnline())
                    rtdb.deleteContact(phone);
                else{
                    SyncDatabases.contacts2Delete.add(rmv);
                    SaveLoadService.getInstance(activity).saveDeletedList(phone);
                }
                SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
                SQLDefs.deleteContact(db, phone);
                StorageDatabase sd = new StorageDatabase();
                if(sd.deleteImage(phone) && rmv != null)
                    Toast.makeText(activity.getApplicationContext(), rmv.getName()+" removido com sucesso!", Toast.LENGTH_LONG).show();
            }
        }
        MainActivity.getSelected().clear();
    }
}
