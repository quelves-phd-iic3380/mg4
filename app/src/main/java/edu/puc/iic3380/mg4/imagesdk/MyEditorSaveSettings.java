package edu.puc.iic3380.mg4.imagesdk;

import android.os.Environment;
import android.os.Parcel;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.io.File;

import ly.img.android.sdk.models.state.EditorSaveSettings;

/**
 * Created by quelves on 02/11/2016.
 */

public class MyEditorSaveSettings extends EditorSaveSettings {
    private int jpegQuality = 80;
    private EditorSaveSettings.SavePolicy savePolicy;
    private String outputFolderPath;
    private String filePrefix;
    private String outputFilePath;

    public MyEditorSaveSettings() {
    }

    public MyEditorSaveSettings(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    public EditorSaveSettings setExportDir(@NonNull MyDirectory directory, @NonNull String folderName) {
        File mMediaFolder = new File(Environment.getExternalStoragePublicDirectory(directory.dir), folderName);
        this.outputFilePath = null;
        this.outputFolderPath = mMediaFolder.getAbsolutePath();
        this.notifyPropertyChanged(EditorSaveSettings.Event.OUTPUT_PATH);
        return this;
    }

    @NonNull
    @Override
    public EditorSaveSettings setExportDir(String path) {
        return super.setExportDir(path);
    }

    @Override
    public EditorSaveSettings setExportPrefix(String prefix) {
        return super.setExportPrefix(prefix);
    }

    @Override
    public EditorSaveSettings setOutputFilePath(@NonNull String outputPath) {
        return super.setOutputFilePath(outputPath);
    }

    @Override
    public EditorSaveSettings setSavePolicy(SavePolicy savePolicy) {
        return super.setSavePolicy(savePolicy);
    }

    @Override
    public EditorSaveSettings setJpegQuality(@IntRange(from = 0L, to = 100L) int jpegQuality) {
        return super.setJpegQuality(jpegQuality);
    }

    @Override
    public SavePolicy getSavePolicy() {
        return super.getSavePolicy();
    }

    @Override
    public int getJpegQuality() {
        return super.getJpegQuality();
    }

    @Override
    public String generateOutputFilePath() {
        return super.generateOutputFilePath();
    }
}
