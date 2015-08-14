package com.halosolutions.vietcomic.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.halosolutions.vietcomic.R;

public class NewComicFragment extends Fragment{

	public static NewComicFragment newInstance() {
		NewComicFragment fragment = new NewComicFragment();
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_comic_list, container, false);
		
		return v;
	}
}
