package com.halosolutions.itranslator.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.halosolutions.itranslator.R;

public class PrefActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref);

        Toolbar mToolbar = (Toolbar)findViewById(R.id.pref_toolbar);
        mToolbar.setTitle("Camera Settings");

        if(savedInstanceState == null){
            getFragmentManager().beginTransaction()
                    .add(R.id.pref_main, new PrefFragment()).commit();
        }
    }


}
