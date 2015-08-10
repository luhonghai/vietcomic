/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.thirdparty.language;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.halosolutions.itranslator.activity.PrefFragment;

/**
 * Created by longnguyen on 6/29/15.
 */
public class Translator {
    public static final String BAD_TRANSLATION_MSG = "[Translation unavailable]";

    private Translator(Activity activity) {
        // Private constructor to enforce noninstantiability
    }

    static String translate(Activity activity, String sourceLanguageCode, String targetLanguageCode, String sourceText) {

        // Check preferences to determine which translation API to use--Google, or Bing.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String api = "Bing Translator";//prefs.getString(PrefFragment.KEY_TRANSLATOR, CaptureActivity.DEFAULT_TRANSLATOR);

        // Delegate the translation based on the user's preference.
        if (api.equals(PrefFragment.TRANSLATOR_BING)) {
            Log.i("","Bing Translate Go");
            // Get the correct code for the source language for this translation service.
            sourceLanguageCode = TranslatorBing.toLanguage(
                    LanguageCodeHelper.getTranslationLanguageName(activity.getBaseContext(), sourceLanguageCode));

            return TranslatorBing.translate(sourceLanguageCode, targetLanguageCode, sourceText);
        }
        /*else if (api.equals(PrefFragment.TRANSLATOR_GOOGLE)) {
            Log.i("", "Google Translate Go");
            // Get the correct code for the source language for this translation service.
            sourceLanguageCode = TranslatorGoogle.toLanguage(
                    LanguageCodeHelper.getTranslationLanguageName(activity.getBaseContext(), sourceLanguageCode));

            return TranslatorGoogle.translate(sourceLanguageCode, targetLanguageCode, sourceText);
        }*/
        return BAD_TRANSLATION_MSG;
    }
}
