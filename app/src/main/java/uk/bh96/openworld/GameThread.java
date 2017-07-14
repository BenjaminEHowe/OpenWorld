package uk.bh96.openworld;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.text.DecimalFormat;

public abstract class GameThread extends Thread {
	//Different mMode states
	static final int STATE_LOSE = 1;
	static final int STATE_PAUSE = 2;
	static final int STATE_READY = 3;
	static final int STATE_RUNNING = 4;

	//Control variable for the mode of the game (e.g. STATE_WIN)
	private int mMode = 1;

	//Control of the actual running inside run()
	private boolean mRun = false;
		
	//The surface this thread (and only this thread) writes upon
	private SurfaceHolder mSurfaceHolder;
	
	//the message handler to the View/Activity thread
	private Handler mHandler;
	
	//Android Context - this stores almost all we need to know
	private Context mContext;
	
	//The view
	private GameView mGameView;

    private long frameCount = 0;
    private long fpsCount = 0;

	//We might want to extend this call - therefore protected
	int mCanvasWidth = 1;
	int mCanvasHeight = 1;

	//Last time we updated the game physics
	private long mLastTime = 0;
 
	private Bitmap mBackgroundImage;
	
	private long score = 0;

    //Used for time keeping
	private long now;
	private float elapsed;

    //Used to ensure appropriate threading
    private static final Integer monitor = 1;
	

	GameThread(GameView gameView) {
		mGameView = gameView;
		
		mSurfaceHolder = gameView.getHolder();
		mHandler = gameView.getmHandler();
		mContext = gameView.getContext();
		
		mBackgroundImage = BitmapFactory.decodeResource
							(gameView.getContext().getResources(), 
							R.drawable.background);
	}
	
	/*
	 * Called when app is destroyed, so not really that important here
	 * But if (later) the game involves more thread, we might need to stop a thread, and then we would need this
	 * Dare I say memory leak...
	 */
	void cleanup() {
		this.mContext = null;
		this.mGameView = null;
		this.mHandler = null;
		this.mSurfaceHolder = null;
	}
	
	//Pre-begin a game
	abstract public void setupBeginning(String seed);
	
	//Starting up the game
	void doStart(String seed) {
		synchronized(monitor) {
			setupBeginning(seed);
			mLastTime = System.currentTimeMillis() + 100;
			setState(STATE_RUNNING);
			setScore(0);
		}
	}
	
	//The thread start
	@Override
	public void run() {
		Canvas canvasRun;
		while (mRun) {
			canvasRun = null;
			try {
				canvasRun = mSurfaceHolder.lockCanvas(null);
				synchronized (monitor) {
					if (mMode == STATE_RUNNING) {
						updatePhysics();
					}
					doDraw(canvasRun);
				}
			} 
			finally {
				if (canvasRun != null) {
					if(mSurfaceHolder != null)
						mSurfaceHolder.unlockCanvasAndPost(canvasRun);
				}
			}
		}
	}
	
