package com.halosolutions.itranslator.utilities;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halosolutions.itranslator.model.Language;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by longnguyen on 06/09/15.
 *
 */
public class DataParser {
    public DataParser(){}

    /**
     * Check if user first use or not
     * @param filename
     * @return
     */
    public boolean parseIsFirstUsage(String filename){
        boolean isFirst=true;
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while((line = br.readLine()) != null){
                JSONObject obj = new JSONObject(line);
                if(obj.getString("firstUsage").equals("1")){
                    Log.i("Check Usage", "first usage: " + obj.getString("firstUsage"));
                    isFirst = true;
                }else{
                    Log.i("Check Usage", "first usage: " + obj.getString("firstUsage"));
                    isFirst = false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isFirst;
    }

    /**
     * Parsing language object from file
     * @param filename
     * @return
     */
    public Language parsingLanguage(String filename){
        String line;
        Language lang = new Language();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while((line = br.readLine()) != null){
                JSONObject obj = new JSONObject(line);
                lang.setLanguage1(obj.getString("language1"));
                lang.setLanguage2(obj.getString("language2"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (lang.getLanguage1() == null || lang.getLanguage1().length() == 0) {
            lang.setLanguage1("English (UK)");
        }
        if (lang.getLanguage2() == null || lang.getLanguage2().length() == 0) {
            lang.setLanguage2("French (FR)");
        }
        return lang;
    }

    /**
     * Read file from sd card
     * @param fileName
     * @return
     * @throws IOException
     */
    public String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
