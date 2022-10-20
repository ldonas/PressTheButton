package com.loredo.pressthebutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

public class MediaPlay {
    private static MediaPlayer _mpMenu, _mpGame, _mpSucceed, _mpFail, _mpCounterBack, _mpVictory;
    @SuppressLint("StaticFieldLeak")
    private static Context _context;
    public static final int PLAY = 0, STOP = 1;

    public static void LoadMusicPlayer(Context context)
    {
        if(_context == null)
            _context = context;

        if(_mpGame == null)
            try {
                _mpGame = MediaPlayer.create(context, R.raw.game_music);
            } catch (Exception e) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.errorGameMusic), Toast.LENGTH_LONG).show();
            }

        if(_mpMenu == null)
            try {
                _mpMenu = MediaPlayer.create(context, R.raw.menu_music);
            } catch (Exception e) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.errorMenuMusic), Toast.LENGTH_LONG).show();
            }
        if(_mpVictory == null)
            try {
                _mpVictory = MediaPlayer.create(context, R.raw.victory_music);
            } catch (Exception e) {
                Toast.makeText(context.getApplicationContext(),context.getString(R.string.errorVictoryMusic), Toast.LENGTH_LONG).show();
            }
    }

    public static void LoadFXPlayers(Context context)
    {
        if(_context == null)
            _context = context;
        if(_mpSucceed == null)
            try {
                _mpSucceed = MediaPlayer.create(context, R.raw.ui_success);
            } catch (Exception e) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.errorSucceedFX), Toast.LENGTH_LONG).show();
            }

        if(_mpFail == null)
            try {
                _mpFail = MediaPlayer.create(context, R.raw.ui_fail);
            } catch (Exception e) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.errorFailFX), Toast.LENGTH_LONG).show();
            }

        if(_mpCounterBack == null)
            try{
                _mpCounterBack = MediaPlayer.create(context, R.raw.ui_counter_back);
            } catch (Exception e) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.errorCounterFX), Toast.LENGTH_LONG).show();
            }
    }

    public static void GameMusicPlayer(int reproduction)
    {
        if(_mpGame != null)
        {
            if(reproduction == PLAY && !_mpGame.isPlaying()) {
                _mpGame.setLooping(true);
                _mpGame.start();
            }
            else if(reproduction == STOP && _mpGame.isPlaying()) {
                _mpGame.seekTo(0);
                _mpGame.pause();
            }
        }
    }

    public static void MenuMusicPlayer(int reproduction)
    {
        if(_mpMenu != null) {
            if (reproduction == PLAY && !_mpMenu.isPlaying()) {
                _mpMenu.setLooping(true);
                _mpMenu.start();
            }
            else if (reproduction == STOP && _mpMenu.isPlaying()) {
                _mpMenu.seekTo(0);
                _mpMenu.pause();
            }
        }
    }

    public static void VictoryMusicPlayer(int reproduction)
    {
        if(_mpVictory != null) {
            if (reproduction == PLAY && !_mpVictory.isPlaying()) {
                _mpVictory.setLooping(false);
                _mpVictory.start();
            }
            else if (reproduction == STOP && _mpMenu.isPlaying()) {
                _mpVictory.seekTo(0);
                _mpVictory.pause();
            }
        }
    }

    public static void PlaySucceed()
    {
        if(_mpSucceed != null)
        {
            if(_mpSucceed.isPlaying())
                _mpSucceed.seekTo(0);

            _mpSucceed.start();
        }

    }

    public static void PlayFail()
    {
        if(_mpFail != null)
            _mpFail.start();
    }

    public static void PlayCounterBack()
    {
        if(_mpCounterBack != null) {
            if (_mpCounterBack.isPlaying())
                _mpCounterBack.seekTo(0);

            _mpCounterBack.start();
        }
    }
}
