package com.example.geckobrowser;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//TODO add horizontal tablet tab picker
//TODO add downloads
//TODO undo and redo
//TODO TABS
//TODO search completion

public class MainActivity extends AppCompatActivity {

    public String SEARCH_URI_BASE = "https://duckduckgo.com/?q=";
    public String INITIAL_URL = "https://start.duckduckgo.com";
    boolean autoHideBar;
    boolean hideWhiteSplash;
    boolean searchSuggestions;
    private GeckoView mGeckoView;
    private GeckoSession mGeckoSession;
    private GeckoSessionSettings.Builder settingsBuilder = new GeckoSessionSettings.Builder();
    private EditText urlEditText;
    private ProgressBar mProgressView;
    private TextView trackersCountView;
    private ArrayList<String> trackersBlockedList;
    private String[] tabURLs;
    private int activeTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressView = findViewById(R.id.pageProgressView);

        setupUrlEditText();
        setupSettings();
        setupGeckoView();
        setupTabs();


        //allow opening url from another app
        Intent mIntent = getIntent();
        String action = mIntent.getAction();
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            commitURL(mIntent.getData().toString());
        }

    }

    private void setupTabs() {

        //restoring tabs
        SharedPreferences prefs = getSharedPreferences("dataPrefs", MODE_PRIVATE);
        tabURLs = prefs.getString("tabURLs", "").split(", ");
        Log.e("DEBUG", tabURLs[0]);
        activeTab = prefs.getInt("activeTab", 0);

        commitURL(tabURLs[activeTab]);

//        <TextView
//        app:layout_rowWeight="1"
//        app:layout_columnWeight="1"
//        android:text="A"
//        android:textAppearance="?android:attr/textAppearanceLarge"
//        android:padding="30dp"
//        app:layout_column="0"
//        app:layout_row="0"
//        app:layout_gravity="fill" />


        //tabview stuff
        TextView tabsCountView = findViewById(R.id.tabsCountView);
        tabsCountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                View darkOverlayView = findViewById(R.id.darkOverlayView);
//                GridLayout tabsGridLayout = findViewById(R.id.tabsGridLayout);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog
                        .setTitle("Tabs")
                        .setItems(tabURLs, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activeTab = i;
                                commitURL(tabURLs[activeTab]);
                            }
                        })
                        .setPositiveButton("New Tab", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activeTab = tabURLs.length;

                                // create a new ArrayList
                                List<String> tmpList = new ArrayList<>(Arrays.asList(tabURLs));
                                // Add the new element
                                tmpList.add("");
                                // Convert the Arraylist to array
                                tabURLs = tmpList.toArray(tabURLs);

                                tabURLs[activeTab] = "";
                                commitURL(tabURLs[activeTab]);
                            }
                        })
                        .setNegativeButton("Delete Current Tab", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (tabURLs.length > 1) {
                                    // create a new ArrayList
//                                List<String> tmpList = new ArrayList<>(Arrays.asList(tabURLs));

                                    List<String> tmpList = new ArrayList<String>();
                                    for (int j = 0; j < tabURLs.length; j++) {
                                        if (j!=activeTab) { //add all to a new list apart from old
                                            tmpList.add(tabURLs[j]);
                                        }
                                    }

                                    // Convert the Arraylist to array
                                    tabURLs = tmpList.toArray(new String[tmpList.size()]);

                                    activeTab = tabURLs.length - 1;
                                    commitURL(tabURLs[activeTab]);
                                } else {
                                    commitURL("");
                                }
                            }
                        })
                        .create()
                        .show();
