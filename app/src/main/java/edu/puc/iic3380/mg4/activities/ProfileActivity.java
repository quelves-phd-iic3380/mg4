package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.imagesdk.MyCameraSettings;
import edu.puc.iic3380.mg4.imagesdk.MyDirectory;
import edu.puc.iic3380.mg4.imagesdk.MyEditorSaveSettings;
import edu.puc.iic3380.mg4.model.User;
import edu.puc.iic3380.mg4.util.Constantes;
import edu.puc.iic3380.mg4.util.FileManager;
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


        storageProfileImageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'
                //storageDownloadFile(profileImageFilePath, Constantes.StorageImageContentType, storageProfileImageRef);
                Log.d(TAG, "storageMetadata.getMd5Hash(): " + storageMetadata.getMd5Hash());
                Glide.with(getApplicationContext()).load(storageMetadata.getDownloadUrl()).into(ivProfile);
                ;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });



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

            /*
            File file =  new File(resultPath);
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            sendBroadcast(scanIntent);
            ivProfile.setImageURI(contentUri);
*/

            //user.setMd5HashImage(storageProfileImageRef.getMetadata().getResult().getMd5Hash());

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

    private String sourcePath;
    private String resultPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_PREVIEW_RESULT) {
            resultPath =
                    data.getStringExtra(CameraPreviewActivity.RESULT_IMAGE_PATH);
            sourcePath =
                    data.getStringExtra(CameraPreviewActivity.SOURCE_IMAGE_PATH);

            if (resultPath != null) {
                // Scan result file
                File file =  new File(resultPath);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);
                sendBroadcast(scanIntent);
                ivProfile.setImageURI(contentUri);
                storageuploadFile(file, Constantes.StorageImageContentType, storageProfileImageRef);

            }

            if (sourcePath != null) {
                // Scan camera file
                File file =  new File(sourcePath);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);

            }
            Toast.makeText(this, "Image Save on: " + resultPath, Toast.LENGTH_LONG).show();



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


    private void getStorageMetadata(StorageReference fileRef) {
        fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'
                //storageDownloadFile(profileImageFilePath, Constantes.StorageImageContentType, storageProfileImageRef);
                Log.d(TAG, "storageMetadata.getMd5Hash(): " + storageMetadata.getMd5Hash());
                Glide.with(getApplicationContext()).load(storageMetadata.getDownloadUrl()).into(ivProfile);
                ;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    private void storageuploadFile(File file, StorageMetadata metadata, StorageReference fileRef) {
        try {
            InputStream stream = new FileInputStream(file);

            // Upload file and metadata to the path of file
            UploadTask uploadTask = fileRef.putStream(stream, metadata);

            // Listen for state changes, errors, and completion of the upload.
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "Upload is " + progress + "% done");
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Upload is paused");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "Upload is Failure");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Handle successful uploads on complete
                    Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                    Log.d(TAG, "donwloadURl: " + downloadUrl.getPath());

                }
            });
            fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    // Metadata now contains the metadata for 'images/forest.jpg'
                    user.setMd5HashImage(storageMetadata.getMd5Hash());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                }
            });
        }
        catch (Exception e) {
            Log.d(TAG, "Error in upload", e);
        }
    }



    private File localFile;


    private void storageDownloadFile(String filename, StorageMetadata metadata, StorageReference fileRef) {

        localFile = new File(filename);

        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                storeProfileImage(taskSnapshot, localFile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Error", exception);
            }
        });
    }

    private void storeProfileImage(FileDownloadTask.TaskSnapshot taskSnapshot, File localFile)  {
        Log.d(TAG, "storageDownloadFile sucess! bytes: " + taskSnapshot.getTotalByteCount());
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(localFile);
        scanIntent.setData(contentUri);
        sendBroadcast(scanIntent);
        ivProfile.setImageURI(contentUri);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's a download in progress, save the reference so you can query it later
        if (storageProfileImageRef != null) {
            outState.putString("reference", storageProfileImageRef.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");

        // If there was a download in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        storageProfileImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        List tasks = storageProfileImageRef.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the download
            FileDownloadTask task = (FileDownloadTask)tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Download sucess in onRestoreInstanceState");
                    storeProfileImage(taskSnapshot, localFile);
                }
            });
        }
    }

    private void _setProfileImage() {
        try {
            File profileImageFile = new File(profileImageFilePath);
            if ((profileImageFile != null) && (profileImageFile.exists())) {
                Uri contentUri = Uri.fromFile(profileImageFile);
                ivProfile.setImageURI(contentUri);
            }
        }
        catch (Exception e) {
            Log.d(TAG, "SetProfileImage Error", e);
        }
    }





}
