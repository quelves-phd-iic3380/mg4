package edu.puc.iic3380.mg4.fragments;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.model.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    public static final String TAG = "contacts_fragment";

    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private ArrayList<User> mContacts;
    private OnContactSelected mListener;
    private ContactsAdapter mAdapter;
    private ListView mContactsListView;

    public interface OnContactSelected {
        void onContactSelected(User user);
    }

    public ContactsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        mContactsListView = (ListView) view.findViewById(R.id.contacts_list_view);
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onContactSelected(mContacts.get(position));
            }
        });

        showContacts();

        setHasOptionsMenu(true);

        return view;
    }

    /**
     * Checks if the app has permission to read phone contacts.
     * Only for SDK > 23.
     */
    private boolean hasContactsPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
    }

    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasContactsPermissions()) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            getContacts(getContext(), new ContactsProviderListener() {
                @Override
                public void OnContactsReady(ArrayList<User> contacts) {
                    mContacts = contacts;
                    mAdapter = new ContactsAdapter(getContext(), R.layout.contact_list_item, mContacts);
                    mContactsListView.setAdapter(mAdapter);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContacts();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_contact){
            Intent intent = new Intent(Intent.ACTION_INSERT,ContactsContract.Contacts.CONTENT_URI);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_reflesh_contact) {
            showContacts();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnContactSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onViewSelected");
        }
    }


    /**
     * static
     */
    public static void getContacts(Context context, ContactsProviderListener listener){
        new GetContactsTask(context, listener).execute();
    }

    public interface ContactsProviderListener{
        void OnContactsReady(ArrayList<User> users);
    }


    /**
     * task
     */

    /**
     * AsyncTask enables proper and easy use of the UI thread. This class allows to perform
     * background operations and publish results on the UI thread without having to manipulate
     * threads and/or handlers.
     * See <a href="https://developer.android.com/reference/android/os/AsyncTask.html">AsyncTask</a>
     */
    private static class GetContactsTask extends AsyncTask<String, Void, ArrayList<User>> {

        private Context context;
        private ContactsProviderListener listener;
        private ContentResolver mResolver;

        public GetContactsTask(Context context, ContactsProviderListener listener){
            super();
            this.context = context;
            this.listener = listener;
            this.mResolver = context.getContentResolver();
        }

        @Override
        protected ArrayList<User> doInBackground(String... params) {
            return getPhoneContacts(context);
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            listener.OnContactsReady(users);
        }

        public ArrayList<User> getPhoneContacts(Context context) {
            if (mResolver == null) mResolver = context.getContentResolver();
            ArrayList<User> contacts = new ArrayList<>();
            Cursor cursor = mResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if(cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
                do {
                    User contact = getContact(cursor);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                } while (cursor.moveToNext()) ;
                cursor.close();
            }
            return contacts;
        }

        private User getContact(Cursor cursor) {
            User contact = null;
            String id = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts._ID));
            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor c = mResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null);
                if(c != null && c.getCount() != 0 && c.moveToFirst()) {
                    String phoneNumber = c.getString(c.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = c.getString(c.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    contact = new User.Builder().name(name).phoneNumber(phoneNumber).build();
                    c.close();
                }
            }
            return contact;
        }
    }

    /**
     * Adapter
     */

    private class ContactsAdapter extends ArrayAdapter<User> {
        private ArrayList<User> mContacts;
        private LayoutInflater mLayoutInflater;

        public ContactsAdapter(Context context, int resource, ArrayList<User> contacts) {
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
            User user = mContacts.get(position);

            TextView nameView = (TextView) view.findViewById(R.id.contact_name);
            TextView phoneView = (TextView) view.findViewById(R.id.contact_phone_number);

            nameView.setText(user.mName);
            phoneView.setText(user.mPhoneNumber);

            return view;
        }
    }

}

