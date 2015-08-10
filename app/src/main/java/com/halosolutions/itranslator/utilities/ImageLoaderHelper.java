package com.halosolutions.itranslator.utilities;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.halosolutions.itranslator.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by longnguyen on 06/09/15.
 *
 */
public class ImageLoaderHelper {
    private static String TAG = "ImageLoaderHelper.class";

    private ImageLoaderHelper(){}

    /**
     * Loading image
     * @param context
     * @return
     */
    public static ImageLoader getImageLoader(Context context){
        try {
            if (ImageLoader.getInstance().isInited()) {
                return ImageLoader.getInstance();
            }
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.gb_round)
                    .showImageForEmptyUri(R.drawable.gb_round)
                    .showImageOnFail(R.drawable.gb_round).cacheInMemory()
                    .cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                    context)
                    .defaultDisplayImageOptions(defaultOptions).build();
            ImageLoader.getInstance().init(config);
            return ImageLoader.getInstance();
        } catch (Exception ex) {
            Log.e(TAG, "Error when get image loader instance: " + ex);
            return null;
        }
    }

    /**
     * Recycle bitmap to flush memory
     * @param view
     */
    public static void destroyBitmap(final ImageView view) {
        final Drawable drawable = view.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            final BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            final Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
    }

    /**
     * Recycle imagebutton to flush memory
     * @param view
     */
    public static void destroyBitmapBtn(final ImageButton view) {
        final Drawable drawable = view.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            final BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            final Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
    }

    /**
     * Load image from Drawable folder
     * @param context
     * @param filename
     * @return
     * @throws IOException
     */
    public static Drawable getAssetImage(Context context, String filename) throws IOException {
        AssetManager assets = context.getResources().getAssets();
        InputStream buffer = new BufferedInputStream((assets.open("drawable/" + filename + ".png")));
        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
