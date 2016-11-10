package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import edu.puc.iic3380.mg4.BR;
import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.imagesdk.MyCameraSettings;
import edu.puc.iic3380.mg4.imagesdk.MyDirectory;
import edu.puc.iic3380.mg4.imagesdk.MyEditorSaveSettings;
import edu.puc.iic3380.mg4.model.ChatMessage;
import edu.puc.iic3380.mg4.model.ChatSettings;
import edu.puc.iic3380.mg4.util.Constantes;
import edu.puc.iic3380.mg4.util.OnMyStorageProcessListener;
import ly.img.android.sdk.models.constant.Directory;
import ly.img.android.sdk.models.state.EditorLoadSettings;
import ly.img.android.sdk.models.state.EditorSaveSettings;
import ly.img.android.sdk.models.state.manager.SettingsList;
import ly.img.android.ui.activities.CameraPreviewActivity;
import ly.img.android.ui.activities.CameraPreviewBuilder;
import ly.img.android.ui.activities.PhotoEditorBuilder;
import ly.img.android.ui.utilities.PermissionRequest;

import static edu.puc.iic3380.mg4.util.Constantes.CAMERA_PREVIEW_RESULT;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_BINDINGS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_MESSAGES;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET_KEY_FILES;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_STORAGE_BUCKET_KEY_IMAGES;
import static edu.puc.iic3380.mg4.util.Constantes.MG4_FOLDER;

public class ConversationActivity extends AppCompatActivity implements PermissionRequest.Response, OnSuccessListener {
    public static final String TAG = "ConversationActivity";

    private static final String KEY_SETTINGS = "settings";


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatRoomReference;
    private FirebaseStorage mFirebaseStorage;

    private ChatSettings mChatSettings;
    private boolean mInitialized;

    private List<ChatMessage> mMessageList;
    private ArrayAdapter<ChatMessage> mAdapter;

    private Intent cameraIntent;

    private ListView lvChat;
    private EditText etMessage;

    private SettingsList settingsListCamera;
    private SettingsList settingsListPhotoEdithor;

    private StorageReference storageFilesRef;
    private StorageReference storageFileRef;
    private MyStorageListener myStorageListener;

    private String userLocalFilesPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // We retrieve the chat settings (username and chat room name)
        mChatSettings = getIntent().getParcelableExtra(KEY_SETTINGS);

