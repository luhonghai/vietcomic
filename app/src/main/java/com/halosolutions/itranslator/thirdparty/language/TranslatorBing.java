/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.thirdparty.language;

import android.util.Log;

import com.halosolutions.itranslator.constant.Constant;
import com.halosolutions.itranslator.thirdparty.CaptureActivity;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

/**
 * Created by longnguyen on 6/29/15.
 */
public class TranslatorBing {
    private static final String TAG = TranslatorBing.class.getSimpleName();
    private static final String CLIENT_ID = Constant.client_id;
    private static final String CLIENT_SECRET = Constant.client_secret;

    /**
     *  Translate using Microsoft Translate API
     * @param sourceLanguageCode Source language code, for example, "en"
     * @param targetLanguageCode Target language code, for example, "es"
     * @param sourceText Text to send for translation
     * @return Translated text
     */
    static String translate(String sourceLanguageCode, String targetLanguageCode, String sourceText) {
        Translate.setClientId(CLIENT_ID);
        Translate.setClientSecret(CLIENT_SECRET);
        try {
            Log.d(TAG, sourceLanguageCode + " -> " + targetLanguageCode);
            return Translate.execute(sourceText, Language.fromString(sourceLanguageCode),
                    Language.fromString(targetLanguageCode));
        } catch (Exception e) {
            Log.e(TAG, "Caught exeption in translation request.");
            e.printStackTrace();
            return Translator.BAD_TRANSLATION_MSG;
        }
    }

    /**
     * Convert the given name of a natural language into a Language from the enum of Languages
     * supported by this translation service.
     *
     * @param languageName The name of the language, for example, "English"
     * @return code representing this language, for example, "en", for this translation API
     * @throws IllegalArgumentException
     */
    public static String toLanguage(String languageName) throws IllegalArgumentException {
        // Convert string to all caps
        String standardizedName = languageName.toUpperCase();

        // Replace spaces with underscores
        standardizedName = standardizedName.replace(' ', '_');

        // Remove parentheses
        standardizedName = standardizedName.replace("(", "");
        standardizedName = standardizedName.replace(")", "");

        // Map Norwegian-Bokmal to Norwegian
        if (standardizedName.equals("NORWEGIAN_BOKMAL")) {
            standardizedName = "NORWEGIAN";
        }

        try {
            return Language.valueOf(standardizedName).toString();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Not found--returning default language code");
            return CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE;
        }
    }
}
