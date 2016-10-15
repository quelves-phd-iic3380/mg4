package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.fragments.ContactsFragment;
import edu.puc.iic3380.mg4.model.ChatSettings;
import edu.puc.iic3380.mg4.model.Contact;

public class ContactsActivity extends AppCompatActivity implements ContactsFragment.OnContactSelected {

    public static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // If fragment is already added, replace it.
        if (getSupportFragmentManager().findFragmentByTag(ContactsFragment.TAG) != null) {
            transaction = transaction.replace(R.id.content_contacts,
                    new ContactsFragment(), null);
        } else {
            transaction = transaction.add(edu.puc.iic3380.mg4.R.id.content_contacts,
                    new ContactsFragment(), ContactsFragment.TAG);
        }
        transaction.commit();
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ContactsActivity.class);
        return intent;
    }

    /**
     * Called when contact is selected on ContactsFragment.
     *
     * @param contact Contact selected.
     */
    @Override
    public void onContactSelected(Contact contact) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasPhonePermissions()) {
            requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_READ_PHONE_STATE);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            startChat(contact);
        }
    }

    private boolean hasPhonePermissions() {
        return ContextCompat.checkSelfPermission(this.getBaseContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
    }

    private void startChat(Contact contact) {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getDeviceId()+" ".replace(" ","");
        ChatSettings chatSettings = new ChatSettings(contact.name, mPhoneNumber + "-" + contact.phoneNumber.replace(" ",""));
        ChatSettings chatSetting2 = new ChatSettings(contact.name, contact.phoneNumber.replace(" ","")+ "-" + mPhoneNumber);
        startActivity(ChatActivity.getIntent(ContactsActivity.this, chatSettings, chatSetting2));
    }


}
