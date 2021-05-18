package ca.mattlack.actioncompass;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ABCManager {
    private Map<UUID, Vector> destinations = new HashMap<>();
    public static ABCManager instance;

    public ABCManager() {
        instance = this;
    }

    public void clearDest(UUID player) {
        destinations.remove(player);
    }

    public void clearAllDestinations() {
        destinations.clear();
    }

    public void setDestination(UUID player, Vector destination) {
        destinations.put(player, destination);
    }

    public void tick() {
        destinations.forEach((id, dest) -> {
            Player player = Bukkit.getPlayer(id);
            if (player == null)
                return;

            Vector direction = player.getLocation().getDirection().normalize();
            Vector destDir = dest.clone().subtract(player.getLocation().toVector().setY(dest.getY())).normalize();

            double yawDir = Math.atan2(direction.getX(), direction.getZ()) + Math.PI;
            double yawDest = Math.atan2(destDir.getX(), destDir.getZ()) + Math.PI;

            double delta = Math.toDegrees(yawDest - yawDir);

            double d = delta;

            StringBuilder builder = new StringBuilder();
            delta *= (60 / 360D); //0-60
            yawDir = Math.toDegrees(yawDir) * (60 / 360D) + 60; //60-120
            int iyawDir = (int) yawDir; //60-120
            int iyawDest = iyawDir + (int) delta; //
            for (int i = iyawDir + 14; i >= iyawDir - 15; i--) {
                if (i % 60 == iyawDest % 60) {
                    builder.append(ChatColor.YELLOW + "" + ChatColor.BOLD + "|" + ChatColor.WHITE);
                } else {
                    builder.append(ChatColor.WHITE + "#");
                }
            }

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(builder.toString()));
        });
    }
}
