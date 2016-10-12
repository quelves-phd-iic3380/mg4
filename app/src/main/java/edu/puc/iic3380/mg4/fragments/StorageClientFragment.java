package edu.puc.iic3380.mg4.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.FileDescriptor;
import java.io.IOException;

import edu.puc.iic3380.mg4.R;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * A simple {@link Fragment} subclass.
 */
public class StorageClientFragment extends Fragment {
    // A request code's purpose is to match the result of a "startActivityForResult" with
    // the type of the original request.  Choose any value.
    private static final int READ_REQUEST_CODE = 1337;

    public static final String TAG = "StorageClientFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        performFileSearch();
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // BEGIN_INCLUDE (use_open_document_intent)
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a file (as opposed to a list
        // of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers, it would be
        // "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
        // END_INCLUDE (use_open_document_intent)
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(TAG, "Received an \"Activity Result\"");
        // BEGIN_INCLUDE (parse_open_document_response)
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                showImage(uri);
            }
            // END_INCLUDE (parse_open_document_response)
        }
    }

    /**
     * Given the URI of an image, shows it on the screen using a DialogFragment.
     *
     * @param uri the Uri of the image to display.
     */
    public void showImage(Uri uri) {
        // BEGIN_INCLUDE (create_show_image_dialog)
        if (uri != null) {
            // Since the URI is to an image, create and show a DialogFragment to display the
            // image to the user.
            FragmentManager fm = getActivity().getSupportFragmentManager();
            ImageViewFragment imageViewFragment = new ImageViewFragment();
            Bundle fragmentArguments = new Bundle();
            fragmentArguments.putParcelable("URI", uri);
            imageViewFragment.setArguments(fragmentArguments);
            FragmentTransaction transaction = fm.beginTransaction();

            // If fragment is already added, replace it.
            if (fm.findFragmentByTag(ContactsFragment.TAG) != null) {
                transaction = transaction.replace(edu.puc.iic3380.mg4.R.id.fASFView,
                        imageViewFragment, null);
            } else {
                transaction = transaction.add(R.id.fASFView,
                        imageViewFragment, imageViewFragment.TAG);
            }
            transaction.commit();



        }
        // END_INCLUDE (create_show_image_dialog)
    }


    /**
     * DialogFragment which displays an image, given a URI.
     */
    public static class ImageViewFragment extends Fragment {
        public static final String TAG = "ImageViewFragment";
        private Dialog mDialog;
        private Uri mUri;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUri = getArguments().getParcelable("URI");
            execute();
        }

        /** Create a Bitmap from the URI for that image and return it.
         *
         * @param uri the Uri for the image to return.
         */
        private Bitmap getBitmapFromUri(Uri uri) {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor =
                        getActivity().getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                return image;
            } catch (Exception e) {
                Log.e(TAG, "Failed to load image.", e);
                return null;
            } finally {
                try {
                    if (parcelFileDescriptor != null) {
                        parcelFileDescriptor.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error closing ParcelFile Descriptor");
                }
            }
        }

        private void execute() {


            final ImageView imageView = (ImageView)getActivity().findViewById(R.id.ivPreview);
            final LinearLayout flSAFSelection = (LinearLayout)getActivity().findViewById(R.id.my_gallery);
            final ImageView ivToAdd = new ImageView(getActivity().getBaseContext());
            flSAFSelection.addView(ivToAdd);
            ivToAdd.setVisibility(View.VISIBLE);

            // BEGIN_INCLUDE (show_image)
            // Loading the image is going to require some sort of I/O, which must occur off the UI
            // thread.  Changing the ImageView to display the image must occur ON the UI thread.
            // The easiest way to divide up this labor is with an AsyncTask.  The doInBackground
            // method will run in a separate thread, but onPostExecute will run in the main
            // UI thread.
            AsyncTask<Uri, Void, Bitmap> imageLoadAsyncTask = new AsyncTask<Uri, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Uri... uris) {
                    dumpImageMetaData(uris[0]);
                    return getBitmapFromUri(uris[0]);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    Log.i(TAG, "onPostExecute" + imageView.getId());
                    imageView.setImageBitmap(bitmap);
                    ivToAdd.setImageBitmap(bitmap);
                }
            };
            imageLoadAsyncTask.execute(mUri);
        }

        /**
         * Grabs metadata for a document specified by URI, logs it to the screen.
         *
         * @param uri The uri for the document whose metadata should be printed.
         */
        public void dumpImageMetaData(Uri uri) {
            // BEGIN_INCLUDE (dump_metadata)

            // The query, since it only applies to a single document, will only return one row.
            // no need to filter, sort, or select fields, since we want all fields for one
            // document.
            Cursor cursor = getActivity().getContentResolver()
                    .query(uri, null, null, null, null, null);

            try {
                // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor != null && cursor.moveToFirst()) {

                    // Note it's called "Display Name".  This is provider-specific, and
                    // might not necessarily be the file name.
                    String displayName = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    Log.i(TAG, "Display Name: " + displayName);

                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    // If the size is unknown, the value stored is null.  But since an int can't be
                    // null in java, the behavior is implementation-specific, which is just a fancy
                    // term for "unpredictable".  So as a rule, check if it's null before assigning
                    // to an int.  This will happen often:  The storage API allows for remote
                    // files, whose size might not be locally known.
                    String size = null;
                    if (!cursor.isNull(sizeIndex)) {
                        // Technically the column stores an int, but cursor.getString will do the
                        // conversion automatically.
                        size = cursor.getString(sizeIndex);
                    } else {
                        size = "Unknown";
                    }
                    Log.i(TAG, "Size: " + size);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            // END_INCLUDE (dump_metadata)
        }
    }

}
