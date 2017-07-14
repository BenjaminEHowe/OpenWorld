package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "dirt". Commonly found at the top of the world.
 */
public class Dirt extends Block {
    public int getBitmapNormal() { return R.drawable.dirt; }
    public int getDestroyScore() { return 1; }
    public int getDurability() {return 1; }
    public String getId() { return "Dirt"; }
    public boolean isSolid() {
        return true;
    }
}
