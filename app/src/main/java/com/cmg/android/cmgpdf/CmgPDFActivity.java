/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.cmgpdf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.cmg.android.cmgpdf.view.TwoWayView;
import com.cmg.android.cmgpdf.view.TwoWayView.Orientation;
import com.cmg.android.common.CommonIntent;
import com.cmg.android.common.Environment;
import com.cmg.android.common.ViewWrapperInfo;
import com.cmg.android.pension.activity.NewsletterDetailActivity;
import com.cmg.android.pension.activity.content.AboutActivity;
import com.cmg.android.pension.activity.content.FeedbackActivity;
import com.cmg.android.pension.activity.content.HelpActivity;
import com.cmg.android.pension.activity.content.ShareActivity;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.plmobile.PreferenceAcitivity;
import com.cmg.android.plmobile.R;
import com.cmg.android.preference.Preference;
import com.cmg.android.task.BookmarkTask;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.ExceptionHandler;
import com.cmg.mobile.shared.data.Newsletter;

import java.io.InputStream;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator Hai Lu
 * @Last changed: $LastChangedDate$
 */

public class CmgPDFActivity extends SherlockActivity implements
        FilePicker.FilePickerSupport, OnQueryTextListener {

    public static final String NEED_REFRESH_PREV_ACTIVITY = "com.cmg.android.cmgpdf.CmgPDFActivity";

    /* The core rendering instance */
    enum TopBarMode {
        Main, Search, Annot, Delete, More, Accept
    }

    ;

    enum AcceptMode {
        Highlight, Underline, StrikeOut, Ink, CopyText
    }

    ;

    private final int OUTLINE_REQUEST = 0;
    private final int PRINT_REQUEST = 1;
    private final int FILEPICK_REQUEST = 2;
    private MuPDFCore core;
    private String mFileName;
    private MuPDFReaderView mDocView;
    private View mButtonsView;
    private boolean mButtonsVisible;
    private EditText mPasswordView;
    // private TextView mFilenameView;
    // private SeekBar mPageSlider;
    private int mPageSliderRes;
    private TextView mPageNumberView;
    private TextView mInfoView;
    // private ImageButton mSearchButton;
    // private ImageButton mReflowButton;
    // private ImageButton mOutlineButton;
    // private ImageButton mMoreButton;
    // private TextView mAnnotTypeText;
    // private ImageButton mAnnotButton;
    // private ViewAnimator mTopBarSwitcher;
    // private ImageButton mLinkButton;
    private TopBarMode mTopBarMode = TopBarMode.Main;
    private AcceptMode mAcceptMode;
    private ImageButton mSearchBack;
    private ImageButton mSearchFwd;
    // private EditText mSearchText;
    private SearchTask mSearchTask;
    private AlertDialog.Builder mAlertBuilder;
    private boolean mLinkHighlight = false;
    private final Handler mHandler = new Handler();
    private final Handler mHandlerPageNumber = new Handler();
    private Runnable runnablePageNumber;
    private boolean mAlertsActive = false;
    private boolean mReflow = false;
    private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;
    private FilePicker mFilePicker;

    private TwoWayView mPreview;
    private PDFPreviewPagerAdapter pdfPreviewPagerAdapter;

    private FrameLayout mPreviewBarHolder;

    private Menu menu;
    protected ViewWrapperInfo viewWrapperInfo;

    private String search;
    private boolean inSearch = false;
    private SearchView searchView;


    private MenuItem actionFavorites;
    private boolean isFavorites = false;
    private boolean enableFavoritesMode = false;

    private Runnable changeFavarRunable;
    private Handler changeFavoritesIconHandler = new Handler();

    private int currentPage = 1;
    private Newsletter newsletter;
    private DatabaseHandler db;
    private BookmarkTask bookmarkTask;

    private boolean needRefreshPrevActivity = false;
    private boolean needRefreshDetailActivity = false;

    public void createAlertWaiter() {
        mAlertsActive = true;
        // All mupdf library calls are performed on asynchronous tasks to avoid
        // stalling
        // the UI. Some calls can lead to javascript-invoked requests to display
        // an
        // alert dialog and collect a reply from the user. The task has to be
        // blocked
        // until the user's reply is received. This method creates an
        // asynchronous task,
        // the purpose of which is to wait of these requests and produce the
        // dialog
        // in response, while leaving the core blocked. When the dialog receives
        // the
        // user's response, it is sent to the core via replyToAlert, unblocking
        // it.
        // Another alert-waiting task is then created to pick up the next alert.
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for (int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1:
                                    index = 0;
                                    break;
                                case AlertDialog.BUTTON2:
                                    index = 1;
                                    break;
                                case AlertDialog.BUTTON3:
                                    index = 2;
                                    break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it
                            // can
                            // continue processing.
                            core.replyToAlert(result);
                            // Create another alert-waiter to pick up the next
                            // alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType) {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType) {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2,
                                getString(R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1,
                                getString(R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3,
                                getString(R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1,
                                getString(R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2,
                                getString(R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                mAlertDialog = null;
                                if (mAlertsActive) {
                                    result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                                    core.replyToAlert(result);
                                    createAlertWaiter();
                                }
                            }
                        });
                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        mFileName = new String(lastSlashPos == -1 ? path
                : path.substring(lastSlashPos + 1));
        System.out.println("Trying to open " + path);
        try {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return core;
    }

    private MuPDFCore openBuffer(byte buffer[]) {
        System.out.println("Trying to open byte buffer");
        try {
            core = new MuPDFCore(this, buffer);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return core;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        registerReceiver(mHandleMessageReceiver, new IntentFilter(
                NEED_REFRESH_PREV_ACTIVITY));

        bookmarkTask = new BookmarkTask(this.getApplicationContext());
        mAlertBuilder = new AlertDialog.Builder(this);

        if (core == null) {
            core = (MuPDFCore) getLastNonConfigurationInstance();

            if (savedInstanceState != null
                    && savedInstanceState.containsKey("FileName")) {
                mFileName = savedInstanceState.getString("FileName");
            }
        }
        if (core == null) {
            Intent intent = getIntent();
            byte buffer[] = null;
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                if (uri.toString().startsWith("content://")) {
                    // Handle view requests from the Transformer Prime's file
                    // manager
                    // Hopefully other file managers will use this same scheme,
                    // if not
                    // using explicit paths.
                    Cursor cursor = getContentResolver().query(uri,
                            new String[]{"_data"}, null, null, null);
                    if (cursor.moveToFirst()) {
                        String str = cursor.getString(0);
                        String reason = null;
                        if (str == null) {
                            try {
                                InputStream is = getContentResolver()
                                        .openInputStream(uri);
                                int len = is.available();
                                buffer = new byte[len];
                                is.read(buffer, 0, len);
                                is.close();
                            } catch (java.lang.OutOfMemoryError e) {
                                System.out
                                        .println("Out of memory during buffer reading");
                                reason = e.toString();
                            } catch (Exception e) {
                                reason = e.toString();
                            }
                            if (reason != null) {
                                buffer = null;
                                Resources res = getResources();
                                AlertDialog alert = mAlertBuilder.create();
                                setTitle(String
                                        .format(res
                                                .getString(R.string.cannot_open_document_Reason),
                                                reason));
                                alert.setButton(AlertDialog.BUTTON_POSITIVE,
                                        getString(R.string.dismiss),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                finish();
                                            }
                                        });
                                alert.show();
                                return;
                            }
                        } else {
                            uri = Uri.parse(str);
                        }
                    }
                }
                if (buffer != null) {
                    core = openBuffer(buffer);
                } else {
                    core = openFile(Uri.decode(uri.getEncodedPath()));
                }
                SearchTaskResult.set(null);
            }
            if (core != null && core.needsPassword()) {
                requestPassword(savedInstanceState);
                return;
            }
            if (core != null && core.countPages() == 0) {
                core = null;
            }
        }
        if (core == null) {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE,
                    getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.show();
            return;
        }

        viewWrapperInfo = new ViewWrapperInfo(getIntent().getExtras());
        viewWrapperInfo.init();
        if (viewWrapperInfo.isValid()) {
            // Set up action bar.
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            db = new DatabaseHandler(this);
            newsletter = db.getById(viewWrapperInfo.getItemIdValue());
            if (newsletter != null) {
                newsletter = db.getBookmark(newsletter);
                enableFavoritesMode = true;
            }
        }

        createUI(savedInstanceState);
    }

    public void requestPassword(final Bundle savedInstanceState) {
        mPasswordView = new EditText(this);
        mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordView
                .setTransformationMethod(new PasswordTransformationMethod());

        AlertDialog alert = mAlertBuilder.create();
        alert.setTitle(R.string.enter_password);
        alert.setView(mPasswordView);
        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (core.authenticatePassword(mPasswordView.getText()
                                .toString())) {
                            createUI(savedInstanceState);
                        } else {
                            requestPassword(savedInstanceState);
                        }
                    }
                });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alert.show();
    }

    private void showPageNumber(int number) {
        mPageNumberView.setText(String.format("%d / %d", number,
                core.countPages()));

        // int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        // if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        // SafeAnimatorInflater safe = new SafeAnimatorInflater(
        // (Activity) this, R.animator.info, (View) mPageNumberView);
        // } else {
        if (!mPageNumberView.isShown()) {
            Animation anim = new TranslateAnimation(0, 0,
                    -mPageNumberView.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageNumberView.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mPageNumberView.startAnimation(anim);
        }

        if (runnablePageNumber != null)
            mHandlerPageNumber.removeCallbacks(runnablePageNumber);
        runnablePageNumber = new Runnable() {
            public void run() {
                Animation anim = new TranslateAnimation(0, 0, 0,
                        -mPageNumberView.getHeight());
                anim.setDuration(200);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        mPageNumberView.setVisibility(View.INVISIBLE);
                    }
                });
                mPageNumberView.startAnimation(anim);
            }
        };
        mHandlerPageNumber.postDelayed(runnablePageNumber, 2000);
        // }
    }

    public void createUI(Bundle savedInstanceState) {
        if (core == null)
            return;
        final Context mContext = this;
        // Now create the UI.
        // First create the document view
        mDocView = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                if (core == null)
                    return;
                showPageNumber(i + 1);
                currentPage = i + 1;
                if (enableFavoritesMode) {
                    isFavorites = newsletter.isBookmarkPage(currentPage);
                    changeFavoritesIcon();

                }

                setCurrentlyViewedPreview();
                super.onMoveToChild(i);
            }

            @Override
            protected void onTapMainDocArea() {
                if (!mButtonsVisible) {
                    showButtons();
                } else {
                    // if (mTopBarMode == TopBarMode.Main)
                    hideButtons();
                }
            }

            @Override
            protected void onDocMotion() {
                hideButtons();
            }

            @Override
            protected void onHit(Hit item) {
                switch (mTopBarMode) {
                    case Annot:
                        if (item == Hit.Annotation) {
                            showButtons();
                            mTopBarMode = TopBarMode.Delete;
                            // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        }
                        break;
                    case Delete:
                        mTopBarMode = TopBarMode.Annot;
                        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        // fall through
                    default:
                        // Not in annotation editing mode, but the pageview will
                        // still select and highlight hit annotations, so
                        // deselect just in case.
                        MuPDFView pageView = (MuPDFView) mDocView
                                .getDisplayedView();
                        if (pageView != null)
                            pageView.deselectAnnotation();
                        break;
                }
            }
        };
        mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

        mSearchTask = new SearchTask(this, core) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                // Ask the ReaderView to move to the resulting page
                mDocView.setDisplayedViewIndex(result.pageNumber);
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView.resetupChildren();
            }
        };

        // Make the buttons overlay, and store all its
        // controls in variables
        makeButtonsView();

        // Set up the page slider
        int smax = Math.max(core.countPages() - 1, 1);
        mPageSliderRes = ((10 + smax - 1) / smax) * 2;

        // Set the file-name text
        // mFilenameView.setText(mFileName);

        // Activate the seekbar
        // mPageSlider
        // .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        // public void onStopTrackingTouch(SeekBar seekBar) {
        // mDocView.setDisplayedViewIndex((seekBar.getProgress() +
        // mPageSliderRes / 2)
        // / mPageSliderRes);
        // }
        //
        // public void onStartTrackingTouch(SeekBar seekBar) {
        // }
        //
        // public void onProgressChanged(SeekBar seekBar,
        // int progress, boolean fromUser) {
        // updatePageNumView((progress + mPageSliderRes / 2)
        // / mPageSliderRes);
        // }
        // });

        // Activate the search-preparing button
        // mSearchButton.setOnClickListener(new View.OnClickListener() {
        // public void onClick(View v) {
        // searchModeOn();
        // }
        // });

        // Activate the reflow button
        // mReflowButton.setOnClickListener(new View.OnClickListener() {
        // public void onClick(View v) {
        // toggleReflow();
        // }
        // });

        // if (core.fileFormat().startsWith("PDF")) {
        // mAnnotButton.setOnClickListener(new View.OnClickListener() {
        // public void onClick(View v) {
        // mTopBarMode = TopBarMode.Annot;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        // }
        // });
        // } else {
        // mAnnotButton.setVisibility(View.GONE);
        // }

        // Search invoking buttons are disabled while there is no text specified
        mSearchBack.setEnabled(false);
        mSearchFwd.setEnabled(false);
        mSearchBack.setColorFilter(Color.argb(255, 233, 233, 233));
        mSearchFwd.setColorFilter(Color.argb(255, 233, 233, 233));

        // React to interaction with the text widget
        // mSearchText.addTextChangedListener(new TextWatcher() {
        //
        // public void afterTextChanged(Editable s) {
        // boolean haveText = s.toString().length() > 0;
        // setButtonEnabled(mSearchBack, haveText);
        // setButtonEnabled(mSearchFwd, haveText);
        //
        // // Remove any previous search results
        // if (SearchTaskResult.get() != null
        // && !mSearchText.getText().toString()
        // .equals(SearchTaskResult.get().txt)) {
        // SearchTaskResult.set(null);
        // mDocView.resetupChildren();
        // }
        // }
        //
        // public void beforeTextChanged(CharSequence s, int start, int count,
        // int after) {
        // }
        //
        // public void onTextChanged(CharSequence s, int start, int before,
        // int count) {
        // }
        // });

        // React to Done button on keyboard
        // mSearchText
        // .setOnEditorActionListener(new TextView.OnEditorActionListener() {
        // public boolean onEditorAction(TextView v, int actionId,
        // KeyEvent event) {
        // if (actionId == EditorInfo.IME_ACTION_DONE)
        // search(1);
        // return false;
        // }
        // });

        // mSearchText.setOnKeyListener(new View.OnKeyListener() {
        // public boolean onKey(View v, int keyCode, KeyEvent event) {
        // if (event.getAction() == KeyEvent.ACTION_DOWN
        // && keyCode == KeyEvent.KEYCODE_ENTER)
        // search(1);
        // return false;
        // }
        // });

        // Activate search invoking buttons
        mSearchBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(-1, search);
            }
        });
        mSearchFwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(1, search);
            }
        });

        // mLinkButton.setOnClickListener(new View.OnClickListener() {
        // public void onClick(View v) {
        // setLinkHighlight(!mLinkHighlight);
        // }
        // });

        // if (core.hasOutline()) {
        // mOutlineButton.setOnClickListener(new View.OnClickListener() {
        // public void onClick(View v) {
        // OutlineItem outline[] = core.getOutline();
        // if (outline != null) {
        // OutlineActivityData.get().items = outline;
        // Intent intent = new Intent(MuPDFActivity.this,
        // OutlineActivity.class);
        // startActivityForResult(intent, OUTLINE_REQUEST);
        // }
        // }
        // });
        // } else {
        // mOutlineButton.setVisibility(View.GONE);
        // }

        // Reenstate last state if it was recorded
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        int selectPage = -1;
        if (enableFavoritesMode) {
            selectPage = viewWrapperInfo.getPage();
        }

        if (selectPage >= 1) {
            mDocView.setDisplayedViewIndex(selectPage - 1);
        } else {
            mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));
        }

        if (savedInstanceState == null
                || !savedInstanceState.getBoolean("ButtonsHidden", false)) {
            showButtons();
            mPreview.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showButtons();
                        }
                    });
                }
            }, 250);
        }

        if (savedInstanceState != null
                && savedInstanceState.getBoolean("SearchMode", false))
            searchModeOn();

        if (savedInstanceState != null
                && savedInstanceState.getBoolean("ReflowMode", false))
            reflowModeSet(true);

        // Stick the document view and the buttons overlay into a parent view
        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(mDocView);
        layout.addView(mButtonsView);
        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OUTLINE_REQUEST:
                if (resultCode >= 0)
                    mDocView.setDisplayedViewIndex(resultCode);
                break;
            case PRINT_REQUEST:
                if (resultCode == RESULT_CANCELED)
                    showInfo(getString(R.string.print_failed));
                break;
            case FILEPICK_REQUEST:
                if (mFilePicker != null && resultCode == RESULT_OK)
                    mFilePicker.onPick(data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Object onRetainNonConfigurationInstance() {
        MuPDFCore mycore = core;
        core = null;
        return mycore;
    }

    private void reflowModeSet(boolean reflow) {
        mReflow = reflow;
        mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core)
                : new MuPDFPageAdapter(this, this, core));
        // mReflowButton.setColorFilter(mReflow ? Color.argb(0xFF, 172, 114, 37)
        // : Color.argb(0xFF, 255, 255, 255));
        // setButtonEnabled(mAnnotButton, !reflow);
        // setButtonEnabled(mSearchButton, !reflow);
        if (reflow)
            setLinkHighlight(false);
        // setButtonEnabled(mLinkButton, !reflow);
        // setButtonEnabled(mMoreButton, !reflow);
        mDocView.refresh(mReflow);
    }

    private void toggleReflow() {
        reflowModeSet(!mReflow);
        showInfo(mReflow ? getString(R.string.entering_reflow_mode)
                : getString(R.string.leaving_reflow_mode));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFileName != null && mDocView != null) {
            outState.putString("FileName", mFileName);

            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded
            // Other info is needed only for screen-orientation change,
            // so it can go in the bundle
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
            edit.commit();
        }

        if (!mButtonsVisible)
            outState.putBoolean("ButtonsHidden", true);

        if (mTopBarMode == TopBarMode.Search)
            outState.putBoolean("SearchMode", true);

        if (mReflow)
            outState.putBoolean("ReflowMode", true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AndroidCommonUtils.takeScreenShot(this);
        if (mSearchTask != null)
            mSearchTask.stop();

        if (mFileName != null && mDocView != null) {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
            edit.commit();
        }
    }

    public void onDestroy() {
        if (mDocView != null)
        mDocView.applyToChildren(new ReaderView.ViewMapper() {
            void applyToView(View view) {
                ((MuPDFView) view).releaseBitmaps();
            }
        });
        if (core != null)
            core.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        core = null;
        Preference.getInstance(this.getApplicationContext()).setSearchPdfText(
                null);
        if (pdfPreviewPagerAdapter!= null)
            pdfPreviewPagerAdapter.recycle();
        if (mHandleMessageReceiver != null) {
            unregisterReceiver(mHandleMessageReceiver);
        }

        super.onDestroy();
    }

    private void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setColorFilter(enabled ? Color.argb(255, 128, 128, 128) : Color
                .argb(255, 233, 233, 233));
    }

    private void setLinkHighlight(boolean highlight) {
        mLinkHighlight = highlight;
        // LINK_COLOR tint
        // mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 172, 114, 37)
        // : Color.argb(0xFF, 255, 255, 255));
        // Inform pages of the change.
        mDocView.setLinksEnabled(highlight);
    }

    private void showButtons() {
        if (core == null)
            return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = mDocView.getDisplayedViewIndex();
            updatePageNumView(index);
            // mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
            // mPageSlider.setProgress(index * mPageSliderRes);
            if (mTopBarMode == TopBarMode.Search) {
                // mSearchText.requestFocus();
                // showKeyboard();
            }

            // Animation anim = new TranslateAnimation(0, 0,
            // -mTopBarSwitcher.getHeight(), 0);
            // anim.setDuration(200);
            // anim.setAnimationListener(new Animation.AnimationListener() {
            // public void onAnimationStart(Animation animation) {
            // mTopBarSwitcher.setVisibility(View.VISIBLE);
            // }
            //
            // public void onAnimationRepeat(Animation animation) {
            // }
            //
            // public void onAnimationEnd(Animation animation) {
            // }
            // });
            // mTopBarSwitcher.startAnimation(anim);
            setCurrentlyViewedPreview();

            // anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
            // anim.setDuration(200);
            // anim.setAnimationListener(new Animation.AnimationListener() {
            // public void onAnimationStart(Animation animation) {
            // mPageSlider.setVisibility(View.VISIBLE);
            // }
            //
            // public void onAnimationRepeat(Animation animation) {
            // }
            //
            // public void onAnimationEnd(Animation animation) {
            // mPageNumberView.setVisibility(View.VISIBLE);
            // }
            // });
            // mPageSlider.startAnimation(anim);

            // mPreviewBarHolder.startAnimation(anim);

            Animation anim = new TranslateAnimation(0, 0,
                    mPreviewBarHolder.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    // mPageSlider.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPreviewBarHolder.setVisibility(View.VISIBLE);
                }
            });
            mPreviewBarHolder.startAnimation(anim);
        }
    }

    private void hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false;
            // hideKeyboard();

            // Animation anim = new TranslateAnimation(0, 0, 0,
            // -mTopBarSwitcher.getHeight());
            // anim.setDuration(200);
            // anim.setAnimationListener(new Animation.AnimationListener() {
            // public void onAnimationStart(Animation animation) {
            // }
            //
            // public void onAnimationRepeat(Animation animation) {
            // }
            //
            // public void onAnimationEnd(Animation animation) {
            // mTopBarSwitcher.setVisibility(View.INVISIBLE);
            // }
            // });
            // mTopBarSwitcher.startAnimation(anim);

            // anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
            // anim.setDuration(200);
            // anim.setAnimationListener(new Animation.AnimationListener() {
            // public void onAnimationStart(Animation animation) {
            // mPageNumberView.setVisibility(View.INVISIBLE);
            // }
            //
            // public void onAnimationRepeat(Animation animation) {
            // }
            //
            // public void onAnimationEnd(Animation animation) {
            // mPageSlider.setVisibility(View.INVISIBLE);
            // }
            // });
            // mPageSlider.startAnimation(anim);

            Animation anim = new TranslateAnimation(0, 0, 0,
                    mPreviewBarHolder.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    // mPreview.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPreviewBarHolder.setVisibility(View.INVISIBLE);
                }
            });
            mPreviewBarHolder.startAnimation(anim);
        }
    }

    private void searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search;
            // Focus on EditTextWidget
            // mSearchText.requestFocus();
            // showKeyboard();
            // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());

            Animation anim = new TranslateAnimation(0, 0,
                    -mSearchBack.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mSearchBack.setVisibility(View.VISIBLE);
                }
            });
            mSearchBack.startAnimation(anim);

            anim = new TranslateAnimation(0, 0, -mSearchFwd.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mSearchFwd.setVisibility(View.VISIBLE);
                }
            });
            mSearchFwd.startAnimation(anim);
        }
    }

    protected void searchModeOff() {
        if (mTopBarMode == TopBarMode.Search) {
            Preference.getInstance(getApplicationContext())
                    .setSearchPdfText("");
            mTopBarMode = TopBarMode.Main;
            hideKeyboard();
            // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
            SearchTaskResult.set(null);
            // Make the ReaderView act on the change to mSearchTaskResult
            // via overridden onChildSetup method.
            mDocView.resetupChildren();
            Animation anim = new TranslateAnimation(0, 0, 0,
                    -mSearchBack.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mSearchBack.setVisibility(View.GONE);
                }
            });
            mSearchBack.startAnimation(anim);

            anim = new TranslateAnimation(0, 0, 0, -mSearchFwd.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mSearchFwd.setVisibility(View.GONE);
                }
            });
            mSearchFwd.startAnimation(anim);
        }
    }

    private void updatePageNumView(int index) {
        if (core == null)
            return;
        mPageNumberView.setText(String.format("%d / %d", index + 1,
                core.countPages()));
    }

    private void printDoc() {
        if (!core.fileFormat().startsWith("PDF")) {
            showInfo(getString(R.string.format_currently_not_supported));
            return;
        }

        Intent myIntent = getIntent();
        Uri docUri = myIntent != null ? myIntent.getData() : null;

        if (docUri == null) {
            showInfo(getString(R.string.print_failed));
        }

        if (docUri.getScheme() == null)
            docUri = Uri.parse("file://" + docUri.toString());

        Intent printIntent = new Intent(this, PrintDialogActivity.class);
        printIntent.setDataAndType(docUri, "aplication/pdf");
        printIntent.putExtra("title", mFileName);
        startActivityForResult(printIntent, PRINT_REQUEST);
    }

    private void showInfo(String message) {
        mInfoView.setText(message);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            SafeAnimatorInflater safe = new SafeAnimatorInflater(
                    (Activity) this, R.animator.info, (View) mInfoView);
        } else {
            mInfoView.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    mInfoView.setVisibility(View.INVISIBLE);
                }
            }, 500);
        }
    }

    private void makeButtonsView() {

        mButtonsView = getLayoutInflater().inflate(R.layout.buttons, null);

        mPreviewBarHolder = (FrameLayout) mButtonsView
                .findViewById(R.id.PreviewBarHolder);
        // mFilenameView = (TextView)
        // mButtonsView.findViewById(R.id.docNameText);
        // mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
        mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);
        mInfoView = (TextView) mButtonsView.findViewById(R.id.info);
        // mSearchButton = (ImageButton)
        // mButtonsView.findViewById(R.id.searchButton);
        // mReflowButton = (ImageButton)
        // mButtonsView.findViewById(R.id.reflowButton);
        // mOutlineButton = (ImageButton)
        // mButtonsView.findViewById(R.id.outlineButton);
        // mAnnotButton = (ImageButton)
        // mButtonsView.findViewById(R.id.editAnnotButton);
        // mAnnotTypeText = (TextView)
        // mButtonsView.findViewById(R.id.annotType);
        // mTopBarSwitcher = (ViewAnimator)
        // mButtonsView.findViewById(R.id.switcher);
        mSearchBack = (ImageButton) mButtonsView.findViewById(R.id.searchBack);
        mSearchFwd = (ImageButton) mButtonsView
                .findViewById(R.id.searchForward);
        // mSearchText = (EditText) mButtonsView.findViewById(R.id.searchText);
        // mLinkButton = (ImageButton)
        // mButtonsView.findViewById(R.id.linkButton);
        // mMoreButton = (ImageButton)
        // mButtonsView.findViewById(R.id.moreButton);
        // mTopBarSwitcher.setVisibility(View.INVISIBLE);
        mPageNumberView.setVisibility(View.INVISIBLE);
        mInfoView.setVisibility(View.INVISIBLE);
        // mSearchBack.setBackgroundColor(Color.TRANSPARENT);
        mSearchBack.setVisibility(View.INVISIBLE);
        // mSearchFwd.setBackgroundColor(Color.TRANSPARENT);
        mSearchFwd.setVisibility(View.INVISIBLE);
        // mPageSlider.setVisibility(View.INVISIBLE);

        mPreview = new TwoWayView(this);
        mPreview.setOrientation(Orientation.HORIZONTAL);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mPreview.setLayoutParams(lp);
        pdfPreviewPagerAdapter = new PDFPreviewPagerAdapter(this, core);
        mPreview.setAdapter(pdfPreviewPagerAdapter);
        mPreview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pArg0, View pArg1,
                                    int position, long id) {
                // hideButtons();
                mDocView.setDisplayedViewIndex((int) id);
            }
        });
        mPreviewBarHolder.addView(mPreview);
    }

    public void centerPreviewAtPosition(int position) {
        if (mPreview.getChildCount() > 0) {
            View child = mPreview.getChildAt(0);
            // assume all children the same width
            int childMeasuredWidth = child.getMeasuredWidth();

            if (childMeasuredWidth > 0) {
                if (core.getDisplayPages() == 2) {
                    mPreview.setSelectionFromOffset(position,
                            (mPreview.getWidth() / 2) - (childMeasuredWidth));
                } else {
                    mPreview.setSelectionFromOffset(position,
                            (mPreview.getWidth() / 2)
                                    - (childMeasuredWidth / 2));
                }
            } else {
                Log.e("centerOnPosition", "childMeasuredWidth = 0");
            }
        } else {
            Log.e("centerOnPosition", "childcount = 0");
        }
    }

    private void setCurrentlyViewedPreview() {
        int i = mDocView.getDisplayedViewIndex();
        if (core.getDisplayPages() == 2) {
            i = (i * 2) - 1;
        }
        pdfPreviewPagerAdapter.setCurrentlyViewing(i);
        centerPreviewAtPosition(i);
    }

    public void OnMoreButtonClick(View v) {
        mTopBarMode = TopBarMode.More;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnCancelMoreButtonClick(View v) {
        mTopBarMode = TopBarMode.Main;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnPrintButtonClick(View v) {
        printDoc();
    }

    public void OnCopyTextButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.CopyText;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        // mAnnotTypeText.setText(getString(R.string.copy_text));
        showInfo(getString(R.string.select_text));
    }

    public void OnEditAnnotButtonClick(View v) {
        mTopBarMode = TopBarMode.Annot;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnCancelAnnotButtonClick(View v) {
        mTopBarMode = TopBarMode.More;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnHighlightButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Highlight;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        // mAnnotTypeText.setText(R.string.highlight);
        showInfo(getString(R.string.select_text));
    }

    public void OnUnderlineButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Underline;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        // mAnnotTypeText.setText(R.string.underline);
        showInfo(getString(R.string.select_text));
    }

    public void OnStrikeOutButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.StrikeOut;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        // mAnnotTypeText.setText(R.string.strike_out);
        showInfo(getString(R.string.select_text));
    }

    public void OnInkButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Ink;
        mDocView.setMode(MuPDFReaderView.Mode.Drawing);
        // mAnnotTypeText.setText(R.string.ink);
        showInfo(getString(R.string.draw_annotation));
    }

    public void OnCancelAcceptButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null) {
            pageView.deselectText();
            pageView.cancelDraw();
        }
        mDocView.setMode(MuPDFReaderView.Mode.Viewing);
        switch (mAcceptMode) {
            case CopyText:
                mTopBarMode = TopBarMode.More;
                break;
            default:
                mTopBarMode = TopBarMode.Annot;
                break;
        }
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnAcceptButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        boolean success = false;
        switch (mAcceptMode) {
            case CopyText:
                if (pageView != null)
                    success = pageView.copySelection();
                mTopBarMode = TopBarMode.More;
                showInfo(success ? getString(R.string.copied_to_clipboard)
                        : getString(R.string.no_text_selected));
                break;

            case Highlight:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.HIGHLIGHT);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case Underline:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.UNDERLINE);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case StrikeOut:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.STRIKEOUT);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case Ink:
                if (pageView != null)
                    success = pageView.saveDraw();
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.nothing_to_save));
                break;
        }
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mDocView.setMode(MuPDFReaderView.Mode.Viewing);
    }

    public void OnCancelSearchButtonClick(View v) {
        searchModeOff();
    }

    public void OnDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.deleteSelectedAnnotation();
        mTopBarMode = TopBarMode.Annot;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnCancelDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.deselectAnnotation();
        mTopBarMode = TopBarMode.Annot;
        // mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    @Deprecated
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // if (imm != null)
        // imm.showSoftInput(mSearchText, 0);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocusView = getCurrentFocus();
            if (currentFocusView != null) {
                imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(),
                        0);
            }
        } catch (Exception ex) {
            // Silent
        }
        // if (imm != null)
        // imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

    protected void search(int direction, String search) {
        if (search == null)
            search = "";
        hideKeyboard();
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;

        mSearchTask.go(search, direction, displayPage, searchPage);
    }

    @Override
    public boolean onSearchRequested() {
        if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
            // hideButtons();
        } else {
            // showButtons();
            searchModeOn();
        }
        return super.onSearchRequested();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
            hideButtons();
        } else {
            showButtons();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        if (core != null) {
            core.startAlerts();
            createAlertWaiter();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (core != null) {
            destroyAlertWaiter();
            core.stopAlerts();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (core.hasChanges()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE)
                        core.save();

                    finish();
                }
            };
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle("Warning");
            alert.setMessage(getString(R.string.document_has_changes_save_them_));
            alert.setButton(AlertDialog.BUTTON_POSITIVE,
                    getString(R.string.yes), listener);
            alert.setButton(AlertDialog.BUTTON_NEGATIVE,
                    getString(R.string.no), listener);
            alert.show();
        } else {
            if (needRefreshPrevActivity) {
                back();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void performPickFor(FilePicker picker) {
        mFilePicker = picker;
        // Intent intent = new Intent(this, ChoosePDFActivity.class);
        // startActivityForResult(intent, FILEPICK_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.bookreader_menu, menu);

        actionFavorites = menu.findItem(R.id.action_favorites);

        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        // searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        search = Preference.getInstance(this.getApplicationContext())
                .getSearchPdfText();
        searchView.setQueryHint(getResources().getString(R.string.search_text));
        searchView.setOnQueryTextListener(this);
        initShareMenu();

        if (search != null && search.length() > 0) {
            searchView.setIconified(false);
            // menu.findItem(R.id.action_search).expandActionView();
            // searchView.performClick();
            searchView.setQuery(search, false);
            inSearch = true;
            setButtonEnabled(mSearchBack, true);
            setButtonEnabled(mSearchFwd, true);
            searchModeOn();
        } else {
            inSearch = false;
            searchModeOff();
        }

        searchView
                .setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        Log.i("onFocusChange", "onFocusChange");
                        inSearch = hasFocus;
                        if (!inSearch
                                && (search == null || search.length() == 0)) {
                            // reset list
                            // inSearch = false;
                            Log.i("onFocusChange", "searchModeOff");
                            searchModeOff();
                        } else if (inSearch) {
                            Log.i("onFocusChange", "searchModeOn");
                            searchModeOn();
                        }
                    }
                });
        changeFavoritesIcon();
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * initial Share menu
     */
    public void initShareMenu() {
        MenuItem actionItem = menu.findItem(R.id.action_share);
        if (viewWrapperInfo != null && viewWrapperInfo.isValid()) {
            ShareActionProvider actionProvider = (ShareActionProvider) actionItem
                    .getActionProvider();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                    viewWrapperInfo.getShareSubject());
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    viewWrapperInfo.getShareText());
            try {
                actionProvider.setShareIntent(shareIntent);
            } catch (Exception e) {
                // silent
            }
        } else {
            actionItem.setVisible(false);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.actionbarsherlock.widget.SearchView.OnQueryTextListener#onQueryTextChange(java.lang.String)
     */
    @Override
    public boolean onQueryTextChange(String arg0) {
        SearchTaskResult.set(null);
        search = arg0;
        Preference.getInstance(this.getApplicationContext()).setSearchPdfText(
                search);
        if (search != null && search.trim().length() > 0) {
            setButtonEnabled(mSearchBack, true);
            setButtonEnabled(mSearchFwd, true);
        } else {
            setButtonEnabled(mSearchBack, false);
            setButtonEnabled(mSearchFwd, false);
        }

        return false;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.actionbarsherlock.widget.SearchView.OnQueryTextListener#onQueryTextSubmit(java.lang.String)
     */
    @Override
    public boolean onQueryTextSubmit(String search) {
        SearchTaskResult.set(null);
        this.search = search;
        Preference.getInstance(this.getApplicationContext()).setSearchPdfText(
                search);
        search(1, search);
        searchView.clearFocus();
        mSearchFwd.requestFocus();
        return false;
    }

    /**
     * navigate between fragments
     *
     * @return
     */
    private boolean back() {
        Intent upIntent = null;
        if (viewWrapperInfo == null) {
            viewWrapperInfo = new ViewWrapperInfo(getIntent().getExtras());
            viewWrapperInfo.init();
        }

        if (viewWrapperInfo.getDetailClass() != null) {
            super.onBackPressed();
//            if (needRefreshDetailActivity) {
//                super.onBackPressed();
//                return true;
//            }
//            upIntent = new Intent(this, viewWrapperInfo.getDetailClass());
//            upIntent.putExtra(viewWrapperInfo.getItemIdKey(),
//                    viewWrapperInfo.getItemIdValue());

        } else if (viewWrapperInfo.getMainClass() != null) {
            if (!needRefreshPrevActivity) {
                super.onBackPressed();
                return true;
            }
            upIntent = new Intent(this, viewWrapperInfo.getMainClass());
        }
        if (upIntent != null) {
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.from(this).addNextIntent(upIntent)
                        .startActivities();
                finish();
                return true;
            } else {
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (needRefreshPrevActivity) {
                    back();
                } else {
                    super.onBackPressed();
                }
                break;
            case R.id.action_about:
                startActivity(AboutActivity.class);
                break;
            case R.id.action_help:
                startActivity(HelpActivity.class);
                break;
            case R.id.action_share_app:
                startActivity(ShareActivity.class);
                break;
            case R.id.action_preferences:
                startActivity(PreferenceAcitivity.class);
                break;
            case R.id.action_feedback:
                startActivity(FeedbackActivity.class);
                break;
            case R.id.action_favorites:
                isFavorites = !isFavorites;
                if (enableFavoritesMode)
                    bookmarkTask.execute(newsletter, currentPage, isFavorites);
                changeFavoritesIcon();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFavoritesIcon() {
        if (actionFavorites == null)
            return;
        if (!enableFavoritesMode) {
            actionFavorites.setVisible(false);
        }

        if (changeFavarRunable != null) {
            changeFavoritesIconHandler.removeCallbacks(changeFavarRunable);
        }
        changeFavarRunable = new Runnable() {
            @Override
            public void run() {
                if (isFavorites) {
                    actionFavorites.setIcon(R.drawable.ic_menu_bookmark_on);
                } else {
                    actionFavorites.setIcon(R.drawable.ic_menu_bookmark);
                }
            }
        };
        changeFavoritesIconHandler.post(changeFavarRunable);


    }

    protected void startActivity(Class<?> c) {
        Intent intent = new Intent();
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(CommonIntent.FROM_ACTIVITY_CLASS, this.getClass().getName());
        //bundle.putString(CommonIntent.FROM_ACTIVITY_CLASS, getIntent().getExtras().getString(CommonIntent.FROM_ACTIVITY_CLASS));
        intent.setClass(this, c);
        intent.putExtras(bundle);
        if (getIntent().getData() != null) {
            intent.setData(getIntent().getData());
        }
        startActivity(intent);
    }

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(NEED_REFRESH_PREV_ACTIVITY)) {
                needRefreshPrevActivity = true;
                if (intent.getExtras().getString(NEED_REFRESH_PREV_ACTIVITY).equalsIgnoreCase(NewsletterDetailActivity.class.getName())) {
                    needRefreshDetailActivity = true;
                }
            }
        }
    };


}