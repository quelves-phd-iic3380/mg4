package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.fragments.ContactsFragment;
import edu.puc.iic3380.mg4.model.ChatSettings;
import edu.puc.iic3380.mg4.model.Contact;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ContactsFragment.OnContactSelected {

    public static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, NavigationActivity.class);
        return intent;
    }


        @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        String fragmentTAG = "";
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction transaction = fm.beginTransaction();

        if (id == R.id.nav_contact) {
            fragment = new ContactsFragment();
            fragmentTAG = ContactsFragment.TAG;
        } else if (id == R.id.nav_chat) {


        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }


        if (fragment != null) {
            // If fragment is already added, replace it.
            if (getSupportFragmentManager().findFragmentByTag(ContactsFragment.TAG) != null) {
                transaction = transaction.replace(R.id.content_navigation,
                        fragment, null);
            } else {
                transaction = transaction.add(edu.puc.iic3380.mg4.R.id.content_navigation,
                        fragment, fragmentTAG);
            }
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
        ChatSettings chatSettings = new ChatSettings(contact.mName, mPhoneNumber + "-" + contact.mPhoneNumber.replace(" ",""));
        ChatSettings chatSetting2 = new ChatSettings(contact.mName, contact.mPhoneNumber.replace(" ","")+ "-" + mPhoneNumber);
        startActivity(ChatActivity.getIntent(NavigationActivity.this, chatSettings, chatSetting2));
    }

}
