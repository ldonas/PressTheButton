package com.loredo.pressthebutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Juego extends AppCompatActivity implements View.OnClickListener{

    private Button _ivExampleColor;
    private Button _ib1x1,_ib1x2,_ib1x3,_ib1x4,
                      _ib2x1,_ib2x2,_ib2x3,_ib2x4,
                      _ib3x1,_ib3x2,_ib3x3,_ib3x4,
                      _ib4x1,_ib4x2,_ib4x3,_ib4x4;
    private Button _btnReplay, _btnShare;

    private ProgressBar _pbarSecond, _pbarMinute;

    private TableLayout _tlColors;
    private LinearLayout _llBottom;

    private final ArrayList<Button> _visibleButtons = new ArrayList<>();
    private final ArrayList<Integer> _points = new ArrayList<>();
    private final ArrayList<Float> _multipliers = new ArrayList<>();
    private final long _MAXTIMEPULSE = 2000L, _MAXTIMEGAME = 60000L;
    private long _averagePressTime = 0L;
    private boolean _bFX, _bMusic, _bBackPressed = false;

    private int[] _colorButtons = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};

    private final int[] _iConffetti = new int[]{R.drawable.conffetti_01, R.drawable.conffetti_02, R.drawable.conffetti_03, R.drawable.conffetti_04,
            R.drawable.conffetti_05, R.drawable.conffetti_06, R.drawable.conffetti_07, R.drawable.conffetti_08};
    private final int[] _iAnimations = new int[]{R.anim.fall_and_rotation_00, R.anim.fall_and_rotation_01, R.anim.fall_and_rotation_02,
            R.anim.fall_and_rotation_03, R.anim.fall_and_rotation_04, R.anim.fall_and_rotation_05, R.anim.fall_and_rotation_06,
            R.anim.fall_and_rotation_07, R.anim.fall_and_rotation_08, R.anim.fall_and_rotation_09};

    private int _pbSecondBefore, _pbSecondAfter, _pbMinuteBefore, _pbMinuteAfter;

    private TextView _tvPoints, _tvCounter, _tvSecondsCount, _tvBestScore;
    private Animation _animToSmall, _animShortTimeText, _animShortTimeBar, _animShake, _animLastSecond;
    private ConstraintLayout _clConfeti;
    private Random _rRandom;

    private final Handler _handler = new Handler();
    private long _startTime, _lastPulse;
    private int _iShortTime, _iLastSecond;
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
                if(delta > _MAXTIMEPULSE - _iLastSecond) {
                    _ivExampleColor.startAnimation(_animLastSecond);
                    _iLastSecond = -1;
                }
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
            //Cuenta atrás inicial
            if(_tvCounter.getVisibility() == View.VISIBLE) {
                if (delta < 0L) {
                    long second = (delta * -1) / 1000L;
                    _tvCounter.setText(String.valueOf(second + 1));
                    if(_animToSmall != null)
                        _tvCounter.startAnimation(_animToSmall);
                    if(_bFX)
                        MediaPlay.PlayCounterBack();
                    _handler.postDelayed(this, 1000);
                } else {
                    _tvCounter.setVisibility(View.GONE);
                    _tlColors.setVisibility(View.VISIBLE);
                    _pbarMinute.setVisibility(View.VISIBLE);
                    _pbarSecond.setVisibility(View.VISIBLE);
                    _ivExampleColor.setVisibility(View.VISIBLE);
                    _tvPoints.setVisibility(View.VISIBLE);
                    _tvSecondsCount.setVisibility(View.VISIBLE);
                    NewColors();
                    if(_bMusic)
                        MediaPlay.GameMusicPlayer(MediaPlay.PLAY);
                    _handler.postDelayed(this, 100);
                }
            }
            //El juego termina a los 60s
            else if(delta >= _MAXTIMEGAME) {
                if(_tvSecondsCount.getText() != getString(R.string.textTimeFinished)) {
                    _pbarMinute.setProgress(_pbarMinute.getMax(), false);
                    _tvSecondsCount.setTextColor(getColor(R.color.ForegroundColor));
                    _tvSecondsCount.setText(getString(R.string.textTimeFinished));
                    EndGame(false);
                    _handler.postDelayed(this, 100);
                }
            }
            //Proceso normal de juego
            else {
                int progressTint = GamesColor.ColorLerp(_pbMinuteBefore,_pbMinuteAfter,(int)delta,(int)_MAXTIMEGAME);
                long secondsLeft = ((_MAXTIMEGAME - delta)/1000L)+1;
                //Si quedan pocos segundos, mostramos una animación y tintamos de rojo el tiempo
                if(secondsLeft > 0 && secondsLeft == _iShortTime){
                    if(_animShortTimeText != null)
                        _tvSecondsCount.startAnimation(_animShortTimeText);
                    if(_animShortTimeBar != null)
                        _pbarMinute.startAnimation(_animShortTimeBar);
                    _tvSecondsCount.setTextColor(getColor(R.color.pbSecondAfterColor));
                    _iShortTime--;
                }
                _pbarMinute.setProgressTintList(ColorStateList.valueOf(progressTint));
                _pbarMinute.setProgress((int)delta, false);
                _tvSecondsCount.setText(String.format(getString(R.string.textSecondsCount),secondsLeft));
                _handler.postDelayed(this,100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);

        if(MainActivity.ad == 3)
            LoadAds();

        _rRandom = new Random(System.currentTimeMillis());
        _iShortTime = 3;
        _iLastSecond = 1000;

        _animToSmall = AnimationUtils.loadAnimation(this, R.anim.small);
        _animShortTimeText = AnimationUtils.loadAnimation(this, R.anim.short_time_text);
        _animShortTimeBar = AnimationUtils.loadAnimation(this, R.anim.short_time_bar);
        _animShake = AnimationUtils.loadAnimation(this, R.anim.fail);
        _animLastSecond = AnimationUtils.loadAnimation(this, R.anim.last_second);
        _clConfeti = findViewById(R.id.clConfeti);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        _bFX = sp.getBoolean(getString(R.string.idFX), true);
        _bMusic = sp.getBoolean(getString(R.string.idMusic), true);

        if(_bMusic)
            MediaPlay.MenuMusicPlayer(MediaPlay.STOP);

        _pbarSecond = findViewById(R.id.pbSecond);
        _pbarSecond.setMax((int)_MAXTIMEPULSE);
        _pbarSecond.setProgress(0, false);

        _pbarMinute = findViewById(R.id.pbMinute);
        _pbarMinute.setMax((int)_MAXTIMEGAME);
        _pbarMinute.setProgress(0,false);

        _visibleButtons.clear();
        _points.clear();

        _tvPoints = findViewById(R.id.tvPoints);
        _tvPoints.setText(String.format(getResources().getString(R.string.textPoints), GetPoints()));
        _tvCounter = findViewById(R.id.tvCounter);
        _tvBestScore = findViewById(R.id.tvBestScoreGame);

        _ivExampleColor = findViewById(R.id.ivExample);

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

    private void Replay() {
        _bBackPressed = true;
        _pbarMinute.setProgress(0, false);
        _pbarSecond.setProgress(0, false);
        if(MainActivity.ad != 52 && MainActivity.mInterstitialAd != null)
        {
            if(_bMusic) {
                MediaPlay.VictoryMusicPlayer(MediaPlay.STOP);
            }
            MainActivity.mInterstitialAd.show(this);
            _btnReplay.setEnabled(false);
            _btnShare.setEnabled(false);
            MainActivity.ad = 52;
        }
        else {
            this.recreate();
        }
    }

    private void NewColors() {
        //Creamos un vector con los índices de color
        int[] colors = new int[GamesColor.getColorCount()];
        Arrays.setAll(colors, p -> p > GamesColor.getColorCount() ? 0 : p);

        //Seleccionamos un color nulo para cada botón
        _colorButtons = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,-1};

        for(int i = 0; i < _visibleButtons.size(); i++)
        {
            int colorIndex = _rRandom.nextInt(colors.length);
            _colorButtons[i] = colors[colorIndex];
            colors[colorIndex] = -1;
            colors = Arrays.stream(colors).filter(x -> x >= 0).toArray();
        }

        //Colocamos los colores en cada botón correspontiende
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

        //Seleccionamos un color para el ejemplo
        _colorButtons[16] = _colorButtons[_rRandom.nextInt(_visibleButtons.size())];
        _ivExampleColor.setBackgroundColor(GamesColor.GetColor(_colorButtons[16]));
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
    public void onClick(@NonNull View view) {
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

        if(btnColor!= -1)
            _ivExampleColor.clearAnimation();

        if(btnColor == _colorButtons[16]) {
            _iLastSecond = 1000;
            long now = System.currentTimeMillis();
            long delta = now - _lastPulse;
            _lastPulse = now;
            //Calculamos el multiplicador y lo limitamos a 0.5
            float multiplier = ((float)(_MAXTIMEPULSE - (delta))) / 1000.0f;
            multiplier = Float.max(multiplier, 0.5f);
            _points.add(_visibleButtons.size());
            _multipliers.add(multiplier);
            _tvPoints.setText(String.format(getResources().getString(R.string.textPoints), GetPoints()));
            _averagePressTime = ((_averagePressTime * (_multipliers.size()-1)) + delta) / _multipliers.size();
            if(_bFX)
                MediaPlay.PlaySucceed();
            if(_visibleButtons.size() < 16)
                NextLevel();
            NewColors();
            _lastPulse = System.currentTimeMillis();
        }
        else {
            if(_bFX)
                MediaPlay.PlayFail();
            _tvPoints.setText(String.format(getResources().getString(R.string.textFail), GetPoints()));
            EndGame(true);
        }
    }

    private void ShakeScreen() {
        _ivExampleColor.startAnimation(_animShake);
        _tlColors.startAnimation(_animShake);
    }

    private void AppearNewBestScore(){
        _tvBestScore.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_rotate_maximize));
        _tvBestScore.setText(getString(R.string.textNewBestScore));
        _tvBestScore.setVisibility(View.VISIBLE);
    }

    private void AppearFailure(){
        _tvBestScore.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_rotate_reverse_maximize));
        _tvBestScore.setText(getString(R.string.textFailFinished));
        _tvBestScore.setTextColor(getColor(R.color.ForegroundColor));
        _tvBestScore.setShadowLayer(10,0,0, getColor(R.color.ForegroundColorInverted));
        _tvBestScore.setVisibility(View.VISIBLE);
    }

    private void AppearTimeout(){
        _tvBestScore.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_rotate_maximize));
        _tvBestScore.setText(getString(R.string.textTimeFinished));
        _tvBestScore.setTextColor(getColor(R.color.ForegroundColor));
        _tvBestScore.setShadowLayer(10,0,0, getColor(R.color.ForegroundColorInverted));
        _tvBestScore.setVisibility(View.VISIBLE);
    }

    private void AppearFinished(){
        _tvBestScore.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_rotate_maximize));
        _tvBestScore.setText(getString(R.string.textGameFinished));
        _tvBestScore.setTextColor(getColor(R.color.ForegroundColor));
        _tvBestScore.setShadowLayer(10,0,0, getColor(R.color.ForegroundColorInverted));
        _tvBestScore.setVisibility(View.VISIBLE);
    }

    private void EndGame(boolean fail) {
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

        UpdateStats();

        if(_tvBestScore.getAnimation() == null){
            if(_pbarMinute.getProgress() == _pbarMinute.getMax())
                AppearFinished();
            else if(_pbarSecond.getProgress() == _pbarSecond.getMax())
                AppearTimeout();
            else if(fail)
                AppearFailure();
        }

        if(fail) {
            ShakeScreen();
            _tvPoints.setText(String.format(getString(R.string.textFail), GetPoints()));
        }
        else {
            if(_bMusic)
                MediaPlay.VictoryMusicPlayer(MediaPlay.PLAY);
        }

        _llBottom.setVisibility(View.VISIBLE);

        _btnReplay.setVisibility(View.VISIBLE);

        _pbarSecond.setEnabled(false);
        _pbarMinute.setEnabled(false);

        _handler.removeCallbacks(_rSeconds);
        _handler.removeCallbacks(_rMinutes);
    }

    private void UpdateStats(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long totalGames = sp.getLong(getString(R.string.idTotalGames), 0L);
        totalGames++;
        sp.edit().putLong(getString(R.string.idTotalGames), totalGames).apply();
        long bestScore = sp.getLong(getString(R.string.idTotalBestScore), 0L);
        if(GetPoints() > bestScore) {
            AppearNewBestScore();
            LaunchConfeti();
            sp.edit().putLong(getString(R.string.idTotalBestScore), GetPoints()).apply();
        }
        if(GetPoints() > 0) {
            sp.edit().putLong(getString(R.string.idTotalLastPressTime), _averagePressTime).apply();
            long totalBestPressTime = sp.getLong(getString(R.string.idTotalBestPressTime), Long.MAX_VALUE);
            if(_averagePressTime < totalBestPressTime || totalBestPressTime == 0)
                sp.edit().putLong(getString(R.string.idTotalBestPressTime), _averagePressTime).apply();
            long totalAveragePress = sp.getLong(getString(R.string.idTotalAveragePressTime), 0L);
            totalAveragePress = ((totalAveragePress * (totalGames - 1)) + _averagePressTime) / totalGames;
            sp.edit().putLong(getString(R.string.idTotalAveragePressTime), totalAveragePress).apply();
            if(_pbarMinute.getProgress() >= _pbarMinute.getMax()) {
                long gamesFinished = sp.getLong(getString(R.string.idGamesFinished), 0L);
                gamesFinished++;
                sp.edit().putLong(getString(R.string.idGamesFinished), gamesFinished).apply();
                long finishedBestPressTime = sp.getLong(getString(R.string.idFinishedBestPressTime), Long.MAX_VALUE);
                if(_averagePressTime < finishedBestPressTime || finishedBestPressTime == 0)
                    sp.edit().putLong(getString(R.string.idFinishedBestPressTime), _averagePressTime).apply();
                long finishedAveragePress = sp.getLong(getString(R.string.idFinishedAveragePressTime), 0L);
                finishedAveragePress = ((finishedAveragePress * (gamesFinished - 1)) + _averagePressTime) / gamesFinished;
                sp.edit().putLong(getString(R.string.idFinishedAveragePressTime), finishedAveragePress).apply();
                sp.edit().putLong(getString(R.string.idFinishedLastPressTime), _averagePressTime).apply();
            }

            _btnShare.setVisibility(View.VISIBLE);
        }
    }

    private long GetPoints() {
        float points = 0;

        for(int i = 0; i < _points.size(); i++)
        {
            points += (float)_points.get(i) * _multipliers.get(i);
        }
        return (long) points;
    }

    private void LaunchConfeti(){
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idParticles), true)) {
            _clConfeti.setVisibility(View.VISIBLE);
            int maxConfetiSize = Integer.max(_clConfeti.getWidth() / 30, 8);
            for (int i = 0; i < 300; i++) {
                ImageView ivConffetti = new ImageView(_clConfeti.getContext());
                _clConfeti.addView(ivConffetti);
                int conffettiID = _iConffetti[_rRandom.nextInt(_iConffetti.length)];
                ivConffetti.setImageResource(conffettiID);
                int width = _rRandom.nextInt(maxConfetiSize) + maxConfetiSize;
                int height = _rRandom.nextInt(maxConfetiSize) + maxConfetiSize;
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, height);
                params.startToStart = _clConfeti.getId();
                params.topToTop = _clConfeti.getId();
                params.leftMargin = _rRandom.nextInt(_clConfeti.getWidth());
                params.topMargin = -(_rRandom.nextInt(100) + 100);
                ivConffetti.setLayoutParams(params);
                Animation fall = AnimationUtils.loadAnimation(getApplicationContext(), _iAnimations[_rRandom.nextInt(_iAnimations.length)]);
                fall.scaleCurrentDuration(1f + _rRandom.nextFloat());
                ivConffetti.startAnimation(fall);
            }
        }
    }

    private void LoadAds() {
        InterstitialAd.load(this, getString(R.string.InterstitialAdUnitID), MainActivity.adRequest.build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        MainActivity.mInterstitialAd = interstitialAd;
                        FullScreenAd();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        MainActivity.ad = 3;
                        MainActivity.mInterstitialAd = null;
                    }
                });
    }

    private void FullScreenAd() {
        MainActivity.mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdClicked() {
                MainActivity.ad = 52;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                MainActivity.mInterstitialAd = null;
                MainActivity.ad = 52;
                Replay();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                MainActivity.mInterstitialAd = null;
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
            }
        });
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