package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.databinding.AppBarNavigationBinding;
import edu.puc.iic3380.mg4.fragments.ContactsFragment;
import edu.puc.iic3380.mg4.fragments.UserContactFragment;
import edu.puc.iic3380.mg4.model.ChatSettings;
import edu.puc.iic3380.mg4.model.Contact;
import edu.puc.iic3380.mg4.model.User;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ContactsFragment.OnContactSelected, UserContactFragment.OnUserContactSelected {

    public static final String TAG = "NavigationActivity";

    public static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference usersRef;
    private static final String FIREBASE_KEY_USERS = "users";
    private User user;
    private AppBarNavigationBinding binding;

    public class NavigationActivityHandler {


        public void onClickActionContacts() {
            Fragment fragment = null;
            String fragmentTAG = "";
            FragmentManager fm = getSupportFragmentManager();
            fragment = new ContactsFragment();
            fragmentTAG = ContactsFragment.TAG;

            FragmentTransaction transaction = fm.beginTransaction();

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
        }

        public void onShowStorageAccess(){

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_top);
        toolbar.setTitle("MemeticaMe");
        toolbar.setSubtitle("IIC3380 G4");
        setSupportActionBar(toolbar);

        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.tb_bottom);

        //binding = DataBindingUtil.setContentView(this, R.layout.app_bar_navigation);
        //binding.setHandler(new NavigationActivityHandler());


        /*
        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_contacts:
                        break;
                    case R.id.action_favorite:
                        break;
                    case R.id.action_chats:
                        break;
                    case R.id.action_settings:
                        break;

                }


                return true;
            }
        });

        toolbarBottom.inflateMenu(R.menu.toolbar_buttom_menu);
*/

       /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Firebase initialization
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS);
        usersRef.addListenerForSingleValueEvent(new OnInitialDataLoaded());
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
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(LoginActivity.getIntent(NavigationActivity.this));
        }

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

        } else if (id == R.id.nav_signout) {
            LoginActivity.sigout();
            startActivity(LoginActivity.getIntent(NavigationActivity.this));

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

    @Override
    public void onUserContactSelected(Contact contact) {

    }

    private boolean hasPhonePermissions() {
        return ContextCompat.checkSelfPermission(this.getBaseContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
    }

    private void startChat(Contact contact) {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getDeviceId()+" ".replace(" ","");
        ChatSettings chatSettings = new ChatSettings(contact.name, mPhoneNumber + "-" + contact.phoneNumber.replace(" ",""));
        ChatSettings chatSetting2 = new ChatSettings(contact.name, contact.phoneNumber.replace(" ","")+ "-" + mPhoneNumber);
        startActivity(ChatActivity.getIntent(NavigationActivity.this, chatSettings, chatSetting2));
    }

    public class OnInitialDataLoaded implements ValueEventListener {
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                user = child.getValue(User.class);
                Log.d(TAG, "user loaded: " + user.toString());
            }
        }
    }

}
