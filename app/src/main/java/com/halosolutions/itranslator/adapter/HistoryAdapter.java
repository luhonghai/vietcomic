/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.sqlite.History;

import java.util.List;

/**
 * Created by longnguyen on 7/10/15.
 *
 */
public class HistoryAdapter extends BaseAdapter {
    public interface OnDimissItem {
        public void onDimissItem(int pos);
    }


    private final Context context;
    public List<History> list;

    private final OnDimissItem onDimissItem;

    public HistoryAdapter(Context context, List<History> list, OnDimissItem onDimissItem){
        this.context = context;
        this.list = list;
        this.onDimissItem = onDimissItem;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public History getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final History item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.row_history, parent, false);
            holder = new ViewHolder();

            holder.history_text = (TextView) convertView.findViewById(R.id.history_text);
            holder.btnDelete = (ImageButton) convertView.findViewById(R.id.btnDelete);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((SwipeListView)parent).recycle(convertView, position);

        holder.history_text.setText(item.getPhase());

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDimissItem.onDimissItem(position);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView history_text;
        ImageButton btnDelete;
    }
}
