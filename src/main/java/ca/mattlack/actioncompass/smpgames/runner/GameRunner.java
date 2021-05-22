package ca.mattlack.actioncompass.smpgames.runner;

import ca.mattlack.actioncompass.ActionBarCompass;
import ca.mattlack.actioncompass.detection.util.EventSubscription;
import ca.mattlack.actioncompass.detection.util.EventSubscriptions;
import ca.mattlack.actioncompass.smpgames.SMPGame;
import ca.mattlack.actioncompass.smpgames.WorldProvider;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.stream.Collectors;

public class GameRunner extends SMPGame<PlayerDataRunner> {

    private boolean running;
    private boolean worldCreated;
    private Map<UUID, ItemStack[]> inventories = new HashMap<>();
    private Map<UUID, Location> lastLocs = new HashMap<>();
    private World world;
    private World netherWorld;
    private int frozenTicks = 0;
    private long endTime = 0;
    private long ticks = 0;
    private int graceLengthTicks = 1 * 60 * 20;

    private Map<UUID, ItemStack[]> loggedOut = new HashMap<>();

    @Override
    public void tick() {
        if (!isRunning())
            return;

        if (frozenTicks > 0) {
            if (frozenTicks == graceLengthTicks - 200) {
                if (getRunner() != null) {
                    unfreeze(getRunner());
                }
            }
            if (--frozenTicks == 0) {
                getPlayers().forEach(this::unfreeze);
            }
            if (frozenTicks % 20 == 0) {
                getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1f, 1f));
            }
        }

        if (ticks++ % 5 == 0 && getRunner() != null) {
            getPlayers().forEach(p -> p.setCompassTarget(getRunner().getLocation()));
        }

        getPlayers().forEach(this::sendActionBars);

        if (System.currentTimeMillis() >= endTime) {
            stop();
            Bukkit.broadcastMessage("Runner wins. They survived 30 minutes of y'all.");
        }
    }

    private Player getRunner() {
        for (Player player : getPlayers()) {
            if (getPlayerData(player.getUniqueId()).isRunner())
                return player;
        }
        return null;
    }

    public void createWorld() {
        Random random = new Random();
        world = WorldProvider.loadWorldAsyncSafe(new WorldCreator(UUID.randomUUID().toString()).seed(random.nextLong()), ActionBarCompass.instance);
        netherWorld = WorldProvider.loadWorldAsyncSafe(new WorldCreator(world.getName() + "_nether").seed(random.nextLong()).environment(World.Environment.NETHER), ActionBarCompass.instance);
        worldCreated = true;
        assert world != null;
        assert netherWorld != null;
    }

    public void deleteWorld() {
        if (worldCreated) {

            //Clear world of players
            world.getPlayers().forEach(this::despawnPlayer);

            netherWorld.getPlayers().forEach(this::despawnPlayer);

            //Unload world
            Bukkit.unloadWorld(world, false);
            Bukkit.unloadWorld(netherWorld, false);

            try {
                FileUtils.deleteDirectory(netherWorld.getWorldFolder());
                FileUtils.deleteDirectory(world.getWorldFolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        world = null;
        netherWorld = null;
        worldCreated = false;
    }

    @Override
    public void asyncPreStart() {
        createWorld();
        System.out.println("Created: " + (world != null & netherWorld != null));
    }

    @Override
    public void stop() {
        EventSubscriptions.instance.unSubscribe(this);

        getPlayers().forEach(this::removePlayer);

        deleteWorld();

        running = false;
    }

    @Override
    public void start() {

        ticks = 0;

        if (!worldCreated)
            throw new IllegalStateException("World hasn't been created yet, call asyncPreStart()");

        //Pick a runner
        Player runner = pickRandom(this.getPlayers());
        assert runner != null;
        getPlayerData(runner.getUniqueId()).setRunner(true);

        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.getWorldBorder().setSize(1000);

        netherWorld.getWorldBorder().setSize(1000);

        this.getPlayers().forEach(this::spawnPlayer);

        runner.sendTitle(ChatColor.GREEN + "You are the runner", "you should probably start running?");
        getPlayers().forEach(p -> p.sendMessage(runner.getName() + ChatColor.YELLOW + " is the runner."));

        endTime = System.currentTimeMillis() + (30 * 60 * 1000); //30 minutes

        frozenTicks = graceLengthTicks;

        getPlayers().forEach(this::freeze);

        EventSubscriptions.instance.subscribe(this);
        running = true;
    }

    private void freeze(Player p) {
        //No movement
        p.setWalkSpeed(0);

        //No jumping
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false, false));
    }

    private void unfreeze(Player p) {
        p.setWalkSpeed(0.2f);
        p.removePotionEffect(PotionEffectType.JUMP);
    }

    @Override
    public PlayerDataRunner createPlayerData(Player player) {
        return new PlayerDataRunner();
    }

    @Override
    public void spawnPlayer(Player player) {
        if (!worldCreated) {
            throw new IllegalStateException("World not created");
        }

        //Clear inventory
        inventories.put(player.getUniqueId(), player.getInventory().getContents());
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

        player.sendMessage(ChatColor.GRAY + "This is a game of runner, the runner has to stay alive for 30 minutes, your goal is to kill them.");
        player.sendMessage("");

        if (getPlayerData(player.getUniqueId()).isRunner()) {
            equipRunner(player);
        } else {
            equipHunter(player);
        }
    }

    private Player pickRandom(List<Player> possiblePlayers) {
        Random random = new Random(System.currentTimeMillis());
        if (possiblePlayers.size() == 0)
            return null;
        return possiblePlayers.get(random.nextInt(possiblePlayers.size()));
    }

    private Location getSpawnLocation() {
        Location spawn = world.getSpawnLocation();
        spawn.setY(world.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
        Location loc = spawn.clone();
        loc.add(0, -1, 0);
        loc.getBlock().setType(Material.BEDROCK);
        return spawn;
    }

    @Override
    public void despawnPlayer(Player player) {
        teleportBack(player);
        unfreeze(player);
        getPlayers().forEach(p -> {
            setGlowing(p, player, false);
            setGlowing(player, p, false);
        });
        setGlowing(player, player, false);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void equipRunner(Player p) {
        List<ItemStack> items = new ArrayList<>();

        items.add(new ItemStack(Material.DIAMOND_SWORD));
        items.add(new ItemStack(Material.DIAMOND_PICKAXE));
        items.add(new ItemStack(Material.DIAMOND_AXE));
        items.add(new ItemStack(Material.DIAMOND_SHOVEL));

        items.forEach(i -> {
            if (!i.getType().name().contains("SWORD")) {
                i.addEnchantment(Enchantment.DIG_SPEED, 1);
            } else {
                i.addEnchantment(Enchantment.DAMAGE_ALL, 1);
            }
            p.getInventory().addItem(i);
        });

        p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 24));
    }

    private void equipHunter(Player p) {
        List<ItemStack> items = new ArrayList<>();

        items.add(new ItemStack(Material.IRON_SWORD));
        items.add(new ItemStack(Material.IRON_PICKAXE));
        items.add(new ItemStack(Material.IRON_AXE));
        items.add(new ItemStack(Material.IRON_SHOVEL));

        items.forEach(i -> {
            if (!i.getType().name().contains("SWORD")) {
                i.addEnchantment(Enchantment.DIG_SPEED, 1);
            } else {
                i.addEnchantment(Enchantment.DAMAGE_ALL, 1);
            }
            p.getInventory().addItem(i);
        });

        p.getInventory().addItem(new ItemStack(Material.COMPASS));
        p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 24));

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
        if (inventories.containsKey(p.getUniqueId())) {
            p.getInventory().setContents(inventories.get(p.getUniqueId()));
            inventories.remove(p.getUniqueId());
        }
        p.teleport(loc);
    }

    private void sendActionBars(Player player) {
        StringBuilder builder = new StringBuilder();
        int timeSeconds = 0;
        if (frozenTicks > 0) {
            timeSeconds = frozenTicks / 20 + 1;
        } else {
            timeSeconds = (int) ((endTime - System.currentTimeMillis()) / 1000 + 1);
        }

        int seconds = timeSeconds % 60;
        int minutes = timeSeconds / 60;
        builder.append(ChatColor.GREEN);
        if (minutes != 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s");

        getPlayers().forEach(p -> p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new TextComponent(TextComponent.fromLegacyText(builder.toString()))));


        if (ticks % 2 == 0) {
            List<Player> players = getPlayers().stream().filter(p -> !getPlayerData(p.getUniqueId()).isRunner()).collect(Collectors.toList());
            for (Player player1 : players) {
                for (Player player2 : players) {
                    setGlowing(player2, player1, true);
                }
            }
        }
    }

    @EventSubscription
    private void onLeave(PlayerQuitEvent event) {
        loggedOut.put(event.getPlayer().getUniqueId(), event.getPlayer().getInventory().getContents());
        removePlayer(event.getPlayer());
    }

    @EventSubscription
    private void onJoin(PlayerJoinEvent event) {
        if(loggedOut.containsKey(event.getPlayer().getUniqueId())) {
            ItemStack[] contents = loggedOut.remove(event.getPlayer().getUniqueId());
            addPlayer(event.getPlayer());
            event.getPlayer().getInventory().setContents(contents);
        }
    }

    @EventSubscription
    private void onMove(PlayerMoveEvent event) {
        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            if (getPlayers().contains(event.getPlayer())) {
                if (frozenTicks > graceLengthTicks - 200 || frozenTicks > 0 && !getPlayerData(event.getPlayer().getUniqueId()).isRunner())
                    event.setCancelled(true);
            }
        }
    }

    @EventSubscription
    private void onDeath(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || !getPlayers().contains((Player) event.getEntity()))
            return;
        if (frozenTicks > graceLengthTicks - 200 || frozenTicks > 0 && !getPlayerData(event.getEntity().getUniqueId()).isRunner()) {
            event.setCancelled(true);
            return;
        }
        if (((Player) event.getEntity()).getHealth() - event.getFinalDamage() > 0)
            return;

        event.setCancelled(true);

        Player player = (Player) event.getEntity();
        player.setHealth(player.getHealthScale());

        //Death

        //Broadcasted message
        Bukkit.broadcastMessage("This idiot died: " + ChatColor.YELLOW + player.getName());

        PlayerDataRunner playerData = getPlayerData(player.getUniqueId());

        if (getPlayerData(player.getUniqueId()).decrementAndGetLives() <= 0) {

            //Broadcasted message
            Bukkit.broadcastMessage(ChatColor.RED + "It was their last death, NO GHOSTING.");

            //Remove from game
            removePlayer(player, false);

            //Change to spectator
            player.getInventory().clear();
            player.setGameMode(GameMode.SPECTATOR);

            //Message
            if (!playerData.isRunner())
                player.sendTitle(ChatColor.RED + "You somehow fucked it up", "You're out, no more lives. DON'T GHOST!");
        } else {
            //Message
            player.sendTitle(getPlayerData(player.getUniqueId()).getLives() + "" + ChatColor.RED + " lives left", "don't fuck it up");

            player.teleport(getSpawnLocation());
        }

        if (playerData.isRunner()) {
            //Game end, runner died
            stop();
            Bukkit.broadcastMessage("Hunters win!");
        } else {
            if (getPlayers().size() == 1) {
                stop();
                Bukkit.broadcastMessage("Runner wins, everyone died.");
            }
        }
    }

    @EventSubscription
    private void onBreak(BlockBreakEvent event) {
        if (!getPlayers().contains((Player) event.getPlayer()))
            return;
        if (frozenTicks > graceLengthTicks - 260 || frozenTicks > 0 && !getPlayerData(event.getPlayer().getUniqueId()).isRunner()) {
            event.setCancelled(true);
        }
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        if (!getPlayers().contains(event.getPlayer()) && isRunning())
            return;
        event.setCancelled(true);

        boolean runner = getPlayerData(event.getPlayer().getUniqueId()).isRunner();

        if (runner) {
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.RED + "" + ChatColor.BOLD + "RUNNER " + ChatColor.WHITE).append(event.getPlayer().getName()).append(ChatColor.GRAY).append(" > ").append(event.getMessage());
            getPlayers().forEach(p -> p.sendMessage(builder.toString()));
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.AQUA + "" + ChatColor.BOLD + "TEAMMATE " + ChatColor.WHITE).append(event.getPlayer().getName()).append(ChatColor.GRAY).append(" > ").append(event.getMessage());
            getPlayers().forEach(p -> {
                if (!getPlayerData(p.getUniqueId()).isRunner())
                    p.sendMessage(builder.toString());
            });
        }
    }

    @EventSubscription
    private void onPortal(PlayerPortalEvent event) {
        if (getPlayers().contains(event.getPlayer())) {
            Location loc = event.getTo();
            if (event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER) {
                loc.setWorld(world);
            } else {
                loc.setWorld(netherWorld);
            }
            Bukkit.broadcastMessage(event.getPlayer().getName() + " traveled to " + loc.getWorld().getEnvironment().toString());
        }
    }

    private static VarHandle bitMaskDataWatcherObjectHandle;

    static {
        try {
            bitMaskDataWatcherObjectHandle = MethodHandles.privateLookupIn(Entity.class, MethodHandles.lookup()).findStaticVarHandle(Entity.class, "S", DataWatcherObject.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void setGlowing(Player glowingPlayer, Player sendPacketPlayer, boolean glow) {
        EntityPlayer entityPlayer = ((CraftPlayer) glowingPlayer).getHandle();
        DataWatcher original = entityPlayer.getDataWatcher();
        DataWatcher dataWatcher2 = new DataWatcher(entityPlayer);

        DataWatcherObject<Byte> object = (DataWatcherObject<Byte>) bitMaskDataWatcherObjectHandle.get();
        byte bitMask = original.get(object);
        bitMask = (byte) (glow ? bitMask | 1 << 6 : bitMask & ~(1 << 6));
        dataWatcher2.register(object, bitMask);

        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(glowingPlayer.getEntityId(), dataWatcher2, true);

        ((CraftPlayer) sendPacketPlayer).getHandle().playerConnection.sendPacket(metadataPacket);
    }
}
