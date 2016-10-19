package edu.puc.iic3380.mg4.fragments;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.activities.ChatActivity;
import edu.puc.iic3380.mg4.fragments.dummy.DummyContent;
import edu.puc.iic3380.mg4.fragments.dummy.DummyContent.DummyItem;
import edu.puc.iic3380.mg4.model.Chat;
import edu.puc.iic3380.mg4.model.ChatMessage;
import edu.puc.iic3380.mg4.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    public static final String TAG = "ChatFragment";

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private ArrayList<Chat> chatList;
    private OnChatSelected mListener;
    private ChatAdapter mAdapter;
    private ListView mChattsListView;



    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference userChatsRef;

    private static final String FIREBASE_KEY_CHATS = "chats";

    public interface OnChatSelected {
        void onChatSelected(Chat chat);
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChatFragment newInstance(int columnCount) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        chatList = new ArrayList<>();

        mAdapter = new ChatAdapter(getContext(), android.R.layout.simple_list_item_1, chatList);

       // mBinding.lvChat.setAdapter(mAdapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mChattsListView = (ListView) view.findViewById(R.id. chats_list_view);
        mChattsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onChatSelected(chatList.get(position));
            }
        });

        mChattsListView.setAdapter(mAdapter);
        // Firebase initialization
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userChatsRef = mFirebaseDatabase.getReference(FIREBASE_KEY_CHATS);
        userChatsRef.addListenerForSingleValueEvent(new ChatFragment.OnInitialDataLoaded());
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnChatSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onViewSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Listener for loading the initial messages of a chat room.
     */
    public class OnInitialDataLoaded implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange");
            for (DataSnapshot childKey : dataSnapshot.getChildren()) {
                Log.d(TAG, "key: " + childKey.getKey());
                for (DataSnapshot child : childKey.getChildren()) {
                    Log.d(TAG, "key: " + child.getKey());
                    ChatMessage chatMessage = child.getValue(ChatMessage.class);
                    //Define
                    chatList.add(new Chat(chatMessage, childKey.getKey()));

                }







            }
            // Update the UI
            mAdapter.notifyDataSetChanged();

            scrollToBottom();

            userChatsRef.addChildEventListener(new OnContactsChanged());
            Log.i(TAG, "Contacts Loaded!");

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
        mChattsListView.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    /**
     * Listener for updating in real time the chat room's messages, after the initial messages have been loaded.
     */
    public class OnContactsChanged implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Chat value = dataSnapshot.getValue(Chat.class);
            addChat(value);
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
     * @param chat to add.
     */
    private void addChat(Chat chat) {
        if (chat != null) {
            Log.d(TAG, "Chat:" + chat.toString());
            //for (Chat value : chatList) {
              //  if (chat.getUuid().equals(value.getUuid())) return;
            //}

            chatList.add(chat);
            mAdapter.notifyDataSetChanged();

            scrollToBottom();
        }
        else {
            Log.d(TAG, "Chat is null");
        }
    }



    /**
     * Adapter
     */
    private class ChatAdapter extends ArrayAdapter<Chat> {
        private ArrayList<Chat> mChats;
        private LayoutInflater mLayoutInflater;

        public ChatAdapter(Context context, int resource, ArrayList<Chat> chats) {
            super(context, resource, chats);
            this.mChats = chats;
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
                view = mLayoutInflater.inflate(R.layout.fragment_chat, parent, false);
            }
            Chat chat = mChats.get(position);

            TextView authorView = (TextView) view.findViewById(R.id.tv_chat_author);
            TextView lastMessageView = (TextView) view.findViewById(R.id.tv_chat_last_message);


            authorView.setText(chat.getChatMessage().getSenderId());
            lastMessageView.setText(chat.getChatMessage().getMessage());


            return view;
        }
    }

    public class Handler {


        public void ActionAny() {


        }


    }
}
