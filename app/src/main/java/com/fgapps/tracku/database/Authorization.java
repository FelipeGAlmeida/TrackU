package com.fgapps.tracku.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.fgapps.tracku.activity.LoginActivity;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.ValidatorActivity;
import com.fgapps.tracku.helper.Constants;
import com.fgapps.tracku.helper.Dialogs;
import com.fgapps.tracku.sqlite.SQLDefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.FirebaseException;

import java.util.concurrent.TimeUnit;

/**
 * Created by (Engenharia) Felipe on 23/03/2018.
 */

public class Authorization {

    private static FirebaseAuth mAuth;

    private Activity activity;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    public Authorization(Activity a) {
        getFirebaseAuth();
        mAuth.setLanguageCode("pt");
        activity = a;
    }

    public static FirebaseAuth getFirebaseAuth(){
        if (mAuth == null)
            mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        return mAuth;
    }


    public void verifyUserPhoneNumber(String phoneNumber){

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                Dialogs.dismissLoadingDialog(false);
                Toast.makeText(activity, "Um erro ocorreu, SMS não enviado, tente novamente", Toast.LENGTH_LONG).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                Intent intent = new Intent(activity.getApplicationContext(), ValidatorActivity.class);
                intent.putExtra("id", verificationId);
                activity.startActivity(intent);

                // Save verification ID and resending token so we can use them later
                //mVerificationId = verificationId;
                //mResendToken = token;

                // ...
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = task.getResult().getUser();
                            LoginActivity.USERCODE = user.getUid();

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
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void disconectUser(){
        FirebaseAuth.getInstance().signOut();
    }
}
