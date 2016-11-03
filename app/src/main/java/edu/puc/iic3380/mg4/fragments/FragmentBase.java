package edu.puc.iic3380.mg4.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.model.User;

import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USERS;
import static edu.puc.iic3380.mg4.util.Constantes.USER_PHONE_NUMBER;

/**
 * Created by quelves on 22/10/2016.
 */

public class FragmentBase extends Fragment {
    public static final String TAG = "FragmentBase";

    protected FirebaseDatabase mFirebaseDatabase;
    protected DatabaseReference userContactsRef;
    protected DatabaseReference usersRef;
    protected User user;

    protected static final String ARG_PHONE = "PHONEKEY";
    protected String phoneKey;


    protected void doAction(Fragment fragment, String TAG, Context context) {
        if (fragment != null) {

            FragmentManager fm = getFragmentManager();


            FragmentTransaction transaction = fm.beginTransaction();

            if (fragment != null) {
                // If fragment is already added, replace it.
                if (fm.findFragmentByTag(TAG) != null) {
                    transaction = transaction.replace(R.id.content_navigation,
                            fragment, TAG);
                } else {
                    transaction = transaction.add(edu.puc.iic3380.mg4.R.id.content_navigation,
                            fragment, TAG);
                }
                transaction.commit();
            }
        } else {
            Log.d(TAG, "Frament is null");
        }
    }

    protected void hide(Fragment fragment, String TAG) {
        if (fragment != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Log.d(TAG, "Hide Fragment");
            transaction.hide(fragment);
            transaction.commit();
        } else {
            Log.d(TAG, "Frament is null");
        }
    }
    protected void show(Fragment fragment, String TAG) {
        if (fragment != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Log.d(TAG, "Hide Fragment");
            transaction.show(fragment);
            transaction.commit();
        } else {
            Log.d(TAG, "Frament is null");
        }
    }

    protected String getPhoneKey() {
        if (phoneKey == null) {

            phoneKey = USER_PHONE_NUMBER;

        }

        return phoneKey;

    }



}
