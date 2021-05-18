package ca.mattlack.actioncompass.smpgames;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SMPGame<T> {

    private Map<UUID, T> players = new HashMap<>();

    public abstract void stop();

    public abstract void start();

    public void asyncPreStart() {}

    public abstract T createPlayerData(Player player);

    public List<Player> getPlayers() {
        return this.players.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public T getPlayerData(UUID playerId) {
        return this.players.get(playerId);
    }

    public void setPlayers(Map<UUID, T> players) {
        this.players.clear();
        this.players.putAll(players);
    }

    public void clearPlayers() {
        this.players.clear();
    }

    public void addPlayer(Player player) {
        addPlayer(player, createPlayerData(player));
    }

    public void addPlayer(Player player, T data) {

        //Add to map
        this.players.put(player.getUniqueId(), data);

        //Spawn if necessary
        if(isRunning())
            spawnPlayer(player);
    }

    public void removePlayer(Player player) {
        removePlayer(player, true);
    }

    public void removePlayer(Player player, boolean despawn) {
        this.players.remove(player.getUniqueId());
        if(isRunning() && despawn)
            despawnPlayer(player);
    }

    public abstract void spawnPlayer(Player player);
    public abstract void despawnPlayer(Player player);

    public abstract void tick();

    public abstract boolean isRunning();
}
