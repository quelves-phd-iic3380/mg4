package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.fragments.ChatFragment;
import edu.puc.iic3380.mg4.fragments.ContactsFragment;
import edu.puc.iic3380.mg4.fragments.GroupsFragment;
import edu.puc.iic3380.mg4.fragments.ProfileFragment;
import edu.puc.iic3380.mg4.fragments.UserContactFragment;
import edu.puc.iic3380.mg4.model.Chat;
import edu.puc.iic3380.mg4.model.ChatBinding;
import edu.puc.iic3380.mg4.model.ChatMessage;
import edu.puc.iic3380.mg4.model.ChatSettings;
import edu.puc.iic3380.mg4.model.Contact;
import edu.puc.iic3380.mg4.model.User;

import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_BINDINGS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USERS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USER_CONTACTS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USER_CONTACT_CHAT;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET_KEY_PROFILES;
import static edu.puc.iic3380.mg4.util.Constantes.USER_UID_DEFAULT;


public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ContactsFragment.OnContactSelected,
        UserContactFragment.OnUserContactSelected,
        ChatFragment.OnChatSelected,
        ProfileFragment.OnFragmentInteractionListener {

    public static final String TAG = "NavigationActivity";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference usersRef;
    protected DatabaseReference userContactRef;
    private User user;
    private ContactsFragment contactsFragment;
    private UserContactFragment userContactFragment;
    private GroupsFragment groupsFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;

    private ImageView ivProfileHeader;

    public void doAction(Fragment fragment, String TAG) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            hide(activeFragment, TAG);
            String fragmentTAG = "";
            FragmentManager fm = getSupportFragmentManager();


            FragmentTransaction transaction = fm.beginTransaction();

            if (fragment != null) {
                // If fragment is already added, replace it.
                if (getSupportFragmentManager().findFragmentByTag(TAG) != null) {
                    transaction = transaction.replace(R.id.content_navigation,
                            fragment, TAG);
                } else {
                    transaction = transaction.add(edu.puc.iic3380.mg4.R.id.content_navigation,
                            fragment, TAG);
                }
                transaction.commit();
                activeFragment = fragment;
                show(activeFragment, TAG);
            }
        } else {
            Toast.makeText(NavigationActivity.this, "Usuario Indefinido!", Toast.LENGTH_SHORT).show();

        }
    }

    protected void hide(Fragment fragment, String TAG) {
        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
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
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Log.d(TAG, "Show Fragment");
            transaction.show(fragment);
            transaction.commit();
        } else {
            Log.d(TAG, "Frament is null");
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactsFragment = new ContactsFragment();
        groupsFragment = new GroupsFragment();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Firebase initialization
        String userUID;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userUID = USER_UID_DEFAULT;
        }

        if (userUID != null) {
            Log.d(TAG, "Buscando detalle para user: " + userUID);
            usersRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS);
            usersRef.orderByChild("uid").equalTo(userUID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        user = child.getValue(User.class);

                        //create fragmenhts
                        profileFragment = ProfileFragment.newInstance(user.getPhoneNumber());
                        userContactFragment = UserContactFragment.newInstance(user.getPhoneNumber());
                        userContactFragment.assign(contactsFragment);

                        ivProfileHeader = (ImageView)findViewById(R.id.ivProfileHeaderNavigation);

                        // Create a storage reference from our app
                        StorageReference storageProfileImageRef= FirebaseStorage.getInstance()
                                .getReferenceFromUrl(FIREBASE_STORAGE_BUCKET)
                                .child(FIREBASE_STORAGE_BUCKET_KEY_PROFILES)
                                .child(user.getPhoneNumber() + ".PNG");

                        storageProfileImageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                // Metadata now contains the metadata for 'images/forest.jpg'
                                //storageDownloadFile(profileImageFilePath, Constantes.StorageImageContentType, storageProfileImageRef);
                                Log.d(TAG, "storageMetadata.getMd5Hash(): " + storageMetadata.getMd5Hash());
                             try {
                                 Glide.with(getApplicationContext()).load(storageMetadata.getDownloadUrl()).into(ivProfileHeader);
                             }
                             catch (Exception e) {
                                 Log.d(TAG, "Error en set Profile Image", e);
                             }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                            }
                        });

                        Log.d(TAG, "User detail loaded: " + user.toString());
                        Toast.makeText(NavigationActivity.this, "User Defined!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(NavigationActivity.this, "Usuario Indefinido!", Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.tb_bottom);
        toolbarBottom.findViewById(R.id.action_contacs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(NavigationActivity.this, "Contacts Pressed", Toast.LENGTH_SHORT).show();

                doAction(userContactFragment, UserContactFragment.TAG);

            }
        });
        toolbarBottom.findViewById(R.id.action_groups).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(NavigationActivity.this, "Contacts Pressed", Toast.LENGTH_SHORT).show();

                doAction(groupsFragment, GroupsFragment.TAG);

            }
        });
        toolbarBottom.findViewById(R.id.action_chats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NavigationActivity.this, "Chats Pressed", Toast.LENGTH_SHORT).show();
                doAction(new ChatFragment(), ChatFragment.TAG);
            }
        });
        toolbarBottom.findViewById(R.id.action_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    startActivity(ProfileActivity.getIntent(NavigationActivity.this, user));
                }
                else {
                    Toast.makeText(NavigationActivity.this, "User Undefined!, Wait", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void myColorTint() {
        int tint = Color.parseColor("#FFFFFF"); // R.color.blue;
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        // add your drawable resources you wish to tint to the drawables array...
        int drawables[] = { R.drawable.ic_format_shapes_black_24dp, R.drawable.ic_group_black_24dp };
        for (int id : drawables) {
            Drawable icon = getResources().getDrawable(id);
            icon.setColorFilter(tint,mode);
        }
    }

    private Drawable setTint(Drawable d, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
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
        getMenuInflater().inflate(R.menu.app_bar_navigation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            startActivity(ConversationActivity.getIntent(NavigationActivity.this));

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_login) {
            LoginActivity.sigout();
            startActivity(LoginActivity.getIntent(NavigationActivity.this));

        } else if (id == R.id.nav_exit) {
            finish();
            System.runFinalization();
            System.exit(0);
            NavigationActivity.this.finish();
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
        userContactFragment.addContact(contact);
        hide(contactsFragment, ContactsFragment.TAG);
        doAction(userContactFragment, UserContactFragment.TAG);
    }


    @Override
    public void onUserContactSelected(Contact contact) {
        startChat(contact);

    }

    public void onChatSelected(Chat chat) {

    }


    public void onChatSelected(ChatMessage chat) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private boolean hasPhonePermissions() {
        return ContextCompat.checkSelfPermission(this.getBaseContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
    }

    private void startChat(Contact contact) {
        //Verificar y actualizar la key del chat
        if (contact.getChatRef() == null) {
            String chatKey = UUID.randomUUID().toString();
            ChatBinding chatBinding = new ChatBinding(chatKey);
            chatBinding.addUserId(user.getPhoneNumber());
            chatBinding.addUserId(contact.getPhoneNumber());
            contact.setChatRef(chatKey);

            DatabaseReference chatBindingRef = mFirebaseDatabase.getReference(FIREBASE_KEY_BINDINGS);
            chatBindingRef.child(chatKey).setValue(chatBinding);

            //chat contacto send in
            DatabaseReference userSendOutRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS)
                    .child(contact.getPhoneNumber())
                    .child(FIREBASE_KEY_USER_CONTACTS)
                    .child(user.getPhoneNumber())
                    .child(FIREBASE_KEY_USER_CONTACT_CHAT);
            userSendOutRef.setValue(contact.getChatRef());

            //Chan contacto send out
            DatabaseReference userSendInRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS)
                    .child(user.getPhoneNumber())
                    .child(FIREBASE_KEY_USER_CONTACTS)
                    .child(contact.getPhoneNumber())
                    .child(FIREBASE_KEY_USER_CONTACT_CHAT);
            userSendInRef.setValue(contact.getChatRef());

        }

        ChatSettings chatSettings = new ChatSettings(user.getUsername(), contact.getChatRef(), user.getPhoneNumber());
        startActivity(ConversationActivity.getIntent(NavigationActivity.this, chatSettings));
    }

}
