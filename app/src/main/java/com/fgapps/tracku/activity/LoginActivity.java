package com.fgapps.tracku.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fgapps.tracku.R;
import com.fgapps.tracku.database.Authorization;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.helper.Notification;
import com.fgapps.tracku.listener.LoginListener;
import com.fgapps.tracku.sqlite.SQLDatabase;
import com.fgapps.tracku.sqlite.SQLDefs;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.firebase.auth.FirebaseUser;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class LoginActivity extends AppCompatActivity {

    public static String USERNAME;
    public static String USERPHONE;
    public static String USERCODE;
    private static Bitmap bmpPhoto;
    private static SQLDatabase db_initializer;

    private static final int SMS_REQUEST_CODE = 0;
    private static final int GPS_REQUEST_CODE = 1;
    private static final int RW_REQUEST_CODE = 2;

    private EditText editDDI;
    private EditText editDDD;
    private EditText editNumber;
    private EditText editUsername;
    private Button btnCadastrar;
    private ImageView imgPhoto;

    private LoginListener listener;

    private boolean hasPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        hasPhoto = false;
        Notification.dismissAll();

        if(!checkSMSPermission(this)) {
            try {
                requestPermissionForSendSMS(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(!checkGPSPermission(this)){
            try {
                requestPermissionForUseGPS(this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(!checkRWPermission(this)){
            try {
                requestPermissionForRW(this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        db_initializer = new SQLDatabase(this);
        db_initializer.onCreate(db_initializer.getReadableDatabase());

        FirebaseUser u = Authorization.getFirebaseAuth().getCurrentUser();
        if(u != null && !u.getPhoneNumber().isEmpty()){

            SQLiteDatabase db = db_initializer.getWritableDatabase();
            String[] fields = {
                    SQLDefs.User_Table.COLUMN_UID,
                    SQLDefs.User_Table.COLUMN_NAME,
                    SQLDefs.User_Table.COLUMN_PHONE
            };
            Cursor c = SQLDefs.select(db,Constants.USER,fields, null, null, null);
            c.moveToFirst();
            if(c.getCount()>0) {
                USERNAME = c.getString(c.getColumnIndexOrThrow(SQLDefs.User_Table.COLUMN_NAME));
                USERPHONE = c.getString(c.getColumnIndexOrThrow(SQLDefs.User_Table.COLUMN_PHONE));
                USERCODE = c.getString(c.getColumnIndexOrThrow(SQLDefs.User_Table.COLUMN_UID));
            }
            c.close();

            if(u.getUid().equals(USERCODE)) {
                Intent intent = new Intent(this, MainActivity.class);
                Bundle b = getIntent().getExtras();
                if(b!=null){
                    intent.putExtra(Constants.PHONE, b.getString(Constants.PHONE));
                }
                startActivity(intent);
            }
        }

        listener = new LoginListener(this);

        editDDI = findViewById(R.id.editUserDDI_id);
        editDDD = findViewById(R.id.editUserDDD_id);
        editNumber = findViewById(R.id.editUserNumber_id);
        editUsername = findViewById(R.id.editUsername_id);
        btnCadastrar = findViewById(R.id.btnCadastrar_id);
        btnCadastrar.setOnClickListener(listener);
        imgPhoto = findViewById(R.id.photo_id);
        imgPhoto.setOnClickListener(listener);

        SimpleMaskFormatter mask_DDI = new SimpleMaskFormatter("+NN");
        SimpleMaskFormatter mask_DDD = new SimpleMaskFormatter("NN");
        SimpleMaskFormatter mask_phone = new SimpleMaskFormatter("N NNNN-NNNN");

        MaskTextWatcher mtw_DDI = new MaskTextWatcher(editDDI, mask_DDI);
        MaskTextWatcher mtw_DDD = new MaskTextWatcher(editDDD, mask_DDD);
        MaskTextWatcher mtw_phone = new MaskTextWatcher(editNumber, mask_phone);

        editDDI.addTextChangedListener(mtw_DDI);
        editDDD.addTextChangedListener(mtw_DDD);
        editNumber.addTextChangedListener(mtw_phone);
        if(USERNAME != null && !USERNAME.isEmpty()) editUsername.setText(USERNAME);
    }

    public static boolean checkSMSPermission(Context c){
        if(Build.VERSION.SDK_INT >= 23){
            int result = c.checkSelfPermission(Manifest.permission.SEND_SMS);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public static boolean checkGPSPermission(Context c){
        if(Build.VERSION.SDK_INT >= 23){
            int result = c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public static boolean checkRWPermission(Context c){
        if(Build.VERSION.SDK_INT >= 23){
            int result = c.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public static void requestPermissionForSendSMS(Activity a) throws Exception {
        try {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.SEND_SMS},
                    SMS_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void requestPermissionForUseGPS(Activity a) throws Exception {
        try {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    GPS_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void requestPermissionForRW(Activity a) throws Exception {
        try {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    RW_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //OVERRIDE

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == 0){
            if(requestCode == SMS_REQUEST_CODE){
                if(!checkGPSPermission(this)) {
                    try {
                        requestPermissionForUseGPS(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else if(requestCode == GPS_REQUEST_CODE){
                if(!checkRWPermission(this)) {
                    try {
                        requestPermissionForRW(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            Dialogs.showPermissionDialog();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if (reqCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri uri = result.getUri();
                InputStream is = null;
                try {
                    is = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bmpPhoto = BitmapFactory.decodeStream(is);
                bmpPhoto = Bitmap.createScaledBitmap(bmpPhoto,100, 100, false);
                imgPhoto.setImageBitmap(bmpPhoto);
                hasPhoto = true;
            }
        }else {
            Toast.makeText(this, "A foto é obrigatória para a conclusão do cadastro," +
                    " escolha uma, você poderá trocar depois",Toast.LENGTH_LONG).show();
        }
    }

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

    public EditText getEditUsername() {
        return editUsername;
    }

    public boolean hasPhoto(){
        return hasPhoto;
    }

    public static Bitmap getBmpPhoto() {
            return bmpPhoto;
    }

    public static SQLDatabase getDb_initializer() {
        return db_initializer;
    }

}
