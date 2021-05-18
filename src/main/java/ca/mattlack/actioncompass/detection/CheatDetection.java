package ca.mattlack.actioncompass.detection;

import ca.mattlack.actioncompass.detection.checks.autoclicker.AutoClicker1;
import ca.mattlack.actioncompass.detection.checks.xray.Xray1;
import ca.mattlack.actioncompass.detection.util.EventSubscription;
import ca.mattlack.actioncompass.detection.util.EventSubscriptions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CheatDetection {

    public static CheatDetection instance;
    public Map<UUID, PlayerProfile> profileList = new HashMap<>();

    public CheatDetection(Plugin plugin) {
        instance = this;
        new EventSubscriptions(plugin);
        EventSubscriptions.instance.subscribe(this);

        Runnable runnable = () -> Bukkit.getOnlinePlayers().forEach(p -> {
            EventSubscriptions.instance.injectPlayerPacketListener(p);
            PlayerProfile playerProfile = new PlayerProfile();
            playerProfile.id = p.getUniqueId();
            playerProfile.checkList = makeCheckList(p);
            profileList.put(p.getUniqueId(), playerProfile);
        });

        try {
            runnable.run();
        } catch(Exception e) {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, 1);
        }
    }

    public void shutdown() {
        this.profileList.forEach((id, p) -> {
            for (Check check : p.checkList) {
                EventSubscriptions.instance.unSubscribe(check);
            }
        });
        this.profileList.clear();
        Bukkit.getOnlinePlayers().forEach(p -> EventSubscriptions.instance.unInjectPlayerPacketListener(p));
    }


    public void recordViolation(Check check, int severity, UUID playerId) {

        Player player = Bukkit.getPlayer(playerId);
        String name = player != null ? player.getName() : playerId.toString();

        ChatColor secondary = ChatColor.of(new Color(0x9985A1, false));

        String msg = ChatColor.of(new Color(0x0F824AC, false)) + "GC " +
                ChatColor.LIGHT_PURPLE + name + secondary + " has failed " + ChatColor.YELLOW + check.getCheckName() + secondary + " severity " + ChatColor.RED + severity;

        Bukkit.getOnlinePlayers().forEach((p) -> {
            if(p.hasPermission("gc.notifications"))
                p.sendMessage(msg);
        });
    }

    private List<Check> makeCheckList(Player player) {
        List<Check> checks = new ArrayList<>();
        checks.add(new AutoClicker1(player.getUniqueId()));
        checks.add(new Xray1(player.getUniqueId()));
        return checks;
    }


    @EventSubscription
    private void onJoin(PlayerJoinEvent e) {
        EventSubscriptions.instance.injectPlayerPacketListener(e.getPlayer());
        PlayerProfile playerProfile = new PlayerProfile();
        playerProfile.id = e.getPlayer().getUniqueId();
        playerProfile.checkList = makeCheckList(e.getPlayer());
        profileList.put(e.getPlayer().getUniqueId(), playerProfile);
    }

    @EventSubscription
    private void onLeave(PlayerQuitEvent e) {
        EventSubscriptions.instance.unInjectPlayerPacketListener(e.getPlayer());
        profileList.get(e.getPlayer().getUniqueId()).checkList.forEach(c -> EventSubscriptions.instance.unSubscribe(c));
        profileList.remove(e.getPlayer().getUniqueId());
    }
}
