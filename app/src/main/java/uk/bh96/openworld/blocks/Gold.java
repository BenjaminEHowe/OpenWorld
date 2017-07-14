package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "gold". A somewhat rare ore only found in the middle and bottom layers of the world.
 */
public class Gold extends Block {
    public int getBitmapNormal() { return R.drawable.gold; }
    public int getDestroyScore() { return 45; }
    public int getDurability() {return 10; }
    public String getId() { return "Gold"; }
    public boolean isSolid() { return true; }
}
