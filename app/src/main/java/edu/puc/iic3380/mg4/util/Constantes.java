package edu.puc.iic3380.mg4.util;

import com.google.firebase.storage.StorageMetadata;

/**
 * Created by quelves on 26/10/2016.
 */

public interface Constantes {
    public static final String USER_UID_DEFAULT = "l37nVJyLG4gUszX4nTZJ2WDoKeF2";
    public static final String USER_PHONE_NUMBER = "+56996455952";

    public static final String FIREBASE_KEY_USERS = "users";
    public static final String FIREBASE_KEY_USER_CONTACTS = "contacts";
    public static final String FIREBASE_KEY_BINDINGS = "bindings";
    public static final String FIREBASE_KEY_MESSAGES = "messages";
    public static final String FIREBASE_KEY_USER_CONTACT_CHAT = "chatRef";

    public static final String FIREBASE_STORAGE_BUCKET = "gs://quelves-phd-iic3380-mg4.appspot.com/";

    public static final String FIREBASE_STORAGE_BUCKET_KEY_IMAGES = "images";
    public static final String FIREBASE_STORAGE_BUCKET_KEY_FILES = "files";
    public static final String FIREBASE_STORAGE_BUCKET_KEY_PROFILES = "profiles";
    public static final String FIREBASE_STORAGE_BUCKET_KEY_PROFILE_IMAGE = "m4g_profile.jpg";

    public static final int GENERIC_FILE_CODE = 0;
    public static final int PICK_IMAGE_CODE = 1;
    public static final int TEXT_MEME_CODE = 2;
    public static final int PICK_MEME_IMAGE_CODE = 3;
    public static final int PICK_MEME_AUDIO_CODE = 4;
    public static final int PICK_CONTACT_CODE = 5;
    public static final int PICK_PUBLIC_MEME = 6;

    public static int CAMERA_PREVIEW_RESULT = 1;

    public static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    public static final String MG4_FOLDER = "mg4";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 1;

    /**
     * Firebase Content types
     */
    public static final StorageMetadata StorageImageContentType =  new StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build();



}
