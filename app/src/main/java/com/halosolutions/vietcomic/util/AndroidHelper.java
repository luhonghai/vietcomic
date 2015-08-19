package com.halosolutions.vietcomic.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.halosolutions.vietcomic.R;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by cmg on 14/08/15.
 */
public class AndroidHelper {

    private static final String VIET_COMIC_DIR = "Vietcomic";

    public static File getApplicationDir(Context context) {
        PackageManager m = context.getPackageManager();
        String s = context.getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            return new File(p.applicationInfo.dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            return new File(Environment.getExternalStorageDirectory().getPath() + File.separator + VIET_COMIC_DIR);
        }
    }

    public static void showRateStar(final Context context, final LinearLayout parent, float fRate) {
        int size = parent.getChildCount();
        int rate = Math.round(fRate);
        if (size != 5) {
            SimpleAppLog.error("Not enough imageview child");
            return;
        }
        for (int i = 0; i < size; i++) {
            final ImageView img = (ImageView) parent.getChildAt(i);
            if (rate > (i * 2) + 1) {
                img.setImageDrawable(
                        context.getResources()
                                .getDrawable(R.drawable.app_icon_star_full_red));
            } else if (rate == (i * 2) + 1) {
                img.setImageDrawable(
                        context.getResources()
                                .getDrawable(R.drawable.app_icon_star_half_red));
            } else {
                img.setImageDrawable(
                        context.getResources()
                                .getDrawable(R.drawable.app_icon_star_empty_red));
            }
        }
    }

    public static void updateImageView(final Context context, final View view, int imgId, int drawableId) {
        ((ImageView) view.findViewById(imgId))
                .setImageDrawable(
                        context.getResources()
                                .getDrawable(drawableId));
    }
}
