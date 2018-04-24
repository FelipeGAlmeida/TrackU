package com.fgapps.tracku.database;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.helper.Constants;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;


/**
 * Created by (Engenharia) Felipe on 23/03/2018.
 */

public class StorageDatabase {

    private static StorageReference mDb;

    public StorageDatabase() {
        this.getInstance();
    }

    public static StorageReference getInstance(){
        if(mDb == null)
            mDb = FirebaseStorage.getInstance().getReference();
        return mDb;
    }

    public void uploadPhoto(Bitmap photo, String phone) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataimg = baos.toByteArray();

        StorageReference ref0 = mDb.child(Constants.PHOTO).child(phone);
        ref0.putBytes(dataimg);
    }

    public void downloadPhoto(String phone){
        StorageReference ref0 = mDb.child(Constants.PHOTO).child(phone);

        File localFile = getOutputMediaFile(phone);
        if(localFile!=null) {
            ref0.getFile(localFile);
        }
    }

    public Bitmap loadPhoto(String phone) {
        File pictureFile = getOutputMediaFile(phone);
        Bitmap myBitmap = null;
        if(pictureFile != null && pictureFile.exists()){
            myBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
        }
        return myBitmap;
    }

    public void deleteImage(String phone){
        StorageReference ref0 = mDb.child(Constants.PHOTO).child(phone);
        ref0.delete();
        File f = getOutputMediaFile(phone);
        if(f != null && f.exists())
            f.delete();
    }

    private File getOutputMediaFile(String photo){
        //O arquivo será salvo em Android > data > {Package} > Files
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MainActivity.currentActivity.getApplicationContext().getPackageName()
                + "/Files");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String mImageName=photo+".jpg";
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

}
