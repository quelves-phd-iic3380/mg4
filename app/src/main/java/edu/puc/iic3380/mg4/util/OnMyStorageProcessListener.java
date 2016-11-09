package edu.puc.iic3380.mg4.util;

import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;

/**
 * Created by quelves on 09/11/2016.
 */

public interface OnMyStorageProcessListener {
    public void loadImageView(ImageView imageView, StorageReference fileRef);
}
