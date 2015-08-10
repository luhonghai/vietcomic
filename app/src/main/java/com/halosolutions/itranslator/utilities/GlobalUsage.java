package com.halosolutions.itranslator.utilities;

import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by longnguyen on 06/09/15.
 *
 */
public class GlobalUsage {
    public static String sourceTxt="";
    /**
     * Check if user first use
     * @return
     */
    public static boolean isFirstUser(){
        DataParser parser = new DataParser();

        StringBuffer sb = new StringBuffer();
        sb.append(FilePath.getPackageInfo().applicationInfo.dataDir)
                .append(File.separator).append("iTranslator")
                .append(File.separator).append("globalUsage.json");
        boolean isFirst = parser.parseIsFirstUsage(sb.toString());

        return isFirst;
    }

    /**
     * Set value of first user
     */
    public static void setIsNotFirstUser(){
        StringBuffer sb = new StringBuffer();
        sb.append(FilePath.getPackageInfo().applicationInfo.dataDir)
                .append(File.separator).append("iTranslator")
                .append(File.separator).append("globalUsage.json");

        JsonWriter writer;
        try {
            writer = new JsonWriter(new FileWriter(sb.toString()));
            writer.beginObject();
            writer.name("firstUsage").value("0");
            writer.endObject();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
