package uk.bh96.openworld;

//Other parts of the android libraries that we use

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import uk.bh96.openworld.blocks.Block;

import uk.bh96.openworld.mobs.Mob;

public class TheGame extends GameThread {
    private float displayDensity;
    private GameView gameView;
    private Player player;
    private World world;

    //This is run before anything else, so we can prepare things here
    TheGame(GameView gameView) {
        //House keeping
        super(gameView);
        this.gameView = gameView;
        this.displayDensity = gameView.getResources().getDisplayMetrics().density;
    }

    private int[] getMinMaxXY() {
        int minXIndex = (int) Math.floor((player.getX() - ((mCanvasWidth / 2) / (Block.size * displayDensity))));
        int maxXIndex = (int) Math.floor((player.getX() + ((mCanvasWidth / 2) / (Block.size * displayDensity))));
        int minYIndex = (int) Math.floor((player.getY() - ((mCanvasHeight / 2) / (Block.size * displayDensity))));
        int maxYIndex = (int) Math.ceil((player.getY() + ((mCanvasHeight / 2) / (Block.size * displayDensity))));
        if (minXIndex < 0) {
            minXIndex = 0;
        }
        if (maxXIndex > 1023) {
            maxXIndex = 1023;
        }
        if (minYIndex < 0) {
            minYIndex = 0;
        }
        if (maxYIndex > 255) {
            maxYIndex = 255;
        }
        return new int[] {minXIndex, maxXIndex, minYIndex, maxYIndex};
    }

    //This is run before a new game (also after an old game)
    @Override
    public void setupBeginning(String seed) {
        // hash the string into a long: see http://stackoverflow.com/a/1660613
        long worldSeed = 1125899906842597L; // prime
        Log.d("Seed", "Seed = \"" + seed + "\".");
        int len = seed.length();
        if (len > 0) {
            Log.d("Seed", "Seed is NOT blank - hashing.");
            for (int i = 0; i < len; i++) {
                worldSeed = 31*worldSeed + seed.charAt(i);
            }
            Log.d("Seed", "Seed hash = \"" + worldSeed + "\".");
        } else {
            Log.d("Seed", "Seed is blank, using currentTimeMillis().");
            worldSeed = System.currentTimeMillis();
        }
        float screenWidthInBlocks = mCanvasWidth / (Block.size * displayDensity);
        float screenHeightInBlocks = mCanvasHeight / (Block.size * displayDensity);
        world = new World(worldSeed, screenWidthInBlocks, screenHeightInBlocks);
        player = new Player(world);
    }

