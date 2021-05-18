package ca.mattlack.actioncompass.detection;

import ca.mattlack.actioncompass.detection.util.EventSubscriptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class Check {
    private String checkName;
    private UUID playerid;
    protected CheatDetection manager;

    public Check(UUID playerId, String checkName) {
        this.checkName = checkName;
        this.playerid = playerId;
        EventSubscriptions.instance.subscribe(this);
    }

    public String getCheckName() {
        return this.checkName;
    }

    public UUID getPlayerId() {
        return this.playerid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerid);
    }

    public void recordViolation(int severity) {
        if(manager == null)
            manager = CheatDetection.instance;
        manager.recordViolation(this, severity, playerid);
    }

}
