package com.halosolutions.vietcomic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.halosolutions.vietcomic.util.AnalyticHelper;
import com.halosolutions.vietcomic.util.AndroidHelper;
import com.halosolutions.vietcomic.util.ExceptionHandler;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cmg on 14/08/15.
 */
public class BaseActivity extends AppCompatActivity {

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
}
