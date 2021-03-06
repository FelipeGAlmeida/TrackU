package com.fgapps.tracku.listener;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.ConfigActivity;
import com.fgapps.tracku.service.SaveLoadService;

/**
 * Created by (Engenharia) Felipe on 06/04/2018.
 */

public class ConfigListener implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private ConfigActivity activity;

    public ConfigListener(ConfigActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        SaveLoadService sls = SaveLoadService.getInstance(activity);
        sls.saveConfigService(b);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnVoltarC_id){
            activity.finish();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        activity.getTxtDistancia().setText(activity.setDistanceText(i));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SaveLoadService sls = SaveLoadService.getInstance(activity);
        sls.saveDistConfig(seekBar.getProgress());
    }
}
