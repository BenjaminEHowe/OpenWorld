package uk.bh96.openworld.mobs;

import uk.bh96.openworld.Direction;
import uk.bh96.openworld.Player;
import uk.bh96.openworld.R;
import uk.bh96.openworld.World;
import uk.bh96.openworld.blocks.Block;

/**
 * A basic mob which moves at half the speed of the player, and attacks the player when within 0.5 block range.
 */
public class Zombie extends Mob {
    /**
     * The animated sprites for this object.
     */
    private static final int bitmaps[] = new int[]{
            R.drawable.zombie_left1,
            R.drawable.zombie_left2,
            R.drawable.zombie_left3,
            R.drawable.zombie_right1,
            R.drawable.zombie_right2,
            R.drawable.zombie_right3,
    };
    /**
     * The player that this mob is targeting.
     */
    private Player target;

    /**
     * @param world the world that the zombie exists within.
     * @param target the player that the zombie is targetting.
     * @param x the x position that the zombie spawns at.
     * @param y the y position that the zombie spawns at.
     */
    public Zombie(World world, Player target, float x, float y) {
        super(world);
        this.target = target;
        setX(x);
        setY(y);
        setHealth(100);
    }

    /**
     * @return the correct bitmap to display in order for smooth animation.
     */
    public int getBitmap() {
        switch (direction) {
            case LEFT:
                return bitmaps[(getBitmapStage() + 128) % 3];
            case RIGHT:
                return bitmaps[((getBitmapStage() + 128) % 3) + 3];
        }
        return 0; // if we get here something has gone seriously wrong
    }

    public final int getWidth() { return 40; }
    protected final float getSpeed() { return 0.1f; }
    public final int getHeight() { return 64; }

    /**
     * Updates the state of the zombie. Most of they heavy lifting is done in the parent class (Lifeform).
     * @param secondsElapsed the number of seconds (or part thereof) that have elapsed since the last update.
     * @see uk.bh96.openworld.Lifeform
     */
    public void update(float secondsElapsed) {
        // ai logic
        if (target.getX() < super.getX()) {
            super.startMoving(Direction.LEFT);
        } else {
            super.startMoving(Direction.RIGHT);
        }

        // attack human if in range
        boolean inXRange = Math.abs(target.getX() - getX()) < (target.getWidth() / (2 * Block.size)) + getWidth() / (2 * Block.size) + 0.5;
        boolean inYRange = Math.abs(target.getY() - getY()) < 1;
        if (inXRange && inYRange) {
            target.damage(20 * secondsElapsed);
        }

        // call the lifeform update function (for movement, damage, etc.)
        super.update(secondsElapsed);
    }
}
