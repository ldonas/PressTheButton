package com.loredo.pressthebutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Bundle;

public class Leaderboard extends AppCompatActivity {

    private boolean _bBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
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