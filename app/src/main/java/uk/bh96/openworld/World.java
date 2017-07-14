package uk.bh96.openworld;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import uk.bh96.openworld.blocks.*;
import uk.bh96.openworld.mobs.*;

/**
 * Created by Ben on 04/04/2017.
 */

public class World {
    /**
     * Holds the block currently being destroyed.
     */
    private int[] blockBeingDestroyed = new int[]{-1, -1};
    /**
     * Holds the block(s) (usually only one block) which were previously being destroyed during this tick.
     */
    private ArrayList<int[]> blocksToReset = new ArrayList<>();
    /**
     * Holds the mobs in this world.
     */
    private ArrayList<Mob> mobs = new ArrayList<>();
    /**
     * Holds the player in this world.
     */
    private Player player;
    /**
     * References the random number generator (with state) - this means two worlds started with the same seed will be identical (and will continue to be so until the inputs between the worlds differ somehow.
     */
    private Random rand;
    /**
     * Records the change in score during this tick.
     */
    private short scoreChange = 0;
    /**
     * The number of seconds (or part thereof) since the world was updated.
     */
    private float secondsSinceUpdate = 0;
    /**
     * The width of the screen as measured in blocks (one block at 160dpi is 56px).
     */
    private float screenWidthInBlocks;
    /**
     * The height of the screen as measured in blocks (one block at 160dpi is 56px).
     */
    private float screenHeightInBlocks;
    /**
     * This chunk of the world.
     */
    private Block[][] thisChunk = new Block[1024][256];

    /**
     * @param seed the seed with which to initialise the random number generator.
     * @param screenWidthInBlocks the width of the screen in blocks.
     * @param screenHeightInBlocks the height of the screen in blocks.
     */
    public World(long seed, float screenWidthInBlocks, float screenHeightInBlocks) {
        this.screenWidthInBlocks = screenWidthInBlocks;
        this.screenHeightInBlocks = screenHeightInBlocks;

        rand = new Random(seed);

        // generate the normal blocks
        for (int x = 0; x < thisChunk.length; x++) { // for each column in the chunk
            for (int y = 0; y < thisChunk[x].length; y++) { // for each row in that column
                if (y < 113) {
                    thisChunk[x][y] = new Stone();
                } else if (y < 119) {
                    if (rand.nextDouble() < 0.75) {
                        thisChunk[x][y] = new Stone();
                    } else {
                        thisChunk[x][y] = new Dirt();
                    }
                } else if (y < 123) {
                    if (rand.nextDouble() < 0.5) {
                        thisChunk[x][y] = new Stone();
                    } else {
                        thisChunk[x][y] = new Dirt();
                    }
                } else if (y < 125) {
                    if (rand.nextDouble() < 0.25) {
                        thisChunk[x][y] = new Stone();
                    } else {
                        thisChunk[x][y] = new Dirt();
                    }
                } else if (y < 127) {
                    thisChunk[x][y] = new Dirt();
                } else if (y == 127) {
                    thisChunk[x][y] = new Grass();
                }
            }
        }

        // generate ore etc.
        for (int x = 1; x < thisChunk.length - 5; x++) { // for each column in the chunk
            for (int y = 5; y < thisChunk[x].length - 1; y++) { // for each row in that column
                if (y <= 20) {
                    if (rand.nextDouble() < 0.01) { // 1% chance of starting a diamond cluster
                        generateCluster(rand, x, y, Blocks.DIAMOND, 3, 5);
                    } else if (rand.nextDouble() < 0.015) { // 1.5% chance of starting a gold cluster
                        generateCluster(rand, x, y, Blocks.GOLD, 4, 8);
                    }
                } else if (y <= 32) {
                    if (rand.nextDouble() < 0.005) { // 0.5% chance of starting a diamond cluster
                        generateCluster(rand, x, y, Blocks.DIAMOND, 3, 5);
                    } else if (rand.nextDouble() < 0.015) { // 1.5% chance of starting a gold cluster
                        generateCluster(rand, x, y, Blocks.GOLD, 3, 6);
                    }
                } else if (y <= 45) {
                    if (rand.nextDouble() < 0.01) { // 1% chance of starting a gold cluster
                        generateCluster(rand, x, y, Blocks.GOLD, 3, 6);
                    } else if (rand.nextDouble() < 0.01) { // 1% chance of starting a iron cluster
                        generateCluster(rand, x, y, Blocks.IRON, 5, 12);
                    }
                } else if (y <= 64) {
                    if (rand.nextDouble() < 0.005) { // 0.5% chance of starting a gold cluster
                        generateCluster(rand, x, y, Blocks.GOLD, 3, 6);
                    } else if (rand.nextDouble() < 0.01) { // 1% chance of starting a iron cluster
                        generateCluster(rand, x, y, Blocks.IRON, 5, 12);
                    } else if (rand.nextDouble() < 0.005) { // 0.5% chance of starting a coal vein
                        generateVein(rand, x, y, Blocks.COAL);
                    }
                } else if (y <= 84) {
                    if (rand.nextDouble() < 0.01) { // 1% chance of starting a iron cluster
                        generateCluster(rand, x, y, Blocks.IRON, 5, 12);
                    } else if (rand.nextDouble() < 0.005) { // 0.5% chance of starting a coal vein
                        generateVein(rand, x, y, Blocks.COAL);
                    }
                } else if (y <= 96) {
                    if (rand.nextDouble() < 0.005) { // 0.5% chance of starting a iron cluster
                        generateCluster(rand, x, y, Blocks.IRON, 5, 12);
                    } else if (rand.nextDouble() < 0.0075) { // 0.75% chance of starting a coal vein
                        generateVein(rand, x, y, Blocks.COAL);
                    }
                } else if (y <= 112) {
                    if (rand.nextDouble() < 0.005) { // 0.5% chance of starting a coal vein
                        generateVein(rand, x, y, Blocks.COAL);
                    }
                }
            }
        }

        // add bedrock and lava to the edges
        for (int x = 0; x < thisChunk.length; x++) { // for each column in the chunk
            for (int y = 0; y < thisChunk[x].length; y++) { // for each row in that column
                if (x == 0 || x == 1023 || y == 0) {
                    thisChunk[x][y] = new Bedrock();
                } else if (y == 1) {
                    thisChunk[x][y] = new Lava();
                }
            }
        }
    }

