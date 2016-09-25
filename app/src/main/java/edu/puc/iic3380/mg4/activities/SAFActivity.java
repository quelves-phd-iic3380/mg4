package edu.puc.iic3380.mg4.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import edu.puc.iic3380.mg4.R;
import edu.puc.iic3380.mg4.fragments.SAFSelectionFragment;
import edu.puc.iic3380.mg4.fragments.SAFViewFragment;
import edu.puc.iic3380.mg4.fragments.StorageClientFragment;

public class SAFActivity extends AppCompatActivity implements SAFViewFragment.OnFragmentInteractionListener, SAFSelectionFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saf);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // If fragment is already added, replace it.
        if (getSupportFragmentManager().findFragmentByTag(StorageClientFragment.TAG) != null) {
            transaction = transaction.replace(R.id.flSAFActivity, new StorageClientFragment(), null);
        } else {
            transaction = transaction.add(R.id.flSAFActivity, new StorageClientFragment(), StorageClientFragment.TAG);
        }
        transaction.commit();


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SAFActivity.class);
        return intent;
    }
}
