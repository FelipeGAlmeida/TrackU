package com.fgapps.tracku.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.listener.ValidatorListener;

public class ValidatorActivity extends AppCompatActivity {

    private EditText editD1;
    private EditText editD2;
    private EditText editD3;
    private EditText editD4;
    private EditText editD5;
    private EditText editD6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validator);

        String id = "";
        Bundle b = getIntent().getExtras();
        if(b != null) {
            id = b.getString("id");
        }else{
            Toast.makeText(this, "Algo deu errado, tente novamente", Toast.LENGTH_LONG).show();
            finish();
        }

        ValidatorListener listener = new ValidatorListener(this, id);

        editD1 = findViewById(R.id.editD1_id);
        editD1.addTextChangedListener(listener);
        editD2 = findViewById(R.id.editD2_id);
        editD2.addTextChangedListener(listener);
        editD3 = findViewById(R.id.editD3_id);
        editD3.addTextChangedListener(listener);
        editD4 = findViewById(R.id.editD4_id);
        editD4.addTextChangedListener(listener);
        editD5 = findViewById(R.id.editD5_id);
        editD5.addTextChangedListener(listener);
        editD6 = findViewById(R.id.editD6_id);
        editD6.addTextChangedListener(listener);
        Button btnCancelar = findViewById(R.id.btnCancelar_id);
        btnCancelar.setOnClickListener(listener);
        Button btnLimpar = findViewById(R.id.btnLimpar_id);
        btnLimpar.setOnClickListener(listener);
    }

    //OVERRIDE

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dialogs.dismissLoadingDialog(false);
    }

    @Override
    protected void onResume() {
        MainActivity.currentActivity = this;
        super.onResume();
    }

    @Override
    public void onBackPressed() {
    }

    //GETTERS AND SETTERS
    
    public EditText getEditD1() {
        return editD1;
    }

    public EditText getEditD2() {
        return editD2;
    }

    public EditText getEditD3() {
        return editD3;
    }

    public EditText getEditD4() {
        return editD4;
    }

    public EditText getEditD5() {
        return editD5;
    }

    public EditText getEditD6() {
        return editD6;
    }
}
