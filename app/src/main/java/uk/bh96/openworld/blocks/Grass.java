package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "grass". Found covering the top layer of the world.
 */
public class Grass extends Block {
    public int getBitmapNormal() { return R.drawable.grass; }
    public int getDestroyScore() { return 1; }
    public int getDurability() {return 1; }
    public String getId() { return "Grass"; }
    public boolean isSolid() {
        return true;
    }
}
