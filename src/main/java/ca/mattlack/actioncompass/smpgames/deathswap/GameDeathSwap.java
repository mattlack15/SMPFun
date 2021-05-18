package ca.mattlack.actioncompass.smpgames.deathswap;

import ca.mattlack.actioncompass.ActionBarCompass;
import ca.mattlack.actioncompass.detection.util.EventSubscription;
import ca.mattlack.actioncompass.detection.util.EventSubscriptions;
import ca.mattlack.actioncompass.smpgames.SMPGame;
import ca.mattlack.actioncompass.smpgames.WorldProvider;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameDeathSwap extends SMPGame<Void> {
    private volatile boolean running = false;
    private volatile boolean worldCreated = false;
    private World world = null;
    private Map<UUID, Location> lastLocs = new HashMap<>();
    private long nextSwap = System.currentTimeMillis();
    private Map<UUID, ItemStack[]> inventoryContents = new HashMap<>();

    public void createWorld() {
        world = WorldProvider.loadWorldAsyncSafe(new WorldCreator(UUID.randomUUID().toString()).seed(new Random().nextLong()), ActionBarCompass.instance);
        worldCreated = true;
        assert world != null;
    }

    public void unloadWorld() {
        if (worldCreated) {

            //Clear world of players
            world.getPlayers().forEach(this::despawnPlayer);

            //Unload world
            Bukkit.unloadWorld(world, false);

            try {
                FileUtils.deleteDirectory(world.getWorldFolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        world = null;
        worldCreated = false;
    }

    @Override
    public void asyncPreStart() {
        createWorld();
    }

    @Override
    public Void createPlayerData(Player player) {
        return null;
    }

    private void teleportBack(Player p) {
        Location loc = new Location(Bukkit.getWorld("world"), 0,
                Objects.requireNonNull(Bukkit.getWorld("world")).getHighestBlockYAt(0, 0), 0);
        if (lastLocs.containsKey(p.getUniqueId())) {
            loc = lastLocs.get(p.getUniqueId());
        } else if (p.getBedSpawnLocation() != null) {
            loc = p.getBedSpawnLocation();
        }
        try {
            loc.getWorld();
        } catch (IllegalArgumentException e) {
            loc.setWorld(Bukkit.getWorld("world"));
        }
        if (inventoryContents.containsKey(p.getUniqueId())) {
            p.getInventory().setContents(inventoryContents.get(p.getUniqueId()));
            inventoryContents.remove(p.getUniqueId());
        }
        p.teleport(loc);
    }

    @Override
    public void stop() {
        EventSubscriptions.instance.unSubscribe(this);
        unloadWorld();
        running = false;
    }

    @Override
    public void start() {
        EventSubscriptions.instance.subscribe(this);
        if (!worldCreated)
            createWorld();

        world.setGameRule(GameRule.DO_INSOMNIA, false);

        this.getPlayers().forEach(this::spawnPlayer);
        running = true;

        incrementSwapTime();
    }

    public void spawnPlayer(Player player) {
        if (!worldCreated) {
            throw new IllegalStateException("World not created");
        }

        //Clear inventory
        inventoryContents.put(player.getUniqueId(), player.getInventory().getContents());
        player.getInventory().clear();

        //Reset health
        player.setHealth(player.getHealthScale());

        //Reset hunger
        player.setFoodLevel(20);
        player.setSaturation(20);

        //Set game mode
        player.setGameMode(GameMode.SURVIVAL);

        //Clear potion effects
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));

        //Teleport to world
        lastLocs.put(player.getUniqueId(), player.getLocation());
        player.teleport(getSpawnLocation());

    }

    @Override
    public void despawnPlayer(Player player) {
        teleportBack(player);
    }

    private Location getSpawnLocation() {
        Location spawn = world.getSpawnLocation();
        spawn.setY(world.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
        return spawn;
    }

    @Override
    public void tick() {
        if (!running)
            return;

        sendActionBars();

        if (nextSwap - System.currentTimeMillis() <= 0) {
            incrementSwapTime();
            swap();
        }
    }

    private void swap() {
        List<Player> players = getPlayers();

        Location first = null;

        Collections.shuffle(players);

        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            Player player = players.get(i);
            int prev = i == 0 ? players.size() - 1 : i - 1;
            Player prevPlayer = players.get(prev);
            if (first == null) {
                first = prevPlayer.getLocation();
            }
            if (i == players.size() - 1) {
                prevPlayer.teleport(first);
            } else {
                prevPlayer.teleport(player.getLocation());
            }

            prevPlayer.playSound(prevPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        }
    }

    private void sendActionBars() {
        StringBuilder msg = new StringBuilder();

        msg.append(ChatColor.GREEN);

        long nextSwapSeconds = (nextSwap - System.currentTimeMillis()) / 1000 + 1;
        int minutes = (int) (nextSwapSeconds / 60);
        int seconds = (int) (nextSwapSeconds % 60);
        if (minutes != 0) {
            msg.append(minutes).append("m ");
        }
        msg.append(seconds).append("s");

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(msg.toString()));

        world.getPlayers().forEach(player -> {
            if (player != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
            }
        });
    }

    private void incrementSwapTime() {
        nextSwap = System.currentTimeMillis() + 8 * 60 * 1000;
    }

    private List<UUID> getPlayerIds() {
        return getPlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());
    }

    @EventSubscription
    private void onLeave(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    @EventSubscription
    private void onDeath(EntityDamageEvent e) {
        if (!getPlayerIds().contains(e.getEntity().getUniqueId()))
            return;
        if (((Player) e.getEntity()).getHealth() - e.getFinalDamage() <= 0) {

            Bukkit.broadcastMessage(ChatColor.YELLOW + e.getEntity().getName() + ChatColor.WHITE + " fucking died.");

            e.setCancelled(true);
            Player p = (Player) e.getEntity();
            p.setHealth(p.getHealthScale());
            p.getInventory().clear();
            p.setGameMode(GameMode.SPECTATOR);
            p.sendTitle(ChatColor.RED + "You fucking died.", "noob");
            removePlayer(p);
            Location loc = p.getLocation();
            loc.setY(1);
            BlockState state = loc.getBlock().getState();
            loc.getBlock().setType(Material.END_GATEWAY);
            UUID id = world.getUID();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getWorld(id) != null)
                        state.update(true);
                }
            }.runTaskLater(ActionBarCompass.instance, 200);
        }

        if (getPlayers().size() == 1) {
            Player p = getPlayers().get(0);
            Bukkit.broadcastMessage(ChatColor.GREEN + p.getName() + ChatColor.WHITE + " won the game of death swap!");
            stop();
        }
    }

    @EventSubscription
    private void onPvp(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player || event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
            if (this.getPlayerIds().contains(event.getEntity().getUniqueId()))
                event.setCancelled(true);
        }
    }

    @EventSubscription
    private void onObby(BlockFormEvent event) {
        if (event.getNewState().getLocation().getWorld() == world && event.getNewState().getType() == Material.OBSIDIAN) {
            Location loc = event.getNewState().getLocation();
            BlockVector vector = new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            boolean b = false;
            for (Player player : getPlayers()) {
                BlockVector v = new BlockVector(player.getEyeLocation().getBlockX(), player.getEyeLocation().getBlockY(), player.getEyeLocation().getBlockZ());
                if (v.equals(vector)) {
                    b = true;
                    break;
                }
            }
            if (b) {
                event.getNewState().setType(Material.AIR);
            }
        }

    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}
