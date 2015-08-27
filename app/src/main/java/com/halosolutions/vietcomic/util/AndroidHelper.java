package com.halosolutions.vietcomic.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.halosolutions.vietcomic.R;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by cmg on 14/08/15.
 */
public class AndroidHelper {

    public static final String THUMBNAIL_DIR = "thumbnails";

    public static final String DOWNLOADED_BOOK_DIR = "comics";

    public static final String DOWNLOAD_TEMP_CACHE_DIR = "download_tmp_cache";

    private static final String VIET_COMIC_DIR = "Vietcomic";

    public static File getApplicationDir(Context context) {
        //Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        File dir = new File(context.getFilesDir(), VIET_COMIC_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        return dir;
//        PackageManager m = context.getPackageManager();
//        String s = context.getPackageName();
//        try {
//            PackageInfo p = m.getPackageInfo(s, 0);
//            return new File(p.applicationInfo.dataDir);
//        } catch (PackageManager.NameNotFoundException e) {
//            return new File(Environment.getExternalStorageDirectory().getPath() + File.separator + VIET_COMIC_DIR);
//        }
    }

    public static File getFolder(Context context, String folderName) {
        File rootApp = getApplicationDir(context);
        File folder = new File(rootApp, folderName);
        if (!folder.exists() || !folder.isDirectory())
            folder.mkdirs();
        return folder;
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

    public static int getScreenWidth(Context context) {
        Point size = new Point();
        WindowManager w = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            return size.x;
        }else{
            Display d = w.getDefaultDisplay();
            return d.getWidth();
        }
    }

    public static int getScreenHeight(Context context) {
        Point size = new Point();
        WindowManager w = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            return size.y;
        }else{
            Display d = w.getDefaultDisplay();
            return d.getHeight();
        }
    }

    public static String getKeyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo("com.cmg.android.bbcaccent", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }
        return "";
    }



    public static String getVersionCode(Context context) {
        String version;
        try {
            version = Integer.toString(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            version = "Unknown";
        }
        return version;
    }

    public static String getVersionName(Context context) {
        String version;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "Unknown";
        }
        return version;
    }

    private static String ADS_ID;
    // Do not call this function from the main thread. Otherwise,
    // an IllegalStateException will be thrown.
    public static String getAdsId(Context mContext) {
        if (ADS_ID != null && ADS_ID.length() > 0) {
            return ADS_ID;
        }

        if (ADS_ID == null || ADS_ID.length() == 0) {
            AdvertisingIdClient.Info adInfo;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
                ADS_ID = adInfo == null ? "" : adInfo.getId();
            } catch (Exception e) {
                // Google Play services is not available entirely.
                SimpleAppLog.error("Could not fetch Ads ID", e);
            }
        }
        return ADS_ID;
    }


    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivity == null) {
                return false;
            } else {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static int getScreenSize(Context context) {
        int screenSize = context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        String toastMsg;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                toastMsg = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                toastMsg = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                toastMsg = "Small screen";
                break;
            default:
                toastMsg = "Screen size is neither large, normal or small";
        }
        return screenSize;
    }


    public static boolean isCorrectWidth(TextView textView, String text)
    {
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTypeface(textView.getTypeface());
        float textSize = textView.getTextSize();
        paint.setTextSize(textSize);
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width() <= textView.getWidth();
    }

    public static boolean isGreatThanApiLevel9() {
        return (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD);
    }

    public static boolean isLowerThanApiLevel11() {
        return (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB);
    }

    public static boolean isLowerThanApiLevel12() {
        return (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR1);
    }
}
