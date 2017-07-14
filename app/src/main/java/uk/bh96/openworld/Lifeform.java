package uk.bh96.openworld;

import uk.bh96.openworld.blocks.Block;
import uk.bh96.openworld.blocks.Lava;

/**
 * This class models a basic lifeform, which can move, jump (and fall under gravity), and take damage.
 */
public abstract class Lifeform extends Entity {
    /**
     * The animation stage of the bitmap.
     */
    private byte bitmapStage = 0;
    /**
     * The direction that the lifeform is facing.
     */
    protected Direction direction = Direction.LEFT;
    /**
     * The health of the lifeform.
     */
    private double health;
    /**
     * Whether the lifeform is moving.
     */
    private boolean moving = false;
    /**
     * The number of seconds (or part thereof) since the bitmap was changed.
     */
    private float secondsSinceBitmapChange = 0;
    /**
     * The number of seconds (or part thereof) since gravity was applied.
     */
    private float secondsSinceGravity = 0;
    /**
     * If the lifeform is falling, whether the lifeform should stop when it lands on solid ground.
     */
    private boolean stopWhenOnSolidGround = false;
    /**
     * The world that the lifeform exists within.
     */
    private World world;
    /**
     * The x coordinate of the lifeform.
     */
    private float x;
    /**
     * The y coordinate of the lifeform.
     */
    private float y;
    /**
     * The vertical speed (jumping and falling) of the lifeform.
     */
    private float ySpeed;

    /**
     * @param world the world that the lifeform exists within.
     */
    protected Lifeform(World world) { this.world = world; }

    /**
     * @param amount the amount to damage the lifeform by.
     */
    public void damage(double amount) { health -= amount; }
    protected byte getBitmapStage() { return bitmapStage; }
    public double getHealth() { return health; }
    /**
     * @return the height of the bitmap representing the lifeform.
     */
    public abstract int getHeight();
    /**
     * @return the speed that the lifeform moves at (distance per tick - norminally every 0.1 seconds).
     */
    protected abstract float getSpeed();
    /**
     * @return the width of the bitmap representing the lifeform.
     */
    public abstract int getWidth();
    public float getX() { return x; }
    /**
     * @return the position of the left foot of the lifeform.
     */
    private float getXLeftFoot() { return x - ((getWidth() / (float) Block.size) * 0.4f); }
    /**
     * @return the position of the right foot of the lifeform.
     */
    private float getXRightFoot() {
        return x + ((getWidth() / (float) Block.size) * 0.4f);
    }
    public float getY() { return y; }

