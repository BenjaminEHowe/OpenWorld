package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "coal". A somewhat common ore scattered throughout the upper and middle layers of the world.
 */
public class Coal extends Block {
    public int getBitmapNormal() { return R.drawable.coal; }
    public int getDestroyScore() { return 15; }
    public int getDurability() {return 4; }
    public String getId() { return "Coal"; }
    public boolean isSolid() {
        return true;
    }
}
