/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.activity;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.adapter.LanguageAdapter;
import com.halosolutions.itranslator.model.LangDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longnguyen on 6/20/15.
 */
public class DialogLanguage extends DialogFragment implements AdapterView.OnItemClickListener {

    private List<LangDescription> listLanguage;

    private ListView mylist;

    private String status;

    public static interface OnCompleteListener{
        public abstract void onComplete(String status, String code);
    }

    private OnCompleteListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_language, null, false);
        mylist = (ListView) view.findViewById(R.id.list);

        status = getArguments().getString("status");

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        initText();

        LanguageAdapter adapter = new LanguageAdapter(getActivity(), listLanguage);

        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        if(status.equalsIgnoreCase("source_language")){
            ((OnCompleteListener)getTargetFragment()).onComplete("source_language", listLanguage.get(position).getTitle());
        }else{
            ((OnCompleteListener) getTargetFragment()).onComplete("target_language", listLanguage.get(position).getTitle());
        }

    }

    /**
     * Initial list of Language
     */
    public void initText(){
        listLanguage = new ArrayList<LangDescription>();
        LangDescription lang = new LangDescription();
        lang.setTitle("English (UK)");
        lang.setDes("en-GB");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("English (USA)");
        lang.setDes("en-US");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Arabic");
        lang.setDes("ar-SA");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Bulgarian");
        lang.setDes("ru-RU");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Catalan");
        lang.setDes("es-ES");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Chinese");
        lang.setDes("zh-CN");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Chinese (HK)");
        lang.setDes("zh-HK");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Chinese (TW)");
        lang.setDes("zh-TW");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Czech");
        lang.setDes("cs-CZ");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Danish");
        lang.setDes("da-DK");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Dutch (BE)");
        lang.setDes("nl-BE");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Dutch (NL)");
        lang.setDes("nl-NL");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("English (AU)");
        lang.setDes("en-AU");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("English (IE)");
        lang.setDes("en-IE");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("English (SA)");
        lang.setDes("en-ZA");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Estonian");
        lang.setDes("pl-PL");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Finnish");
        lang.setDes("fi-FI");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("French (CA)");
        lang.setDes("fr-CA");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("French (FR)");
        lang.setDes("fr-FR");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("German");
        lang.setDes("de-DE");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Greek");
        lang.setDes("el-GR");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Haitian Creole");
        lang.setDes("fr-FR");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Hebrew");
        lang.setDes("ro-RO");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Hindi");
        lang.setDes("hi-IN");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Hmong Daw");
        lang.setDes("th-TH");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Hungarian");
        lang.setDes("hu-HU");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Indonesian");
        lang.setDes("id-ID");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Italian");
        lang.setDes("it-IT");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Japanese");
        lang.setDes("jp-JP");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Korean");
        lang.setDes("ko-KR");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Lithuanian");
        lang.setDes("pl-PL");
        listLanguage.add(lang);


        lang = new LangDescription();
        lang.setTitle("Latvian");
        lang.setDes("pl-PL");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Malay");
        lang.setDes("id-ID");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Norwegian");
        lang.setDes("no-NO");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Persian");
        lang.setDes("ar-SA");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Polish");
        lang.setDes("pl-PL");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Portuguese (BR)");
        lang.setDes("pt-BR");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Portuguese (PT)");
        lang.setDes("pt-PT");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Romanian");
        lang.setDes("ro-RO");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Russian");
        lang.setDes("ru-RU");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Slovak");
        lang.setDes("sk-SK");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Slovenian");
        lang.setDes("sk-SK");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Spanish (MX)");
        lang.setDes("es-MX");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Spanish (ES)");
        lang.setDes("es-ES");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Swedish");
        lang.setDes("sv-SE");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Thai");
        lang.setDes("th-TH");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Turkish");
        lang.setDes("tr-TR");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Ukrainian");
        lang.setDes("ru-RU");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Urdu");
        lang.setDes("ar-SA");
        listLanguage.add(lang);

        lang = new LangDescription();
        lang.setTitle("Vietnamese");
        lang.setDes("vi-VN");
        listLanguage.add(lang);
    }
}
