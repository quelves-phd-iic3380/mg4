package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.imagesdk.MyCameraSettings;
import edu.puc.iic3380.mg4.imagesdk.MyDirectory;
import edu.puc.iic3380.mg4.imagesdk.MyEditorSaveSettings;
import edu.puc.iic3380.mg4.model.User;
import ly.img.android.sdk.models.state.EditorSaveSettings;
import ly.img.android.sdk.models.state.manager.SettingsList;
import ly.img.android.ui.activities.CameraPreviewActivity;
import ly.img.android.ui.activities.CameraPreviewBuilder;
import ly.img.android.ui.utilities.PermissionRequest;

import static edu.puc.iic3380.mg4.util.Constantes.CAMERA_PREVIEW_RESULT;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USERS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET_KEY_PROFILES;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET_KEY_PROFILE_IMAGE;
import static edu.puc.iic3380.mg4.util.Constantes.GENERIC_FILE_CODE;
import static edu.puc.iic3380.mg4.util.Constantes.PICK_IMAGE_CODE;

public class ProfileActivity extends AppCompatActivity  implements PermissionRequest.Response {
    public static final String TAG = "ProfileActivity";

    private ProfileActivity self;

    private static final String KEY_USER = "KEY_USER";

    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mPhoneView;
    private AutoCompleteTextView mUsernameView;
    private AutoCompleteTextView mStateView;
    private AutoCompleteTextView mMessageView;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference usersRef;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference storageProfileRef;
    private StorageReference storageProfileImageRef;
    private User user;
    private User _user;


    private static final String FOLDER = "ImgLy";


    private SettingsList settingsList;

    private ImageView ivProfile;

    private String profileImageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        self = this;

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Profile Settings");

        _user = getIntent().getParcelableExtra(KEY_USER);
        String phoneKey = _user.getPhoneNumber();

        profileImageFilePath = MyDirectory.STORE.getPath() + "/mg4_profile_" + phoneKey + ".PNG";

        //Directory store = new Directory("STORE", 1, Environment.getExternalStorageDirectory().getAbsolutePath());
        settingsList = new SettingsList();
        settingsList
                // Set custom camera export settings
                .getSettingsModel(MyCameraSettings.class)
                .setExportDir(MyDirectory.STORE, FOLDER)
                .setExportPrefix("camera_")
                // Set custom editor export settings
                .getSettingsModel(MyEditorSaveSettings.class)
                .setExportDir(MyDirectory.STORE, FOLDER)
                .setExportPrefix("result_")
                .setOutputFilePath(profileImageFilePath)
                .setSavePolicy(
                        EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT
                );

        Log.d(TAG, "output: " + settingsList.toString());

        mUsernameView = (AutoCompleteTextView) findViewById(R.id.profile_name);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.profile_email);
        mPhoneView = (AutoCompleteTextView) findViewById(R.id.profile_phone);
        mStateView = (AutoCompleteTextView) findViewById(R.id.profile_estado);
        mMessageView = (AutoCompleteTextView) findViewById(R.id.profile_message);

         // Firebase initialization
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS).child(phoneKey);
        usersRef.addListenerForSingleValueEvent(new OnInitialDataLoaded());
        Button btApply = (Button) findViewById(R.id.profile_apply_action);

        btApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apply();
            }
        });

        //Storage
        mFirebaseStorage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageProfileImageRef= mFirebaseStorage
                .getReferenceFromUrl(FIREBASE_STORAGE_BUCKET)
                .child(FIREBASE_STORAGE_BUCKET_KEY_PROFILES)
                .child(phoneKey + ".PNG");

        ivProfile = (ImageView)findViewById(R.id.ivProfile);



        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CameraPreviewBuilder(self)
                        .setSettingsList(settingsList)
                        .startActivityForResult(self, CAMERA_PREVIEW_RESULT);
            }
        });

        setProfileImage();
    }

    public static Intent getIntent(Context context, User user) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(KEY_USER, user);
        return intent;
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

    private void setProfileImage() {
        File profileImageFile = getProfileImageFile();
        if (profileImageFile != null) {
            Uri contentUri = Uri.fromFile(profileImageFile);
            ivProfile.setImageURI(contentUri);
            ivProfile.refreshDrawableState();
        }
    }

    private File getProfileImageFile() {
        return new File(profileImageFilePath);
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

    //Important for Android 6.0 and above permisstion request, don't forget this!
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void permissionGranted() {

    }

    @Override
    public void permissionDenied() {
        // The Permission was rejected by the user, so the Editor was not opened because it can not save the result image.
        // TODO for you: Show a Hint to the User
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_PREVIEW_RESULT) {
            String resultPath =
                    data.getStringExtra(CameraPreviewActivity.RESULT_IMAGE_PATH);
            String sourcePath =
                    data.getStringExtra(CameraPreviewActivity.SOURCE_IMAGE_PATH);

            if (resultPath != null) {
                // Scan result file
                File file =  new File(resultPath);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);
                sendBroadcast(scanIntent);
            }

            if (sourcePath != null) {
                // Scan camera file
                File file =  new File(sourcePath);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);

            }
            Toast.makeText(this, "Image Save on: " + resultPath, Toast.LENGTH_LONG).show();
            setProfileImage();
        } else
        if (requestCode == GENERIC_FILE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri));
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }

        }
        else if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String mimeType = getContentResolver().getType(uri);

        }

    }

    private void uploadProfileImage() {
        // Get the data from an ImageView as bytes
        ivProfile.setDrawingCacheEnabled(true);
        ivProfile.buildDrawingCache();
        Bitmap bitmap = ivProfile.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageProfileImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Toast.makeText(ProfileActivity.this, "uploadProfileImage Failure!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(ProfileActivity.this, "uploadProfileImage download!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
