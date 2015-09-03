package com.cmg.android.cmgpdf;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmg.android.cmgpdf.util.ImageHelper;
import com.cmg.android.cmgpdf.util.Utility;
import com.halosolutions.mangaworld.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.ref.WeakReference;


/**
 * @author Hai Lu
 */
public class PDFPreviewPagerAdapter extends BaseAdapter {

    private static final String TAG = PDFPreviewPagerAdapter.class
            .getSimpleName();

    private static final String DEFAULT_CACHE_FOLDER = ".thumbnail";

    private Context mContext;
    private MuPDFCore mCore;

    private Point mPreviewSize;
    private final SparseArray<Bitmap> mBitmapCache = new SparseArray<Bitmap>();
    private String mPath;

    private int currentlyViewing;
    private Bitmap mLoadingBitmap;

    private String mCachedThumbnailSize;

    public PDFPreviewPagerAdapter(Context context, MuPDFCore core) {
        mContext = context;
        mCore = core;
        mPath = core.getFileDirectory() + File.separator + DEFAULT_CACHE_FOLDER + File.separator;
        File mCacheDirectory = new File(mPath);
        if (!mCacheDirectory.exists())
            mCacheDirectory.mkdirs();
        mCachedThumbnailSize = mPath + Utility.md5(Uri.fromFile(new File(mCore.getFileName())).toString()) + ".s";
        mLoadingBitmap = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.darkdenim3);
    }

    public void recycle() {
        for (int i = 0; i < mBitmapCache.size(); i++) {
            Bitmap bm = mBitmapCache.get(i);
            if (bm != null && !bm.isRecycled())
             bm.recycle();
        }
    }

    @Override
    public int getCount() {
        int count = mCore.countSinglePages();
        return count;
    }

    @Override
    public Object getItem(int pPosition) {
        return null;
    }

    @Override
    public long getItemId(int pPosition) {
        if (mCore.getDisplayPages() == 1)
            return pPosition;
        else if (pPosition > 0)
            return (pPosition + 1) / 2;
        else
            return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.preview_pager_item_layout,
                    parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mPreviewSize != null) {
            final ImageView imageView = holder.previewPageImageView;
            holder.previewPageImageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setLayoutParams(new FrameLayout.LayoutParams(
                            mPreviewSize.x, mPreviewSize.y));
                }
            });

        }
        holder.previewPageNumber.setText(String.valueOf(position + 1));
        holder.previewPageFrameLayout.setBackgroundColor(Color.TRANSPARENT);
        drawPageImageView(holder, position);
        return convertView;
    }

    static class ViewHolder {
        ImageView previewPageImageView = null;
        TextView previewPageNumber = null;
        FrameLayout previewPageFrameLayout = null;

        ViewHolder(View view) {
            this.previewPageImageView = (ImageView) view
                    .findViewById(R.id.PreviewPageImageView);
            this.previewPageNumber = (TextView) view
                    .findViewById(R.id.PreviewPageNumber);
            this.previewPageFrameLayout = (FrameLayout) view
                    .findViewById(R.id.PreviewPageFrameLayout);
        }
    }

    private void drawPageImageView(ViewHolder holder, int position) {
        if (cancelPotentialWork(holder, position)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(holder, position);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(
                    mContext.getResources(), mLoadingBitmap, task);
            holder.previewPageImageView.setImageDrawable(asyncDrawable);
            task.execute();
        }
    }

    public static boolean cancelPotentialWork(ViewHolder holder, int position) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(holder.previewPageImageView);

        if (bitmapWorkerTask != null) {
            final int bitmapPosition = bitmapWorkerTask.position;
            if (bitmapPosition != position) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        private final WeakReference<ViewHolder> viewHolderReference;
        private int position;

        public BitmapWorkerTask(ViewHolder holder, int position) {
            viewHolderReference = new WeakReference<ViewHolder>(holder);
            this.position = position;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            if (mPreviewSize == null) {
                mPreviewSize = new Point();
                int padding = mContext.getResources().getDimensionPixelSize(
                        R.dimen.page_preview_size);
                PointF mPageSize = mCore.getSinglePageSize(0);
                float scale = mPageSize.y / mPageSize.x;
                mPreviewSize.x = (int) ((float) padding / scale);
                mPreviewSize.y = padding;
                saveThumbnailSize(mPreviewSize.x, mPreviewSize.y);
            }
            Bitmap lq = null;
            lq = getCachedBitmap(position);
            mBitmapCache.put(position, lq);
            return lq;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (viewHolderReference != null && bitmap != null) {
                final ViewHolder holder = viewHolderReference.get();
                if (holder != null) {
                    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(holder.previewPageImageView);
                    if (this == bitmapWorkerTask && holder != null) {
                        holder.previewPageImageView.setImageBitmap(bitmap);
                        holder.previewPageNumber.setText(String
                                .valueOf(position + 1));
                        if (getCurrentlyViewing() == position
                                || (mCore.getDisplayPages() == 2 && getCurrentlyViewing() == position - 1)) {
                            holder.previewPageFrameLayout
                                    .setBackgroundColor(mContext
                                            .getResources()
                                            .getColor(
                                                    R.color.thumbnail_selected_background));
                        } else {
                            holder.previewPageFrameLayout
                                    .setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                }
            }
        }
    }

    private void saveThumbnailSize(int w, int h) {
        File file = new File(mCachedThumbnailSize);
        if (file.exists())
            return;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(w + "|" + h);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }


    private Bitmap getCachedBitmap(int position) {
        StringBuffer sb = new StringBuffer();
        sb.append(Uri.fromFile(new File(mCore.getFileName())));
        sb.append(File.separator);
        sb.append(position);

        String mCachedBitmapFilePath = mPath + Utility.md5(sb.toString()) + ".png";
        File mCachedBitmapFile = new File(mCachedBitmapFilePath);
        Bitmap lq = null;
        try {
            if (mCachedBitmapFile.exists() && mCachedBitmapFile.canRead()) {
                Log.d(TAG, "page " + position + " found in cache");
                lq = BitmapFactory.decodeFile(mCachedBitmapFilePath);
                return lq;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // some error with cached file,
            // delete the file and get rid of bitmap
            mCachedBitmapFile.delete();
            lq = null;
        }
        if (lq == null) {
            lq = Bitmap.createBitmap(mPreviewSize.x, mPreviewSize.y,
                    Bitmap.Config.ARGB_8888);
            mCore.drawSinglePage(position, lq, mPreviewSize.x, mPreviewSize.y);
            lq = ImageHelper.getRoundedCornerBitmap(lq, 10);
            try {
                lq.compress(CompressFormat.PNG, 50, new FileOutputStream(
                        mCachedBitmapFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mCachedBitmapFile.delete();
            }
        }
        return lq;
    }

    public int getCurrentlyViewing() {
        return currentlyViewing;
    }

    public void setCurrentlyViewing(int currentlyViewing) {
        this.currentlyViewing = currentlyViewing;
        notifyDataSetChanged();
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
                    bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }


}
