package ca.mattlack.actioncompass;

import ca.mattlack.actioncompass.smpgames.deathswap.GameDeathSwap;
import ca.mattlack.actioncompass.smpgames.SMPGame;
import ca.mattlack.actioncompass.smpgames.runner.GameRunner;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.concurrent.ForkJoinPool;

public class TestCommand implements CommandExecutor {

    public static SMPGame<?> game = null;

    public TestCommand() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game != null) {
                    if (game.isRunning())
                        game.tick();
                }
            }
        }.runTaskTimer(ActionBarCompass.instance, 0, 1);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (game != null && game.isRunning()) {
            game.stop();
            game = null;
        } else {
            game = new GameRunner();

            ForkJoinPool.commonPool().submit(() -> {
                game.asyncPreStart();
                System.out.println("World created.");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getOnlinePlayers().forEach(game::addPlayer);
                        long ms = System.currentTimeMillis();
                        game.start();
                        ms = System.currentTimeMillis() - ms;
                        System.out.println("Started game in " + ms + "ms");
                    }
                }.runTask(ActionBarCompass.instance);
            });
        }

        System.out.println("Done.");
        return true;
    }
}
