package uk.bh96.openworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int MENU_RESUME = 1;
    private static final int MENU_START = 2;
    private static final int MENU_STOP = 3;

    private GameThread mGameThread;
    private GameView mGameView;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mGameView = (GameView)findViewById(R.id.gamearea);
        mGameView.setFpsView((TextView)findViewById(R.id.fps));
        mGameView.setCoordsView((TextView)findViewById(R.id.coords));
        mGameView.setScoreView((TextView)findViewById(R.id.score));
        mGameView.setHealthView((TextView)findViewById(R.id.health));
        mGameView.setStatusView((TextView)findViewById(R.id.text));
        this.startGame(mGameView, null, savedInstanceState);

        final Button button = (Button) findViewById(R.id.home_play);
        final EditText seed = (EditText) findViewById(R.id.home_seed);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGameThread.doStart(seed.getText().toString());
                button.setVisibility(View.INVISIBLE);
                seed.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void startGame(GameView gView, GameThread gThread, Bundle savedInstanceState) {
        //Set up a new game, we don't care about previous states
        mGameThread = new TheGame(mGameView);
        mGameView.setThread(mGameThread);
        mGameThread.setState(GameThread.STATE_READY);
    }

	/*
	 * Activity state functions
	 */

    @Override
    protected void onPause() {
        super.onPause();
        if(mGameThread.getMode() == GameThread.STATE_RUNNING) {
            mGameThread.setState(GameThread.STATE_PAUSE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameView.cleanup();
        mGameThread = null;
        mGameView = null;
    }
}

// This file is part of "OpenWorld"
// Copyright: Benjamin Howe
// It is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// It is is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//
// You should have received a copy of the GNU General Public License
// along with it.  If not, see <http://www.gnu.org/licenses/>.