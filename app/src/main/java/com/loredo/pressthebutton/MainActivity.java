package com.loredo.pressthebutton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultLauncherKt;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.ump.ConsentForm;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static int ad = 0;
    private Button _btnPlay, _btnLeaderboard, _btnHelp, _btnSettings, _btnStats, _btnCredits, _btnSignIn;
    private boolean _launchedIntent = false, _launchHelp = false;
    public static InterstitialAd mInterstitialAd;
    private ConsentForm _consentForm;
    public static AdRequest.Builder adRequest;
    private GoogleSignInClient _gsiaGoogleClient;
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private EventsClient mEventsClient;
    private PlayersClient mPlayersClient;

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
        _btnSignIn.setOnClickListener(view -> startSignInIntent());

        SetSettings();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, "com.google.android.gms.permission.AD_ID") == PackageManager.PERMISSION_GRANTED) {
            MobileAds.initialize(this, initializationStatus -> LoadAds());
        }
        else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET, "com.google.android.gms.permission.AD_ID"}, 1);
        }

        //_gsiaGoogleClient = GoogleSignIn.getClient(MainActivity.this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        //startSignInIntent();
    }

    private void SetSettings() {
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
            sp.edit().putBoolean(getString(R.string.idMusic), true)
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

    private void LoadAds() {
        adRequest = new AdRequest.Builder();

        Bundle extras = new Bundle();
        extras.putString("npa", "1");

        adRequest.addNetworkExtrasBundle(AdMobAdapter.class, extras);

        InterstitialAd.load(this, getString(R.string.InterstitialAdUnitID), adRequest.build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        FullScreenAd();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        ad = 3;
                        mInterstitialAd = null;
                    }
                });
    }

    private void FullScreenAd() {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.idMusic), true)) {
                    MediaPlay.MenuMusicPlayer(MediaPlay.STOP);
                    MediaPlay.GameMusicPlayer(MediaPlay.STOP);
                }
                ad = 52;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                mInterstitialAd = null;
                ad = 52;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
            this.finish();
    }

    private void signInSilently() {
        //Log.d("Games", "signInSilently()");

        _gsiaGoogleClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            //Log.d("Games", "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            _btnSignIn.setVisibility(View.VISIBLE);
                            //Log.d("Games", "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        //Log.d("Games", "onConnected(): connected to Google APIs");

        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mEventsClient = Games.getEventsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        // Show sign-out button on main menu
        //mMainMenuFragment.setShowSignInButton(false);

        // Show "you are signed in" message on win screen, with no sign in button.
        //mWinFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            Exception e = task.getException();
                            //Log.println(Log.ERROR,"Exception", e.getMessage());
                            displayName = "???";
                        }
                        //Log.d("UserName", displayName);
                    }
                });


        // if we have accomplishments to push, push them
        /*if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
        }

        loadAndPrintEvents();*/
    }

    private void onDisconnected() {
        //Log.d("Games", "onDisconnected()");

        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;

        // Show sign-in button on main menu
        //mMainMenuFragment.setShowSignInButton(true);

        // Show sign-in button on win screen
        //mWinFragment.setShowSignInButton(true);

        //mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
    }

    private void startSignInIntent() {
        //startActivityForResult(_gsiaGoogleClient.getSignInIntent(), 10);
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        onConnected(account);
                    } catch (ApiException apiException) {
                        String message = apiException.getMessage();
                        //Log.println(Log.ERROR, "Game", message);
                        onDisconnected();
                    }
                }
                //else Log.println(Log.ERROR, "Game", result.toString());
            }
        });

        launcher.launch(_gsiaGoogleClient.getSignInIntent());
    }

}