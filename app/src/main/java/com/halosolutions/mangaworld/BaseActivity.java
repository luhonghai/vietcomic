package com.halosolutions.mangaworld;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.halosolutions.mangaworld.R;
import com.halosolutions.mangaworld.util.AnalyticHelper;
import com.halosolutions.mangaworld.util.AndroidHelper;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cmg on 14/08/15.
 */
public class BaseActivity extends AppCompatActivity {
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
        if (!ImageLoader.getInstance().isInited()) {
            File cacheDir = AndroidHelper.getFolder(this,AndroidHelper.THUMBNAIL_DIR);
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .threadPoolSize(5)
                    .threadPriority(Thread.NORM_PRIORITY - 1)
                    .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                    .memoryCache(new LruMemoryCache(4 * 1024 * 1024))
                    .memoryCacheSize(4 * 1024 * 1024)
                    .memoryCacheSizePercentage(13) // default
                    .diskCache(new UnlimitedDiscCache(cacheDir)) // default
                    .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                    .build();
            ImageLoader.getInstance().init(config);
        }
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
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

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
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
        try {
            if (isRunning) {
                SweetAlertDialog d = new SweetAlertDialog(BaseActivity.this, SweetAlertDialog.ERROR_TYPE);
                d.setTitleText("Không có kết nối mạng");
                d.setContentText("Vui lòng kiểm tra kết nối mạng 3G/Wifi của bạn");
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
        } catch (Exception e) {
            SimpleAppLog.error("Could not show error network message",e);
        }

    }
}
