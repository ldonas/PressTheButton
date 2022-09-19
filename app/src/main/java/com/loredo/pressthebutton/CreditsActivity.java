
package com.loredo.pressthebutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

public class CreditsActivity extends AppCompatActivity {

    private boolean _bBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits_actiity);

        ActionBar actionBar = getSupportActionBar();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        _bBackPressed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!_bBackPressed)
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
                MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
    }
}