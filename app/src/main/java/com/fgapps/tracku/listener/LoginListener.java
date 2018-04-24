package com.fgapps.tracku.listener;

import android.view.View;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.LoginActivity;
import com.fgapps.tracku.database.Authorization;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.service.SyncDatabases;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by (Engenharia) Felipe on 23/03/2018.
 */

public class LoginListener implements View.OnClickListener {

    private LoginActivity activity;

    public LoginListener(LoginActivity a) {
        activity = a;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCadastrar_id) {
            if (SyncDatabases.isOnline()) {
                if (view.getId() == R.id.btnCadastrar_id) {
                    if(!activity.hasPhoto()){
                        Toast.makeText(activity, "Insira uma foto de modo que outros usuários possam te identificar", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String name = activity.getEditUsername().getText().toString();
                    if (name.length() < 3) {
                        Toast.makeText(activity, "Insira seu nome de modo que outros usuários possam te identificar", Toast.LENGTH_LONG).show();
                        return;
                    }
                    LoginActivity.USERNAME = name;

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
                        Dialogs.showNumberFormatDialog(DDI + DDD, number, true);
                        return;
                    }

                    String phone = DDI + DDD + number;
                    LoginActivity.USERPHONE = phone;

                    Dialogs.showLoadingDialog("Enviando SMS", "aguarde um instante...", false);
                    new Authorization(activity).verifyUserPhoneNumber(phone);
                }
            } else {
                Toast.makeText(activity, "Você precisa de internet para realizar o login na aplicação", Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.photo_id){
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .setInitialCropWindowPaddingRatio(0)
                    .start(activity);
        }
    }
}
