package edu.puc.iic3380.mg4.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class FileManager {
    public static final String TAG = "FileManager";
    private FileManager() {}

    public final static String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MemeticaMe";
    public final static String MEME_PATH = BASE_PATH + "/memes/";

    private final static int BUFFER_SIZE = 4096;


    public static Uri saveBitmap(Context context, Bitmap bitmap) throws IOException {

        String path = BASE_PATH;
        File directory = new File(path);
        directory.mkdirs();

        OutputStream fOut = null;
        File file = new File(path, generateFileName("image/jpeg")); // the File to save to
        fOut = new FileOutputStream(file);

        Bitmap pictureBitmap = bitmap;
        pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        fOut.flush();
        fOut.close(); // do not forget to close the stream

        MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(),
                file.getName(), file.getName());
        return Uri.fromFile(file);
    }


    public static String loadBase64(Context context, Uri uri) throws IOException {

        InputStream inputStream=null;
        ByteArrayOutputStream buffer=null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] byteBuffer = new byte[16384];
            while ((nRead = inputStream.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                buffer.write(byteBuffer, 0, nRead);
            }

            buffer.flush();
            byte[] byteArray = buffer.toByteArray();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        finally {
            if (inputStream!=null) {
                inputStream.close();
            }
            if (buffer!=null) {
                buffer.close();
            }
        }
    }

    public static String saveBase64(String base64EncodedData, String mimeType) throws IOException {
        return saveBase64(base64EncodedData, mimeType, getSubfolderName(mimeType));
    }

    public static String saveBase64(String base64EncodedData, String mimeType, String subfolder) throws IOException {
        String path = BASE_PATH + "/" + subfolder + "/" + generateFileName(mimeType);
        new File(path.substring(0, path.lastIndexOf('/'))).mkdirs();

        byte[] decoded = Base64.decode(base64EncodedData, Base64.DEFAULT);

        FileOutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(path);
            outputStream.write(decoded);
        }
        finally {
            if (outputStream!=null) {
                outputStream.close();
            }
        }
        return path;
    }


    public static String generateFileName(String mimeType) {
        new File(BASE_PATH).mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (mimeType.equals("meme/audio")) {
            extension="mma";
        }
        if (extension==null) {
            extension="bin";
        }
        if (extension.equals("")) {
            extension="bin";
        }
        return timestamp + "." + extension;
    }

    public static String getSubfolderName(String mimeType) {
        if (mimeType.startsWith("image/")) {
            return "images";
        }
        else {
            return "files";
        }
    }

    public static Uri downloadFile(String urlToDownload, String path) {
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(new File(path));
    }

    /**
     * Firebase storage functions
     */
    public static void storageuploadFile(File file, StorageMetadata metadata, StorageReference fileRef) {
        try {
            InputStream stream = new FileInputStream(file);

            // Upload file and metadata to the path of file
            UploadTask uploadTask = fileRef.putStream(stream, metadata);

            // Listen for state changes, errors, and completion of the upload.
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Handle successful uploads on complete
                    Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                    Log.d(TAG, "donwloadURl: " + downloadUrl.getPath());
                }
            });
        }
        catch (Exception e) {
            Log.d(TAG, "Error in upload", e);
        }
    }

    public static File storageDownloadFile(String filename, StorageMetadata metadata, StorageReference fileRef) {

        File localFile = new File(filename);

        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "storageDownloadFile sucess! bytes: " + taskSnapshot.getTotalByteCount());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Error", exception);
            }
        });

        return localFile;
    }


}