    /**
     * @param player the player to be added to the world.
     */
    public void addPlayer(Player player) { this.player = player; }

    /**
     * (attempts to) attack a mob at given coordinates.
     * @param x the x coordinate to attack.
     * @param y the y coordinate to attack.
     */
    public void attackMob(float x, float y) {
        Log.d("AttackMob", "Attempting to attack mob at (" + x + ", " + y + ")");
        for (Mob mob : mobs) {
            boolean inXRange = Math.abs(mob.getX() - x) < (mob.getWidth() * 1.2) / (2 * Block.size);
            boolean inYRange = Math.abs(mob.getY() - y) < (mob.getHeight() * 1.2) / (2 * Block.size);
            if (inXRange && inYRange) {
                mob.damage(15);
                Log.d("MobAttacked", "Mob damaged! New health: " + mob.getHealth());
            }
        }
    }

    /**
     * Destroy a block at given coordinates.
     * @param x the x coordinate of the block to destroy.
     * @param y the y coordinate of the block to destroy.
     */
    public void destroyBlock(int x, int y) {
        if (blockBeingDestroyed[0] > -1 && blockBeingDestroyed[1] > -1) { // if there's a block selected for destruction
            blocksToReset.add(blockBeingDestroyed);
        }
        blockBeingDestroyed = new int[]{x, y};
    }

    /**
     * Generate a block.
     * @param type the type of block to generate.
     * @return a new block of the specified type.
     */
    private Block generateBlock(Blocks type) {
        switch (type) {
            case BEDROCK:
                return new Bedrock();
            case COAL:
                return new Coal();
            case DIAMOND:
                return new Diamond();
            case GOLD:
                return new Gold();
            case GRASS:
                return new Grass();
            case DIRT:
                return new Dirt();
            case IRON:
                return new Iron();
            case LAVA:
                return new Lava();
            case STONE:
                return new Stone();
        }
        // we really should have returned by now...
        return null;
    }

