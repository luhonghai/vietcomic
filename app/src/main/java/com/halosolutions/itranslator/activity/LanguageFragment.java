/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.halosolutions.itranslator.BuildConfig;
import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.constant.AppInfo;
import com.halosolutions.itranslator.constant.Constant;
import com.halosolutions.itranslator.model.LangDescription;
import com.halosolutions.itranslator.model.Language;
import com.halosolutions.itranslator.model.VocalizerObject;
import com.halosolutions.itranslator.sqlite.History;
import com.halosolutions.itranslator.sqlite.ext.HistoryDBAdapter;
import com.halosolutions.itranslator.thirdparty.CaptureActivity;
import com.halosolutions.itranslator.utilities.DataParser;
import com.halosolutions.itranslator.utilities.FilePath;
import com.halosolutions.itranslator.utilities.GlobalUsage;
import com.halosolutions.itranslator.utilities.SimpleAppLog;
import com.memetix.mst.translate.Translate;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;
import com.rey.material.widget.EditText;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.SnackBar;

import java.sql.SQLException;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by longnguyen on 6/19/15.
 *
 */
public class LanguageFragment extends Fragment implements DialogLanguage.OnCompleteListener{

    protected static final int RESULT_SPEECH = 1;
    private static int translated_count = 0;
    private EditText source_text;
    private EditText result_text;
    private ImageButton language1;
    private ImageButton language2;
    private FloatingActionButton btn_speak;
    private FloatingActionButton buttonVoice;
    private FloatingActionButton buttonCamera;
    private FloatingActionButton buttonTranslate;
    private FloatingActionButton buttonTalk;
    private FloatingActionButton buttonShare;
    
    private Language language;
    private LangDescription description;
    private VocalizerObject vol1;
    private VocalizerObject vol2;
    private Object lang1;
    private Object lang2;
    private String translatedText="";

    private static SpeechKit _speechKit;
    private Vocalizer _vocalizer;
    private Object _lastTtsContext = null;
    private FilePath filePath;
    private DataParser parser = new DataParser();
    private SnackBar snackBar;
    private Gson gson;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private HistoryDBAdapter historyDBAdapter;

    private SweetAlertDialog dialogProcess;

