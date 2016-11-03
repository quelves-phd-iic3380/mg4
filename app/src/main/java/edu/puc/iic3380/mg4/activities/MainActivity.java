package edu.puc.iic3380.mg4.activities;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.fragments.ContactsFragment;
import edu.puc.iic3380.mg4.model.ChatSettings;
import edu.puc.iic3380.mg4.model.Contact;

public class MainActivity extends AppCompatActivity  {

    public static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setContentView(edu.puc.iic3380.mg4.R.layout.activity_main);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // If fragment is already added, replace it.
        if (getSupportFragmentManager().findFragmentByTag(ContactsFragment.TAG) != null) {
            transaction = transaction.replace(edu.puc.iic3380.mg4.R.id.main_container,
                    new ContactsFragment(), null);
        } else {
            transaction = transaction.add(edu.puc.iic3380.mg4.R.id.main_container,
                    new ContactsFragment(), ContactsFragment.TAG);
        }
        transaction.commit();
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }



    private boolean hasPhonePermissions() {
        return ContextCompat.checkSelfPermission(this.getBaseContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
    }



}


