package com.loredo.pressthebutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    private TextView _tvPoints;
    private Button _btnExampleEx;
    private Button _btnNextHint;
    private TableLayout _tlButtons;
    private LinearLayout _llMinutes;
    private ProgressBar _pbSecond, _pbMinute;

    private int _iHint = 0;
    private boolean _bBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        _tvPoints = findViewById(R.id.tvPointsEx);
        _tvPoints.setText(String.format(getString(R.string.textPoints), 100));

        _btnExampleEx = findViewById(R.id.ivExampleEx);
        _tlButtons = findViewById(R.id.tlButtonsEx);
        _pbSecond = findViewById(R.id.pbSecondEx);
        _llMinutes = findViewById(R.id.llMinuteEx);
        _pbMinute = findViewById(R.id.pbMinuteEx);
        _btnNextHint = findViewById(R.id.btnNextHint);
        _btnNextHint.setOnClickListener(view -> NextHint());
        FrameLayout flBackground = findViewById(R.id.flBackground);
        flBackground.setOnClickListener(view -> NextHint());
    }

    private void NextHint()
    {
        int _bgSelected = getResources().getColor(R.color.ActionBarBackground, getTheme());
        int _bgTransparent = getResources().getColor(R.color.none, getTheme());
        if(_btnNextHint != null) {
            if(_iHint == 0) {
                _tvPoints.setElevation(0);
                _tvPoints.setBackgroundColor(_bgTransparent);
                _btnExampleEx.setElevation(10);
                _btnNextHint.setText(getString(R.string.textHint2));
                _iHint++;
            }
            else if(_iHint == 1) {
                _btnExampleEx.setElevation(0);
                _pbSecond.setElevation(10);
                _pbSecond.setBackgroundColor(_bgSelected);
                _btnNextHint.setText(getString(R.string.textHint3));
                _iHint++;
            }
            else if(_iHint == 2) {
                _pbSecond.setElevation(0);
                _pbSecond.setBackgroundColor(_bgTransparent);
                _llMinutes.setElevation(10);
                _llMinutes.setBackgroundColor(_bgSelected);
                _btnNextHint.setText(getString(R.string.textHint4));
                _iHint++;
            }
            else if(_iHint == 3) {
                _llMinutes.setElevation(0);
                _llMinutes.setBackgroundColor(_bgTransparent);
                _tlButtons.setElevation(10);
                _tlButtons.setBackgroundColor(_bgSelected);
                _btnNextHint.setText(getString(R.string.textHint5));
                _iHint++;
            }
            else if(_iHint == 4) {
                _tlButtons.setElevation(0);
                _tlButtons.setBackgroundColor(_bgTransparent);
                _btnNextHint.setText(getString(R.string.textHintReturn));
                _iHint++;
            }
            else if(_iHint == 5) {
                _bBackPressed = true;
                this.finish();
            }
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
}