    private void showProcessDialog() {
        dialogProcess = new SweetAlertDialog(this.getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        dialogProcess.setTitleText(getString(R.string.processing));
        dialogProcess.setCancelable(false);
        dialogProcess.show();
    }

    private void hideProcessDialog() {
        if (dialogProcess != null) {
            dialogProcess.dismissWithAnimation();
            dialogProcess = null;
        }
    }

    public static LanguageFragment newInstance(){
        LanguageFragment fragment = new LanguageFragment();

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_language, container, false);
        initSpeechKit();
        historyDBAdapter = new HistoryDBAdapter(getActivity());
        try {
            historyDBAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open database",e);
        }
        filePath = new FilePath(getActivity());
        language = new Language();
        description = new LangDescription();
        vol1 = new VocalizerObject();
        vol2 = new VocalizerObject();
        parser = new DataParser();
        snackBar = ((MainActivity)getActivity()).getSnackBar();

        if(GlobalUsage.isFirstUser()){
            language = filePath.initLanguage();
            GlobalUsage.setIsNotFirstUser();
        }else{
            language = parser.parsingLanguage(filePath.getLanguageJsonPath());
        }
        translateLanguage1();
        translateLanguage2();

        source_text = (EditText)v.findViewById(R.id.source_text);
        result_text = (EditText)v.findViewById(R.id.result_text);
        language1 = (ImageButton)v.findViewById(R.id.button_language1);
        language2 = (ImageButton)v.findViewById(R.id.button_language2);
        buttonVoice = (FloatingActionButton)v.findViewById(R.id.btnVoice);
        buttonCamera = (FloatingActionButton)v.findViewById(R.id.btnCamera);
        buttonTranslate = (FloatingActionButton)v.findViewById(R.id.btnTranslate);
        buttonTalk = (FloatingActionButton)v.findViewById(R.id.button_talk);
        buttonShare = (FloatingActionButton)v.findViewById(R.id.button_share);

        buttonTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = source_text.getText().toString();
                if (text.trim().length() > 0) {
                    showProcessDialog();
                    new TranslateBing().execute(source_text.getText(), lang1, lang2);
                    translated_count++;
                } else {
                    SweetAlertDialog d = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
                    d.setTitleText("Please input your text");
                    d.setContentText("");
                    d.setConfirmText(getString(R.string.dialog_ok));
                    d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    d.show();
                }
            }
        });

        btn_speak = (FloatingActionButton)v.findViewById(R.id.button_speak);

        language1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLanguage1();
            }
        });

        language2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLanguage2();
            }
        });

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CaptureActivity.class);
                startActivity(i);
            }
        });

        buttonVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translated_count++;
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //Log.i("Speak", description.getDes());
                String languagePref = description.getDes();
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languagePref);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languagePref);
                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languagePref);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 15); // number of maximum results..
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getActivity(), "Opps! Your device doesn't support Speech to Text", Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });

        btn_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new speakLanguage().execute(translatedText, vol2);
            }
        });

        buttonTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new speakLanguage().execute(source_text.getText(), vol1);
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, translatedText);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        v.findViewById(R.id.button_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DialogHelp dialog = new DialogHelp();
                dialog.show(manager, "dialog");
            }
        });

        checkLanguage1();
        checkLanguage2();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAdView = (AdView) v.findViewById(R.id.adView);
        if (mAdView != null && BuildConfig.IS_FREE) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        if (BuildConfig.IS_FREE) {
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(getString(R.string.popup_ad_unit_id));

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });
            requestNewInterstitial();
        }
        return v;
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void clickLanguage1(){
        FragmentManager manager = getFragmentManager();

        DialogLanguage dialog = new DialogLanguage();

        Bundle args = new Bundle();
        args.putString("status", "source_language");
        dialog.setArguments(args);

        dialog.setTargetFragment(this, 0);
        dialog.show(manager, "dialog");
    }

    public void clickLanguage2(){
        FragmentManager manager = getFragmentManager();

        DialogLanguage dialog = new DialogLanguage();

        Bundle args = new Bundle();
        args.putString("status", "target_language");
        dialog.setArguments(args);

        dialog.setTargetFragment(this, 0);
        dialog.show(manager, "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == -1 && null != data) {

                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    source_text.setText(text.get(0));

                    new TranslateBing().execute(text.get(0), lang1, lang2);
                }
                break;
            }
        }
    }

    class TranslateBing extends AsyncTask<Object, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            Translate.setClientId(Constant.client_id);
            Translate.setClientSecret(Constant.client_secret);
            try {
                translatedText = Translate.execute(params[0].toString(), (com.memetix.mst.language.Language)params[1], (com.memetix.mst.language.Language)params[2]);
                addHistory(params[0].toString());
            } catch(Exception e) {
                translatedText = e.toString();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            result_text.setText(translatedText);
            new speakLanguage().execute(translatedText, vol2);
        }
    }

    private void initSpeechKit(){
        _speechKit = (SpeechKit)onRetainNonConfigurationInstance();
        if(_speechKit == null){
            _speechKit = SpeechKit.initialize(getActivity(), AppInfo.SpeechKitAppId, AppInfo.SpeechKitServer, AppInfo.SpeechKitPort, AppInfo.SpeechKitSsl, AppInfo.SpeechKitApplicationKey);
            _speechKit.connect();
        }
        _vocalizer = _speechKit.createVocalizerWithLanguage("en_UK", vocalizerListener, new Handler());
    }

    class speakLanguage extends AsyncTask<Object, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            _vocalizer.setLanguage(((VocalizerObject) params[1]).getLanguage());
            _vocalizer.setVoice(((VocalizerObject) params[1]).getVoice());
            _lastTtsContext = new Object();
            _vocalizer.speakString(params[0].toString(), _lastTtsContext);
            return true;
        }

        protected void onPostExecute(Boolean result) {
            hideProcessDialog();

        }
    }

    public Object onRetainNonConfigurationInstance()
    {
        // Save the SpeechKit instance, because we know the Activity will be immediately recreated.
        SpeechKit sk = _speechKit;
        _speechKit = null; // Prevent onDestroy() from releasing SpeechKit
        return sk;
    }

    // Create Vocalizer listener
    Vocalizer.Listener vocalizerListener = new Vocalizer.Listener()
    {
        @Override
        public void onSpeakingBegin(Vocalizer vocalizer, String text, Object context) {

        }

        @Override
        public void onSpeakingDone(Vocalizer vocalizer,
                                   String text, SpeechError error, Object context){
            if(translated_count == 3){
                if (BuildConfig.IS_FREE && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                translated_count = 0;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
        historyDBAdapter.close();
    }

    @Override
    public void onComplete(String status, String code) {
        if(status.equalsIgnoreCase("source_language")){
//            snackBar.applyStyle(R.style.SnackBarMultiLine)
//                    .text("Chose " + code + " as source language")
//                    .duration(3000)
//                    .show();
            language.setLanguage1(code);
            checkLanguage1();
            translateLanguage1();
        }else{
//            snackBar.applyStyle(R.style.SnackBarMultiLine)
//                    .text("Chose " + code + " as target language")
//                    .duration(3000)
//                    .show();
            language.setLanguage2(code);
            checkLanguage2();
            translateLanguage2();
        }
    }

    public void checkLanguage1(){
        if(language.getLanguage1().equals("English (UK)")){
            language1.setBackgroundResource(R.drawable.gb_round);
            description.setDes("en-GB");
            vol1.setLanguage("en_UK");
            vol1.setVoice("Oliver");
        }else if(language.getLanguage1().equals("English (USA)")){
            language1.setBackgroundResource(R.drawable.us_round);
            description.setDes("en-US");
            vol1.setLanguage("en_US");
            vol1.setVoice("Susan");
        }else if(language.getLanguage1().equals("Arabic")){
            language1.setBackgroundResource(R.drawable.sa_round);
            description.setDes("ar-SA");
            vol1.setLanguage("ar_WW");
            vol1.setVoice("Tarik");
        }else if(language.getLanguage1().equals("Bulgarian")){
            language1.setBackgroundResource(R.drawable.bga_round);
            description.setDes("ru-RU");
            vol1.setLanguage("ru_RU");
            vol1.setVoice("Yuri");
        }else if(language.getLanguage1().equals("Catalan")){
            language1.setBackgroundResource(R.drawable.ad_round);
            description.setDes("es-ES");
            vol1.setLanguage("ca_ES");
            vol1.setVoice("Jordi");
        }else if(language.getLanguage1().equals("Chinese")){
            language1.setBackgroundResource(R.drawable.cn_round);
            description.setDes("cmn-Hans-CN");
            vol1.setLanguage("zh_CN");
            vol1.setVoice("Tian-Tian");
        }else if(language.getLanguage1().equals("Chinese (HK)")){
            language1.setBackgroundResource(R.drawable.hk_round);
            description.setDes("cmn-Hans-HK");
            vol1.setLanguage("zh_HK");
            vol1.setVoice("Sin-Ji");
        }else if(language.getLanguage1().equals("Chinese (TW)")){
            language1.setBackgroundResource(R.drawable.tw_round);
            description.setDes("cmn-Hant-TW");
            vol1.setLanguage("zh_TW");
            vol1.setVoice("Mei-Jia");
        }else if(language.getLanguage1().equals("Czech")){
            language1.setBackgroundResource(R.drawable.cz_round);
            description.setDes("cs-CZ");
            vol1.setLanguage("cs_CZ");
            vol1.setVoice("Zuzana");
        }else if(language.getLanguage1().equals("Danish")){
            language1.setBackgroundResource(R.drawable.dk_round);
            description.setDes("da-DK");
            vol1.setLanguage("da_DK");
            vol1.setVoice("Magnus");
        }else if(language.getLanguage1().equals("Dutch (BE)")){
            language1.setBackgroundResource(R.drawable.be_round);
            description.setDes("nl-NL");
            vol1.setLanguage("nl_BE");
            vol1.setVoice("Ellen");
        }else if(language.getLanguage1().equals("Dutch (NL)")){
            language1.setBackgroundResource(R.drawable.nl_round);
            description.setDes("nl-NL");
            vol1.setLanguage("nl_NL");
            vol1.setVoice("Claire");
        }else if(language.getLanguage1().equals("English (AU)")){
            language1.setBackgroundResource(R.drawable.au_round);
            description.setDes("en-AU");
            vol1.setLanguage("en_AU");
            vol1.setVoice("Karen");
        }else if(language.getLanguage1().equals("English (IE)")){
            language1.setBackgroundResource(R.drawable.ie_round);
            description.setDes("en-IE");
            vol1.setLanguage("en_IE");
            vol1.setVoice("Moira");
        }else if(language.getLanguage1().equals("English (SA)")){
            language1.setBackgroundResource(R.drawable.za_round);
            description.setDes("en-ZA");
            vol1.setLanguage("en_ZA");
            vol1.setVoice("Tessa");
        }else if(language.getLanguage1().equals("Estonian")){
            language1.setBackgroundResource(R.drawable.ee_round);
            description.setDes("pl-PL");
            vol1.setLanguage("pl_PL");
            vol1.setVoice("Zosia");
        }else if(language.getLanguage1().equals("Finnish")){
            language1.setBackgroundResource(R.drawable.fi_round);
            description.setDes("fi-FI");
            vol1.setLanguage("fi_FI");
            vol1.setVoice("Satu");
        }else if(language.getLanguage1().equals("French (CA)")){
            language1.setBackgroundResource(R.drawable.ca_round);
            description.setDes("fr-FR");
            vol1.setLanguage("fr_CA");
            vol1.setVoice("Nicolas");
        }else if(language.getLanguage1().equals("French (FR)")){
            language1.setBackgroundResource(R.drawable.fr_round);
            description.setDes("fr-FR");
            vol1.setLanguage("fr_FR");
            vol1.setVoice("Aurelie");
        }else if(language.getLanguage1().equals("German")){
            language1.setBackgroundResource(R.drawable.de_round);
            description.setDes("de-DE");
            vol1.setLanguage("de_DE");
            vol1.setVoice("Markus");
        }else if(language.getLanguage1().equals("Greek")){
            language1.setBackgroundResource(R.drawable.gr_round);
            description.setDes("el-GR");
            vol1.setLanguage("el_GR");
            vol1.setVoice("Nikos");
        }else if(language.getLanguage1().equals("Haitian Creole")){
            language1.setBackgroundResource(R.drawable.ht_round);
            description.setDes("fr-FR");
            vol1.setLanguage("fr_FR");
            vol1.setVoice("Thomas");
        }else if(language.getLanguage1().equals("Hebrew")){
            language1.setBackgroundResource(R.drawable.il_round);
            description.setDes("he-IL");
            vol1.setLanguage("he_IL");
            vol1.setVoice("Carmit");
        }else if(language.getLanguage1().equals("Hindi")){
            language1.setBackgroundResource(R.drawable.in_round);
            description.setDes("hi-IN");
            vol1.setLanguage("hi_IN");
            vol1.setVoice("Lekha");
        }else if(language.getLanguage1().equals("Hmong Daw")){
            language1.setBackgroundResource(R.drawable.la_round);
            description.setDes("th-TH");
            vol1.setLanguage("th_TH");
            vol1.setVoice("Kanya");
        }else if(language.getLanguage1().equals("Hungarian")){
            language1.setBackgroundResource(R.drawable.hu_round);
            description.setDes("hu-HU");
            vol1.setLanguage("hu_HU");
            vol1.setVoice("Mariska");
        }else if(language.getLanguage1().equals("Indonesian")){
            language1.setBackgroundResource(R.drawable.id_round);
            description.setDes("id-ID");
            vol1.setLanguage("id_ID");
            vol1.setVoice("Damayanti");
        }else if(language.getLanguage1().equals("Italian")){
            language1.setBackgroundResource(R.drawable.ita_round);
            description.setDes("it-IT");
            vol1.setLanguage("it_IT");
            vol1.setVoice("Luca");
        }else if(language.getLanguage1().equals("Japanese")){
            language1.setBackgroundResource(R.drawable.jp_round);
            description.setDes("ja-JP");
            vol1.setLanguage("jp_JP");
            vol1.setVoice("Kyoko");
        }else if(language.getLanguage1().equals("Korean")){
            language1.setBackgroundResource(R.drawable.kr_round);
            description.setDes("ko-KR");
            vol1.setLanguage("ko_KR");
            vol1.setVoice("Sora");
        }else if(language.getLanguage1().equals("Lithuanian")){
            language1.setBackgroundResource(R.drawable.lt_round);
            description.setDes("lt-LT");
            vol1.setLanguage("pl_PL");
            vol1.setVoice("Ewa");
        }else if(language.getLanguage1().equals("Latvian")){
            language1.setBackgroundResource(R.drawable.lv_round);
            description.setDes("pl-PL");
            vol1.setLanguage("pl_PL");
            vol1.setVoice("Ewa");
        }else if(language.getLanguage1().equals("Malay")){
            language1.setBackgroundResource(R.drawable.sg_round);
            description.setDes("id-ID");
            vol1.setLanguage("id_ID");
            vol1.setVoice("Damayanti");
        }else if(language.getLanguage1().equals("Norwegian")){
            language1.setBackgroundResource(R.drawable.no_round);
            description.setDes("nb-NO");
            vol1.setLanguage("no_NO");
            vol1.setVoice("Henrik");
        }else if(language.getLanguage1().equals("Persian")){
            language1.setBackgroundResource(R.drawable.ir_round);
            description.setDes("ar-SA");
            vol1.setLanguage("pl_PL");
            vol1.setVoice("Ewa");
        }else if(language.getLanguage1().equals("Polish")){
            language1.setBackgroundResource(R.drawable.pl_round);
            description.setDes("pl-PL");
            vol1.setLanguage("pl_PL");
            vol1.setVoice("Ewa");
        }else if(language.getLanguage1().equals("Portuguese (BR)")){
            language1.setBackgroundResource(R.drawable.br_round);
            description.setDes("pt-BR");
            vol1.setLanguage("pt_BR");
            vol1.setVoice("Luciana");
        }else if(language.getLanguage1().equals("Portuguese (PT)")){
            language1.setBackgroundResource(R.drawable.pt_round);
            description.setDes("pt-PT");
            vol1.setLanguage("pt_PT");
            vol1.setVoice("Catarina");
        }else if(language.getLanguage1().equals("Romanian")){
            language1.setBackgroundResource(R.drawable.ro_round);
            description.setDes("ro-RO");
            vol1.setLanguage("ro_RO");
            vol1.setVoice("Ioana");
        }else if(language.getLanguage1().equals("Russian")){
            language1.setBackgroundResource(R.drawable.ru_round);
            description.setDes("ru-RU");
            vol1.setLanguage("ru_RU");
            vol1.setVoice("Katya");
        }else if(language.getLanguage1().equals("Slovak")){
            language1.setBackgroundResource(R.drawable.sk_round);
            description.setDes("sk-SK");
            vol1.setLanguage("sk_SK");
            vol1.setVoice("Laura");
        }else if(language.getLanguage1().equals("Slovenian")){
            language1.setBackgroundResource(R.drawable.si_round);
            description.setDes("sk-SK");
            vol1.setLanguage("sk_SK");
            vol1.setVoice("Laura");
        }else if(language.getLanguage1().equals("Spanish (MX)")){
            language1.setBackgroundResource(R.drawable.mx_round);
            description.setDes("es-MX");
            vol1.setLanguage("es_MX");
            vol1.setVoice("Angelica");
        }else if(language.getLanguage1().equals("Spanish (ES)")){
            language1.setBackgroundResource(R.drawable.es_round);
            description.setDes("es-ES");
            vol1.setLanguage("es_ES");
            vol1.setVoice("Jorge");
        }else if(language.getLanguage1().equals("Swedish")){
            language1.setBackgroundResource(R.drawable.se_round);
            description.setDes("sv-SE");
            vol1.setLanguage("sv_SE");
            vol1.setVoice("Alva");
        }else if(language.getLanguage1().equals("Thai")){
            language1.setBackgroundResource(R.drawable.th_round);
            description.setDes("th-TH");
            vol1.setLanguage("th_TH");
            vol1.setVoice("Kanya");
        }else if(language.getLanguage1().equals("Turkish")){
            language1.setBackgroundResource(R.drawable.tr_round);
            description.setDes("tr-TR");
            vol1.setLanguage("tr_TR");
            vol1.setVoice("Yelda");
        }else if(language.getLanguage1().equals("Ukrainian")){
            language1.setBackgroundResource(R.drawable.ua_round);
            description.setDes("ru-RU");
            vol1.setLanguage("ru_RU");
            vol1.setVoice("Milena");
        }else if(language.getLanguage1().equals("Urdu")){
            language1.setBackgroundResource(R.drawable.pk_round);
            description.setDes("ar-SA");
            vol1.setLanguage("en_US");
            vol1.setVoice("Ava");
        }else if(language.getLanguage1().equals("Vietnamese")){
            language1.setBackgroundResource(R.drawable.vn_round);
            description.setDes("vi-VN");
            vol1.setLanguage("en_US");
            vol1.setVoice("Zoe");
        }
    }

    public void checkLanguage2(){
        if (language == null) return;
        if(language.getLanguage2().equals("English (UK)")){
            language2.setBackgroundResource(R.drawable.gb_round);
            vol2.setLanguage("en_UK");
            vol2.setVoice("Oliver");
        }else if(language.getLanguage2().equals("English (USA)")){
            language2.setBackgroundResource(R.drawable.us_round);
            vol2.setLanguage("en_US");
            vol2.setVoice("Susan");
        }else if(language.getLanguage2().equals("Arabic")){
            language2.setBackgroundResource(R.drawable.sa_round);
            vol2.setLanguage("ar_WW");
            vol2.setVoice("Tarik");
        }else if(language.getLanguage2().equals("Bulgarian")){
            language2.setBackgroundResource(R.drawable.bga_round);
            vol2.setLanguage("ru_RU");
            vol2.setVoice("Yuri");
        }else if(language.getLanguage2().equals("Catalan")){
            language2.setBackgroundResource(R.drawable.ad_round);
            vol2.setLanguage("ca_ES");
            vol2.setVoice("Jordi");
        }else if(language.getLanguage2().equals("Chinese")){
            language2.setBackgroundResource(R.drawable.cn_round);
            vol2.setLanguage("zh_CN");
            vol2.setVoice("Tian-Tian");
        }else if(language.getLanguage2().equals("Chinese (HK)")){
            language2.setBackgroundResource(R.drawable.hk_round);
            vol2.setLanguage("zh_HK");
            vol2.setVoice("Sin-Ji");
        }else if(language.getLanguage2().equals("Chinese (TW)")){
            language2.setBackgroundResource(R.drawable.tw_round);
            vol2.setLanguage("zh_TW");
            vol2.setVoice("Mei-Jia");
        }else if(language.getLanguage2().equals("Czech")){
            language2.setBackgroundResource(R.drawable.cz_round);
            vol2.setLanguage("cs_CZ");
            vol2.setVoice("Iveta");
        }else if(language.getLanguage2().equals("Danish")){
            language2.setBackgroundResource(R.drawable.dk_round);
            vol2.setLanguage("da_DK");
            vol2.setVoice("Magnus");
        }else if(language.getLanguage2().equals("Dutch (BE)")){
            language2.setBackgroundResource(R.drawable.be_round);
            vol2.setLanguage("nl_BE");
            vol2.setVoice("Ellen");
        }else if(language.getLanguage2().equals("Dutch (NL)")){
            language2.setBackgroundResource(R.drawable.nl_round);
            vol2.setLanguage("nl_NL");
            vol2.setVoice("Claire");
        }else if(language.getLanguage2().equals("English (AU)")){
            language2.setBackgroundResource(R.drawable.au_round);
            vol2.setLanguage("en_AU");
            vol2.setVoice("Karen");
        }else if(language.getLanguage2().equals("English (IE)")){
            language2.setBackgroundResource(R.drawable.ie_round);
            vol2.setLanguage("en_IE");
            vol2.setVoice("Moira");
        }else if(language.getLanguage2().equals("English (SA)")){
            language2.setBackgroundResource(R.drawable.za_round);
            vol2.setLanguage("en_ZA");
            vol2.setVoice("Tessa");
        }else if(language.getLanguage2().equals("Estonian")){
            language2.setBackgroundResource(R.drawable.ee_round);
            vol2.setLanguage("pl_PL");
            vol2.setVoice("Zosia");
        }else if(language.getLanguage2().equals("Finnish")){
            language2.setBackgroundResource(R.drawable.fi_round);
            vol2.setLanguage("fi_FI");
            vol2.setVoice("Satu");
        }else if(language.getLanguage2().equals("French (CA)")){
            language2.setBackgroundResource(R.drawable.ca_round);
            vol2.setLanguage("fr_CA");
            vol2.setVoice("Nicolas");
        }else if(language.getLanguage2().equals("French (FR)")){
            language2.setBackgroundResource(R.drawable.fr_round);
            vol2.setLanguage("fr_FR");
            vol2.setVoice("Aurelie");
        }else if(language.getLanguage2().equals("German")){
            language2.setBackgroundResource(R.drawable.de_round);
            vol2.setLanguage("de_DE");
            vol2.setVoice("Markus");
        }else if(language.getLanguage2().equals("Greek")){
            language2.setBackgroundResource(R.drawable.gr_round);
            vol2.setLanguage("el_GR");
            vol2.setVoice("Nikos");
        }else if(language.getLanguage2().equals("Haitian Creole")){
            language2.setBackgroundResource(R.drawable.ht_round);
            vol2.setLanguage("fr_FR");
            vol2.setVoice("Thomas");
        }else if(language.getLanguage2().equals("Hebrew")){
            language2.setBackgroundResource(R.drawable.il_round);
            vol2.setLanguage("he_IL");
            vol2.setVoice("Carmit");
        }else if(language.getLanguage2().equals("Hindi")){
            language2.setBackgroundResource(R.drawable.in_round);
            vol2.setLanguage("hi_IN");
            vol2.setVoice("Lekha");
        }else if(language.getLanguage2().equals("Hmong Daw")){
            language2.setBackgroundResource(R.drawable.la_round);
            vol2.setLanguage("th_TH");
            vol2.setVoice("Kanya");
        }else if(language.getLanguage2().equals("Hungarian")){
            language2.setBackgroundResource(R.drawable.hu_round);
            vol2.setLanguage("hu_HU");
            vol2.setVoice("Mariska");
        }else if(language.getLanguage2().equals("Indonesian")){
            language2.setBackgroundResource(R.drawable.id_round);
            vol2.setLanguage("id_ID");
            vol2.setVoice("Damayanti");
        }else if(language.getLanguage2().equals("Italian")){
            language2.setBackgroundResource(R.drawable.ita_round);
            vol2.setLanguage("it_IT");
            vol2.setVoice("Luca");
        }else if(language.getLanguage2().equals("Japanese")){
            language2.setBackgroundResource(R.drawable.jp_round);
            vol2.setLanguage("jp_JP");
            vol2.setVoice("Kyoko");
        }else if(language.getLanguage2().equals("Korean")){
            language2.setBackgroundResource(R.drawable.kr_round);
            vol2.setLanguage("ko_KR");
            vol2.setVoice("Sora");
        }else if(language.getLanguage2().equals("Lithuanian")){
            language2.setBackgroundResource(R.drawable.lt_round);
            vol2.setLanguage("pl_PL");
            vol2.setVoice("Ewa");
        }else if(language.getLanguage2().equals("Latvian")){
            language2.setBackgroundResource(R.drawable.lv_round);
            vol2.setLanguage("pl_PL");
            vol2.setVoice("Ewa");
        }else if(language.getLanguage2().equals("Malay")){
            language2.setBackgroundResource(R.drawable.sg_round);
            vol2.setLanguage("id_ID");
            vol2.setVoice("Damayanti");
        }else if(language.getLanguage2().equals("Norwegian")){
            language2.setBackgroundResource(R.drawable.no_round);
            vol2.setLanguage("no_NO");
            vol2.setVoice("Henrik");
        }else if(language.getLanguage2().equals("Persian")){
            language2.setBackgroundResource(R.drawable.ir_round);
            vol2.setLanguage("pl_PL");
            vol2.setVoice("Ewa");
        }else if(language.getLanguage2().equals("Polish")){
            language2.setBackgroundResource(R.drawable.pl_round);
            vol2.setLanguage("pl_PL");
            vol2.setVoice("Ewa");
        }else if(language.getLanguage2().equals("Portuguese (BR)")){
            language2.setBackgroundResource(R.drawable.br_round);
            vol2.setLanguage("pt_BR");
            vol2.setVoice("Luciana");
        }else if(language.getLanguage2().equals("Portuguese (PT)")){
            language2.setBackgroundResource(R.drawable.pt_round);
            vol2.setLanguage("pt_PT");
            vol2.setVoice("Catarina");
        }else if(language.getLanguage2().equals("Romanian")){
            language2.setBackgroundResource(R.drawable.ro_round);
            vol2.setLanguage("ro_RO");
            vol2.setVoice("Ioana");
        }else if(language.getLanguage2().equals("Russian")){
            language2.setBackgroundResource(R.drawable.ru_round);
            vol2.setLanguage("ru_RU");
            vol2.setVoice("Katya");
        }else if(language.getLanguage2().equals("Slovak")){
            language2.setBackgroundResource(R.drawable.sk_round);
            vol2.setLanguage("sk_SK");
            vol2.setVoice("Laura");
        }else if(language.getLanguage2().equals("Slovenian")){
            language2.setBackgroundResource(R.drawable.si_round);
            vol2.setLanguage("sk_SK");
            vol2.setVoice("Laura");
        }else if(language.getLanguage2().equals("Spanish (MX)")){
            language2.setBackgroundResource(R.drawable.mx_round);
            vol2.setLanguage("es_MX");
            vol2.setVoice("Angelica");
        }else if(language.getLanguage2().equals("Spanish (ES)")){
            language2.setBackgroundResource(R.drawable.es_round);
            vol2.setLanguage("es_ES");
            vol2.setVoice("Jorge");
        }else if(language.getLanguage2().equals("Swedish")){
            language2.setBackgroundResource(R.drawable.se_round);
            vol2.setLanguage("sv_SE");
            vol2.setVoice("Alva");
        }else if(language.getLanguage2().equals("Thai")){
            language2.setBackgroundResource(R.drawable.th_round);
            vol2.setLanguage("th_TH");
            vol2.setVoice("Kanya");
        }else if(language.getLanguage2().equals("Turkish")){
            language2.setBackgroundResource(R.drawable.tr_round);
            vol2.setLanguage("tr_TR");
            vol2.setVoice("Yelda");
        }else if(language.getLanguage2().equals("Ukrainian")){
            language2.setBackgroundResource(R.drawable.ua_round);
            vol2.setLanguage("ru_RU");
            vol2.setVoice("Milena");
        }else if(language.getLanguage2().equals("Urdu")){
            language2.setBackgroundResource(R.drawable.pk_round);
            vol2.setLanguage("en_US");
            vol2.setVoice("Ava");
        }else if(language.getLanguage2().equals("Vietnamese")){
            language2.setBackgroundResource(R.drawable.vn_round);
            vol2.setLanguage("en_US");
            vol2.setVoice("Zoe");
        }
    }

    public void translateLanguage1(){
        if (language == null) return;
        if(language.getLanguage1().contains("English")){
            lang1 = com.memetix.mst.language.Language.ENGLISH;
        }else if(language.getLanguage1().contains("Dutch")){
            lang1 = com.memetix.mst.language.Language.DUTCH;
        }else if(language.getLanguage1().equals("Arabic")){
            lang1 = com.memetix.mst.language.Language.ARABIC;
        }else if(language.getLanguage1().equals("Bulgarian")){
            lang1 = com.memetix.mst.language.Language.BULGARIAN;
        }else if(language.getLanguage1().equals("Catalan")){
            lang1 = com.memetix.mst.language.Language.CATALAN;
        }else if(language.getLanguage1().equals("Chinese")){
            lang1 = com.memetix.mst.language.Language.CHINESE_TRADITIONAL;
        }else if(language.getLanguage1().equals("Chinese (HK)")){
            lang1 = com.memetix.mst.language.Language.CHINESE_SIMPLIFIED;
        }else if(language.getLanguage1().equals("Chinese (TW)")){
            lang1 = com.memetix.mst.language.Language.CHINESE_TRADITIONAL;
        }else if(language.getLanguage1().equals("Czech")){
            lang1 = com.memetix.mst.language.Language.CZECH;
        }else if(language.getLanguage1().equals("Danish")){
            lang1 = com.memetix.mst.language.Language.DANISH;
        }else if(language.getLanguage1().equals("Estonian")){
            lang1 = com.memetix.mst.language.Language.ESTONIAN;
        }else if(language.getLanguage1().equals("Finnish")){
            lang1 = com.memetix.mst.language.Language.FINNISH;
        }else if(language.getLanguage1().contains("French")){
            lang1 = com.memetix.mst.language.Language.FRENCH;
        }else if(language.getLanguage1().equals("German")){
            lang1 = com.memetix.mst.language.Language.GERMAN;
        }else if(language.getLanguage1().equals("Greek")){
            lang1 = com.memetix.mst.language.Language.GREEK;
        }else if(language.getLanguage1().equals("Haitian Creole")){
            lang1 = com.memetix.mst.language.Language.HAITIAN_CREOLE;
        }else if(language.getLanguage1().equals("Hebrew")){
            lang1 = com.memetix.mst.language.Language.HEBREW;
        }else if(language.getLanguage1().equals("Hindi")){
            lang1 = com.memetix.mst.language.Language.HINDI;
        }else if(language.getLanguage1().equals("Hmong Daw")){
            lang1 = com.memetix.mst.language.Language.HMONG_DAW;
        }else if(language.getLanguage1().equals("Hungarian")){
            lang1 = com.memetix.mst.language.Language.HUNGARIAN;
        }else if(language.getLanguage1().equals("Indonesian")){
            lang1 = com.memetix.mst.language.Language.INDONESIAN;
        }else if(language.getLanguage1().equals("Italian")){
            lang1 = com.memetix.mst.language.Language.ITALIAN;
        }else if(language.getLanguage1().equals("Japanese")){
            lang1 = com.memetix.mst.language.Language.JAPANESE;
        }else if(language.getLanguage1().equals("Korean")){
            lang1 = com.memetix.mst.language.Language.KOREAN;
        }else if(language.getLanguage1().equals("Lithuanian")){
            lang1 = com.memetix.mst.language.Language.LITHUANIAN;
        }else if(language.getLanguage1().equals("Latvian")){
            lang1 = com.memetix.mst.language.Language.LATVIAN;
        }else if(language.getLanguage1().equals("Malay")){
            lang1 = com.memetix.mst.language.Language.MALAY;
        }else if(language.getLanguage1().equals("Norwegian")){
            lang1 = com.memetix.mst.language.Language.NORWEGIAN;
        }else if(language.getLanguage1().equals("Persian")){
            lang1 = com.memetix.mst.language.Language.PERSIAN;
        }else if(language.getLanguage1().equals("Polish")){
            lang1 = com.memetix.mst.language.Language.POLISH;
        }else if(language.getLanguage1().contains("Portuguese")){
            lang1 = com.memetix.mst.language.Language.PORTUGUESE;
        }else if(language.getLanguage1().equals("Romanian")){
            lang1 = com.memetix.mst.language.Language.ROMANIAN;
        }else if(language.getLanguage1().equals("Russian")){
            lang1 = com.memetix.mst.language.Language.RUSSIAN;
        }else if(language.getLanguage1().equals("Slovak")){
            lang1 = com.memetix.mst.language.Language.SLOVAK;
        }else if(language.getLanguage1().equals("Slovenian")){
            lang1 = com.memetix.mst.language.Language.SLOVENIAN;
        }else if(language.getLanguage1().contains("Spanish")){
            lang1 = com.memetix.mst.language.Language.SPANISH;
        }else if(language.getLanguage1().equals("Swedish")){
            lang1 = com.memetix.mst.language.Language.SWEDISH;
        }else if(language.getLanguage1().equals("Thai")){
            lang1 = com.memetix.mst.language.Language.THAI;
        }else if(language.getLanguage1().equals("Turkish")){
            lang1 = com.memetix.mst.language.Language.TURKISH;
        }else if(language.getLanguage1().equals("Ukrainian")){
            lang1 = com.memetix.mst.language.Language.UKRAINIAN;
        }else if(language.getLanguage1().equals("Urdu")){
            lang1 = com.memetix.mst.language.Language.URDU;
        }else if(language.getLanguage1().equals("Vietnamese")){
            lang1 = com.memetix.mst.language.Language.VIETNAMESE;
        }
    }

    public void translateLanguage2(){
        if(language.getLanguage2().contains("English")){
            lang2 = com.memetix.mst.language.Language.ENGLISH;
        }else if(language.getLanguage2().contains("Dutch")){
            lang2 = com.memetix.mst.language.Language.DUTCH;
        }else if(language.getLanguage2().equals("Arabic")){
            lang2 = com.memetix.mst.language.Language.ARABIC;
        }else if(language.getLanguage2().equals("Bulgarian")){
            lang2 = com.memetix.mst.language.Language.BULGARIAN;
        }else if(language.getLanguage2().equals("Catalan")){
            lang2 = com.memetix.mst.language.Language.CATALAN;
        }else if(language.getLanguage2().equals("Chinese")){
            lang2 = com.memetix.mst.language.Language.CHINESE_TRADITIONAL;
        }else if(language.getLanguage2().equals("Chinese (HK)")){
            lang2 = com.memetix.mst.language.Language.CHINESE_SIMPLIFIED;
        }else if(language.getLanguage2().equals("Chinese (TW)")){
            lang2 = com.memetix.mst.language.Language.CHINESE_TRADITIONAL;
        }else if(language.getLanguage2().equals("Czech")){
            lang2 = com.memetix.mst.language.Language.CZECH;
        }else if(language.getLanguage2().equals("Danish")){
            lang2 = com.memetix.mst.language.Language.DANISH;
        }else if(language.getLanguage2().equals("Estonian")){
            lang2 = com.memetix.mst.language.Language.ESTONIAN;
        }else if(language.getLanguage2().equals("Finnish")){
            lang2 = com.memetix.mst.language.Language.FINNISH;
        }else if(language.getLanguage2().contains("French")){
            lang2 = com.memetix.mst.language.Language.FRENCH;
        }else if(language.getLanguage2().equals("German")){
            lang2 = com.memetix.mst.language.Language.GERMAN;
        }else if(language.getLanguage2().equals("Greek")){
            lang2 = com.memetix.mst.language.Language.GREEK;
        }else if(language.getLanguage2().equals("Haitian Creole")){
            lang2 = com.memetix.mst.language.Language.HAITIAN_CREOLE;
        }else if(language.getLanguage2().equals("Hebrew")){
            lang2 = com.memetix.mst.language.Language.HEBREW;
        }else if(language.getLanguage2().equals("Hindi")){
            lang2 = com.memetix.mst.language.Language.HINDI;
        }else if(language.getLanguage2().equals("Hmong Daw")){
            lang2 = com.memetix.mst.language.Language.HMONG_DAW;
        }else if(language.getLanguage2().equals("Hungarian")){
            lang2 = com.memetix.mst.language.Language.HUNGARIAN;
        }else if(language.getLanguage2().equals("Indonesian")){
            lang2 = com.memetix.mst.language.Language.INDONESIAN;
        }else if(language.getLanguage2().equals("Italian")){
            lang2 = com.memetix.mst.language.Language.ITALIAN;
        }else if(language.getLanguage2().equals("Japanese")){
            lang2 = com.memetix.mst.language.Language.JAPANESE;
        }else if(language.getLanguage2().equals("Korean")){
            lang2 = com.memetix.mst.language.Language.KOREAN;
        }else if(language.getLanguage2().equals("Lithuanian")){
            lang2 = com.memetix.mst.language.Language.LITHUANIAN;
        }else if(language.getLanguage2().equals("Latvian")){
            lang2 = com.memetix.mst.language.Language.LATVIAN;
        }else if(language.getLanguage2().equals("Malay")){
            lang2 = com.memetix.mst.language.Language.MALAY;
        }else if(language.getLanguage2().equals("Norwegian")){
            lang2 = com.memetix.mst.language.Language.NORWEGIAN;
        }else if(language.getLanguage2().equals("Persian")){
            lang2 = com.memetix.mst.language.Language.PERSIAN;
        }else if(language.getLanguage2().equals("Polish")){
            lang2 = com.memetix.mst.language.Language.POLISH;
        }else if(language.getLanguage2().contains("Portuguese")){
            lang2 = com.memetix.mst.language.Language.PORTUGUESE;
        }else if(language.getLanguage2().equals("Romanian")){
            lang2 = com.memetix.mst.language.Language.ROMANIAN;
        }else if(language.getLanguage2().equals("Russian")){
            lang2 = com.memetix.mst.language.Language.RUSSIAN;
        }else if(language.getLanguage2().equals("Slovak")){
            lang2 = com.memetix.mst.language.Language.SLOVAK;
        }else if(language.getLanguage2().equals("Slovenian")){
            lang2 = com.memetix.mst.language.Language.SLOVENIAN;
        }else if(language.getLanguage2().contains("Spanish")){
            lang2 = com.memetix.mst.language.Language.SPANISH;
        }else if(language.getLanguage2().equals("Swedish")){
            lang2 = com.memetix.mst.language.Language.SWEDISH;
        }else if(language.getLanguage2().equals("Thai")){
            lang2 = com.memetix.mst.language.Language.THAI;
        }else if(language.getLanguage2().equals("Turkish")){
            lang2 = com.memetix.mst.language.Language.TURKISH;
        }else if(language.getLanguage2().equals("Ukrainian")){
            lang2 = com.memetix.mst.language.Language.UKRAINIAN;
        }else if(language.getLanguage2().equals("Urdu")){
            lang2 = com.memetix.mst.language.Language.URDU;
        }else if(language.getLanguage2().equals("Vietnamese")){
            lang2 = com.memetix.mst.language.Language.VIETNAMESE;
        }
    }

    public void addHistory(String txt){
        if(txt.length() > 0){
            History history = new History();
            history.setPhase(txt.trim());
            try {
                historyDBAdapter.insert(history);
            } catch (Exception e) {
                SimpleAppLog.error("Could not insert history",e);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(GlobalUsage.sourceTxt.length() > 0){
                source_text.setText(GlobalUsage.sourceTxt);
            }
        }
    }

}
