package com.fgapps.tracku.listener;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.LoginActivity;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.ValidatorActivity;
import com.fgapps.tracku.database.Authorization;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.database.StorageDatabase;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.service.SyncDatabases;
import com.fgapps.tracku.sqlite.SQLDefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

/**
 * Created by (Engenharia) Felipe on 23/03/2018.
 */

public class ValidatorListener implements TextWatcher, View.OnClickListener {

    ValidatorActivity activity;

    String verificationId;

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCancelar_id) {
            Dialogs.dismissLoadingDialog(true);
            Intent i = new Intent(activity, LoginActivity.class);
            activity.startActivity(i);
        }else if(view.getId() == R.id.btnLimpar_id){
            clearFields();
        }
    }

    public ValidatorListener(ValidatorActivity a, String id) {
        activity = a;
        verificationId = id;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {

        View focused = activity.getCurrentFocus();
        switch (focused.getId()){
            case R.id.editD1_id: activity.getEditD2().requestFocus();
                break;
            case R.id.editD2_id: activity.getEditD3().requestFocus();
                break;
            case R.id.editD3_id: activity.getEditD4().requestFocus();
                break;
            case R.id.editD4_id: activity.getEditD5().requestFocus();
                break;
            case R.id.editD5_id: activity.getEditD6().requestFocus();
                break;
            case R.id.editD6_id: {
                if(SyncDatabases.isOnline()) {
                    String code = activity.getEditD1().getText().toString();
                    code += activity.getEditD2().getText().toString();
                    code += activity.getEditD3().getText().toString();
                    code += activity.getEditD4().getText().toString();
                    code += activity.getEditD5().getText().toString();
                    code += activity.getEditD6().getText().toString();

                    if(code.length()==6) {
                        Dialogs.showLoadingDialog("Finalizando cadastro","Aguarde um pouco...", false);
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                        signInWithPhoneAuthCredential(credential);
                    }
                }else{
                    Toast.makeText(activity, "Você precisa de internet para realizar esta ação", Toast.LENGTH_LONG).show();
                    clearFields();
                }
            }
                break;
            default:
                Toast.makeText(activity, "EA: CHAPOU JORDAN", Toast.LENGTH_LONG).show();
        }
    }

    public void clearFields() {
        activity.getEditD1().setText("");
        activity.getEditD2().setText("");
        activity.getEditD3().setText("");
        activity.getEditD4().setText("");
        activity.getEditD5().setText("");
        activity.getEditD6().setText("");
        activity.getEditD1().requestFocus();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Dialogs.dismissLoadingDialog(false);
                Toast.makeText(activity, "Caso o código não esteja funcionando, toque no botão " +
                        "'Cancelar' abaixo e tente novamente", Toast.LENGTH_LONG).show();
                clearFields();
            }
        }, 8000);
        Authorization.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            h.removeCallbacksAndMessages(null);

                            FirebaseUser user = task.getResult().getUser();
                            LoginActivity.USERCODE = user.getUid();

//                            SaveLoadService.getInstance(activity).saveUserData(
//                                    LoginActivity.USERNAME,
//                                    LoginActivity.USERPHONE,
//                                    LoginActivity.USERCODE
//                            );

                            SQLiteDatabase db = LoginActivity.getDb_initializer().getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put(SQLDefs.User_Table.COLUMN_NAME, LoginActivity.USERNAME);
                            values.put(SQLDefs.User_Table.COLUMN_PHONE, LoginActivity.USERPHONE);
                            values.put(SQLDefs.User_Table.COLUMN_UID, LoginActivity.USERCODE);
                            SQLDefs.insert(db, Constants.USER, values);

                            RealtimeDatabase rtdb = new RealtimeDatabase();
                            rtdb.addUser(LoginActivity.USERNAME, LoginActivity.USERPHONE, LoginActivity.USERCODE);
                            StorageDatabase st = new StorageDatabase();
                            st.uploadPhoto(LoginActivity.getBmpPhoto(), LoginActivity.USERPHONE);

                            Dialogs.dismissLoadingDialog(true);

                            Intent intent = new Intent(activity, MainActivity.class);
                            activity.startActivity(intent);

                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Dialogs.dismissLoadingDialog(false);
                            h.removeCallbacksAndMessages(null);
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(activity, "O código não pôde ser verificado com sucesso, " +
                                        "verifique o código e tente novamente", Toast.LENGTH_LONG).show();
                                clearFields();
                            }
                        }
                    }
                });
    }
}
