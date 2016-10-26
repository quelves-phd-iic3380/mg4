package edu.puc.iic3380.mg4.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.model.Contact;
import edu.puc.iic3380.mg4.model.User;

import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USERS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USER_CONTACTS;

public class UserContactFragment extends FragmentBase {
    public static final String TAG = "UserContactFragment";
    public static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private OnUserContactSelected mListener;
    private ContactsFragment  contactsFragment;
    private UserContactListner userContactListner;



    // private ArrayList<Contact> mContacts;
    private ArrayList<Contact> contactList;
    private ArrayList<User> usersList;

    private ContactsAdapter mAdapter;
    private ListView mContactsListView;



    public interface OnUserContactSelected {
        void onUserContactSelected(Contact contact);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserContactFragment() {
    }

    public static UserContactFragment newInstance(String phoneKey) {
        UserContactFragment fragment = new UserContactFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phoneKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phoneKey = getArguments().getString(ARG_PHONE);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_usercontacts, container, false);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_usercontacts, container, false);

        mContactsListView = (ListView) view.findViewById(R.id.usercontacts_list_view);
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onUserContactSelected(contactList.get(position));
            }
        });

        contactList = new ArrayList<Contact>();
        usersList = new ArrayList<User>();
        mAdapter = new ContactsAdapter(getContext(), R.layout.usercontact_list_item, contactList);
        mContactsListView.setAdapter(mAdapter);

        // Firebase initialization
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        String dbRef = FIREBASE_KEY_USERS + "/" +
                phoneKey + "/" +
                FIREBASE_KEY_USER_CONTACTS;

        Log.d(TAG, "dbRef: " + dbRef);

        userContactsRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS)
                .child(phoneKey)
                .child(FIREBASE_KEY_USER_CONTACTS);
        userContactsRef.addListenerForSingleValueEvent(new OnInitialDataLoaded());
        Log.d(TAG, "dbRefKey: " + userContactsRef.getKey());

        dbRef = FIREBASE_KEY_USERS;
        Log.d(TAG, "dbRef: " + dbRef);
        usersRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS);
        usersRef.addListenerForSingleValueEvent(new OnInitialDataLoadedUser());
        Log.d(TAG, "dbRefKey: " + usersRef.getKey());

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_contacts);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();


                doAction(contactsFragment, TAG, context);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        return view;
    }

    private boolean hasPhonePermissions() {
        return ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserContactSelected) {
            mListener = (OnUserContactSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Adapter
     */
    private class ContactsAdapter extends ArrayAdapter<Contact> {
        private ArrayList<Contact> mContacts;
        private LayoutInflater mLayoutInflater;

        public ContactsAdapter(Context context, int resource, ArrayList<Contact> contacts) {
            super(context, resource, contacts);
            mContacts = contacts;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Return the view of a row.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            // Recycle views. Inflate the view only if its not already inflated.
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.contact_list_item, parent, false);
            }
            Contact contact = mContacts.get(position);

            TextView nameView = (TextView) view.findViewById(R.id.contact_name);
            TextView phoneView = (TextView) view.findViewById(R.id.contact_phone_number);


            nameView.setText(contact.getName());
            phoneView.setText(contact.getPhoneNumber());

            return view;
        }
    }


    /**
     * Listener for loading the initial messages of a chat room.
     */
    public class OnInitialDataLoaded implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                Contact contact = child.getValue(Contact.class);

                //Define
                contactList.add(contact);
            }
            // Update the UI
            mAdapter.notifyDataSetChanged();

            scrollToBottom();

            userContactsRef.addChildEventListener(new OnContactsChanged());
            Log.i(TAG, "Contacts Loaded!");

        }



        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.i(TAG, "Could not initialize chat.");
            // TODO: Inform the user about the error and handle gracefully.

        }
    }

    /**
     * Listener for loading the initial messages of a chat room.
     */
    public class OnInitialDataLoadedUser implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                User user = child.getValue(User.class);

                //Define
                usersList.add(user);
                Log.d(TAG, "User: " + user.toString());
            }

            Log.i(TAG, "Users Loaded!");

        }



        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.i(TAG, "Could not initialize chat.");
            // TODO: Inform the user about the error and handle gracefully.

        }
    }




    /**
     * Scrolls the list view to the bottom.
     */
    private void scrollToBottom() {
        mContactsListView.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    /**
     * Listener for updating in real time the chat room's messages, after the initial messages have been loaded.
     */
    public class OnContactsChanged implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Contact contact = dataSnapshot.getValue(Contact.class);
            Log.d(TAG, "Contacto: " + contact.toString());
           // addContact(contact);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    /**
     * Adds a chat message to the current list of messages only if it hasn't been previously added.
     *
     * @param contact to add.
     */
    public void addContact(Contact contact) {
        Log.d(TAG, "addcontact" + contact.toString());
        for (Contact value : contactList) {
            if (contact.getUid().equals(value.getUid())) return;
        }


        userContactsRef.child(contact.getPhoneNumber()).setValue(contact);
        //userContactsRef.push().setValue(contact);
        contactList.add(contact);
        mAdapter.notifyDataSetChanged();

        scrollToBottom();
    }


    public void assign(ContactsFragment contactsFragment) {
        this.contactsFragment = contactsFragment;
    }

}
