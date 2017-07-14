package uk.bh96.openworld;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private volatile GameThread thread;

	//Handle communication from the GameThread to the View/Activity Thread
	private Handler mHandler;
	
	//Pointers to the views
	private TextView mCoordsView;
	private TextView mFpsView;
    private TextView mScoreView;
	private TextView mHealthView;
	private TextView mStatusView;

	public GameView(final Context context, AttributeSet attrs) {
		super(context, attrs);

		//Get the holder of the screen and register interest
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		//Set up a handler for messages from GameThread
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message m) {
				if(m.getData().getBoolean("fps")) {
					mFpsView.setText(context.getString(R.string.fps, m.getData().getString("text")));
				} else if(m.getData().getBoolean("score")) {
					mScoreView.setText(context.getString(R.string.score, m.getData().getString("text")));
				} else if (m.getData().getBoolean("health")) {
                    mHealthView.setText(context.getString(R.string.health, m.getData().getString("text")));
				} else if (m.getData().getBoolean("coords")) {
                    mCoordsView.setText(m.getData().getString("text"));
                } else {
					//So it is a status
                    int i = m.getData().getInt("viz");
                    switch(i) {
                        case View.VISIBLE:
                            mStatusView.setVisibility(View.VISIBLE);
                            break;
                        case View.INVISIBLE:
                            mStatusView.setVisibility(View.INVISIBLE);
                            break;
                        case View.GONE:
                            mStatusView.setVisibility(View.GONE);
                            break;
                    }

                    mStatusView.setText(m.getData().getString("text"));
				}
 			}
		};
	}
	
	//Used to release any resources.
	public void cleanup() {
		this.thread.setRunning(false);
		this.thread.cleanup();
		
		this.removeCallbacks(thread);
		thread = null;
		
		this.setOnTouchListener(null);
		
		SurfaceHolder holder = getHolder();
		holder.removeCallback(this);
	}
	
	/*
	 * Setters and Getters
	 */

	public void setThread(GameThread newThread) {
		thread = newThread;
		setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
                return thread != null && thread.onTouch(event);
            }
		});
        setClickable(true);
		setFocusable(true);
	}

	public void setFpsView(TextView mFpsView) { this.mFpsView = mFpsView; }
	public void setCoordsView(TextView mCoordsView) { this.mCoordsView = mCoordsView; }
    public void setScoreView(TextView mScoreView) { this.mScoreView = mScoreView; }
	public void setHealthView(TextView mHealthView) { this.mHealthView = mHealthView; }
	public void setStatusView(TextView mStatusView) { this.mStatusView = mStatusView; }
	public Handler getmHandler() { return mHandler; }
	
	/*
	 * Screen functions
	 */
	
	//ensure that we go into pause state if we go out of focus
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if(thread!=null) {
			if (!hasWindowFocus) thread.pause();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if(thread!=null) {
			thread.setRunning(true);
			
			if(thread.getState() == Thread.State.NEW){
				//Just start the new thread
				thread.start();
			} else if(thread.getState() == Thread.State.TERMINATED){
				//Start a new thread
				//Should be this to update screen with old game: new GameThread(this, thread);
				//The method should set all fields in new thread to the value of old thread's fields
				thread = new TheGame(this);
				thread.setRunning(true);
				thread.start();
			}
		}
	}
	
	//Always called once after surfaceCreated. Tell the GameThread the actual size
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(thread!=null) {
			thread.setSurfaceSize(width, height);			
		}
	}

	/*
	 * Need to stop the GameThread if the surface is destroyed
	 * Remember this doesn't need to happen when app is paused on even stopped.
	 */
	public void surfaceDestroyed(SurfaceHolder arg0) {
		
		boolean retry = true;
		if(thread!=null) {
			thread.setRunning(false);
		}
		
		//join the thread with this thread
		while (retry) {
			try {
				if(thread!=null) {
					thread.join();
				}
				retry = false;
			} 
			catch (InterruptedException e) {
			}
		}
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