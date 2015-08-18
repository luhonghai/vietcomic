package com.halosolutions.vietcomic.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

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

    public static Map<Integer, Integer> getDrawableRateStar(float fRate) {
        Map<Integer, Integer> map = new Hashtable<Integer, Integer>();
        int rate = Math.round(fRate);
        int d1, d2, d3, d4, d5;
        switch (rate) {
            case 10:
                d1 = d2 = d3 = d4 = d5 = R.drawable.app_icon_star_full_red;
                break;
            case 9:
                d5 = R.drawable.app_icon_star_half_red;
                d1 = d2 = d3 = d4 = R.drawable.app_icon_star_full_red;
                break;
            case 8:
                d5 = R.drawable.app_icon_star_empty_red;
                d1 = d2 = d3 = d4 = R.drawable.app_icon_star_full_red;
                break;
            case 7:
                d5 = R.drawable.app_icon_star_empty_red;
                d4 = R.drawable.app_icon_star_half_red;
                d1 = d2 = d3 = R.drawable.app_icon_star_full_red;
                break;
            case 6:
                d4 = d5 = R.drawable.app_icon_star_empty_red;
                d1 = d2 = d3 = R.drawable.app_icon_star_full_red;
                break;
            case 5:
                d4 = d5 = R.drawable.app_icon_star_empty_red;
                d3 = R.drawable.app_icon_star_half_red;
                d1 = d2 = R.drawable.app_icon_star_full_red;
                break;
            case 4:
                d3 = d4 = d5 = R.drawable.app_icon_star_empty_red;
                d1 = d2 = R.drawable.app_icon_star_full_red;
                break;
            case 3:
                d3 = d4 = d5 = R.drawable.app_icon_star_empty_red;
                d2 = R.drawable.app_icon_star_half_red;
                d1 = R.drawable.app_icon_star_full_red;
                break;
            case 2:
                d2 = d3 = d4 = d5 = R.drawable.app_icon_star_empty_red;
                d1 = R.drawable.app_icon_star_full_red;
                break;
            case 1:
                d2 = d3 = d4 = d5 = R.drawable.app_icon_star_empty_red;
                d1 = R.drawable.app_icon_star_half_red;
                break;
            case 0:
            default:
                d1 = d2 = d3 = d4 = d5 = R.drawable.app_icon_star_empty_red;
                break;
        }
        map.put(1, d1);
        map.put(2, d2);
        map.put(3, d3);
        map.put(4, d4);
        map.put(5, d5);
        return map;
    }

    public static void updateImageView(final Context context, final View view, int imgId, int drawableId) {
        ((ImageView) view.findViewById(imgId))
                .setImageDrawable(
                        context.getResources()
                                .getDrawable(drawableId));
    }
}
