package edu.puc.iic3380.mg4.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import edu.puc.iic3380.mg4.BR;


public class ChatSettings extends BaseObservable implements Parcelable {
    private String mUsername;
    private String mChatRoom;
    private String mUserPhone;



    public ChatSettings(String username, String chatRoom, String userPhone) {
        mUsername = username;
        mChatRoom = chatRoom;
        mUserPhone = userPhone;
    }

    protected ChatSettings(Parcel in) {
        mUsername = in.readString();
        mChatRoom = in.readString();
        mUserPhone =  in.readString();
    }

    @Bindable
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public String getChatRoom() {
        return mChatRoom;
    }

    public void setChatRoom(String chatRoom) {
        mChatRoom = chatRoom;
        notifyPropertyChanged(BR.chatRoom);
    }

    public String getmUserPhone() {
        return mUserPhone;
    }

    public void setmUserPhone(String mUserPhone) {
        this.mUserPhone = mUserPhone;
    }

    // Parcelable implementation

    public static final Creator<ChatSettings> CREATOR = new Creator<ChatSettings>() {
        @Override
        public ChatSettings createFromParcel(Parcel in) {
            return new ChatSettings(in);
        }

        @Override
        public ChatSettings[] newArray(int size) {
            return new ChatSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mChatRoom);
        dest.writeString(mUserPhone);
    }
}
