package ca.mattlack.actioncompass;

import ca.mattlack.actioncompass.smpgames.WorldProvider;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

public class NickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(strings.length < 1)
            return true;

        if(strings[0].length() > 16) {
            commandSender.sendMessage("Can't be longer than 16 characters sorry.");
            return true;
        }

        Player player = (Player) commandSender;
        nick(player, strings[0]);
        player.sendMessage("You have been nicknamed to " + player.getName());

        return true;
    }


    private void nick(Player player, String name) {
        if(name.length() > 16)
            return;
        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        Field f;
        try {
            f = GameProfile.class.getDeclaredField("name");
            f.setAccessible(true);
            f.set(entityPlayer.getProfile(), name);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            UUID id = Bukkit.getOfflinePlayer(name).getUniqueId();

            setSkin(entityPlayer.getProfile(), id);
            WorldProvider.sync(() -> {
                Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(ActionBarCompass.instance, player));

                Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(ActionBarCompass.instance, player));

                PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
                entityPlayer.playerConnection.sendPacket(info);

                CraftWorld world = (CraftWorld) player.getWorld();

                int level = player.getLevel();
                float xp = player.getExp();
                Location loc = player.getLocation();

                PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(world.getHandle().getDimensionManager(),
                        world.getHandle().getDimensionKey(), -1, EnumGamemode.getById(player.getGameMode().getValue()),
                        EnumGamemode.getById(player.getGameMode().getValue()), false, player.getWorld().getWorldType() == WorldType.FLAT,
                        true);

                entityPlayer.playerConnection.sendPacket(respawn);

                player.teleport(loc);
                player.setExp(xp);
                player.setLevel(level);
                player.updateInventory();


                PacketPlayOutPlayerInfo info2 = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
                entityPlayer.playerConnection.sendPacket(info2);
            }, ActionBarCompass.instance);
        }).start();
    }

    public boolean setSkin(GameProfile profile, UUID uuid) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder reply = new StringBuilder();
                while(reader.ready()) {
                    reply.append(reader.readLine());
                    reply.append("\n");
                }
                reader.close();
                String skin = reply.toString().split("\"value\" : \"")[1].split("\"")[0];
                String signature = reply.toString().split("\"signature\" : \"")[1].split("\"")[0];

                WorldProvider.sync(() -> {
                    profile.getProperties().removeAll("textures");
                    profile.getProperties().put("textures", new Property("textures", skin, signature));
                }, ActionBarCompass.instance);
                return true;
            } else {
                System.out.println("Connection could not be opened (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
