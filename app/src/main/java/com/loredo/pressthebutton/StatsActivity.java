package com.loredo.pressthebutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity {

    private boolean _bBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.textHintReturn);
            actionBar.setDisplayHomeAsUpEnabled(true);
            ColorDrawable actionBarBackground = new ColorDrawable(getResources().getColor(R.color.ActionBarBackground, getTheme()));
            actionBar.setBackgroundDrawable(actionBarBackground);
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long totalGames = sp.getLong(getString(R.string.idTotalGames), 0L);
        long gamesFinished = sp.getLong(getString(R.string.idGamesFinished), 0L);
        long bestScore = sp.getLong(getString(R.string.idBestScore), 0L);
        long lastMedianPressSetting = sp.getLong(getString(R.string.idLastAverageTimePress), 0L);
        float lastMedianPress = ((float)lastMedianPressSetting)/1000.0f;
        long medianPressSetting = sp.getLong(getString(R.string.idAverageTimePress), 0L);
        float medianPress = ((float)medianPressSetting)/1000.0f;

        TextView _tvTotalGames = findViewById(R.id.tvTotalGames);
        _tvTotalGames.setText(String.format(getString(R.string.textTotalGames), totalGames));
        TextView _tvBestScore = findViewById(R.id.tvBestScore);
        _tvBestScore.setText(String.format(getString(R.string.textBestScore), bestScore));
        TextView _tvGamesFinished = findViewById(R.id.tvGamesFinished);
        _tvGamesFinished.setText(String.format(getString(R.string.textGamesFinished), gamesFinished));
        TextView _tvMedianPress = findViewById(R.id.tvMedianPress);
        _tvMedianPress.setText(String.format(getString(R.string.textMedianPressTime), medianPress));
        TextView _tvLastMedianPress = findViewById(R.id.tvLastMedianPress);
        _tvLastMedianPress.setText(String.format(getString(R.string.textLastMedianPressTime),lastMedianPress));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        _bBackPressed = true;
        this.finish();
        return super.onOptionsItemSelected(item);
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