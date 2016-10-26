package edu.puc.iic3380.mg4.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.activities.LoginActivity;
import edu.puc.iic3380.mg4.model.User;

import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USERS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends FragmentBase {
    public static final String TAG = "ProfileFragment";




    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mPhoneView;
    private AutoCompleteTextView mUsernameView;
    private AutoCompleteTextView mStateView;
    private AutoCompleteTextView mMessageView;

    private OnFragmentInteractionListener mListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference usersRef;
    private User user;


    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance(String phoneKey) {
        ProfileFragment fragment = new ProfileFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mUsernameView = (AutoCompleteTextView) view.findViewById(R.id.profile_name);
        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.profile_email);
        mPhoneView = (AutoCompleteTextView) view.findViewById(R.id.profile_phone);
        mStateView = (AutoCompleteTextView) view.findViewById(R.id.profile_estado);
        mMessageView = (AutoCompleteTextView) view.findViewById(R.id.profile_message);

        // Firebase initialization
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS).child(phoneKey);
        usersRef.addListenerForSingleValueEvent(new OnInitialDataLoaded());
        Button btApply = (Button) view.findViewById(R.id.profile_apply_action);

        btApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apply();
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void apply() {
        // Reset errors.
        mEmailView.setError(null);
        mPhoneView.setError(null);
        mUsernameView.setError(null);
        mStateView.setError(null);
        mMessageView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String state = mStateView.getText().toString();
        String message = mMessageView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            user.setEmail(email);
            user.setPhone(phone);
            user.setUsername(username);
            user.setState(state);
            user.setMessage(message);

            Log.d(TAG, "Update User: " + user.toString());

            usersRef.updateChildren(user.toMap());

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPhoneValid(String phone) {
        //TODO: Replace this with your own logic
        return phone.length() > 6;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 4;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class OnInitialDataLoaded implements ValueEventListener {
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);

            mEmailView.setText(user.getEmail());
            mUsernameView.setText(user.getUsername());
            mPhoneView.setText(user.getPhoneNumber());

            Log.d(TAG, "user loaded: " + user.toString());

        }
    }


}