    /**
     * Generates a cluster of blocks, at the given coordinates.
     * @param rand the random number generator to use.
     * @param x the x coordinate of the first block of the custer.
     * @param y the y coordinate of the first block of the cluster.
     * @param type the type of blocks in the cluster.
     * @param min the minimum (minimum 3) number of blocks to be in the cluster.
     * @param max the maximum (maximum 12) number of blocks to be in the cluster.
     */
    private void generateCluster(Random rand, int x, int y, Blocks type, int min, int max) {
        if (min < 3) {
            min = 3;
        }
        if (max > 12) {
            max = 12;
        }
        int quantity = min + (int) Math.floor((max - min + 1) * rand.nextDouble());
        int emptySpots = 8;
        // generate blocks 1-3 in the cluster
        thisChunk[x][y] = generateBlock(type);
        thisChunk[x + 1][y] = generateBlock(type);
        thisChunk[x][y - 1] = generateBlock(type);
        // generate block 4 if required
        if (quantity >= 4) {
            thisChunk[x + 1][y - 1] = generateBlock(type);
        }
        // generate blocks 5 - 12 if required
        ArrayList<int[]> spots = new ArrayList<>(Arrays.asList(
                new int[]{0, 1},   // top left
                new int[]{1, 1},   // top right
                new int[]{2, 0},   // right top
                new int[]{2, -1},  // right bottom
                new int[]{1, -2},  // bottom right
                new int[]{0, -2},  // bottom left
                new int[]{-1, -1}, // left bottom
                new int[]{-1, 0}   // left top
        ));
        for (int i = 5; i <= quantity; i++) {
            int spot = rand.nextInt(emptySpots);
            int[] spotTranslation = spots.get(spot);
            thisChunk[x + spotTranslation[0]][y + spotTranslation[1]] = generateBlock(type);
            spots.remove(spot);
            emptySpots--;
        }
    }

    /**
     * Generates a vein of blocks (similar to a cluster - but in a line).
     * @param rand the random number generator to use.
     * @param x the x coordinate of the first block of the vein.
     * @param y the y coordinate of the first block of the vein.
     * @param type the type of blocks in the vein.
     */
    private void generateVein(Random rand, int x, int y, Blocks type) {
        boolean isHorizontal = rand.nextBoolean();
        int quantity = 5 + (int) Math.floor(10 * rand.nextDouble()); // between 5 and 14 blocks per vein
        boolean flip = false;
        if (quantity > 8) {
            flip = rand.nextBoolean();
        }
        if (isHorizontal) {
            switch (quantity) {
                case 14:
                    thisChunk[x + 5][y - 1] = generateBlock(type);
                case 13:
                    thisChunk[x + 5][y] = generateBlock(type);
                case 12:
                    thisChunk[x + 4][y] = generateBlock(type);
                case 11:
                    thisChunk[x + 4][y - 1] = generateBlock(type);
                case 10:
                    if (flip) {
                        thisChunk[x + 2][y + 1] = generateBlock(type);
                    } else {
                        thisChunk[x + 2][y - 2] = generateBlock(type);
                    }
                case 9:
                    if (flip) {
                        thisChunk[x + 1][y + 1] = generateBlock(type);
                    } else {
                        thisChunk[x + 1][y - 2] = generateBlock(type);
                    }
                case 8:
                    thisChunk[x + 3][y - 1] = generateBlock(type);
                case 7:
                    thisChunk[x + 3][y] = generateBlock(type);
                case 6:
                    thisChunk[x + 2][y - 1] = generateBlock(type);
                case 5:
                    thisChunk[x + 2][y] = generateBlock(type);
                    thisChunk[x + 1][y - 1] = generateBlock(type);
                    thisChunk[x + 1][y] = generateBlock(type);
                    thisChunk[x][y - 1] = generateBlock(type);
                    thisChunk[x][y] = generateBlock(type);
            }
        } else {
            switch (quantity) {
                case 14:
                    thisChunk[x + 1][y - 5] = generateBlock(type);
                case 13:
                    thisChunk[x][y - 5] = generateBlock(type);
                case 12:
                    thisChunk[x][y - 4] = generateBlock(type);
                case 11:
                    thisChunk[x + 1][y - 4] = generateBlock(type);
                case 10:
                    if (flip) {
                        thisChunk[x - 1][y - 2] = generateBlock(type);
                    } else {
                        thisChunk[x + 2][y - 2] = generateBlock(type);
                    }
                case 9:
                    if (flip) {
                        thisChunk[x - 1][y - 2] = generateBlock(type);
                    } else {
                        thisChunk[x + 2][y - 2] = generateBlock(type);
                    }
                case 8:
                    thisChunk[x + 1][y - 3] = generateBlock(type);
                case 7:
                    thisChunk[x][y - 3] = generateBlock(type);
                case 6:
                    thisChunk[x + 1][y - 2] = generateBlock(type);
                case 5:
                    thisChunk[x][y - 2] = generateBlock(type);
                    thisChunk[x + 1][y - 1] = generateBlock(type);
                    thisChunk[x][y - 1] = generateBlock(type);
                    thisChunk[x + 1][y] = generateBlock(type);
                    thisChunk[x][y] = generateBlock(type);
            }
        }
    }

