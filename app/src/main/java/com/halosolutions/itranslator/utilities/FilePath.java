package com.halosolutions.itranslator.utilities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.halosolutions.itranslator.model.Language;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by longnguyen on 06/09/15.
 *
 */
public class FilePath {
    private static Context context;

    public FilePath(Context context){this.context = context;}

    /**
     * Get global usage folder
     * @return String
     */
    public static String getGlobalUsageFolder() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(getPackageInfo().applicationInfo.dataDir)
                    .append(File.separator).append("iTranslator");
            File file = new File(sb.toString());
            if(!file.exists()){
                file.mkdirs();
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Check the first time usage
     * @return String
     */

    public String checkUsage(){
        getGlobalUsageFolder();
        StringBuffer sb = new StringBuffer();
        try {
            sb.append(getPackageInfo().applicationInfo.dataDir)
                    .append(File.separator).append("iTranslator")
                    .append(File.separator).append("globalUsage.json");
            File f = new File(sb.toString());
            if(!f.exists()){
                JSONObject obj = new JSONObject();
                obj.put("firstUsage","1");
                FileWriter file = new FileWriter(f);
                file.write(obj.toString());
                file.flush();
                file.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * get json path
     * @return String
     */
    public String getLanguageJsonPath(){
        StringBuffer sb = new StringBuffer();
        sb.append(getPackageInfo().applicationInfo.dataDir)
                    .append(File.separator).append("iTranslator")
                    .append(File.separator).append("language.json");
        return sb.toString();
    }

    /**
     * initial language for the first use
     * @return
     */
    public Language initLanguage(){
        Language lang = null;
        try {
            File f = new File(getLanguageJsonPath());
            if(!f.exists()){
                lang = new Language();
                lang.setLanguage1("English (UK)");
                lang.setLanguage2("French (FR)");
                Gson gson = new Gson();
                String json = gson.toJson(lang);
                //Log.i("json", json);
                FileWriter file = new FileWriter(f);
                file.write(json.toString());
                file.flush();
                file.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (lang == null) {
            lang = new Language();
            lang.setLanguage1("English (UK)");
            lang.setLanguage2("French (FR)");
        }
        return lang;
    }

    /**
     * get json path
     * @return
     */
    public String getHistoryJsonPath(){
        StringBuffer sb = new StringBuffer();
        sb.append(getPackageInfo().applicationInfo.dataDir)
                .append(File.separator).append("iTranslator")
                .append(File.separator).append("history.json");
        return sb.toString();
    }

    /**
     * Get Package Information
     * @return
     */
    public static PackageInfo getPackageInfo() {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Write json to file locally
     * @param filename
     * @param json
     * @return
     */
    public boolean writeFileToLocal(String filename, String json){
        boolean done = false;
        try {
            File f = new File(filename);
            if(f.exists()){
                FileWriter writer = new FileWriter(f);
                writer.write(json);
                writer.close();
                done = true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return done;
    }
}
