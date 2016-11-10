package edu.puc.iic3380.mg4.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.imagesdk.MyCameraSettings;
import edu.puc.iic3380.mg4.imagesdk.MyDirectory;
import edu.puc.iic3380.mg4.imagesdk.MyEditorSaveSettings;
import edu.puc.iic3380.mg4.model.User;
import ly.img.android.sdk.models.state.EditorSaveSettings;
import ly.img.android.sdk.models.state.manager.SettingsList;
import ly.img.android.ui.activities.CameraPreviewBuilder;
import ly.img.android.ui.utilities.PermissionRequest;

import static edu.puc.iic3380.mg4.util.Constantes.CAMERA_PREVIEW_RESULT;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USERS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET_KEY_PROFILES;

/**
 * Created by quelves on 09/11/2016.
 */

public abstract class MG4ActivityBase extends AppCompatActivity  implements PermissionRequest.Response {
    public static final String TAG = "v";

    protected ProfileActivity self;

    protected static final String KEY_USER = "KEY_USER";

    protected FirebaseDatabase mFirebaseDatabase;
    protected DatabaseReference usersRef;

    protected FirebaseStorage mFirebaseStorage;
    protected StorageReference storageProfileRef;
    protected StorageReference storageProfileImageRef;
    protected User user;
    protected User _user;


    protected static final String FOLDER = "ImgLy";


    protected SettingsList settingsList;


    protected String profileImageFilePath;

    protected String phoneKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _user = getIntent().getParcelableExtra(KEY_USER);
         phoneKey = _user.getPhoneNumber();

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
                .setSavePolicy(
                        EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT
                );



        // Firebase initialization
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = mFirebaseDatabase.getReference(FIREBASE_KEY_USERS).child(phoneKey);
        usersRef.addListenerForSingleValueEvent(new OnInitialDataLoadedUser());

        //Storage
        mFirebaseStorage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageProfileImageRef= mFirebaseStorage
                .getReferenceFromUrl(FIREBASE_STORAGE_BUCKET)
                .child(FIREBASE_STORAGE_BUCKET_KEY_PROFILES)
                .child(phoneKey + ".PNG");


    }

    public class OnInitialDataLoadedUser implements ValueEventListener {
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
            notifyAsignedUser(user);
            Log.d(TAG, "user loaded: " + user.toString());

        }
    }

    protected abstract void notifyAsignedUser(User user);

    protected void storageUploadFile(File file, StorageMetadata metadata, StorageReference fileRef, OnSuccessListener<StorageMetadata> onSuccessListener, OnFailureListener onFailureListener) {
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
            fileRef.getMetadata().addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        }
        catch (Exception e) {
            Log.d(TAG, "Error in upload", e);
        }
    }

    protected abstract void nofifyFinishedStorageUpload(StorageMetadata storageMetadata);




}
