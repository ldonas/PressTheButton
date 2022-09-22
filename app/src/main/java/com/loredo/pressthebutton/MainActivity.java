package com.loredo.pressthebutton;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.ump.ConsentForm;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static int ad = 0;
    private Button _btnPlay, _btnLeaderboard, _btnHelp, _btnSettings, _btnStats, _btnCredits, _btnSignIn;
    private boolean _launchedIntent = false, _launchHelp = false;
    public static InterstitialAd mInterstitialAd;
    private ConsentForm _consentForm;
    public static AdRequest.Builder adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btnPlay = findViewById(R.id.btnPlay);
        _btnPlay.setOnClickListener(this);
        _btnStats = findViewById(R.id.btnStats);
        _btnStats.setOnClickListener(this);
        _btnLeaderboard = findViewById(R.id.btnLeaderboard);
        _btnLeaderboard.setOnClickListener(this);
        _btnCredits = findViewById(R.id.btnCredits);
        _btnCredits.setOnClickListener(this);
        _btnHelp = findViewById(R.id.btnHelp);
        _btnHelp.setOnClickListener(this);
        _btnSettings = findViewById(R.id.btnSettings);
        _btnSettings.setOnClickListener(this);
        TextView _tvVersion = findViewById(R.id.tvVersion);
        _tvVersion.setText(BuildConfig.VERSION_NAME);
        _btnSignIn = findViewById(R.id.btnSignIn);

        SetSettings();

        MobileAds.initialize(this, initializationStatus -> LoadAds());
    }

    private void SetSettings()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = sp.getBoolean(getString(R.string.idFirstTime), true);
        if(firstTime)
        {
            sp.edit().putBoolean(getString(R.string.idFirstTime), false).apply();
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    sp.edit().putBoolean(getString(R.string.idNightMode), true).apply();
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    sp.edit().putBoolean(getString(R.string.idNightMode), false).apply();
                    break;
            }
            sp.edit().putLong(getString(R.string.idBestTimePress), 0L)
                    .putLong(getString(R.string.idAverageTimePress), 0L)
                    .putLong(getString(R.string.idLastAverageTimePress), 0L)
                    .putLong(getString(R.string.idTotalGames), 0L)
                    .putLong(getString(R.string.idGamesFinished), 0L)
                    .putLong(getString(R.string.idBestScore),0L)
                    .putBoolean(getString(R.string.idMusic), true)
                    .putBoolean(getString(R.string.idFX), true)
                    .putInt(getString(R.string.idLastVersion),BuildConfig.VERSION_CODE)
                    .putBoolean(getString(R.string.idConsent), false)
                    .apply();

            _launchHelp = true;

            //GetAdsConsent();
        }
        else {
            boolean nightMode = sp.getBoolean(getString(R.string.idNightMode), false);
            if (nightMode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
/*
            if(!sp.getBoolean(getString(R.string.idConsent), false))
            {
                GetAdsConsent();
            }*/
        }

        if(sp.getBoolean(getString(R.string.idMusic), true))
        {
            MediaPlay.LoadMusicPlayer(getApplicationContext());
        }
    }

    private void LoadAds()
    {
        adRequest = new AdRequest.Builder();

        Bundle extras = new Bundle();
        extras.putString("npa", "1");

        adRequest.addNetworkExtrasBundle(AdMobAdapter.class, extras);

        InterstitialAd.load(this, getString(R.string.InterstitialAdUnitID), adRequest.build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.println(Log.ERROR, "AD LOADED", mInterstitialAd.toString());
                        FullScreenAd();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.println(Log.ERROR, "AD ERROR", loadAdError.getMessage());
                        try {
                            wait(5000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LoadAds();
                        //ad = 52;
                        mInterstitialAd = null;
                    }
                });
    }

    private void FullScreenAd()
    {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.idMusic), true)) {
                    MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
                    MediaPlay.GameMusicPlayer(MediaPlay.STOP);
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                mInterstitialAd = null;
                PlayGame();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                mInterstitialAd = null;
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
    public void onClick(View view) {
        if(view.getId() == _btnPlay.getId()) {
            if(_launchHelp)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.textTutorialQuestion)
                        .setPositiveButton(R.string.textYes, (dialog, id) -> {
                            _launchedIntent = true;
                            _launchHelp = false;
                            Intent i = new Intent(MainActivity.this, HelpActivity.class);
                            startActivity(i);
                        })
                        .setNegativeButton(R.string.textNo, (dialog, id) -> {
                            _launchHelp = false;
                            PlayGame();
                        });
                builder.create().show();
            }
            else if(ad != 52 && mInterstitialAd != null){
                    mInterstitialAd.show(MainActivity.this);
                    ad = 52;
            }
            else
                PlayGame();
        }
        else if(view.getId() == _btnStats.getId())
        {
            _launchedIntent = true;
            Intent i = new Intent(this, StatsActivity.class);
            startActivity(i);
        }
        else if(view.getId() == _btnLeaderboard.getId()) {
            _launchedIntent = true;
        }
        else if(view.getId() == _btnCredits.getId())
        {
            _launchedIntent = true;
            Intent i = new Intent(this, CreditsActivity.class);
            startActivity(i);
        }
        else if(view.getId() == _btnHelp.getId()) {
            _launchedIntent = true;
            Intent i = new Intent(this, HelpActivity.class);
            startActivity(i);
        }
        else if(view.getId() == _btnSettings.getId()) {
            _launchedIntent = true;
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }
    }

    private void PlayGame() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(sp.getBoolean(getString(R.string.idMusic),true))
            MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
        if(sp.getBoolean(getString(R.string.idFX), true))
            MediaPlay.LoadFXPlayers(getApplicationContext());

        _launchedIntent = true;
        Intent i = new Intent(this, Juego.class);
        this.startActivity(i);
    }

    @Override
    protected void onDestroy() {
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
            MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
            MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!_launchedIntent)
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
                MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.idMusic), true))
            MediaPlay.MenuMusicPlayer(MediaPlay.PLAY);
        _launchedIntent = false;
    }
}