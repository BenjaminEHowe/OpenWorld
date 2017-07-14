package uk.bh96.openworld.blocks;

import uk.bh96.openworld.R;

/**
 * Represents the block "bedrock". A virtually indestructible block existing at the sides and bottom of the world.
 */
public class Bedrock extends Block {
    public int getBitmapNormal() { return R.drawable.bedrock; }
    public int getDestroyScore() { return 0; }
    public int getDurability() {return Integer.MAX_VALUE; }
    public String getId() { return "Bedrock"; }
    public boolean isSolid() {
        return true;
    }
}
