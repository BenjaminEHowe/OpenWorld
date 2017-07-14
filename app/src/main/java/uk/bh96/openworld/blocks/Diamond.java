package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "diamond". A rare ore only found towards the bottom of the world.
 */
public class Diamond extends Block {
    public int getBitmapNormal() { return R.drawable.diamond; }
    public int getDestroyScore() { return 75; }
    public int getDurability() {return 20; }
    public String getId() { return "Diamond"; }
    public boolean isSolid() { return true; }
}
