package ca.mattlack.actioncompass.smpgames;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import net.minecraft.server.v1_16_R3.*;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class WorldProvider {

    private static final Convertable CONVERTABLE;

    static {
        Path path;
        try{
            path = Files.createTempDirectory("smpwp-" + UUID.randomUUID().toString().substring(0, 5) + "-");
        }catch(IOException ex) {
            System.out.println("\n\nFAILED to create temp directory\n");
            ex.printStackTrace();
            path = null;
            System.exit(1);
        }

        CONVERTABLE = Convertable.a(path);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try {
                FileUtils.deleteDirectory(CONVERTABLE.universe.toFile());
            } catch (IOException ex) {
                System.out.println("\n\nFailed to delete temp directory\n");
                ex.printStackTrace();
            }

        }));
    }

    public static World loadWorldAsyncSafe(WorldCreator creator, Plugin plugin) {
        ChunkGenerator generator = creator.generator();
        CraftServer server = (CraftServer) Bukkit.getServer();
        MinecraftServer console = server.getServer();

        long start = System.currentTimeMillis();

        if (generator == null) {
            generator = server.getGenerator(creator.name());
        }

        ResourceKey<net.minecraft.server.v1_16_R3.World> worldKey = ResourceKey.a(IRegistry.L, new MinecraftKey("smp:" + creator.name().toLowerCase()));
        Convertable.ConversionSession session;
        try {
            session = CONVERTABLE.c(creator.name(), WorldDimension.OVERWORLD);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        WorldDataServer worldDataServer = createWorldData(creator, console);
        worldDataServer.checkName(creator.name());
        worldDataServer.a(console.getServerModName(), console.getModded().isPresent());
        worldDataServer.c(true);

        List<MobSpawner> list = ImmutableList.of(new MobSpawnerPhantom(), new MobSpawnerPatrol(), new MobSpawnerCat(), new VillageSiege(), new MobSpawnerTrader(worldDataServer));

        ResourceKey<WorldDimension> actualDimension;
        switch(creator.environment().ordinal()) {
            case 0:
                actualDimension = WorldDimension.OVERWORLD;
                break;
            case 1:
                actualDimension = WorldDimension.THE_NETHER;
                break;
            case 2:
                actualDimension = WorldDimension.THE_END;
                break;
            default:
                throw new IllegalArgumentException("Illegal dimension");
        }


        AtomicReference<WorldServer> r = new AtomicReference<>();

        ChunkGenerator finalGenerator = generator;

        sync(() -> {

            WorldServer worldServer = new WorldServer(console, console.executorService, session, worldDataServer,
                    worldKey, worldDataServer.getGeneratorSettings().d().a(actualDimension).b(),
                    MinecraftServer.getServer().worldLoadListenerFactory.create(11),
                    worldDataServer.getGeneratorSettings().d().a(actualDimension).c(), //Possibly change to .d().a(WorldDimension.OVERWORLD).whatever()
                    false,
                    BiomeManager.a(creator.seed()),
                    list,
                    true,
                    creator.environment(),
                    finalGenerator);

            worldServer.keepSpawnInMemory = false;
            console.initWorld(worldServer, worldDataServer, console.getSaveData(), worldDataServer.getGeneratorSettings());
            worldServer.setSpawnFlags(true, true);
            console.worldServer.put(worldServer.getDimensionKey(), worldServer);
            Bukkit.getPluginManager().callEvent(new WorldInitEvent(worldServer.getWorld()));
            console.loadSpawn(worldServer.getChunkProvider().playerChunkMap.worldLoadListener, worldServer);
            Bukkit.getPluginManager().callEvent(new WorldLoadEvent(worldServer.getWorld()));
            r.set(worldServer);
            }, plugin);

        return r.get().getWorld();
    }

    private static WorldDataServer createWorldData(WorldCreator creator, MinecraftServer console) {
        WorldDataServer worlddata;
        Properties properties = new Properties();
        properties.put("generator-settings", Objects.toString(creator.generatorSettings()));
        properties.put("level-seed", Objects.toString(creator.seed()));
        properties.put("generate-structures", Objects.toString(creator.generateStructures()));
        properties.put("level-type", Objects.toString(creator.type().getName()));
        GeneratorSettings generatorsettings = GeneratorSettings.a(console.getCustomRegistry(), properties);
        WorldSettings worldSettings = new WorldSettings(creator.name(), EnumGamemode.SURVIVAL, false, EnumDifficulty.EASY, false, new GameRules(), console.datapackconfiguration);
        worlddata = new WorldDataServer(worldSettings, generatorsettings, Lifecycle.stable());
        return worlddata;
    }

    public static void sync(Runnable runnable, Plugin plugin) {
        if(Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }
        CompletableFuture<Void> f = new CompletableFuture<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
                f.complete(null);
            }
        }.runTask(plugin);
        f.join();
    }

}