//                for (String tabURL : tabURLs) {
//                    Button mValue = new Button(MainActivity.this);
//                    mValue.setLayoutParams(new TableLayout.LayoutParams(
//                            TableLayout.LayoutParams.WRAP_CONTENT,
//                            TableLayout.LayoutParams.WRAP_CONTENT));
//                    mValue.setTextSize(20);
//                    mValue.setPadding(10,10,10,10);
//                    mValue.setAllCaps(false);
////                    mValue.setBackgroundResource(R.color.colorAccent);
//                    mValue.setText(tabURL);
//                    mValue.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            activeTab = Arrays.asList(tabURLs).indexOf(tabURL);
//                            commitURL(tabURL);
//                        }
//                    });
//
//                    tabsGridLayout.addView(mValue);
//                }
//
//
//                if (tabsGridLayout.getVisibility() == View.GONE) {
//                    tabsGridLayout.setVisibility(View.VISIBLE);
//                    darkOverlayView.setVisibility(View.VISIBLE);
//                } else {
//                    tabsGridLayout.setVisibility(View.GONE);
//                    darkOverlayView.setVisibility(View.GONE);
//                }

            }
        });

    }

    private void setupSettings() {
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        SharedPreferences prefs = getSharedPreferences("settingsPrefs", MODE_PRIVATE);

//        if (cookieStoreId != null) {
//            settingsBuilder.contextId(cookieStoreId);
//        }
        settingsBuilder.allowJavascript(prefs.getBoolean("javascript", true));
        settingsBuilder.useTrackingProtection(prefs.getBoolean("trackingProtection", true));
        settingsBuilder.usePrivateMode(prefs.getBoolean("privateMode", false));

        if (!prefs.getString("userAgent", "").equals("")) { //reversed using !
            settingsBuilder.userAgentOverride(prefs.getString("userAgent", ""));
        } else if (prefs.getBoolean("desktopMode", false)) {
            settingsBuilder.userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP);
            settingsBuilder.displayMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP); // not used in example code, remove if issues are caused
            settingsBuilder.viewportMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP);
        } else {
            settingsBuilder.userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE); //better safe than sorry
            settingsBuilder.viewportMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE); //better safe than sorry
        }

        autoHideBar = prefs.getBoolean("autoHideBar", false); //TODO autoHideBar need to set/use elsewhere
        hideWhiteSplash = prefs.getBoolean("hideWhiteSplash", false);
        searchSuggestions = prefs.getBoolean("searchSuggestions", true);

        if (!prefs.getString("searchEngine", "").equals("")) {
            SEARCH_URI_BASE = prefs.getString("searchEngine", "https://duckduckgo.com/?q=");
        }
    }

    private void setupGeckoView() {
        mGeckoView = findViewById(R.id.geckoView);
        GeckoRuntime runtime = GeckoRuntime.create(this);
        mGeckoSession = new GeckoSession(settingsBuilder.build());

        setupTrackersCounter();
        mGeckoSession.setProgressDelegate(new createProgressDelegate());

        mGeckoSession.open(runtime);
        mGeckoView.setSession(mGeckoSession);
        mGeckoSession.loadUri(INITIAL_URL);

//        mGeckoSession.getSettings().setUseTrackingProtection(true); moved elsewhere
        mGeckoSession.setContentBlockingDelegate(new createBlockingDelegate());
    }

    private void setupTrackersCounter() {
        trackersCountView = findViewById(R.id.trackerCountView);
        trackersCountView.setText(R.string._0);
        trackersBlockedList = new ArrayList<String>();

        trackersCountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(trackersBlockedList.isEmpty())) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog
                            .setTitle(R.string.trackers_blocked)
                            .setItems(trackersBlockedList.toArray(new String[0]), null)
                            .create()
                            .show();
                }
            }
        });
    }

    private void setupUrlEditText() {
        urlEditText = findViewById(R.id.urlEditText);

        urlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
//                if (actionId == KeyEvent.KEYCODE_ENDCALL || actionId == KeyEvent.KEYCODE_ENTER) {
                commitURL(urlEditText.getText().toString());
//                }
                return true;
            }
        });
    }

    private void commitURL(String url) {
        if ((url.contains(".") || url.contains(":")) && !url.contains(" ")) { //maybe add list of domain endings here though an array
            url = url.toLowerCase();
            if (!url.contains("https://") && !url.contains("http://")) {
                url = "https://" + url;
            }
            mGeckoSession.loadUri(url);
        } else if (url.equals("") || url.equals(" ")) {
            mGeckoSession.loadUri(INITIAL_URL);
        } else {
            mGeckoSession.loadUri(SEARCH_URI_BASE + url);
        }

        mGeckoView.requestFocus();
        hideKeyboard(this);
    }

    private void clearTrackersCount() {
        try {
            trackersBlockedList.clear();
            trackersCountView.setText(R.string._0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void refreshTabCount() {
        TextView tabsCountView = findViewById(R.id.tabsCountView);
        tabsCountView.setText("" + tabURLs.length);
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void changeLockColour(boolean isHttps) {
        ImageView v = findViewById(R.id.trackerCountViewImage);
        if (isHttps) {
            v.setImageResource(R.drawable.lock_icon_green);
        } else {
            v.setImageResource(R.drawable.lock_icon_red);
        }
    }

    @Override
    protected void onDestroy() {
        Runtime.getRuntime().exit(0); //fix only one runtime allowed crash
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //saving stuff
        SharedPreferences.Editor editor = getSharedPreferences("dataPrefs", MODE_PRIVATE).edit();
        //tabs
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < tabURLs.length; ) {
            a.append(tabURLs[i]).append(", ");
            i++;
        }
        editor.putString("tabURLs", a.toString());
        Log.e("DEBUG", a.toString());
        //activeTab
        editor.putInt("activeTab", activeTab);

        editor.apply();
        super.onPause();
    }

    private class createProgressDelegate implements GeckoSession.ProgressDelegate {

        @Override
        public void onPageStart(GeckoSession session, String url) {
            urlEditText.setText(url);
            tabURLs[activeTab] = url;
            if (url.contains("https://")) {
                changeLockColour(true);
            } else {
                changeLockColour(false);
            }
            clearTrackersCount();
            refreshTabCount();
        }

        @Override
        public void onProgressChange(GeckoSession session, int progress) {
            mProgressView.setProgress(progress);

            if (progress > 0 && progress < 100) {
                mProgressView.setVisibility(View.VISIBLE);
                if (hideWhiteSplash) {
                    mGeckoView.setVisibility(View.INVISIBLE);
                }

            } else {
                mProgressView.setVisibility(View.GONE);
                mGeckoView.setVisibility(View.VISIBLE);
            }

        }
    }

    private class createBlockingDelegate implements ContentBlocking.Delegate {
        @Override
        public void onContentBlocked(GeckoSession session, ContentBlocking.BlockEvent event) {
            Log.e("DEBUG", "Something happened here" + event);

            URL url = null;
            try {
                url = new URL(event.uri);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            assert url != null;
            trackersBlockedList.add(url.getHost());
            trackersCountView.setText("" + trackersBlockedList.size());
        }
    }

}


