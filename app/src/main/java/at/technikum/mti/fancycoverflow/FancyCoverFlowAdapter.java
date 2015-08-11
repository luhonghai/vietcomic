/*
 * Copyright 2013 David Schreiber
 *           2013 John Paul Nalog
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.technikum.mti.fancycoverflow;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class FancyCoverFlowAdapter extends BaseAdapter {

	private static final int CACHE_SIZE = 10;
	private final Map<Integer, SoftReference<Bitmap>> cache = new HashMap<Integer, SoftReference<Bitmap>>();

	// private final List<SoftReference<Bitmap>> cache = Collections
	// .synchronizedList(new LinkedList<SoftReference<Bitmap>>());

	// =============================================================================
	// Supertype overrides
	// =============================================================================

	public final View getView(int i, View reusableView, ViewGroup viewGroup) {
		FancyCoverFlow coverFlow = (FancyCoverFlow) viewGroup;

		View wrappedView = null;
		FancyCoverFlowItemWrapper coverFlowItem;

		if (reusableView != null) {
			coverFlowItem = (FancyCoverFlowItemWrapper) reusableView;
			wrappedView = coverFlowItem.getChildAt(0);
			coverFlowItem.removeAllViews();
			// coverFlowItem.recycle();
		} else {
			coverFlowItem = new FancyCoverFlowItemWrapper(
					viewGroup.getContext());
			coverFlowItem.setCache(cache, i);
		}

		wrappedView = this.getCoverFlowItem(i, wrappedView, viewGroup);

		if (wrappedView == null) {
			throw new NullPointerException(
					"getCoverFlowItem() was expected to return a view, but null was returned.");
		}

		final boolean isReflectionEnabled = coverFlow.isReflectionEnabled();
		coverFlowItem.setReflectionEnabled(isReflectionEnabled);

		if (isReflectionEnabled) {
			coverFlowItem.setReflectionGap(coverFlow.getReflectionGap());
			coverFlowItem.setReflectionRatio(coverFlow.getReflectionRatio());
		}

		coverFlowItem.addView(wrappedView);
		coverFlowItem.setLayoutParams(wrappedView.getLayoutParams());
		//calCache(i);
		return coverFlowItem;
	}

	// =============================================================================
	// Abstract methods
	// =============================================================================

	public abstract View getCoverFlowItem(int position, View reusableView,
			ViewGroup parent);

	public void clear() {
		if (cache != null) {
			synchronized (cache) {
				if (!cache.isEmpty()) {
					Iterator<Integer> keys = cache.keySet().iterator();
					while (keys.hasNext()) {
						Bitmap bm = cache.get(keys.next()).get();
						if (bm != null && !bm.isRecycled()) {
							bm.recycle();
							bm = null;
						}
					}
				}
				cache.clear();
			}
		}
	}
}
