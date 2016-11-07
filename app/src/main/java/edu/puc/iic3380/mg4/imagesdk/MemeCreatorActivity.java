package edu.puc.iic3380.mg4.imagesdk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import edu.puc.iic3380.mg4.R;
import ly.img.android.sdk.models.state.EditorSaveSettings;
import ly.img.android.sdk.models.state.manager.SettingsList;
import ly.img.android.ui.activities.CameraPreviewActivity;
import ly.img.android.ui.activities.CameraPreviewBuilder;
import ly.img.android.ui.utilities.PermissionRequest;

import static edu.puc.iic3380.mg4.util.Constantes.GENERIC_FILE_CODE;
import static edu.puc.iic3380.mg4.util.Constantes.PICK_IMAGE_CODE;


public class MemeCreatorActivity extends AppCompatActivity implements PermissionRequest.Response {
    public static final String TAG = "MainActivity";

    private static final String FOLDER = "ImgLy";
    public static int CAMERA_PREVIEW_RESULT = 1;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_creator);

        imageView = (ImageView)findViewById(R.id.ivMeme);

        //Directory store = new Directory("STORE", 1, Environment.getExternalStorageDirectory().getAbsolutePath());
        SettingsList settingsList = new SettingsList();
        settingsList
                // Set custom camera export settings
                .getSettingsModel(MyCameraSettings.class)
                .setExportDir(MyDirectory.STORE, FOLDER)
                .setExportPrefix("camera_")
                // Set custom editor export settings
                .getSettingsModel(MyEditorSaveSettings.class)
                .setExportDir(MyDirectory.STORE, FOLDER)
                .setExportPrefix("result_")
                .setSavePolicy(
                        EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT
                );

        Log.d(TAG, "output: " + settingsList.toString());

        new CameraPreviewBuilder(this)
                .setSettingsList(settingsList)
                .startActivityForResult(this, CAMERA_PREVIEW_RESULT);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_open_galery) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_CODE);
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_PREVIEW_RESULT) {
            String resultPath =
                    data.getStringExtra(CameraPreviewActivity.RESULT_IMAGE_PATH);
            String sourcePath =
                    data.getStringExtra(CameraPreviewActivity.SOURCE_IMAGE_PATH);

            if (resultPath != null) {
                // Scan result file
                File file =  new File(resultPath);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);
                sendBroadcast(scanIntent);
            }

            if (sourcePath != null) {
                // Scan camera file
                File file =  new File(sourcePath);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);
                sendBroadcast(scanIntent);

                imageView.setImageURI(contentUri);
            }

            Toast.makeText(this, "Image Save on: " + resultPath, Toast.LENGTH_LONG).show();
        } else
        if (requestCode == GENERIC_FILE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri));
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            /*
            ConversationFragment conversationFragment =
                    (ConversationFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.ChatActivity_ConversationFragment);
            conversationFragment.sendFileMessage(uri, type);
            */
        }
        else if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String mimeType = getContentResolver().getType(uri);
            /*
            ConversationFragment conversationFragment =
                    (ConversationFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.ChatActivity_ConversationFragment);
            conversationFragment.sendFileMessage(uri, mimeType);
            */
        }
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

