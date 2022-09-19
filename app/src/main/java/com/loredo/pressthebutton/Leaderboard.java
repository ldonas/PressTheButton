package com.loredo.pressthebutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Bundle;

import com.google.android.gms.games.PlayGames;

public class Leaderboard extends AppCompatActivity {

    private static final int RC_LEADERBOARD_UI = 1052;
    private boolean _bBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
    }

    private void showLeaderboard() {
        PlayGames.getLeaderboardsClient(this)
                .getLeaderboardIntent(getString(R.string.leaderboard_mejores_puntuaciones))
                .addOnSuccessListener(intent -> startActivityForResult(intent, RC_LEADERBOARD_UI));
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
}