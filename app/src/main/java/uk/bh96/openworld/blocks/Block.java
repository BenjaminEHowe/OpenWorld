package uk.bh96.openworld.blocks;

import uk.bh96.openworld.Entity;
import uk.bh96.openworld.R;

/**
 * The base class for all blocks.
 */
public abstract class Block extends Entity {
    /**
     * How many stages this block has been destroyed. This is compared to getDurabiltiy() (below) to determine if the block has been completely destroyed.
     */
    private int destructionStage = 0;
    /**
     * A constant used to mark the height / width of blocks.
     */
    public static final int size = 56;

    /**
     * Destroy the block by once stage. Checks the durability of the block to determine if the block has been destroyed.
     * @return true if the block is completely destroyed, false if it's only destroyed by one stage.
     */
    public int destroy() {
        destructionStage++;
        if (destructionStage > getDurability()) {
            return getDestroyScore();
        } else {
            return 0;
        }
    }

    /**
     * @return the bitmap of the block normally (i.e. when it's not being destroyed).
     */
    abstract public int getBitmapNormal();

    /**
     * @return the amount by which to increment the score when this block is destroyed.
     */
    abstract public int getDestroyScore();

    /**
     * @return the number of destruction stages required before this block is completely destroyed.
     */
    abstract public int getDurability();

    /**
     * @return a string representing this block. Not unique - all blocks of the same type will return the same getId().
     */
    abstract public String getId();

    /**
     * @return an apppropriate bitmap, depending on if the block is being destroyed or not.
     */
    public int getBitmap() {
        if (destructionStage > 0) {
            return R.drawable.destroy;
        } else {
            return getBitmapNormal();
        }
    }

    /**
     * @return if the block is solid (if the player stands on it, should they pass through?).
     */
    abstract public boolean isSolid();

    /**
     * Resets the block if it ceases to be under destruction.
     */
    public void reset() {
        destructionStage = 0;
    }
}
