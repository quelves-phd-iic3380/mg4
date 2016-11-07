package edu.puc.iic3380.mg4.imagesdk;

/**
 * Created by quelves on 02/11/2016.
 */

import android.os.Environment;


public enum MyDirectory {
    DCIM(Environment.DIRECTORY_DCIM),
    DOWNLOADS(Environment.DIRECTORY_DOWNLOADS),
    DOCUMENTS(Environment.DIRECTORY_DOCUMENTS),
    STORE(Environment.getExternalStorageDirectory().getAbsolutePath()),
    PICTURES(Environment.DIRECTORY_PICTURES);

    public final String dir;

    private MyDirectory(String dir) {
        this.dir = dir;
    }
    public  String getPath() {
        return dir;
    }
}