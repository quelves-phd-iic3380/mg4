package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import edu.puc.iic3380.mg4.BR;
import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.model.ChatMessage;
import edu.puc.iic3380.mg4.model.ChatSettings;

import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_BINDINGS;
import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_MESSAGES;

public class ConversationActivity extends AppCompatActivity {
    public static final String TAG = "ConversationActivity";

    private static final String KEY_SETTINGS = "settings";


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatRoomReference;

    private ChatSettings mChatSettings;
    private boolean mInitialized;

    private List<ChatMessage> mMessageList;
    private ArrayAdapter<ChatMessage> mAdapter;

    private Intent cameraIntent;

    private ListView lvChat;
    private EditText etMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // We retrieve the chat settings (username and chat room name)
        mChatSettings = getIntent().getParcelableExtra(KEY_SETTINGS);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mChatSettings.getUsername());

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

            if (!message.getSenderId().equals(mChatSettings.getUsername())) {
                row = inflater.inflate(R.layout.message_other_side, parent, false);
            }else{
                row = inflater.inflate(R.layout.message_mine_side, parent, false);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Camera onActivityResult  1");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView mImageView = (ImageView)findViewById(R.id.ivUser);
            mImageView.setImageBitmap(imageBitmap);
        }
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            //mVideoView.setVideoURI(videoUri);
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








}
