package uk.bh96.openworld;

import android.util.Log;

/**
 * The player character, which the user controls. Most of the work is now done by the Lifeform class.
 */
public class Player extends Lifeform {
    /**
     * The animated sprites for this object.
     */
    private static final int bitmaps[] = new int[]{
        R.drawable.player_left1,
        R.drawable.player_left2,
        R.drawable.player_left3,
        R.drawable.player_left4,
        R.drawable.player_right1,
        R.drawable.player_right2,
        R.drawable.player_right3,
        R.drawable.player_right4,
    };
    /**
     * The world that this player exists within.
     */
    private World world;

    /**
     * @param world the world that the player exists within.
     */
    public Player(World world) {
        super(world);
        this.world = world;
        world.addPlayer(this);
        setHealth(100);
        setX(512);
        setY(128);
    }

    /**
     * @return the correct bitmap to display in order for smooth animation.
     */
    public int getBitmap() {
        switch (direction) {
            case LEFT:
                return bitmaps[(getBitmapStage() + 128) % 4];
            case RIGHT:
                return bitmaps[((getBitmapStage() + 128) % 4) + 4];
        }
        return 0; // if we get here something has gone seriously wrong
    }

    public int getWidth() { return 49; }
    protected final float getSpeed() { return 0.2f; }
    public int getHeight() { return 68; }

    /**
     * Makes the parent "jump" function publicly visible (as opposed to protected / child classes only).
     */
    public boolean jump() { return super.jump(); }
    /**
     * Makes the parent "startMoving" function publicly visible (as opposed to protected / child classes only).
     */
    public boolean startMoving(Direction direction) { return super.startMoving(direction); }
    /**
     * Makes the parent "stopMoving" function publicly visible (as opposed to protected / child classes only).
     */
    public void stopMoving() { super.stopMoving(); }

    /**
     * Updates this player, and then the world that the player exists within.
     * @param secondsElapsed the number of seconds (or part thereof) that have elapsed since the last update.
     */
    public void update(float secondsElapsed) {
        super.update(secondsElapsed);
        world.update(secondsElapsed);
    }
}