	/*
	 * Surfaces and drawing
	 */
	void setSurfaceSize(int width, int height) {
		synchronized (monitor) {
			mCanvasWidth = width;
			mCanvasHeight = height;

			// don't forget to resize the background image
			mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, width, height, true);
		}
	}


	protected void doDraw(Canvas canvas) {
		
		if(canvas == null) return;

		if(mBackgroundImage != null) canvas.drawBitmap(mBackgroundImage, 0, 0, null);
	}
	
	private void updatePhysics() {
		now = System.currentTimeMillis();
		elapsed = (now - mLastTime) / 1000.0f;
		updateGame(elapsed);
		mLastTime = now;
	}
	
	abstract protected void updateGame(float secondsElapsed);
	
	/*
	 * Control functions
	 */
	
	//Finger touches the screen
	boolean onTouch(MotionEvent e) {
		int action = e.getAction();
		if(action != MotionEvent.ACTION_DOWN &&
				action != MotionEvent.ACTION_MOVE &&
				action != MotionEvent.ACTION_UP) return false;

		if(mMode == STATE_READY || mMode == STATE_LOSE) return false;
		
		if(action == MotionEvent.ACTION_UP && mMode == STATE_PAUSE) {
			unpause();
			return true;
		}

		if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
			synchronized (monitor) {
				this.actionOnTouch(e.getRawX(), e.getRawY());
			}
		} else if(action == MotionEvent.ACTION_UP) {
			synchronized (monitor) {
				this.actionOnRelease(e.getRawX(), e.getRawY());
			}
		}
		 
		return false;
	}

	protected abstract void actionOnRelease(float x, float y);
	protected abstract void actionOnTouch(float x, float y);
	
	/*
	 * Game states
	 */
	void pause() {
		synchronized (monitor) {
			if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
		}
	}
	
	void unpause() {
		// Move the real time clock up to now
		synchronized (monitor) {
			mLastTime = System.currentTimeMillis();
		}
		setState(STATE_RUNNING);
	}	

	//Send messages to View/Activity thread
	void setState(int mode) {
		synchronized (monitor) {
			setState(mode, null);
		}
	}

	void setState(int mode, CharSequence message) {
		synchronized (monitor) {
			mMode = mode;

			if (mMode == STATE_RUNNING) {
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", "");
				b.putInt("viz", View.INVISIBLE);
				b.putBoolean("showAd", false);	
				msg.setData(b);
				mHandler.sendMessage(msg);
			} 
			else {				
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				
				Resources res = mContext.getResources();
				CharSequence str = "";
				if (mMode == STATE_READY)
					str = res.getText(R.string.mode_ready);
				else 
					if (mMode == STATE_PAUSE)
						str = res.getText(R.string.mode_pause);
					else 
						if (mMode == STATE_LOSE)
							str = res.getText(R.string.mode_lose);

				if (message != null) {
					str = message + "\n" + str;
				}

				b.putString("text", str.toString());
				b.putInt("viz", View.VISIBLE);

				msg.setData(b);
				mHandler.sendMessage(msg);
			}
		}
	}
	
	/*
	 * Getter and setter
	 */	
	public void setSurfaceHolder(SurfaceHolder h) {
		mSurfaceHolder = h;
	}
	
	public boolean isRunning() {
		return mRun;
	}
	
	void setRunning(boolean running) {
		mRun = running;
	}
	
	int getMode() {
		return mMode;
	}

	public void setMode(int mMode) {
		this.mMode = mMode;
	}
	
	
	/* ALL ABOUT SCORES */
	
	//Send a score to the View to view 
	//Would it be better to do this inside this thread writing it manually on the screen?
	private void setScore(long score) {
		this.score = score;
		synchronized (monitor) {
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("score", true);
			b.putString("text", getScoreString().toString());
			msg.setData(b);
			mHandler.sendMessage(msg);
		}
	}

	public float getScore() { return score; }
	
	void updateScore(long score) {
		if (score != 0) {
			this.setScore(this.score + score);
		}
	}

	private CharSequence getScoreString() {
		return Long.toString(Math.round(this.score));
	}

	/* DISPLAY */
	private void displayX(String x, String value) {
        synchronized (monitor) {
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putBoolean(x, true);
            b.putString("text", value);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }
    }

	void displayFps(double fps) {
        frameCount++;
        fpsCount += fps;
        Log.v("FPS", Float.toString(fpsCount / (float) frameCount));
        displayX("fps", Long.toString(Math.round(fps)));
	}

	void displayHealth(double health) {
		displayX("health", Long.toString(Math.round(health)));
	}

    void displayCoords(float x, float y) {
        String xVal = new DecimalFormat("0.00").format(x);
        String yVal = new DecimalFormat("0.00").format(y);
        displayX("coords", "(" + xVal + ", " + yVal + ")");
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