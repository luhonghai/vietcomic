package com.halosolutions.vietcomic.comic;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halosolutions.vietcomic.R;
import com.halosolutions.vietcomic.util.AndroidHelper;
import com.halosolutions.vietcomic.util.Hash;
import com.halosolutions.vietcomic.util.SimpleAppLog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Created by luhonghai on 8/14/15.
 */
public class ComicVersion {

    private static final String COMIC_BOOK_DATA_FOLDER = "comic_data";

    private int version;
    private String url;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static ComicVersion getComicVersion(Context context) {
        Gson gson = new Gson();
        try {
            File v = new File(AndroidHelper.getApplicationDir(context), "version.json");
            String raw;
            if (!v.exists()) {
                raw = IOUtils.toString(context.getAssets().open("comic/version.json"), "UTF-8");
                FileUtils.writeStringToFile(v, raw, "UTF-8");
            } else {
                raw = FileUtils.readFileToString(v, "UTF-8");
            }
            SimpleAppLog.info("Comic data version: " + raw);
            return gson.fromJson(raw, ComicVersion.class);
        } catch (Exception e) {
            SimpleAppLog.error("Could not load version", e);
        }
        return null;
    }

    public void saveComicVersion(Context context, ComicVersion version) {
        Gson gson = new Gson();
        try {
            File v = new File(AndroidHelper.getApplicationDir(context), "version.json");
            FileUtils.writeStringToFile(v, gson.toJson(version), "UTF-8");
        } catch (Exception e) {
            SimpleAppLog.error("Could not save comic version",e);
        }
    }

    public static ComicVersion fetchComicVersion(Context context) {
        File tmp = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString() + ".version.json");
        String url = context.getString(R.string.url_version);
        try {
            FileUtils.copyURLToFile(new URL(url), tmp);
            return new Gson().fromJson(FileUtils.readFileToString(tmp, "UTF-8"), ComicVersion.class);
        } catch (Exception e) {
            SimpleAppLog.error("Could not fetch version from " + url,e);
        } finally {
            if (tmp.exists()) {
                try {
                    FileUtils.forceDelete(tmp);
                } catch (Exception e) {
                    SimpleAppLog.error("Could not delete temp version " + tmp, e);
                }
            }
        }
        return null;
    }

    public static void deleteBookData(Context context, ComicVersion version) {
        File folder = new File(AndroidHelper.getApplicationDir(context), COMIC_BOOK_DATA_FOLDER);
        File data = new File(folder, Hash.md5(version.getUrl() + ".json"));
        if (data.exists()) {
            try {
                FileUtils.forceDelete(data);
            } catch (Exception e) {
                SimpleAppLog.error("Could not delete comic data version " + version.getVersion()
                        + ". URL " + version.getUrl()
                        + ". Data: " +data,e);
            }
        }
    }

    public static List<ComicBook> getBookData(Context context, ComicVersion version) {
        String url = version.getUrl();
        File folder = new File(AndroidHelper.getApplicationDir(context), COMIC_BOOK_DATA_FOLDER);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        File data = new File(folder, Hash.md5(url + ".json"));
        if (!data.exists()) {
            if (!(url.startsWith("http") || url.startsWith("https"))) {
                try {
                    FileUtils.copyURLToFile(new URL(url), data);
                } catch (IOException e) {
                    SimpleAppLog.error("Could not download comic data from url " + url, e );
                }
            } else {
                try {
                    FileUtils.copyInputStreamToFile(context.getAssets().open(url),data);
                } catch (IOException e) {
                    SimpleAppLog.error("Could not save comic data from url " + url, e);
                }
            }
        }
        if (data.exists()) {
            try {
                return new Gson().fromJson(FileUtils.readFileToString(data, "UTF-8"), new TypeToken<List<ComicBook>>(){}.getType());
            } catch (Exception e) {
                SimpleAppLog.error("Could not get book data from file " + data,e);
            }
        }
        return null;
    }
}
