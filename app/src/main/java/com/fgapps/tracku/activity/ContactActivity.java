package com.fgapps.tracku.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.fgapps.tracku.R;
import com.fgapps.tracku.listener.ContactListener;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

public class ContactActivity extends AppCompatActivity {

    private EditText editDDI;
    private EditText editDDD;
    private EditText editNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ContactListener listener = new ContactListener(this);

        editDDI = findViewById(R.id.editContactDDI_id);
        editDDD = findViewById(R.id.editContactDDD_id);
        editNumber = findViewById(R.id.editContactNumber_id);
        Button btnAdcionar = findViewById(R.id.adicionar_id);
        Button btnCancelar = findViewById(R.id.cancelar_id);

        SimpleMaskFormatter mask_DDI = new SimpleMaskFormatter("+NN");
        SimpleMaskFormatter mask_DDD = new SimpleMaskFormatter("NN");
        SimpleMaskFormatter mask_phone = new SimpleMaskFormatter("N NNNN-NNNN");

        MaskTextWatcher mtw_DDI = new MaskTextWatcher(editDDI, mask_DDI);
        MaskTextWatcher mtw_DDD = new MaskTextWatcher(editDDD, mask_DDD);
        MaskTextWatcher mtw_phone = new MaskTextWatcher(editNumber, mask_phone);

        editDDI.addTextChangedListener(mtw_DDI);
        editDDD.addTextChangedListener(mtw_DDD);
        editNumber.addTextChangedListener(mtw_phone);

        btnAdcionar.setOnClickListener(listener);
        btnCancelar.setOnClickListener(listener);
        editDDD.requestFocus();
    }

    //OVERRIDE

    @Override
    protected void onResume() {
        MainActivity.currentActivity = this;
        super.onResume();
    }

    //GETTERS AND SETTERS

    public EditText getEditDDI() {
        return editDDI;
    }

    public EditText getEditDDD() {
        return editDDD;
    }

    public EditText getEditNumber() {
        return editNumber;
    }
}