    @Override
    protected void doDraw(Canvas canvas) {
        long startDrawTime = System.currentTimeMillis();

        //If there isn't a canvas to do nothing
        //It is ok not understanding what is happening here
        if (canvas == null) return;

        //House keeping
        super.doDraw(canvas);

        if (getMode() == STATE_RUNNING) {
            // draw blocks
            int[] minMaxXY = getMinMaxXY();
            int minXIndex = minMaxXY[0];
            int maxXIndex = minMaxXY[1];
            int minYIndex = minMaxXY[2];
            int maxYIndex = minMaxXY[3];
            for (int x = minXIndex; x <= maxXIndex; x++) {
                for (int y = minYIndex; y <= maxYIndex; y++) { // for each row in that column
                    try {
                        Bitmap blockBitmap = BitmapFactory.decodeResource(gameView.getContext().getResources(), world.getBlock(x, y).getBitmap());
                        canvas.drawBitmap(
                                blockBitmap,
                                (x - ((player.getX() - ((mCanvasWidth / 2) / (Block.size * displayDensity))))) * Block.size * displayDensity,
                                mCanvasHeight - ((y - ((player.getY() - ((mCanvasHeight / 2) / (Block.size * displayDensity))))) * Block.size * displayDensity + ((44 / 2) * displayDensity)),
                                null);
                    } catch (NullPointerException e) {
                        // do nothing - no need to draw "air"
                    }
                }
            }

            // draw player
            Bitmap playerBitmap = BitmapFactory.decodeResource(gameView.getContext().getResources(), player.getBitmap());
            canvas.drawBitmap(
                    playerBitmap,
                    (mCanvasWidth / 2) - ((player.getWidth() / 2) * displayDensity),
                    mCanvasHeight - (mCanvasHeight / 2) - ((player.getHeight() / 2) * displayDensity),
                    null);

            // draw mobs
            for (Mob mob : world.getMobs()) {
                Bitmap mobBitmap = BitmapFactory.decodeResource(gameView.getContext().getResources(), mob.getBitmap());
                canvas.drawBitmap(
                        mobBitmap,
                        (mCanvasWidth / 2) - ((mob.getWidth() / 2) * displayDensity) + ((mob.getX() - player.getX()) * Block.size * displayDensity),
                        ((mCanvasHeight / 2) - ((mob.getHeight() / 2) * displayDensity)) - ((mob.getY() - player.getY()) * Block.size * displayDensity),
                        null);
            }
        }

        if (getMode() == STATE_RUNNING) {
            try {
                displayFps(1000 / (System.currentTimeMillis() - startDrawTime));
            } catch (ArithmeticException e) { // if James Anderson appears and does a cheeky division by zero
                displayFps(Double.POSITIVE_INFINITY);
            }
        }
    }


    //This is run whenever the phone is touched by the user
    @Override
    protected void actionOnTouch(float x, float y) {
        Log.d("TouchDetected", "x: " + Float.toString(x) + ", y: " + Float.toString(y));

        if (getMode() == STATE_RUNNING) {
            // player movement: left / right
            if (x < mCanvasWidth * 0.15) {
                Log.d("MovePlayer", "Start player left");
                player.startMoving(Direction.LEFT);
            } else if (x > mCanvasWidth * 0.85) {
                Log.d("MovePlayer", "Start player right");
                player.startMoving(Direction.RIGHT);
            } else {
                Log.d("MovePlayer", "Player still");
                player.stopMoving();
            }

            // player movement: jump
            if (y < mCanvasHeight * 0.15) {
                Log.d("MovePlayer", "Player jump");
                player.jump();
            } else {
                Log.d("MovePlayer", "Player no jump");
            }

            // destroying blocks & attacking mobs
            float touchWorldX = ((x - mCanvasWidth / 2) / (Block.size * displayDensity)) + player.getX();
            float touchWorldY = ((mCanvasHeight / 2 - y) / (Block.size * displayDensity)) + player.getY() + (player.getHeight() / Block.size) - 0.4f;
            Log.d("TouchDetected", "Touch detected at world coords (" + touchWorldX + ", " + touchWorldY + ")");
            if (x > mCanvasWidth * 0.25 && x < mCanvasWidth * 0.75 && y > mCanvasHeight * 0.25 && y < mCanvasHeight * 0.75) {
                int blockX = (int) Math.floor(touchWorldX);
                int blockY = (int) Math.floor(touchWorldY);
                world.destroyBlock(blockX, blockY);
            }
            world.attackMob(touchWorldX, touchWorldY);
        }
    }

    @Override
    protected void actionOnRelease(float x, float y) {
        // player movement: left / right
        if (x < mCanvasWidth * 0.2 || x > mCanvasWidth * 0.8) {
            player.stopMoving();
        }
        world.destroyBlock(-1, -1);
    }

    //This is run just before the game "scenario" is printed on the screen
    @Override
    protected void updateGame(float secondsElapsed) {
        player.update(secondsElapsed);
        displayCoords(player.getX(), player.getY());
        updateScore(world.getScoreChange());
        displayHealth(player.getHealth());
        if (player.getHealth() <= 0) {
            displayHealth(0); // in case the player health dropped below 0
            setState(STATE_LOSE);
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