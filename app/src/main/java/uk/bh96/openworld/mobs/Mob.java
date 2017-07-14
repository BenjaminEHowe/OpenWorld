package uk.bh96.openworld.mobs;

import uk.bh96.openworld.Lifeform;
import uk.bh96.openworld.World;

/**
 * The base class for all mobile NPCs (see https://en.wikipedia.org/wiki/Mob_(gaming)#Origin_of_the_term).
 */
public abstract class Mob extends Lifeform {
    /**
     * Calls the parent constructor.
     * @param world the world in which this mob exists.
     */
    protected Mob(World world) { super(world); }
}