        myStorageListener = new MyStorageListener();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mChatSettings.getUsername());

        userLocalFilesPath = MyDirectory.STORE.getPath() + "/mg4_files/";

        //Toolbar toolbar = (Toolbar) findViewById(R.id.tb_chat_top);
        //toolbar.setTitle(mChatSettings.getUsername());
        //toolbar.setSubtitle("active");
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tvUser = (TextView)findViewById(R.id.tvUser);
        lvChat = (ListView)findViewById(R.id.lvChat);

        etMessage = (EditText)findViewById(R.id.etMessage);

        ImageButton ibSendMessage = (ImageButton)findViewById(R.id.ibSendMessage);
        ibSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInitialized) {
                    ChatMessage newMessage = new ChatMessage(mChatSettings.getUsername(), etMessage.getText().toString());
                    newMessage.setMessageDate(GregorianCalendar.getInstance().getTime());

                    mChatRoomReference.push().setValue(newMessage);
                    mAdapter.add(newMessage);
                    mAdapter.notifyDataSetChanged();

                    scrollToBottom();

                    // Empty the message text box.
                    etMessage.setText("");
                }
            }
        });


        ImageButton ibSAF = (ImageButton)findViewById(R.id.ibSAF);
        ibSAF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SAFActivity.getIntent(ConversationActivity.this));
            }
        });



        // List configuration
        mMessageList = new ArrayList<>();
        //mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMessageList);
        //mBinding.listView.setAdapter(mAdapter);
        mAdapter = new ChatMessagesAdapter(getBaseContext(), android.R.layout.simple_list_item_1, mMessageList);
        lvChat.setAdapter(mAdapter);

        // Firebase initialization
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatRoomReference = mFirebaseDatabase.getReference(FIREBASE_KEY_BINDINGS).child(mChatSettings.getChatRoom()).child(FIREBASE_KEY_MESSAGES);
        mChatRoomReference.addListenerForSingleValueEvent(new OnInitialDataLoaded());

        //Storage
        mFirebaseStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageFilesRef  = mFirebaseStorage
                .getReferenceFromUrl(FIREBASE_STORAGE_BUCKET)
                .child(FIREBASE_STORAGE_BUCKET_KEY_FILES + "/" + mChatSettings.getmUserPhone());

        //Directory store = new Directory("STORE", 1, Environment.getExternalStorageDirectory().getAbsolutePath());
        settingsListCamera = new SettingsList();
        settingsListPhotoEdithor = new SettingsList();

        settingsListCamera
                // Set custom camera export settings
                .getSettingsModel(MyCameraSettings.class)
                .setExportDir(MyDirectory.STORE, MG4_FOLDER)
                .setExportPrefix("camera_")
                // Set custom editor export settings
                .getSettingsModel(MyEditorSaveSettings.class)
                .setExportDir(MyDirectory.STORE, MG4_FOLDER)
                .setExportPrefix("result_")
                .setSavePolicy(
                        EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT
                );


    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ConversationActivity.class);
        return intent;
    }

    // Static helpers

    public static Intent getIntent(Context context, ChatSettings settings) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(KEY_SETTINGS, settings);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_bar_navigation_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_file) {
            /*
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_CODE);
            */
            openCamera();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openCamera() {
        settingsListCamera
        .getSettingsModel(MyEditorSaveSettings.class)
                .setExportPrefix("")
                .setOutputFilePath(UUID.randomUUID().toString() + ".png");
        new CameraPreviewBuilder(this)
                .setSettingsList(settingsListCamera)
                .startActivityForResult(this, CAMERA_PREVIEW_RESULT);
    }

    private void openPhotoEdithor(Uri uri) {
        String myPicture = getRealPathFromURI(uri);
        settingsListPhotoEdithor
                .getSettingsModel(EditorLoadSettings.class)
                .setImageSourcePath(myPicture, true) // Load with delete protection true!
                .getSettingsModel(EditorSaveSettings.class)
                .setExportDir(Directory.DCIM, MG4_FOLDER)
                .setExportPrefix("result_")
                .setSavePolicy(
                        EditorSaveSettings.SavePolicy.KEEP_SOURCE_AND_CREATE_ALWAYS_OUTPUT
                );

        new PhotoEditorBuilder(this)
                .setSettingsList(settingsListPhotoEdithor)
                .startActivityForResult(this, CAMERA_PREVIEW_RESULT);
    }

    private String sourcePath;
    private String resultPath;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Camera onActivityResult  1");
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
                //ImageView imageView new ImageView();
                //imageView.setImageURI(contentUri);
                storageUploadFile(file, Constantes.StorageImageContentType, storageFilesRef, myStorageListener);

            }

            if (sourcePath != null) {
                // Scan camera file
                File file =  new File(sourcePath);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);

            }
            Toast.makeText(this, "Image Save on: " + resultPath, Toast.LENGTH_LONG).show();



        }  else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            //mVideoView.setVideoURI(videoUri);

        }
    }

    /**
     * Adds a chat message to the current list of messages only if it hasn't been previously added.
     *
     * @param chatMessage The chat message to add.
     */
    private void addChatMessage(ChatMessage chatMessage) {
        for (ChatMessage message : mMessageList) {
            if (message.getUid().equals(chatMessage.getUid())) return;
        }

        mMessageList.add(chatMessage);
        mAdapter.notifyDataSetChanged();

        scrollToBottom();
    }

    private void createChatMessage(Uri uri) {
        String url = getRealPathFromURI(uri);
        ChatMessage newMessage = new ChatMessage(mChatSettings.getUsername(), url, ChatMessage.MessageType.IMAGE);
        newMessage.setMessageDate(GregorianCalendar.getInstance().getTime());

        mChatRoomReference.push().setValue(newMessage);
        mAdapter.add(newMessage);
        mAdapter.notifyDataSetChanged();

        scrollToBottom();

        // Empty the message text box.
        etMessage.setText("");
    }

    private class MyStorageListener implements OnMyStorageProcessListener {
        @Override
        public void loadImageView(final ImageView imageView, StorageReference fileRef) {
            fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    // Metadata now contains the metadata for 'images/forest.jpg'
                    //storageDownloadFile(profileImageFilePath, Constantes.StorageImageContentType, storageProfileImageRef);
                    Log.d(TAG, "storageMetadata.getMd5Hash(): " + storageMetadata.getMd5Hash());
                    Glide.with(getApplicationContext()).load(storageMetadata.getDownloadUrl()).into(imageView);
                    ;

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                }
            });
        }
    }



    private void storageUploadFile(File file, StorageMetadata metadata, StorageReference fileRef, final OnMyStorageProcessListener listener) {
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
                    //// Handle successful uploads on complete
                    //listener.loadImageView();
                    Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                    Log.d(TAG, "donwloadURl: " + downloadUrl.getPath());

                }
            });
            fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    // Metadata now contains the metadata for 'images/forest.jpg'
                    //user.setMd5HashImage(storageMetadata.getMd5Hash());
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

    @Override
    public void onSuccess(Object o) {

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
        //ivProfile.setImageURI(contentUri);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's a download in progress, save the reference so you can query it later
        if (storageFileRef != null) {
            outState.putString("reference", storageFileRef.toString());
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
        storageFileRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        List tasks = storageFileRef.getActiveDownloadTasks();
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

    /**
     * Listener for loading the initial messages of a chat room.
     */
    public class OnInitialDataLoaded implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                ChatMessage chatMessage = child.getValue(ChatMessage.class);

                //Define
                //chatMessage.setOwner(true);
                mMessageList.add(chatMessage);
            }
            // Update the UI
            mAdapter.notifyDataSetChanged();

            scrollToBottom();

            mInitialized = true;
            mChatRoomReference.addChildEventListener(new OnMessagesChanged());
            Log.i(TAG, "Chat initialized");

        }



        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.i(TAG, "Could not initialize chat.");
            // TODO: Inform the user about the error and handle gracefully.

        }
    }


    /**
     * Listener for updating in real time the chat room's messages, after the initial messages have been loaded.
     */
    public class OnMessagesChanged implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
            addChatMessage(chatMessage);
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
     * Scrolls the list view to the bottom.
     */
    private void scrollToBottom() {
        lvChat.smoothScrollToPosition(mAdapter.getCount() - 1);
    }


    /**
     * adapter
     */
    private class ChatMessagesAdapter extends ArrayAdapter<ChatMessage> {
        private List<ChatMessage> mChatMessages;
        private LayoutInflater mLayoutInflater;

        public ChatMessagesAdapter(Context context, int resource, List<ChatMessage> chatMessages) {
            super(context, resource, chatMessages);
            mChatMessages = chatMessages;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void add(ChatMessage chatMessage) {
            mChatMessages.add(chatMessage);
        }

        /**
         * Return the view of a row.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChatMessage message = mChatMessages.get(position);
            View row = convertView;
            String space = "                                                                      ";

            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (message.getMimeType() == ChatMessage.MessageType.TEXT) {
                if (!message.getSenderId().equals(mChatSettings.getUsername())) {
                    row = inflater.inflate(R.layout.message_other_text, parent, false);
                } else {
                    row = inflater.inflate(R.layout.message_mine_text, parent, false);
                }
                TextView chatText = (TextView) row.findViewById(R.id.msgr);
                String dateFormated = "";
                try {
                    android.text.format.DateFormat df = new android.text.format.DateFormat();
                    if (message.getMessageDate().getDay() == GregorianCalendar.getInstance().getTime().getDay())
                        dateFormated = df.format("hh:mm", message.getMessageDate()).toString();
                    else
                        dateFormated = df.format("yyyy-MM-dd hh:mm:ss", message.getMessageDate()).toString();


                }
                catch (Exception ex) {
                    Log.e(TAG, "Error en parse de fecha " + message.getMessageDate());
                }


                chatText.setText(Html.fromHtml("<small align='left'>" + message.getSenderId() + "</small>" +  "<br />" +
                        "<b align='left'>" + message.getMessage() + "</b>" +  "<br />" +
                        "<small align='right'>" + dateFormated + "</small>"));
            }
            else if (message.getMimeType() == ChatMessage.MessageType.IMAGE) {
                if (!message.getSenderId().equals(mChatSettings.getUsername())) {
                    row = inflater.inflate(R.layout.message_other_image, parent, false);
                } else {
                    row = inflater.inflate(R.layout.message_mine_image, parent, false);
                }


                final ImageView imageView = (ImageView)row.findViewById(R.id.message_content);



            }







            //chatText.setText(message.getMessage());

            return row;



        }
    }

    // Inner classes implementations
    public class TextHolder extends BaseObservable {
        private String mText;

        @Bindable
        public String getText() {
            return mText;
        }

        public void setText(String text) {
            mText = text;
            notifyPropertyChanged(BR.text);
        }
    }
    public class ChatActivityHandler {
        public void onSendMessage(TextHolder textHolder) {
            if (mInitialized) {
                ChatMessage newMessage = new ChatMessage(mChatSettings.getUsername(), textHolder.getText());
                newMessage.setMessageDate(GregorianCalendar.getInstance().getTime());

                mChatRoomReference.push().setValue(newMessage);
                mAdapter.add(newMessage);
                mAdapter.notifyDataSetChanged();

                scrollToBottom();

                // Empty the message text box.
                textHolder.setText("");
            }
        }

        public void onBackToContacts() {

            startActivity(MainActivity.getIntent(ConversationActivity.this));
        }

        public void onShowStorageAccess(){
            startActivity(SAFActivity.getIntent(ConversationActivity.this));
        }

        public void onOpenCamera() {
            dispatchTakePictureIntent1();
        }

        public void onOpenMic() {
            startActivity(AudioRecordActivity.getIntent(ConversationActivity.this));
        }
    }



    /**
     * Camera
     */
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent1() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i(TAG, "Camera Intent  1");
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            Log.i(TAG, "Camera Intent  2");
            File photoFile = null;
            try {
                Log.i(TAG, "Camera Intent  3");
                photoFile = createImageFile();
                Log.i(TAG, photoFile.getName());
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "Camera Intent  3");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i(TAG, "Camera Intent  5");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i(TAG, "Camera Intent  6");
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.i(TAG, "Camera Intent  7");
            }
        }
    }

    static final int REQUEST_VIDEO_CAPTURE = 1;

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }



    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
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
}
