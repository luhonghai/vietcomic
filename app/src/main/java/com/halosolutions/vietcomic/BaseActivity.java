package com.halosolutions.vietcomic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.halosolutions.vietcomic.util.AndroidHelper;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;

/**
 * Created by cmg on 14/08/15.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
