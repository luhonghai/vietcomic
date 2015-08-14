package com.halosolutions.vietcomic.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;

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
}
