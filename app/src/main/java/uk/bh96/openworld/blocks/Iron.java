package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "iron". A somewhat uncommon ore scattered throughout the middle layers of the world.
 */
public class Iron extends Block {
    public int getBitmapNormal() { return R.drawable.iron; }
    public int getDestroyScore() { return 25; }
    public int getDurability() {return 6; }
    public String getId() { return "Iron"; }
    public boolean isSolid() { return true; }
}
