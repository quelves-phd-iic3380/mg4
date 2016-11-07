package edu.puc.iic3380.mg4.imagesdk;

import android.os.Environment;
import android.os.Parcel;
import android.support.annotation.NonNull;

import java.io.File;

import ly.img.android.sdk.models.state.CameraSettings;

/**
 * Created by quelves on 02/11/2016.
 */

public class MyCameraSettings extends CameraSettings {

    private String outputFolderPath = null;
    private String filePrefix = null;
    private String outputFilePath = null;
    private String cameraRollIntent = null;

    public MyCameraSettings() {
    }

    public MyCameraSettings(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @NonNull
    public CameraSettings setExportDir(@NonNull MyDirectory directory, @NonNull String folderName) {
        File mMediaFolder = new File(Environment.getExternalStoragePublicDirectory(directory.dir), folderName);
        this.outputFilePath = null;
        this.outputFolderPath = mMediaFolder.getAbsolutePath();
        this.notifyPropertyChanged(CameraSettings.Event.OUTPUT_PATH);
        return this;
    }

    @NonNull
    @Override
    public CameraSettings setExportDir(String path) {
        return super.setExportDir(path);
    }

    @Override
    public CameraSettings setExportPrefix(String prefix) {
        return super.setExportPrefix(prefix);
    }

    @Override
    public CameraSettings setOutputFilePath(@NonNull String outputPath) {
        return super.setOutputFilePath(outputPath);
    }
}
