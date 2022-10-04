package com.loredo.pressthebutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity {

    private boolean _bBackPressed = false;
    private boolean _bTotalStatsShow = false, _bFinishedStatsShow = false;

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
        long totalBestScore = sp.getLong(getString(R.string.idTotalBestScore), 0L);
        long totalAveragePressSetting = sp.getLong(getString(R.string.idTotalAveragePressTime), 0L);
        float totalAveragePress = ((float)totalAveragePressSetting)/1000.0f;
        long totalBestPressTimeSetting = sp.getLong(getString(R.string.idTotalBestPressTime), 0L);
        float totalBestPressTime = ((float)totalBestPressTimeSetting)/1000.0f;
        long totalLastAveragePressSetting = sp.getLong(getString(R.string.idTotalLastPressTime), 0L);
        float totalLastAveragePress = ((float)totalLastAveragePressSetting)/1000.0f;
        long gamesFinished = sp.getLong(getString(R.string.idGamesFinished), 0L);
        long finishedAveragePressTimeSetting = sp.getLong(getString(R.string.idFinishedAveragePressTime), 0L);
        float finishedAveragePressTime = ((float)finishedAveragePressTimeSetting)/1000.0f;
        long finishedBestPressTimeSetting = sp.getLong(getString(R.string.idFinishedBestPressTime), 0L);
        float finishedBestPressTime = ((float)finishedBestPressTimeSetting)/1000.0f;
        long finishedLastAveragePressTimeSetting = sp.getLong(getString(R.string.idFinishedLastPressTime), 0L);
        float finishedLastAveragePressTime = ((float)finishedLastAveragePressTimeSetting)/1000.0f;

        Button btnTotalGames = findViewById(R.id.btnTotalGames);
        LinearLayout llTotalStats = findViewById(R.id.llTotalStats);
        btnTotalGames.setText(String.format(getString(R.string.textTotalGames), totalGames));
        btnTotalGames.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(btnTotalGames.getContext(), R.drawable.ic_baseline_arrow_drop_down),null,null,null);
        btnTotalGames.setOnClickListener(v -> {
            if(_bTotalStatsShow) {
                btnTotalGames.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(btnTotalGames.getContext(), R.drawable.ic_baseline_arrow_drop_down),null,null,null);
                llTotalStats.setVisibility(View.GONE);
                _bTotalStatsShow = false;
            }
            else {
                btnTotalGames.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(btnTotalGames.getContext(), R.drawable.ic_baseline_arrow_drop_up),null,null,null);
                llTotalStats.setVisibility(View.VISIBLE);
                _bTotalStatsShow = true;
            }
        });
        TextView tvTotalBestScore = findViewById(R.id.tvBestScore);
        tvTotalBestScore.setText(String.format(getString(R.string.textBestScore), totalBestScore));
        TextView tvTotalAveragePress = findViewById(R.id.tvTotalAveragePress);
        tvTotalAveragePress.setText(String.format(getString(R.string.textAveragePressTime), totalAveragePress));
        TextView tvTotalBestPressTime = findViewById(R.id.tvTotalBestPressTime);
        tvTotalBestPressTime.setText(String.format(getString(R.string.textBestPressTime), totalBestPressTime));
        TextView tvTotalLastPressTime = findViewById(R.id.tvTotalLastPressTime);
        tvTotalLastPressTime.setText(String.format(getString(R.string.textLastPressTime),totalLastAveragePress));
        Button btnGamesFinished = findViewById(R.id.btnGamesFinished);
        LinearLayout llFinishedStats = findViewById(R.id.llFinishedStats);
        btnGamesFinished.setText(String.format(getString(R.string.textGamesFinished), gamesFinished));
        btnGamesFinished.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(btnGamesFinished.getContext(), R.drawable.ic_baseline_arrow_drop_down),null,null,null);
        btnGamesFinished.setOnClickListener(v -> {
            if(_bFinishedStatsShow) {
                btnGamesFinished.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(btnGamesFinished.getContext(), R.drawable.ic_baseline_arrow_drop_down),null,null,null);
                llFinishedStats.setVisibility(View.GONE);
                _bFinishedStatsShow = false;
            }
            else {
                btnGamesFinished.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(btnGamesFinished.getContext(), R.drawable.ic_baseline_arrow_drop_up),null,null,null);
                llFinishedStats.setVisibility(View.VISIBLE);
                _bFinishedStatsShow = true;
            }
        });
        TextView tvFinishedAveragePress = findViewById(R.id.tvFinishedAveragePress);
        tvFinishedAveragePress.setText(String.format(getString(R.string.textAveragePressTime), finishedAveragePressTime));
        TextView tvFinishedBestPressTime = findViewById(R.id.tvFinishedBestPressTime);
        tvFinishedBestPressTime.setText(String.format(getString(R.string.textBestPressTime), finishedBestPressTime));
        TextView tvFinishedLastAveragePressTime = findViewById(R.id.tvFinishedLastPressTime);
        tvFinishedLastAveragePressTime.setText(String.format(getString(R.string.textLastPressTime), finishedLastAveragePressTime));

        Button btnResetStats = findViewById(R.id.btnResetStats);

        btnResetStats.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StatsActivity.this);
            builder.setMessage(R.string.textResetStatsQuestion)
                    .setPositiveButton(R.string.textbtnResetAccept, (dialog, id) -> {
                        sp.edit().putLong(getString(R.string.idTotalGames), 0L)
                                .putLong(getString(R.string.idTotalBestScore), 0L)
                                .putLong(getString(R.string.idTotalAveragePressTime), 0L)
                                .putLong(getString(R.string.idTotalBestPressTime), 0L)
                                .putLong(getString(R.string.idTotalLastPressTime), 0L)
                                .putLong(getString(R.string.idGamesFinished), 0L)
                                .putLong(getString(R.string.idFinishedBestScore), 0L)
                                .putLong(getString(R.string.idFinishedAveragePressTime), 0L)
                                .putLong(getString(R.string.idFinishedBestPressTime), 0L)
                                .putLong(getString(R.string.idFinishedLastPressTime), 0L)
                                .apply();
                        _bBackPressed = true;
                        Update();
                    })
                    .setNegativeButton(R.string.textbtnResetCancel, (dialog, id) -> {
                        // User cancelled the dialog
                    });
            builder.create().show();
        });
    }

    private void Update() {
        this.recreate();
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

    @Override
    protected void onResume() {
        super.onResume();
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
            MediaPlay.MenuMusicPlayer(MediaPlay.PLAY);
        _bBackPressed = false;
    }
}