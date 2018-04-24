package com.fgapps.tracku.listener;

import android.view.View;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.ContactActivity;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.service.SyncDatabases;

/**
 * Created by (Engenharia) Felipe on 27/03/2018.
 */

public class ContactListener implements View.OnClickListener{

    ContactActivity activity;

    public ContactListener(ContactActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.adicionar_id){
            if(SyncDatabases.isOnline()) {
                String DDI = activity.getEditDDI().getText().toString();
                if (DDI.length() < 3) {
                    Toast.makeText(activity, "Revise o código do país (DDI), deve estar no formato +##", Toast.LENGTH_LONG).show();
                    return;
                }
                String DDD = activity.getEditDDD().getText().toString();
                if (DDD.length() < 2) {
                    Toast.makeText(activity, "Revise o código de área (DDD), deve estar no formato ##", Toast.LENGTH_LONG).show();
                    return;
                }

                String number = activity.getEditNumber().getText().toString().replace("-", "")
                        .replace(" ", "");
                if (number.length() < 9) {
                    if (number.length() < 8) {
                        Toast.makeText(activity, "Revise o número de telefone, deve estar no formato # ####-####", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Dialogs.showNumberFormatDialog(DDI + DDD, number, false);
                    return;
                }

                String phone = DDI + DDD + number;

                Dialogs.showLoadingDialog("Aguarde", "Adicionando novo contato...", false);

                RealtimeDatabase rtdb = new RealtimeDatabase();
                rtdb.addContact(phone, true);
            }else{
                Toast.makeText(activity, "Você precisa de internet para realizar esta ação",Toast.LENGTH_LONG).show();
            }

        }else if(view.getId() == R.id.cancelar_id){
            activity.finish();
        }
    }

}
