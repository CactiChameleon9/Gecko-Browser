package com.example.geckobrowser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button b = findViewById(R.id.settingsSaveButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = getSharedPreferences("settingsPrefs", MODE_PRIVATE).edit();

                Switch s;

                s = findViewById(R.id.desktopToggle);
                if (s.isChecked()){
                    editor.putBoolean("desktopMode", true);
                } else {
                    editor.putBoolean("desktopMode", false);
                }

                s = findViewById(R.id.disableJavascriptToggle);
                if (s.isChecked()){
                    editor.putBoolean("javascript", false);
                } else {
                    editor.putBoolean("javascript", true);

                }

                s = findViewById(R.id.autoHideBarToggle);
                if (s.isChecked()){
                    editor.putBoolean("autoHideBar", true);
                } else {
                    editor.putBoolean("autoHideBar", false);
                }

                s = findViewById(R.id.disableTPToggle);
                if (s.isChecked()){
                    editor.putBoolean("trackingProtection", false);
                } else {
                    editor.putBoolean("trackingProtection", true);
                }

                s = findViewById(R.id.privateModeToggle);
                if (s.isChecked()){
                    editor.putBoolean("privateMode", true);
                } else {
                    editor.putBoolean("privateMode", false);
                }

                s = findViewById(R.id.hideWhiteSplashToggle);
                if (s.isChecked()){
                    editor.putBoolean("hideWhiteSplash", true);
                } else {
                    editor.putBoolean("hideWhiteSplash", false);
                }

                s = findViewById(R.id.searchSuggestionsToggle);
                if (s.isChecked()){
                    editor.putBoolean("searchSuggestions", true);
                } else {
                    editor.putBoolean("searchSuggestions", false);
                }
                EditText e;

                e = findViewById(R.id.userAgentEditText);
                editor.putString("userAgent", e.getText().toString());

                e = findViewById(R.id.searchEngineEditText);
                editor.putString("searchEngine", e.getText().toString());

                editor.commit(); //needs to be commit otherwise its stopped by restart

                restartApp();
            }
        });

        SharedPreferences prefs = getSharedPreferences("settingsPrefs", MODE_PRIVATE);
        Switch s;

        s = findViewById(R.id.desktopToggle);
        s.setChecked(prefs.getBoolean("desktopMode", false));

        s = findViewById(R.id.disableJavascriptToggle);
        s.setChecked(!prefs.getBoolean("javascript", true)); //reversed with !

        s = findViewById(R.id.autoHideBarToggle);
        s.setChecked(prefs.getBoolean("autoHideBar", false));

        s = findViewById(R.id.disableTPToggle);
        s.setChecked(!prefs.getBoolean("trackingProtection", true)); //reversed with !

        s = findViewById(R.id.privateModeToggle);
        s.setChecked(prefs.getBoolean("privateMode", false));

        s = findViewById(R.id.searchSuggestionsToggle);
        s.setChecked(prefs.getBoolean("searchSuggestions", true));

        s = findViewById(R.id.hideWhiteSplashToggle);
        s.setChecked(prefs.getBoolean("hideWhiteSplash", false));

        EditText e;
        e = findViewById(R.id.userAgentEditText);
        e.setText(prefs.getString("userAgent", ""));

        e = findViewById(R.id.searchEngineEditText);
        e.setText(prefs.getString("searchEngine", ""));
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    public void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}