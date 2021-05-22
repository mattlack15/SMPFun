package ca.mattlack.actioncompass.smpgames.randomdrops;

import ca.mattlack.actioncompass.ActionBarCompass;
import ca.mattlack.actioncompass.smpgames.SMPGame;
import ca.mattlack.actioncompass.smpgames.WorldProvider;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GameRandomDrops extends SMPGame<Void> {

    private World world = null;
    private World worldEnd = null;

    private volatile boolean running = false;

    //States
    private Map<UUID, Location> locationMap =  new HashMap<>();
    private Map<UUID, ItemStack[]> inventoryMap = new HashMap<>();

    private void createWorld() {
        Random random = new Random();

        world = WorldProvider.loadWorldAsyncSafe(new WorldCreator(UUID.randomUUID().toString())
                .seed(random.nextLong()), ActionBarCompass.instance);

        worldEnd = WorldProvider.loadWorldAsyncSafe(new WorldCreator(UUID.randomUUID().toString()).environment(World.Environment.NETHER)
                .seed(random.nextLong()), ActionBarCompass.instance);

        world.setGameRule(GameRule.DO_INSOMNIA, false);
    }

    private void deleteWorld() {

        try {

            //Despawn players in the world
            world.getPlayers().forEach(this::despawnPlayer);
            worldEnd.getPlayers().forEach(this::despawnPlayer);

            Bukkit.unloadWorld(world, false);
            Bukkit.unloadWorld(worldEnd, false);

            //Delete world directory
            FileUtils.deleteDirectory(world.getWorldFolder());
            FileUtils.deleteDirectory(worldEnd.getWorldFolder());
            world = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

        if (running)
            return;

        getPlayers().forEach(this::removePlayer);
        deleteWorld();
    }

    @Override
    public void start() {

        for (Player player : getPlayers()) {
            player.getInventory().clear();
        }

        running = true;
    }

    @Override
    public Void createPlayerData(Player player) {
        return null;
    }

    @Override
    public void spawnPlayer(Player player) {

    }

    @Override
    public void despawnPlayer(Player player) {

    }

    @Override
    public void tick() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
