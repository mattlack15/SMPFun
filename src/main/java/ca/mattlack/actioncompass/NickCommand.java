package ca.mattlack.actioncompass;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

public class NickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(strings.length < 1)
            return true;

        Player player = (Player) commandSender;
        nick(player, strings[0]);

        return true;
    }


    private void nick(Player player, String name) {
        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        Field f;
        try {
            f = GameProfile.class.getDeclaredField("name");
            f.setAccessible(true);
            f.set(entityPlayer.getProfile(), name);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Bukkit.getOnlinePlayers().forEach(p -> {
            p.hidePlayer(ActionBarCompass.instance, player);
            p.showPlayer(ActionBarCompass.instance, player);
        });
    }
}
