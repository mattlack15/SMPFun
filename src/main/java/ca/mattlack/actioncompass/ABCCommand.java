package ca.mattlack.actioncompass;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ABCCommand  implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(args.length < 2) {
            ABCManager.instance.clearDest(((Player)sender).getUniqueId());
            sender.sendMessage("Destination cleared");
            return true;
        }

        String xs = args[0], zs = args[1];

        try {
            int x = Integer.parseInt(xs);
            int z = Integer.parseInt(zs);

            Vector vector = new Vector(x, 0, z);
            ABCManager.instance.setDestination(((Player)sender).getUniqueId(), vector);
            sender.sendMessage("Your destination has been set to " + ChatColor.YELLOW + x + ", " + z);

        } catch (NumberFormatException e) {
            sender.sendMessage("Arguments must be numbers!");
        }


        return true;
    }
}
