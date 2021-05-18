package ca.mattlack.actioncompass.detection.checks.xray;

import ca.mattlack.actioncompass.detection.Check;
import ca.mattlack.actioncompass.detection.util.CuboidRegion;
import ca.mattlack.actioncompass.detection.util.EventSubscription;
import ca.mattlack.actioncompass.detection.util.Vector3D;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Xray1 extends Check {

    long lastScan = System.currentTimeMillis() - 20 * 1000;

    int failed = 0;
    long lastFail = System.currentTimeMillis();

    int minedOre = 0;
    long l2 = System.currentTimeMillis();

    List<CuboidRegion> veins = new ArrayList<>();
    ReentrantLock veinsLock = new ReentrantLock();
    double[] speeds = new double[50];
    int speedsLength = 0;

    long timeSinceLastMove = System.currentTimeMillis();

    int timeSinceFirstHit = 0;

    public Xray1(UUID playerId) {
        super(playerId, "Xray-1");
    }

    @EventSubscription
    private void onLook(PlayerMoveEvent event) {
        if (!event.getPlayer().getUniqueId().equals(getPlayerId()))
            return;
        if (event.getTo() == null)
            return;
        if (event.getFrom().getYaw() == event.getTo().getYaw() && event.getFrom().getPitch() == event.getTo().getPitch())
            return;

        if(System.currentTimeMillis() - lastFail > 30 * 1000) {
            failed--;
            lastFail = System.currentTimeMillis();
        }

        if(System.currentTimeMillis() - lastScan > 30 * 1000) {
            return;
        }

        //Get the vein they are looking at
        CuboidRegion view = null;
        Vector3D lookDirection = Vector3D.fromBukkitVector(event.getPlayer().getEyeLocation().getDirection());
        Vector3D origin = Vector3D.fromBukkitVector(event.getPlayer().getEyeLocation().toVector());
        for (CuboidRegion vein : veins) {
            if (vein.intersects(origin, lookDirection) && vein.smallestDistance(origin) > 15) {
                view = vein;
                break;
            }
        }

        double approxSpeed = event.getTo().getDirection().distance(event.getFrom().getDirection()); //Not exact, but it'll do for our purposes
        approxSpeed /= ((System.currentTimeMillis() - timeSinceLastMove) / 50D) + 0.1D;
        int multiple = Math.min(10, Math.max(1, (int) ((System.currentTimeMillis() - timeSinceLastMove) / 50D)));
        timeSinceLastMove = System.currentTimeMillis();

        for (int i = 0; i < multiple; i++) {
            if (timeSinceFirstHit > 0) timeSinceFirstHit++; //If the clock is running, then tick it

            System.arraycopy(speeds, 0, speeds, 1, Math.min(speedsLength, speeds.length - 1)); //Shift array to the right by 1
            speedsLength = Math.min(speedsLength + 1, speeds.length); //Possibly increment speeds length
            speeds[0] = approxSpeed; //Set the new value

            //Looking at a vein
            if (view != null) {
                if (timeSinceFirstHit == 0) {
                    timeSinceFirstHit++; //Make it 1, (starts the clock)
                }
            }
        }

        //If limit has been hit, or they're gaze has left the vein
        boolean gazeLeftVein = timeSinceFirstHit > 8 && view == null;
        if (timeSinceFirstHit >= 20 || gazeLeftVein) {

            //Only check if they looked at it for an acceptable amount of time, otherwise just discard
            if(speedsLength > 8 && (speedsLength - timeSinceFirstHit >= 7)) {
                //Check if the speed of their gaze's movement slows towards the front (end) of the array
                double score;

                double avgOutside = 0;
                double avgInside = 0;

                for (int i = 0; i < timeSinceFirstHit; i++) {
                    avgInside += speeds[i];
                }
                avgInside /= timeSinceFirstHit;

                for (int i = timeSinceFirstHit; i < speedsLength; i++) {
                    avgOutside += speeds[i];
                }
                avgOutside /= speedsLength-timeSinceFirstHit;

                score = avgInside - avgOutside;

                if(score < -0.025) {
                    if(++failed >= 6 && minedOre > 10) {
                        recordViolation(1);
                        failed = 0;
                    }
                    lastFail = System.currentTimeMillis();
                }
            }

            //Reset
            timeSinceFirstHit = 0;
        }

    }


    @EventSubscription
    private void onMine(BlockBreakEvent event) {
        if (!event.getPlayer().getUniqueId().equals(this.getPlayerId()))
            return;
        if(event.getBlock().getType() == Material.DIAMOND_ORE) {
            minedOre++;
        }
        if(System.currentTimeMillis() - l2 > 5 * 60 * 1000) {
            l2 = System.currentTimeMillis();
            minedOre--;
        }
        if (event.getBlock().getType() == Material.STONE) {
            if (System.currentTimeMillis() - lastScan >= 30 * 1000) {
                lastScan = System.currentTimeMillis();

                List<ChunkSnapshot> snapshots = new ArrayList<>();

                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        snapshots.add(event.getPlayer().getLocation()
                                .add(new Vector(x * 16, 0, z * 16)).getChunk().getChunkSnapshot());
                    }
                }

                Runnable run = () -> {

                    //Create vein boxes
                    List<CuboidRegion> veinBoxes = new ArrayList<>();
                    for (ChunkSnapshot snapshot : snapshots) {
                        scanChunk(snapshot).forEach(v -> veinBoxes.add(new CuboidRegion(null,
                                new Vector3D(v.getX() - 2.5 + (snapshot.getX() * 16),
                                        v.getY() - 2.5, v.getZ() - 2.5 + (snapshot.getZ() * 16)),
                                new Vector3D(v.getX() + 2.5 + (snapshot.getX() * 16),
                                        v.getY() + 2.5, v.getZ() + 2.5 + (snapshot.getZ() * 16)))));
                    }

                    //Update veins
                    veinsLock.lock();
                    try {
                        veins.clear();
                        veins.addAll(veinBoxes);
                    } finally {
                        veinsLock.unlock();
                    }
                };

                ForkJoinPool.commonPool().submit(run);

            }
        }
    }

    public static List<Vector> scanChunk(ChunkSnapshot snapshot) {
        List<Vector> veins = new ArrayList<>();

        long ms = System.currentTimeMillis();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 60; y++) {
                for (int z = 0; z < 16; z++) {
                    //For every block in the region we are checking
                    CHECK:
                    if (snapshot.getBlockType(x, y, z) == Material.DIAMOND_ORE) {
                        //Check if it meets the criteria for a vein (check in order of computational difficulty)
                        if (x - 2 < 0 || x + 2 > 15 || y - 2 < 0 || z - 2 < 0 || z + 2 > 15) //Out of bounds
                            break CHECK;

                        Vector v = new Vector(x, y, z);

                        for (Vector vein : veins) {
                            if (vein.distanceSquared(v) <= 3 * 3) {
                                break CHECK;
                            }
                        }

                        //Check for solid blocks around vein
                        int solid = 0;
                        int volume = 5 * 5 * 5;
                        for (int x2 = x - 2; x2 <= x + 2; x2++) {
                            for (int y2 = y - 2; y2 <= y + 2; y2++) {
                                for (int z2 = z - 2; z2 <= z + 2; z2++) {
                                    if (snapshot.getBlockType(x2, y2, z2).isSolid())
                                        solid++;
                                }
                            }
                        }

                        if (solid == volume)
                            veins.add(new Vector(x, y, z));
                    }
                }
            }
        }

        ms = System.currentTimeMillis() - ms;
        return veins;
    }
}
