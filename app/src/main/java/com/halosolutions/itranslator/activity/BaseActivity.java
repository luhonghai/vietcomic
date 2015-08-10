package com.halosolutions.itranslator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.utilities.AnalyticHelper;
import com.halosolutions.itranslator.utilities.AndroidHelper;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.fabric.sdk.android.Fabric;

/**
 * Created by longnguyen on 7/7/15.
 *
 */
public abstract class BaseActivity extends Activity {
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker t = AnalyticHelper.getTracker(this);
        if (t != null) {
            t.setScreenName(this.getClass().getName());
            t.send(new HitBuilders.ScreenViewBuilder().build());
        }

        Fabric.with(this, new Crashlytics());

        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (ImageLoader.getInstance().isInited())
//            ImageLoader.getInstance().destroy();
    }

    @Override
    protected void onPause() {
        isRunning = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        isRunning = true;
        super.onResume();
    }

    public boolean checkNetwork() {
        return checkNetwork(true);
    }

    public boolean checkNetwork(final boolean closeApp) {
        boolean isNetworkAvailable = AndroidHelper.isNetworkAvailable(this);
        if (!isNetworkAvailable) {
            showErrorNetworkMessage(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    if (closeApp) {
                        BaseActivity.this.finish();
                    } else {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }
            });
        }
        return isNetworkAvailable;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    protected void showErrorNetworkMessage(SweetAlertDialog.OnSweetClickListener onConfirmListener) {
        SweetAlertDialog d = new SweetAlertDialog(BaseActivity.this, SweetAlertDialog.ERROR_TYPE);
        d.setTitleText(getString(R.string.error_network_title));
        d.setContentText(getString(R.string.error_network_message));
        d.setConfirmText(getString(R.string.dialog_ok));
        if (onConfirmListener == null) {
            onConfirmListener = new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                }
            };
        }
        d.setConfirmClickListener(onConfirmListener);
        d.show();
    }
}
