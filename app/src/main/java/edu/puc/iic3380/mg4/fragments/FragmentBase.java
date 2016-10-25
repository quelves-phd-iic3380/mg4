package edu.puc.iic3380.mg4.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import edu.puc.iic3380.mg4.R;

/**
 * Created by quelves on 22/10/2016.
 */

public class FragmentBase extends Fragment {
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

}
