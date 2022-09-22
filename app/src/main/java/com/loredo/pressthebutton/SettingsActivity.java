package com.loredo.pressthebutton;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private boolean _bBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ActionBar actionBar = getSupportActionBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();

            getSharedPreferences(getPackageName() + "_preferences",MODE_PRIVATE).registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
                if(s.equals(getString(R.string.idNightMode))) {
                    if (sharedPreferences.getBoolean(s, false))
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                    ActionBar actionBar1 = getSupportActionBar();
                    if (actionBar1 != null) {
                        ColorDrawable actionBarBackground = new ColorDrawable(getResources().getColor(R.color.ActionBarBackground, getTheme()));
                        actionBar1.setBackgroundDrawable(actionBarBackground);
                    }
                } else if(s.equals(getString(R.string.idMusic))) {
                    MediaPlay.LoadMusicPlayer(getApplicationContext());
                    if(sharedPreferences.getBoolean(s, false))
                        MediaPlay.MenuMusicPlayer(MediaPlay.PLAY);
                    else
                        MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
                }
            });

        }

        if (actionBar != null) {
            actionBar.setTitle(R.string.textHintReturn);
            actionBar.setDisplayHomeAsUpEnabled(true);
            ColorDrawable actionBarBackground = new ColorDrawable(getResources().getColor(R.color.ActionBarBackground, getTheme()));
            actionBar.setBackgroundDrawable(actionBarBackground);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        _bBackPressed = true;
        this.finish();
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    @Override
    public void onBackPressed() {
        _bBackPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!_bBackPressed)
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
                MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
            MediaPlay.MenuMusicPlayer(MediaPlay.PLAY);
        _bBackPressed = false;
    }
}