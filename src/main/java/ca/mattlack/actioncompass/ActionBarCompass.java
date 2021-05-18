package ca.mattlack.actioncompass;

import ca.mattlack.actioncompass.detection.CheatDetection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class ActionBarCompass extends JavaPlugin {

    public static ActionBarCompass instance;

    private ABCManager manager = new ABCManager();

    @Override
    public void onEnable() {
        super.onEnable();

        instance = this;

        new CheatDetection(this);

        Bukkit.getScheduler().runTaskTimer(this, manager::tick, 0, 2);
        Objects.requireNonNull(this.getCommand("abc")).setExecutor(new ABCCommand());
        Objects.requireNonNull(this.getCommand("test")).setExecutor(new TestCommand());
        Objects.requireNonNull(this.getCommand("nic")).setExecutor(new NickCommand());

    }

    @Override
    public void onDisable() {
        CheatDetection.instance.shutdown();
        if(TestCommand.game != null && TestCommand.game.isRunning()) {
            TestCommand.game.stop();
        }
    }
}
