package com.cmg.android.cmgpdf;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.view.View;

import com.halosolutions.vietcomic.util.AndroidHelper;

public class SafeAnimatorInflater
{
	private View mView;

	public SafeAnimatorInflater(Activity activity, int animation, View view)
	{
		if (AndroidHelper.isGreatThanApiLevel9()) {
			AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(activity, animation);
			mView = view;
			set.setTarget(view);
			set.addListener(new Animator.AnimatorListener() {
				public void onAnimationStart(Animator animation) {
					mView.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animator animation) {
					mView.setVisibility(View.VISIBLE);
				}

				public void onAnimationEnd(Animator animation) {
					mView.setVisibility(View.INVISIBLE);
				}

				public void onAnimationCancel(Animator animation) {
					mView.setVisibility(View.VISIBLE);
				}
			});
			set.start();
		} else {
			view.setVisibility(View.INVISIBLE);
		}
	}
}
