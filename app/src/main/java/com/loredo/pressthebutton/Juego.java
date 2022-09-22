package com.loredo.pressthebutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Juego extends AppCompatActivity implements View.OnClickListener{

    private Button mImageView;
    private Button _ib1x1,_ib1x2,_ib1x3,_ib1x4,
                      _ib2x1,_ib2x2,_ib2x3,_ib2x4,
                      _ib3x1,_ib3x2,_ib3x3,_ib3x4,
                      _ib4x1,_ib4x2,_ib4x3,_ib4x4;
    private Button _btnReplay, _btnShare;

    private ProgressBar _pbarSecond, _pbarMinute;

    private TableLayout _tlColors;
    private LinearLayout _llBottom, _llExample;

    private final ArrayList<Button> _visibleButtons = new ArrayList<>();
    private final ArrayList<Integer> _points = new ArrayList<>();
    private final ArrayList<Float> _multipliers = new ArrayList<>();
    private final long _MAXTIMEPULSE = 2000L, _MAXTIMEGAME = 60000L;
    private long _medianPressTime = 0L;
    private boolean _bFX, _bMusic, _bBackPressed = false;

    private int[] _colorButtons = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};

    private int _pbSecondBefore, _pbSecondAfter, _pbMinuteBefore, _pbMinuteAfter;

    private TextView _tvPoints, _tvCounter, _tvSecondsCount;
    private Animation _anim;

    private final Handler _handler = new Handler();
    private long _startTime, _lastPulse;
    private final Runnable _rSeconds = new Runnable() {
        @Override
        public void run() {
            long delta = System.currentTimeMillis() - _lastPulse;
            if(delta > _MAXTIMEPULSE) {
                _pbarSecond.setProgress((int)delta,false);
                _tvSecondsCount.setText(getString(R.string.textTimeFinished));
                EndGame(true);
                return;
            }
            else if(delta >= 0) {
                int progressTint = GamesColor.ColorLerp(_pbSecondBefore, _pbSecondAfter, (int) delta, (int) _MAXTIMEPULSE);
                _pbarSecond.setProgress((int) delta, false);
                _pbarSecond.setProgressTintList(ColorStateList.valueOf(progressTint));
            }
            _handler.postDelayed(this, 1);
        }
    };

    private final Runnable _rMinutes = new Runnable() {
        @Override
        public void run() {
            long delta = System.currentTimeMillis() - _startTime;
            if(_tvCounter.getVisibility() == View.VISIBLE) {
                if (delta < 0L) {
                    long second = (delta * -1) / 1000L;
                    _tvCounter.setText(String.valueOf(second + 1));
                    _tvCounter.startAnimation(_anim);
                    if(_bFX)
                        MediaPlay.PlayCounterBack();
                    _handler.postDelayed(this, 1000);
                } else {
                    _tvCounter.setVisibility(View.GONE);
                    _tlColors.setVisibility(View.VISIBLE);
                    _pbarMinute.setVisibility(View.VISIBLE);
                    _pbarSecond.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    _tvPoints.setVisibility(View.VISIBLE);
                    _tvSecondsCount.setVisibility(View.VISIBLE);
                    NewColors();
                    if(_bMusic)
                        MediaPlay.GameMusicPlayer(MediaPlay.PLAY);
                    _handler.postDelayed(this, 100);
                }
            }
            else if(delta > _MAXTIMEGAME)
            {
                delta = _MAXTIMEGAME;
                _pbarMinute.setProgress((int)delta,true);
                _tvSecondsCount.setText(getString(R.string.textTimeFinished));
                EndGame(false);
            }
            else
            {
                int progressTint = GamesColor.ColorLerp(_pbMinuteBefore,_pbMinuteAfter,(int)delta,(int)_MAXTIMEGAME);
                _pbarMinute.setProgressTintList(ColorStateList.valueOf(progressTint));
                _pbarMinute.setProgress((int)delta, true);
                _tvSecondsCount.setText(String.format(getString(R.string.textSecondsCount),((_MAXTIMEGAME - delta)/1000L)));
                _handler.postDelayed(this,100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);

        _anim = AnimationUtils.loadAnimation(this, R.anim.small);
        _llExample = findViewById(R.id.llExample);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        _bFX = sp.getBoolean(getString(R.string.idFX), true);
        _bMusic = sp.getBoolean(getString(R.string.idMusic), true);

        if(_bMusic)
            MediaPlay.MenuMusicPlayer(MediaPlay.STOP);

        _pbarSecond = findViewById(R.id.pbSecond);
        _pbarSecond.setMax((int)_MAXTIMEPULSE);
        _pbarMinute = findViewById(R.id.pbMinute);
        _pbarMinute.setMax((int)_MAXTIMEGAME);

        _visibleButtons.clear();
        _points.clear();

        _tvPoints = findViewById(R.id.tvPoints);
        _tvPoints.setText(String.format(getResources().getString(R.string.textPoints), GetPoints()));
        _tvCounter = findViewById(R.id.tvCounter);

        mImageView = findViewById(R.id.ivExample);

        _tlColors = findViewById(R.id.tlColors);

        _btnReplay = findViewById(R.id.btnReplay);
        _btnReplay.setOnClickListener(view -> Replay());
        _btnShare = findViewById(R.id.btnShare);
        _btnShare.setOnClickListener(this);
        _tvSecondsCount = findViewById(R.id.tvSecondsCount);

        Button _btnShare = findViewById(R.id.btnShare);
        _btnShare.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.textShare), GetPoints()));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        _pbSecondBefore = getResources().getColor(R.color.pbSecondBeforeColor, getTheme());
        _pbSecondAfter = getResources().getColor(R.color.pbSecondAfterColor, getTheme());
        _pbMinuteBefore = getResources().getColor(R.color.pbMinuteBeforeColor, getTheme());
        _pbMinuteAfter = getResources().getColor(R.color.pbMinuteAfterColor, getTheme());

        _ib1x1 = findViewById(R.id.ib1x1);
        _ib1x1.setOnClickListener(this);
        _visibleButtons.add(_ib1x1);

        _ib1x2 = findViewById(R.id.ib1x2);
        _ib1x2.setOnClickListener(this);
        _visibleButtons.add(_ib1x2);

        _ib1x3 = findViewById(R.id.ib1x3);
        _ib1x3.setOnClickListener(this);

        _ib1x4 = findViewById(R.id.ib1x4);
        _ib1x4.setOnClickListener(this);

        _ib2x1 = findViewById(R.id.ib2x1);
        _ib2x1.setOnClickListener(this);

        _ib2x2 = findViewById(R.id.ib2x2);
        _ib2x2.setOnClickListener(this);

        _ib2x3 = findViewById(R.id.ib2x3);
        _ib2x3.setOnClickListener(this);

        _ib2x4 = findViewById(R.id.ib2x4);
        _ib2x4.setOnClickListener(this);

        _ib3x1 = findViewById(R.id.ib3x1);
        _ib3x1.setOnClickListener(this);

        _ib3x2 = findViewById(R.id.ib3x2);
        _ib3x2.setOnClickListener(this);

        _ib3x3 = findViewById(R.id.ib3x3);
        _ib3x3.setOnClickListener(this);

        _ib3x4 = findViewById(R.id.ib3x4);
        _ib3x4.setOnClickListener(this);

        _ib4x1 = findViewById(R.id.ib4x1);
        _ib4x1.setOnClickListener(this);

        _ib4x2 = findViewById(R.id.ib4x2);
        _ib4x2.setOnClickListener(this);

        _ib4x3 = findViewById(R.id.ib4x3);
        _ib4x3.setOnClickListener(this);

        _ib4x4 = findViewById(R.id.ib4x4);
        _ib4x4.setOnClickListener(this);

        if(_bFX)
        {
            _ib1x1.setSoundEffectsEnabled(false);
            _ib1x2.setSoundEffectsEnabled(false);
            _ib1x3.setSoundEffectsEnabled(false);
            _ib1x4.setSoundEffectsEnabled(false);
            _ib2x1.setSoundEffectsEnabled(false);
            _ib2x2.setSoundEffectsEnabled(false);
            _ib2x3.setSoundEffectsEnabled(false);
            _ib2x4.setSoundEffectsEnabled(false);
            _ib3x1.setSoundEffectsEnabled(false);
            _ib3x2.setSoundEffectsEnabled(false);
            _ib3x3.setSoundEffectsEnabled(false);
            _ib3x4.setSoundEffectsEnabled(false);
            _ib4x1.setSoundEffectsEnabled(false);
            _ib4x2.setSoundEffectsEnabled(false);
            _ib4x3.setSoundEffectsEnabled(false);
            _ib4x4.setSoundEffectsEnabled(false);
        }

        _llBottom = findViewById(R.id.llBottom);

        _lastPulse = System.currentTimeMillis() + 3000L;
        _startTime = System.currentTimeMillis() + 3000L;
        _handler.post(_rSeconds);
        _handler.post(_rMinutes);
    }

    private void Replay()
    {
        _bBackPressed = true;
        if(MainActivity.ad != 52 && MainActivity.mInterstitialAd != null)
        {
            MainActivity.mInterstitialAd.show(this);
            MainActivity.ad = 52;
        }
        else {
            this.recreate();
        }
    }

    private void NewColors()
    {
        Random r = new Random(System.currentTimeMillis());
        int[] numbers = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};

        _colorButtons = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

        for(int i = 0; i < _visibleButtons.size(); i++)
        {
            int number = r.nextInt(numbers.length);
            _colorButtons[i] = numbers[number];
            numbers[number] = -1;
            numbers = Arrays.stream(numbers).filter(x -> x >= 0).toArray();
        }

        _colorButtons[16] = _colorButtons[r.nextInt(_visibleButtons.size())];

        _ib1x1.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[0])));
        _ib1x2.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[1])));

        if(_visibleButtons.size() > 2)
        {
            _ib2x1.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[2])));
            _ib2x2.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[3])));
        }
        if(_visibleButtons.size() > 4)
        {
            _ib1x3.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[4])));
            _ib2x3.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[5])));
        }
        if(_visibleButtons.size() > 6)
        {
            _ib3x1.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[6])));
            _ib3x2.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[7])));
            _ib3x3.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[8])));
        }
        if(_visibleButtons.size() > 9)
        {
            _ib1x4.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[9])));
            _ib2x4.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[10])));
            _ib3x4.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[11])));
        }
        if(_visibleButtons.size() > 12)
        {
            _ib4x1.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[12])));
            _ib4x2.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[13])));
            _ib4x3.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[14])));
            _ib4x4.setBackgroundColor(GamesColor.ColorVariant(GamesColor.GetColor(_colorButtons[15])));
        }

        mImageView.setBackgroundColor(GamesColor.GetColor(_colorButtons[16]));
    }

    private void NextLevel(){
        if(_visibleButtons.size() == 2 && GetPoints() > 20)
        {
            _ib2x1.setVisibility(View.VISIBLE);
            _ib2x2.setVisibility(View.VISIBLE);
            _visibleButtons.add(_ib2x1);
            _visibleButtons.add(_ib2x2);
        }
        else if(_visibleButtons.size() == 4 && GetPoints() > 100)
        {
            _ib1x3.setVisibility(View.VISIBLE);
            _ib2x3.setVisibility(View.VISIBLE);
            _visibleButtons.add(_ib1x3);
            _visibleButtons.add(_ib2x3);
        }
        else if(_visibleButtons.size() == 6 && GetPoints() > 220)
        {
            _ib3x1.setVisibility(View.VISIBLE);
            _ib3x2.setVisibility(View.VISIBLE);
            _ib3x3.setVisibility(View.VISIBLE);
            _visibleButtons.add(_ib3x1);
            _visibleButtons.add(_ib3x2);
            _visibleButtons.add(_ib3x3);
        }
        else if(_visibleButtons.size() == 9 && GetPoints() > 400)
        {
            _ib1x4.setVisibility(View.VISIBLE);
            _ib2x4.setVisibility(View.VISIBLE);
            _ib3x4.setVisibility(View.VISIBLE);
            _visibleButtons.add(_ib1x4);
            _visibleButtons.add(_ib2x4);
            _visibleButtons.add(_ib3x4);
        }
        else if(_visibleButtons.size() == 12 && GetPoints() > 640)
        {
            _ib4x1.setVisibility(View.VISIBLE);
            _ib4x2.setVisibility(View.VISIBLE);
            _ib4x3.setVisibility(View.VISIBLE);
            _ib4x4.setVisibility(View.VISIBLE);
            _visibleButtons.add(_ib4x1);
            _visibleButtons.add(_ib4x2);
            _visibleButtons.add(_ib4x3);
            _visibleButtons.add(_ib4x4);
        }
    }

    @Override
    public void onClick(View view) {
        int btnColor = -1;
        if(view.getId() == _ib1x1.getId())
            btnColor = _colorButtons[0];
        if(view.getId() == _ib1x2.getId())
            btnColor = _colorButtons[1];
        if(view.getId() == _ib2x1.getId())
            btnColor = _colorButtons[2];
        if(view.getId() == _ib2x2.getId())
            btnColor = _colorButtons[3];
        if(view.getId() == _ib1x3.getId())
            btnColor = _colorButtons[4];
        if(view.getId() == _ib2x3.getId())
            btnColor = _colorButtons[5];
        if(view.getId() == _ib3x1.getId())
            btnColor = _colorButtons[6];
        if(view.getId() == _ib3x2.getId())
            btnColor = _colorButtons[7];
        if(view.getId() == _ib3x3.getId())
            btnColor = _colorButtons[8];
        if(view.getId() == _ib1x4.getId())
            btnColor = _colorButtons[9];
        if(view.getId() == _ib2x4.getId())
            btnColor = _colorButtons[10];
        if(view.getId() == _ib3x4.getId())
            btnColor = _colorButtons[11];
        if(view.getId() == _ib4x1.getId())
            btnColor = _colorButtons[12];
        if(view.getId() == _ib4x2.getId())
            btnColor = _colorButtons[13];
        if(view.getId() == _ib4x3.getId())
            btnColor = _colorButtons[14];
        if(view.getId() == _ib4x4.getId())
            btnColor = _colorButtons[15];

        if(btnColor == _colorButtons[16]) {
            long now = System.currentTimeMillis();
            long delta = now - _lastPulse;
            _lastPulse = now;
            float multiplier = ((float)(_MAXTIMEPULSE - (delta))) / 1000.0f;
            _points.add(_visibleButtons.size());
            _multipliers.add(multiplier);
            _tvPoints.setText(String.format(getResources().getString(R.string.textPoints), GetPoints()));
            _medianPressTime = ((_medianPressTime * (_multipliers.size()-1)) + delta) / _multipliers.size();
            if(_bFX)
                MediaPlay.PlaySucceed();
            if(_visibleButtons.size() < 16)
                NextLevel();
            NewColors();
            _lastPulse = System.currentTimeMillis();
        }
        else {
            final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
            if(_bFX)
                MediaPlay.PlayFail();
            _tvPoints.setText(String.format(getResources().getString(R.string.textFail), GetPoints()));
            mImageView.startAnimation(animShake);
            _tlColors.startAnimation(animShake);
            EndGame(true);
        }
    }

    private void EndGame(boolean fail)
    {
        if(_bMusic)
            MediaPlay.GameMusicPlayer(MediaPlay.STOP);
        _ib1x1.setEnabled(false);
        _ib1x2.setEnabled(false);
        _ib1x3.setEnabled(false);
        _ib1x4.setEnabled(false);
        _ib2x1.setEnabled(false);
        _ib2x2.setEnabled(false);
        _ib2x3.setEnabled(false);
        _ib2x4.setEnabled(false);
        _ib3x1.setEnabled(false);
        _ib3x2.setEnabled(false);
        _ib3x3.setEnabled(false);
        _ib3x4.setEnabled(false);
        _ib4x1.setEnabled(false);
        _ib4x2.setEnabled(false);
        _ib4x3.setEnabled(false);
        _ib4x4.setEnabled(false);

        _tvPoints.setText(String.format(getString(R.string.textEnd), GetPoints()));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long totalGames = sp.getLong(getString(R.string.idTotalGames), 0L);
        totalGames++;
        sp.edit().putLong(getString(R.string.idTotalGames), totalGames).apply();
        long bestScore = sp.getLong(getString(R.string.idBestScore), 0L);
        if(GetPoints() > bestScore)
        {
            sp.edit().putLong(getString(R.string.idBestScore), GetPoints()).apply();
        }
        if(GetPoints() > 0)
        {
            sp.edit().putLong(getString(R.string.idLastAverageTimePress), _medianPressTime).apply();
            long medianPress = sp.getLong(getString(R.string.idLastAverageTimePress), 0L);
            medianPress = ((medianPress * (totalGames - 1)) + _medianPressTime) / totalGames;
            sp.edit().putLong(getString(R.string.idAverageTimePress), medianPress).apply();
        }
        if(!fail) {
            long gamesFinished = sp.getLong(getString(R.string.idGamesFinished), 0L);
            gamesFinished++;
            sp.edit().putLong(getString(R.string.idGamesFinished), gamesFinished).apply();
        }
        if(GetPoints() > 0) {
            /*mImageView.setText(getString(R.string.textSubmit));
            mImageView.setEnabled(true);
            mImageView.setTextColor(GamesColor.TextColorInverted(GamesColor.GetColor(_colorButtons[16])));
            mImageView.setOnClickListener(view -> SubmitScore());*/

            _btnShare.setVisibility(View.VISIBLE);
        }
        _llBottom.setVisibility(View.VISIBLE);

        _btnReplay.setVisibility(View.VISIBLE);

        _pbarSecond.setEnabled(false);
        _pbarMinute.setEnabled(false);

        _handler.removeCallbacks(_rSeconds);
        _handler.removeCallbacks(_rMinutes);
    }

    private long GetPoints()
    {
        float points = 0;

        for(int i = 0; i < _points.size(); i++)
        {
            points += (float)_points.get(i) * _multipliers.get(i);
        }
        return (long) points;
    }

    @Override
    public void onBackPressed() {
        _bBackPressed = true;
        _handler.removeCallbacks(_rSeconds);
        _handler.removeCallbacks(_rMinutes);
        if(_bMusic) {
            MediaPlay.GameMusicPlayer(MediaPlay.STOP);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        _handler.removeCallbacks(_rSeconds);
        _handler.removeCallbacks(_rMinutes);
        if(_bMusic) {
            MediaPlay.GameMusicPlayer(MediaPlay.STOP);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(System.currentTimeMillis() - _startTime <= 0)
            if(!_bBackPressed) {
                _handler.removeCallbacks(_rSeconds);
                _handler.removeCallbacks(_rMinutes);
                finish();
            }
        if(_bMusic)
            MediaPlay.GameMusicPlayer(MediaPlay.STOP);
    }
}