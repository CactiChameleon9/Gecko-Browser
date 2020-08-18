package com.example.geckobrowser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
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
//TODO add horizontal tablet tab picker
//TODO add downloads
//TODO undo and redo
//TODO TABS

public class MainActivity extends AppCompatActivity {

    public String SEARCH_URI_BASE = "https://duckduckgo.com/?q=";
    public String INITIAL_URL = "https://start.duckduckgo.com";

    private GeckoView mGeckoView;
    private GeckoSession mGeckoSession;
    private GeckoSessionSettings.Builder settingsBuilder = new GeckoSessionSettings.Builder();
    private EditText urlEditText;
    private ProgressBar mProgressView;
    private TextView trackersCountView;
    private ArrayList<String> trackersBlockedList;

    boolean autoHideBar;
    boolean hideWhiteSplash;
    boolean searchSuggestions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressView = findViewById(R.id.pageProgressView);

        setupUrlEditText();
        setupSettings();
        setupGeckoView();

        Intent mIntent = getIntent();
        String action = mIntent.getAction();
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            onCommitURL(mIntent.getData().toString());
        }
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
            settingsBuilder.displayMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP); // test, not used in example.
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
                    onCommitURL(urlEditText.getText().toString());
//                }
                return true;
            }
        });
    }

    private void onCommitURL(String url) {
        clearTrackersCount();

        if ((url.contains(".") || url.contains(":")) && !url.contains(" ")) { //maybe add list of domain endings here though an array
            url = url.toLowerCase();
            if (url.contains("https://") || url.contains("http://")) { } else {
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

    private class createProgressDelegate implements GeckoSession.ProgressDelegate {

        @Override
        public void onPageStart(GeckoSession session, String url) {
            urlEditText.setText(url);
        }

        @Override
        public void onProgressChange(GeckoSession session, int progress) {
            mProgressView.setProgress(progress);

            if (progress > 0 && progress < 100) {
                mProgressView.setVisibility(View.VISIBLE);
                if (hideWhiteSplash) {mGeckoView.setVisibility(View.INVISIBLE);}

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
                url = new URL(event.uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            assert url != null;
            trackersBlockedList.add(url.getHost());
            trackersCountView.setText("" + trackersBlockedList.size());
        }
    }

    @Override
    protected void onDestroy() {
        Runtime.getRuntime().exit(0); //fix only one runtime allowed crash
        super.onDestroy();
    }
}