    public Block getBlock(int x, int y) { return thisChunk[x][y]; }
    public ArrayList<Mob> getMobs() { return mobs; }
    public long getScoreChange() { return scoreChange; }

    /**
     * Updates the state of the world.
     * @param secondsElapsed the number of seconds (or part thereof) that have elapsed since the last update.
     */
    public void update(float secondsElapsed) {
        // destroy blocks etc. & spawn mobs
        scoreChange = 0;
        secondsSinceUpdate += secondsElapsed;
        if (secondsSinceUpdate > 0.1f) {
            secondsSinceUpdate = 0;
            for (int[] blockToReset : blocksToReset) {
                if (blockToReset != blockBeingDestroyed) {
                    try {
                        thisChunk[blockToReset[0]][blockToReset[1]].reset();
                    } catch (NullPointerException e) {
                    }
                }
            }
            blocksToReset.clear(); // clear the array of blocks requiring reset
            if (blockBeingDestroyed[0] > -1 && blockBeingDestroyed[1] > -1) { // if there's a block selected for destruction
                int destroyPoints;
                try {
                    destroyPoints = thisChunk[blockBeingDestroyed[0]][blockBeingDestroyed[1]].destroy();
                } catch (NullPointerException e) {
                    destroyPoints = 0;
                }
                if (destroyPoints > 0) {
                    // if the block is successfully destroyed, remove the object
                    thisChunk[blockBeingDestroyed[0]][blockBeingDestroyed[1]] = null;
                    scoreChange += destroyPoints;
                }
            }

            // (potentially) spawn mob
            if (rand.nextInt(200) == 0) { // spawn one mob every 20 seconds (on average!)
                Log.d("SpawnMob", "Attempting to spawn mob...");
                // find an appropriate* space to spawn immidiately off the edge of the screen
                // * appropriate - a null block with (at least) one null block above and a solid block below
                ArrayList<int[]> potentialSpawnLocations = new ArrayList<>();
                int[] leftRightEdges = new int[] {
                        (int) Math.floor(player.getX() - (screenWidthInBlocks / 2f)), // left edge
                        (int) Math.floor(player.getX() + (screenWidthInBlocks / 2f))  // right edge
                };
                int bottomEdgeY = (int) Math.floor(player.getY() - (screenHeightInBlocks / 2f));
                int topEdgeY = (int) Math.floor(player.getY() + (screenHeightInBlocks / 2f));
                for (int y = bottomEdgeY; y <= topEdgeY; y++) {
                    for (int x : leftRightEdges) {
                        try {
                            if (thisChunk[x][y] == null && thisChunk[x][y + 1] == null && thisChunk[x][y - 1].isSolid()) {
                                potentialSpawnLocations.add(new int[]{x, y});
                            } else {
                                Log.d("MobSpawn", "Location (" + x + ", " + y + ") is unsuitable!");
                            }
                        } catch (NullPointerException|ArrayIndexOutOfBoundsException e) {
                            // if the block below this block is air (or out of range), it's not solid so this can't be a spawn location
                        }
                    }
                }
                if (potentialSpawnLocations.size() != 0) { // if there's a space to spawn in
                    int[] coords = potentialSpawnLocations.get(rand.nextInt(potentialSpawnLocations.size()));
                    mobs.add(new Zombie(this, player, coords[0], coords[1]));
                    //mobs.add(new Zombie(this, player, player.getX(), player.getY()));
                    Log.d("SpawnMob", "New mob spawned at (" + coords[0] + ", " + coords[1] + ")");
                } else {
                    Log.d("SpawnMob", "Spawning mob failed - no appropriate spaces!");
                }
            }
        }

        // for each mob
        for (int i = 0; i < mobs.size(); i++) {
            // remove the mob if it's a long way off-screen
            boolean mobTooFarX = Math.abs(mobs.get(i).getX() - player.getX()) > 2 * screenWidthInBlocks;
            boolean mobTooFarY = Math.abs(mobs.get(i).getY() - player.getY()) > 2 * screenHeightInBlocks;
            if (mobTooFarX || mobTooFarY) {
                Log.d("MobDespawned", "Mob at (" + mobs.get(i).getX() + ", " + mobs.get(i).getY() + ") despawned.");
                mobs.remove(i);
                // position i now contains a new (unupdated) mob, so decrement i so it doesn't get skipped
                i--;
                continue;
            }
            // update mob
            mobs.get(i).update(secondsElapsed);
            // remove the mob if it's now dead
            if (mobs.get(i).getHealth() <= 0) {
                mobs.remove(i);
                i--;
            }
        }
    }
}
