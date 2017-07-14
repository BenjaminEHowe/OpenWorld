package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "stone". Commonly found throughout the world.
 */
public class Stone extends Block {
    public int getBitmapNormal() { return R.drawable.stone; }
    public int getDestroyScore() { return 2; }
    public int getDurability() {return 2; }
    public String getId() { return "Stone"; }
    public boolean isSolid() {
        return true;
    }
}
