package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "lava". A liquid block which is extremely damaging to touch.
 */
public class Lava extends Block {
    public int getBitmapNormal() {
        return R.drawable.lava;
    }
    public int getDestroyScore() { return 0; }
    public int getDurability() {return Integer.MAX_VALUE; }
    public String getId() { return "Lava"; }
    public boolean isSolid() { return false; }
}
