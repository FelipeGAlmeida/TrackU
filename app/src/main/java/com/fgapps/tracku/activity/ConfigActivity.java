package com.fgapps.tracku.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.Button;
import android.widget.Switch;

import com.fgapps.tracku.R;
import com.fgapps.tracku.listener.ConfigListener;
import com.fgapps.tracku.service.SaveLoadService;

public class ConfigActivity extends AppCompatActivity {

    private Switch swtKill;
    private Button btnVoltar;

    private ConfigListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        listener = new ConfigListener(this);

        swtKill = findViewById(R.id.kill_service_id);
        swtKill.setOnCheckedChangeListener(listener);
        SaveLoadService sls = SaveLoadService.getInstance(this);
        swtKill.setChecked(sls.getConfigService());

        btnVoltar = findViewById(R.id.btnVoltarC_id);
        btnVoltar.setOnClickListener(listener);
    }

    //OVERRIDE

    @Override
    protected void onResume() {
        MainActivity.currentActivity = this;
        super.onResume();
    }
}
