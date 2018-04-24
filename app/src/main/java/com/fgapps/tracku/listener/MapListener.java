package com.fgapps.tracku.listener;

import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.MapsActivity;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.service.LocationService;
import com.fgapps.tracku.service.SaveLoadService;
import com.fgapps.tracku.service.SyncDatabases;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by (Engenharia) Felipe on 03/04/2018.
 */

public class MapListener implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private MapsActivity activity;
    private RealtimeDatabase rtdb;

    public MapListener(MapsActivity activity) {
        this.activity = activity;
        rtdb = new RealtimeDatabase();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnMode_id) {
            if (activity.getMode() == 0) { //Tracking contact
                activity.getBtnMode().setImageResource(R.drawable.contact_icon);
                Toast.makeText(activity, "Mostrando sua localização", Toast.LENGTH_LONG).show();
                activity.setMode(1);
                String[] loc = LocationService.getSeparatedLocation();
                if (loc != null) activity.updateMap(loc[0], loc[1], true);
                else
                    Toast.makeText(activity, "Verifique se sua localização está ativada", Toast.LENGTH_LONG).show();
            } else { //tracking user
                activity.getBtnMode().setImageResource(R.drawable.user_icon);
                Toast.makeText(activity, "Mostrando localização do contato", Toast.LENGTH_LONG).show();
                activity.setMode(0);
                String[] loc = activity.getSeparatedLocation();
                if (loc != null) activity.updateMap(loc[0], loc[1], false);
            }
        }else if(view.getId() == R.id.btnType_id) {
            SaveLoadService sls = new SaveLoadService(activity);
            if (activity.getType() == GoogleMap.MAP_TYPE_HYBRID) {
                activity.setType(GoogleMap.MAP_TYPE_NORMAL);
                activity.getBtnType().setImageResource(R.drawable.satellite_icon);
                sls.saveConfigMap(GoogleMap.MAP_TYPE_NORMAL);
            } else {
                activity.setType(GoogleMap.MAP_TYPE_HYBRID);
                activity.getBtnType().setImageResource(R.drawable.normal_icon);
                sls.saveConfigMap(GoogleMap.MAP_TYPE_HYBRID);
            }
        }else if(view.getId()==R.id.btnZoom_id){
            activity.setZoom(17);
        }else if(view.getId()== R.id.voltarMap_id){
            if(SyncDatabases.isOnline())
                rtdb.stopGettingLocation(activity.getPhone());
            activity.setMode(0);
            Intent i = new Intent(activity, MainActivity.class);
            activity.startActivity(i);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        activity.setZoom(i+11);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