    /**
     * Causes the lifeform to jump.
     * @return true if the lifeform jumped as a result of calling jump(). False if it didn't jump (e.g. because it's falling or jumping already).
     */
    protected boolean jump() {
        if (ySpeed == 0) { // jump if we're not falling or already jumping
            ySpeed = 0.4f;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if the block beneath the feet of the lifeform is "solid", false otherwise.
     */
    private boolean onSolidGround() {
        int y = (int) Math.floor(this.y);
        if (y == this.y) { // if we're exactly on a block, examine the block below
            y -= 1;
        }
        int leftFootX = (int) Math.floor(getXLeftFoot());
        int rightFootX = (int) Math.floor(getXRightFoot());
        boolean leftFootOnSolidGround;
        try {
            leftFootOnSolidGround = world.getBlock(leftFootX, y).isSolid();
        } catch (NullPointerException e) {
            leftFootOnSolidGround = false; // air is not solid
        }
        try {
            if (leftFootOnSolidGround) {
                return true;
            } else if (leftFootX != rightFootX) { // the right foot might stop us falling
                return world.getBlock(rightFootX, y).isSolid();
            } else {
                return false;
            }
        } catch (NullPointerException e) { // catch the right foot being on air
            return false; // air is not solid
        }
    }

    protected void setHealth(float health) { this.health = health; }
    protected void setX(float x) { this.x = x; }
    protected void setY(float y) { this.y = y; }

    /**
     * Causes the lifeform to start moving in the direction specified.
     * @param newDirection the direction to move in.
     * @return true if the lifeform is now moving. False otherwise (e.g. because the lifeform is currently falling / jumping).
     */
    protected boolean startMoving(Direction newDirection) {
        if (this.onSolidGround()) {
            moving = true;
            direction = newDirection;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Stops the lifeform from moving - when it lands, if it's jumping / falling.
     */
    protected void stopMoving() {
        bitmapStage = 0;
        if (this.onSolidGround()) {
            moving = false;
        } else {
            stopWhenOnSolidGround = true;
        }
    }

    /**
     * Updates the state of the lifeform.
     * @param secondsElapsed the number of seconds (or part thereof) that have elapsed since the last update.
     */
    public void update(float secondsElapsed) {
        updateDamage(secondsElapsed);
        updateMovement(secondsElapsed);
        updateGravity(secondsElapsed);
    }

    /**
     * Causes the lifeform to take damage / updates the health.
     * @param secondsElapsed the number of seconds (or part thereof) that have elapsed since the last update.
     */
    private void updateDamage(float secondsElapsed) {
        boolean takenDamage = false;
        boolean touchingLava = false;
        int[][] coords = new int[][]{
                new int[]{(int) Math.floor(getXLeftFoot()),  (int) Math.floor(y)},
                new int[]{(int) Math.floor(getXRightFoot()), (int) Math.floor(y)},
                new int[]{(int) Math.floor(getXLeftFoot()),  (int) Math.floor(y + 1)},
                new int[]{(int) Math.floor(getXRightFoot()), (int) Math.floor(y + 1)}
        };
        for (int[] coord : coords) {
            if (world.getBlock(coord[0], coord[1]) instanceof Lava) {
                touchingLava = true;
                break;
            }
        }
        if (touchingLava) {
            health -= 200 * secondsElapsed;
            takenDamage = true;
        }
        // if we didn't take damage this tick, restore the equivalent of 2% health per second
        if (!takenDamage && health < 100) {
            health += 2 * secondsElapsed;
        } else if (takenDamage && health < 0) {
            health = 0;
        }
    }

    /**
     * Causes the lifeform to be affected by gravity / fall.
     * @param secondsElapsed the number of seconds (or part thereof) that have elapsed since the last update.
     */
    private void updateGravity(float secondsElapsed) {
        secondsSinceGravity += secondsElapsed;
        if (secondsSinceGravity > 1.0/30) {
            secondsSinceGravity = 0;
            y += ySpeed;
            if (this.onSolidGround()) {
                if (ySpeed < 0) { // if we've just landed
                    ySpeed = 0;
                    y = (float) Math.ceil(y);
                }
                if (stopWhenOnSolidGround) {
                    this.stopMoving();
                    stopWhenOnSolidGround = false;
                }
            } else {
                // if we've hit our head on something, set ySpeed to 0
                boolean hitHeadLeftBlock = false, hitHeadRightBlock = false;
                try {
                    hitHeadLeftBlock = world.getBlock((int) Math.floor(getXLeftFoot()), (int) Math.floor(y + (getHeight() / Block.size) + 0.1)).isSolid();
                } catch (NullPointerException e) {
                }
                try {
                    hitHeadRightBlock = world.getBlock((int) Math.floor(getXRightFoot()), (int) Math.floor(y + (getHeight() / Block.size) + 0.1)).isSolid();
                } catch (NullPointerException e) {
                }
                if (hitHeadLeftBlock || hitHeadRightBlock) {
                    ySpeed = 0;
                }
                if (ySpeed > -2) {
                    ySpeed -= 0.08;
                }
            }
        }
    }

    /**
     * Causes the lifeform to move left or right, if possible.
     * @param secondsElapsed the number of seconds (or part thereof) that have elapsed since the last update.
     */
    private void updateMovement(float secondsElapsed) {
        if (moving) {
            secondsSinceBitmapChange += secondsElapsed;
            if (secondsSinceBitmapChange > 0.1f) {
                secondsSinceBitmapChange = 0;
                if (!stopWhenOnSolidGround) {
                    bitmapStage++;
                }
                boolean nextStepObstructed = false;
                switch (direction) {
                    case LEFT:
                        int nextLeftFootX = (int) Math.floor(getXLeftFoot() - ((getWidth() / (float) Block.size) * 0.1f));
                        try {
                            nextStepObstructed = world.getBlock(nextLeftFootX, (int) Math.floor(y)).isSolid();
                        } catch (NullPointerException e) {
                        }
                        if (!nextStepObstructed) {
                            try {
                                nextStepObstructed = world.getBlock(nextLeftFootX, (int) Math.floor(y) + 1).isSolid();
                            } catch (NullPointerException e) {
                            }
                            if (!nextStepObstructed) {
                                x -= getSpeed();
                            }
                        }
                        break;
                    case RIGHT:
                        int nextRightFootX = (int) Math.floor(getXRightFoot() + ((getWidth() / (float) Block.size) * 0.1f));
                        try {
                            nextStepObstructed = world.getBlock(nextRightFootX, (int) Math.floor(y)).isSolid();
                        } catch (NullPointerException e) {
                        }
                        if (!nextStepObstructed) {
                            try {
                                nextStepObstructed = world.getBlock(nextRightFootX, (int) Math.floor(y) + 1).isSolid();
                            } catch (NullPointerException e) {
                            }
                            if (!nextStepObstructed) {
                                x += getSpeed();
                            }
                        }
                        break;
                }
            }
        }
    }
}
