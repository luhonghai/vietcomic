

package com.halosolutions.vietcomic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.cmg.android.cmgpdf.AsyncTask;
import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.http.UploadFeedbackAsync;
import com.halosolutions.vietcomic.util.AndroidHelper;
import com.halosolutions.vietcomic.util.DeviceUuidFactory;
import com.halosolutions.vietcomic.util.ExceptionHandler;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rey.material.app.ThemeManager;
import com.rey.material.app.ToolbarManager;
import com.rey.material.drawable.NavigationDrawerDrawable;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FeedbackActivity extends BaseActivity {

    public static final String DEVICE_NAME = "Device";

    public static final String OS_API_LEVEL = "OS API Level";

    public static final String OS_VERSION = "OS Version";

    public static final String MODEL = "Model";

    public static final String APP_VERSION = "Application version";

    public static final String FEEDBACK_DESCRIPTION = "Feedback description";

    public static final String ACCOUNT = "Account";

    public static final String IMEI = "imei";

    public static final String STACK_TRACE = "Stack trace";

    public static final String SEND_FEEDBACK_FINISH = FeedbackActivity.class.getName();

    private String stackTrace;

    private SweetAlertDialog dialogProcess;

    private ComicBook comicBook;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(ComicBook.class.getName())) {
                Gson gson = new Gson();
                try {
                    comicBook = gson.fromJson(bundle.getString(ComicBook.class.getName()), ComicBook.class);
                    if (comicBook != null) {
                        final ImageView imgThumbnail = (ImageView) findViewById(R.id.thumbnail);
                        if (imgThumbnail != null && imgThumbnail.getTag() == null) {
                            String thumbnail = comicBook.getThumbnail();
                            ImageLoader.getInstance().displayImage(thumbnail,
                                    imgThumbnail,
                                    new DisplayImageOptions.Builder()
                                            .showImageForEmptyUri(R.drawable.comic_thumbnail_default) // resource or drawable
                                            .showImageOnFail(R.drawable.comic_thumbnail_error) // resource or drawable
                                            .cacheInMemory(true)
                                            .cacheOnDisk(true)
                                            .build());
                            imgThumbnail.setTag(new Object());
                        }

                        findViewById(R.id.rlBookInfo).setVisibility(View.VISIBLE);
                        ((HtmlTextView) findViewById(R.id.txtComicName)).setHtmlFromString(getString(R.string.comic_name,
                                comicBook.getName()), new HtmlTextView.RemoteImageGetter());
                        ((HtmlTextView) findViewById(R.id.txtComicOtherName)).setHtmlFromString(getString(R.string.comic_other_name,
                                comicBook.getOtherName()), new HtmlTextView.RemoteImageGetter());
                        ((HtmlTextView) findViewById(R.id.txtComicAuthor)).setHtmlFromString(getString(R.string.comic_author,
                                comicBook.getAuthor()), new HtmlTextView.RemoteImageGetter());
                    } else {
                        findViewById(R.id.rlBookInfo).setVisibility(View.GONE);
                    }
                } catch (Exception e) {}
            }
            if (bundle.containsKey(ExceptionHandler.STACK_TRACE)) {
                stackTrace = bundle.getString(ExceptionHandler.STACK_TRACE);
                SweetAlertDialog d = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                d.setTitleText("Xảy ra lỗi ngoài ý muốn");
                d.setContentText("Xin lỗi về sự bất tiện này. Chúng tôi sẽ cố gắng sữa chữa sớm nhất có thể!");
                d.setConfirmText(getResources().getString(R.string.dialog_ok));
                d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
                d.show();
            }
        }
        registerReceiver(mHandleMessageReader, new IntentFilter(SEND_FEEDBACK_FINISH));

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle("Phản hồi");
        setSupportActionBar(mToolbar);
        final NavigationDrawerDrawable drawable =
                (new com.rey.material.drawable.NavigationDrawerDrawable.Builder(this.mToolbar.getContext(),
                        ThemeManager.getInstance().getCurrentStyle(R.array.navigation_drawer)))
                        .build();
        mToolbar.setNavigationIcon(drawable);
        drawable.switchIconState(NavigationDrawerDrawable.STATE_ARROW, false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SimpleAppLog.debug("onOptionsItemSelected " + item.getItemId());
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProcessDialog() {
        dialogProcess = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialogProcess.setTitleText("Đang xử lý ...");
        dialogProcess.setCancelable(false);
        dialogProcess.show();
    }

    public String getTextDescription() {
        String desc = null;
        EditText text = (EditText) findViewById(R.id.textDescription);
        desc = text.getText().toString();
        return desc;
    }


    private void sendFeedback() {
        if (checkNetwork(false)) {
            if (getTextDescription().trim().length() == 0) {
                SweetAlertDialog d = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                d.setTitleText("Vui lòng nhập thông tin");
                d.setContentText("");
                d.setConfirmText(getString(R.string.dialog_ok));
                d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
                d.show();
            } else {
                Map<String, String> params = new HashMap<String, String>();
                params.put("firstName","Vietcomic");
                params.put("lastName", "Halo Solutions");
                params.put("email", "halo.app.solutions@gmail.com");
                params.put("happy", "true");
                params.put("message", generatePreviewHtmlFeedback(getFormData()));
                UploadFeedbackAsync uploadAsync = new UploadFeedbackAsync(this.getApplicationContext(), params);
                uploadAsync.execute();
                showProcessDialog();
            }
        }
    }

    private Map<String, String> getFormData() {
        Map<String, String> infos = new HashMap<String, String>();
        infos.put(FEEDBACK_DESCRIPTION, getTextDescription());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            infos.put(ComicBook.class.getName(), bundle.getString(ComicBook.class.getName()));
        }
        DeviceUuidFactory uIdFac = new DeviceUuidFactory(this);
        infos.put(IMEI, uIdFac.getDeviceUuid().toString());
        infos.put(APP_VERSION, AndroidHelper.getVersionName(this.getApplicationContext()));
        infos.put(MODEL, android.os.Build.MODEL);
        infos.put(OS_VERSION, System.getProperty("os.version"));
        infos.put(OS_API_LEVEL, android.os.Build.VERSION.SDK);
        infos.put(DEVICE_NAME, android.os.Build.DEVICE);
        if (stackTrace != null && stackTrace.length() > 0) {
            infos.put(STACK_TRACE, stackTrace);
        }
        return infos;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mHandleMessageReader);
        } catch (Exception e) {

        }
    }

    private void closeFeedBack(){
        this.finish();
    }

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialogProcess != null)
                        dialogProcess.dismissWithAnimation();
                    SweetAlertDialog d = new SweetAlertDialog(FeedbackActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                    d.setTitleText("Gửi phản hồi thành công");
                    d.setContentText("Cảm ơn bạn đã đóng góp ý kiến. Chúng tôi sẽ xem xét sớm nhất có thể!");
                    d.setConfirmText(getString(R.string.dialog_ok));
                    d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            FeedbackActivity.this.finish();
                        }
                    });
                    d.show();
                }
            });
        }
    };

    private String generatePreviewHtmlFeedback(Map<String, String> infos) {
        StringBuffer html = new StringBuffer();
        Iterator<String> keys = infos.keySet().iterator();
        while (keys.hasNext()) {
            String k = keys.next();
            html.append("<h4 style=\"color:#4acd00\">" + k + "</h4>");
            html.append("<p><label>" + infos.get(k) + "</label></p>");
            html.append("<hr>");
        }
        return html.toString();
    }

}
