package com.fgapps.tracku.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.fgapps.tracku.R;
import com.fgapps.tracku.listener.ConfigListener;
import com.fgapps.tracku.service.SaveLoadService;

import java.util.Locale;

public class ConfigActivity extends AppCompatActivity {

    private Switch swtKill;
    private Button btnDw;
    private Button btnUp;
    private Button btnVoltar;
    private SeekBar skbDistancia;
    private TextView txtDistancia;

    private ConfigListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        listener = new ConfigListener(this);

        SaveLoadService sls = SaveLoadService.getInstance(this);

        swtKill = findViewById(R.id.kill_service_id);
        swtKill.setOnCheckedChangeListener(listener);
        swtKill.setChecked(sls.getConfigService());

        skbDistancia = findViewById(R.id.skbDistancia);
        skbDistancia.incrementProgressBy(10);
        skbDistancia.setProgress(sls.getDistConfig());
        skbDistancia.setOnSeekBarChangeListener(listener);

        btnDw = findViewById(R.id.btnDistDown);
        btnDw.setOnClickListener( (view) -> setDistance(false));

        btnUp = findViewById(R.id.btnDistUp);
        btnUp.setOnClickListener( (view) -> setDistance(true));

        txtDistancia = findViewById(R.id.txtDistancia);
        txtDistancia.setText(setDistanceText(sls.getDistConfig()));

        btnVoltar = findViewById(R.id.btnVoltarC_id);
        btnVoltar.setOnClickListener(listener);
    }

    private void setDistance(boolean up){
        int dist = skbDistancia.getProgress();

        if(up) {
            if (dist >= 1000) dist = dist+100;
            else if (dist >= 10) dist++;
            else dist = 10;
        }else{
            if (dist >= 1100) dist = dist-100;
            else if(dist > 1000) dist = 1000;
            else if (dist > 10) dist--;
            else dist = 0;
        }

        skbDistancia.setProgress(dist);

        SaveLoadService sls = SaveLoadService.getInstance(this);
        sls.saveDistConfig(dist);
    }

    public String setDistanceText(int dist){
        if(dist<10)
            return "Enviar a cada atualização de posição";
        else if(dist<1000)
            return "Enviar a cada "+dist+" metros";
        return "Enviar a cada "+ String.format(Locale.getDefault(),
                "%.1f", dist/1000f) +" quilômetros";
    }

    //OVERRIDE
    @Override
    protected void onResume() {
        MainActivity.currentActivity = this;
        super.onResume();
    }

    //GET AND SETTER
    public TextView getTxtDistancia() {
        return txtDistancia;
    }
}
