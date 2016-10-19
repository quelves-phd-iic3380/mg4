package edu.puc.iic3380.mg4.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.fragments.dummy.DummyContent;
import edu.puc.iic3380.mg4.fragments.dummy.DummyContent.DummyItem;
import edu.puc.iic3380.mg4.model.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private ArrayList<Chat> chatList;
    private OnChatSelected mListener;
    private ChatAdapter mAdapter;
    private ListView mChattsListView;

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

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mChattsListView = (ListView) view.findViewById(R.id. chats_list_view);
        mChattsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onChatSelected(chatList.get(position));
            }
        });


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
                view = mLayoutInflater.inflate(R.layout.contact_list_item, parent, false);
            }
            Chat chat = mChats.get(position);

            TextView nameView = (TextView) view.findViewById(R.id.contact_name);


            nameView.setText(chat.getName());


            return view;
        }
    }
}
