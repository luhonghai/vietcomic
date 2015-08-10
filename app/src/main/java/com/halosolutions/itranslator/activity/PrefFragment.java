package com.halosolutions.itranslator.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.thirdparty.CaptureActivity;
import com.halosolutions.itranslator.thirdparty.OcrCharacterHelper;
import com.halosolutions.itranslator.thirdparty.language.LanguageCodeHelper;
import com.halosolutions.itranslator.thirdparty.language.TranslatorBing;

public class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    // Preference keys not carried over from ZXing project
    public static final String KEY_SOURCE_LANGUAGE_PREFERENCE = "sourceLanguageCodeOcrPref";
    public static final String KEY_TARGET_LANGUAGE_PREFERENCE = "targetLanguageCodeTranslationPref";
    public static final String KEY_TOGGLE_TRANSLATION = "preference_translation_toggle_translation";
    public static final String KEY_CONTINUOUS_PREVIEW = "preference_capture_continuous";
    public static final String KEY_PAGE_SEGMENTATION_MODE = "preference_page_segmentation_mode";
    public static final String KEY_OCR_ENGINE_MODE = "preference_ocr_engine_mode";
    public static final String KEY_TOGGLE_LIGHT = "preference_toggle_light";

    // Preference keys carried over from ZXing project
    public static final String KEY_AUTO_FOCUS = "preferences_auto_focus";
    public static final String KEY_DISABLE_CONTINUOUS_FOCUS = "preferences_disable_continuous_focus";
    public static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";
    public static final String KEY_NOT_OUR_RESULTS_SHOWN = "preferences_not_our_results_shown";
    public static final String KEY_PLAY_BEEP = "preferences_play_beep";
    public static final String KEY_VIBRATE = "preferences_vibrate";

    public static final String TRANSLATOR_BING = "Bing Translator";

    private ListPreference listPreferenceSourceLanguage;
    private ListPreference listPreferenceTargetLanguage;
    private ListPreference listPreferenceOcrEngineMode;

    private static SharedPreferences sharedPreferences;

    public static PrefFragment newInstance(){
        PrefFragment fragment = new PrefFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        listPreferenceSourceLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_SOURCE_LANGUAGE_PREFERENCE);
        listPreferenceTargetLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_TARGET_LANGUAGE_PREFERENCE);
        listPreferenceOcrEngineMode = (ListPreference) getPreferenceScreen().findPreference(KEY_OCR_ENGINE_MODE);

        // Create the entries/entryvalues for the translation target language list.
        initTranslationTargetList();
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p/>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(KEY_SOURCE_LANGUAGE_PREFERENCE)) {
            // Set the summary text for the source language name
            listPreferenceSourceLanguage.setSummary(LanguageCodeHelper.getOcrLanguageName(getActivity(), sharedPreferences.getString(key, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)));

            // Retrieve the character blacklist/whitelist for the new language
            String blacklist = OcrCharacterHelper.getBlacklist(sharedPreferences, listPreferenceSourceLanguage.getValue());
            String whitelist = OcrCharacterHelper.getWhitelist(sharedPreferences, listPreferenceSourceLanguage.getValue());

        } else if (key.equals(KEY_TARGET_LANGUAGE_PREFERENCE)) {
            listPreferenceTargetLanguage.setSummary(LanguageCodeHelper.getTranslationLanguageName(getActivity(), sharedPreferences.getString(key, CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE)));
        } else if (key.equals(KEY_OCR_ENGINE_MODE)) {
            listPreferenceOcrEngineMode.setSummary(sharedPreferences.getString(key, CaptureActivity.DEFAULT_OCR_ENGINE_MODE));
        }
    }

    /**
     * Sets the list of available languages and the current target language for translation. Called
     * when the key for the current translator is changed.
     */
    void initTranslationTargetList() {
        // Set the preference for the target language code, in case we've just switched from Google
        // to Bing, or Bing to Google.
        String currentLanguageCode = sharedPreferences.getString(KEY_TARGET_LANGUAGE_PREFERENCE,
                CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE);

        // Get the name of our language
        String currentLanguage = LanguageCodeHelper.getTranslationLanguageName(getActivity(),currentLanguageCode);
        String newLanguageCode = "";
        listPreferenceTargetLanguage.setEntries(R.array.translationtargetlanguagenames_microsoft);
        listPreferenceTargetLanguage.setEntryValues(R.array.translationtargetiso6391_microsoft);

        // Get the corresponding code for our language name
        newLanguageCode = TranslatorBing.toLanguage(currentLanguage);

        // Store the code as the target language preference
        String newLanguageName = LanguageCodeHelper.getTranslationLanguageName(getActivity(),newLanguageCode);
        listPreferenceTargetLanguage.setValue(newLanguageName); // Set the radio button in the list
        sharedPreferences.edit().putString(KEY_TARGET_LANGUAGE_PREFERENCE, newLanguageCode).commit();
        listPreferenceTargetLanguage.setSummary(newLanguageName);
    }

    /**
     * Sets up initial preference summary text
     * values and registers the OnSharedPreferenceChangeListener.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Set up the initial summary values
        listPreferenceSourceLanguage.setSummary(LanguageCodeHelper.getOcrLanguageName(getActivity(), sharedPreferences.getString(KEY_SOURCE_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)));
        listPreferenceTargetLanguage.setSummary(LanguageCodeHelper.getTranslationLanguageName(getActivity(), sharedPreferences.getString(KEY_TARGET_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE)));
        listPreferenceOcrEngineMode.setSummary(sharedPreferences.getString(KEY_OCR_ENGINE_MODE, CaptureActivity.DEFAULT_OCR_ENGINE_MODE));

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when Activity is about to lose focus. Unregisters the
     * OnSharedPreferenceChangeListener.
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
