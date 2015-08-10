package com.halosolutions.itranslator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.halosolutions.itranslator.R;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends BaseActivity {
    private ImageView circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_splash2);

        circle = (ImageView)findViewById(R.id.circle);
        circle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_rotate));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (checkNetwork(true)) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
