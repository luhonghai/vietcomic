package com.halosolutions.itranslator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.model.LangDescription;

import java.util.List;

/**
 * Created by longnguyen on 06/29/15.
 *
 */
public class LanguageAdapter extends BaseAdapter {
    private Context context;
    private List<LangDescription> listLanguage;
    private TextView title;
    private ImageView imgLanguage;

    public LanguageAdapter(Context context, List<LangDescription> list){
        this.context = context;
        this.listLanguage = list;
    }
    @Override
    public int getCount() {
        return listLanguage.size();
    }

    @Override
    public LangDescription getItem(int position) {
        return listLanguage.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listCell;

        listCell = inflater.inflate(R.layout.row_language, null);

        title = (TextView)listCell.findViewById(R.id.txtTitle);

        imgLanguage = (ImageView)listCell.findViewById(R.id.imgFlag);

        title.setText(listLanguage.get(position).getTitle());

        if(position == 0){
            imgLanguage.setBackgroundResource(R.drawable.gb_round);
        }else if(position == 1){
            imgLanguage.setBackgroundResource(R.drawable.us_round);
        }else if(position == 2){
            imgLanguage.setBackgroundResource(R.drawable.sa_round);
        }else if(position == 3){
            imgLanguage.setBackgroundResource(R.drawable.bga_round);
        }else if(position == 4){
            imgLanguage.setBackgroundResource(R.drawable.ad_round);
        }else if(position == 5){
            imgLanguage.setBackgroundResource(R.drawable.cn_round);
        }else if(position == 6){
            imgLanguage.setBackgroundResource(R.drawable.hk_round);
        }else if(position == 7){
            imgLanguage.setBackgroundResource(R.drawable.tw_round);
        }else if(position == 8){
            imgLanguage.setBackgroundResource(R.drawable.cz_round);
        }else if(position == 9){
            imgLanguage.setBackgroundResource(R.drawable.dk_round);
        }else if(position == 10){
            imgLanguage.setBackgroundResource(R.drawable.be_round);
        }else if(position == 11){
            imgLanguage.setBackgroundResource(R.drawable.nl_round);
        }else if(position == 12){
            imgLanguage.setBackgroundResource(R.drawable.au_round);
        }else if(position == 13){
            imgLanguage.setBackgroundResource(R.drawable.ie_round);
        }else if(position == 14){
            imgLanguage.setBackgroundResource(R.drawable.za_round);
        }else if(position == 15){
            imgLanguage.setBackgroundResource(R.drawable.ee_round);
        }else if(position == 16){
            imgLanguage.setBackgroundResource(R.drawable.fi_round);
        }else if(position == 17){
            imgLanguage.setBackgroundResource(R.drawable.ca_round);
        }else if(position == 18){
            imgLanguage.setBackgroundResource(R.drawable.fr_round);
        }else if(position == 19){
            imgLanguage.setBackgroundResource(R.drawable.de_round);
        }else if(position == 20){
            imgLanguage.setBackgroundResource(R.drawable.gr_round);
        }else if(position == 21){
            imgLanguage.setBackgroundResource(R.drawable.ht_round);
        }else if(position == 22){
            imgLanguage.setBackgroundResource(R.drawable.il_round);
        }else if(position == 23){
            imgLanguage.setBackgroundResource(R.drawable.in_round);
        }else if(position == 24){
            imgLanguage.setBackgroundResource(R.drawable.la_round);
        }else if(position == 25){
            imgLanguage.setBackgroundResource(R.drawable.hu_round);
        }else if(position == 26){
            imgLanguage.setBackgroundResource(R.drawable.id_round);
        }else if(position == 27){
            imgLanguage.setBackgroundResource(R.drawable.ita_round);
        }else if(position == 28){
            imgLanguage.setBackgroundResource(R.drawable.jp_round);
        }else if(position == 29){
            imgLanguage.setBackgroundResource(R.drawable.kr_round);
        }else if(position == 30){
            imgLanguage.setBackgroundResource(R.drawable.lt_round);
        }else if(position == 31){
            imgLanguage.setBackgroundResource(R.drawable.lv_round);
        }else if(position == 32){
            imgLanguage.setBackgroundResource(R.drawable.sg_round);
        }else if(position == 33){
            imgLanguage.setBackgroundResource(R.drawable.no_round);
        }else if(position == 34){
            imgLanguage.setBackgroundResource(R.drawable.ir_round);
        }else if(position == 35){
            imgLanguage.setBackgroundResource(R.drawable.pl_round);
        }else if(position == 36){
            imgLanguage.setBackgroundResource(R.drawable.br_round);
        }else if(position == 37){
            imgLanguage.setBackgroundResource(R.drawable.pt_round);
        }else if(position == 38){
            imgLanguage.setBackgroundResource(R.drawable.ro_round);
        }else if(position == 39){
            imgLanguage.setBackgroundResource(R.drawable.ru_round);
        }else if(position == 40){
            imgLanguage.setBackgroundResource(R.drawable.sk_round);
        }else if(position == 41){
            imgLanguage.setBackgroundResource(R.drawable.si_round);
        }else if(position == 42){
            imgLanguage.setBackgroundResource(R.drawable.mx_round);
        }else if(position == 43){
            imgLanguage.setBackgroundResource(R.drawable.es_round);
        }else if(position == 44){
            imgLanguage.setBackgroundResource(R.drawable.se_round);
        }else if(position == 45){
            imgLanguage.setBackgroundResource(R.drawable.th_round);
        }else if(position == 46){
            imgLanguage.setBackgroundResource(R.drawable.tr_round);
        }else if(position == 47){
            imgLanguage.setBackgroundResource(R.drawable.ua_round);
        }else if(position == 48){
            imgLanguage.setBackgroundResource(R.drawable.pk_round);
        }else if(position == 49){
            imgLanguage.setBackgroundResource(R.drawable.vn_round);
        }
        return listCell;
    }
}
