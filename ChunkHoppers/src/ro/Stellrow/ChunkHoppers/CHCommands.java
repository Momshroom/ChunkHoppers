package ro.Stellrow.ChunkHoppers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CHCommands implements CommandExecutor {
    private final ChunkHoppers chunkHoppers;

    public CHCommands(ChunkHoppers chunkHoppers) {
        this.chunkHoppers = chunkHoppers;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String sa, String[] args) {
        if(sender.hasPermission("chunkhoppers.give")) {
            if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if(target==null){
                sender.sendMessage("No player found with this name!");
                return true;
            }
            Integer amount = 1;
            try{
                amount=Integer.parseInt(args[2]);
                ItemStack i = chunkHoppers.createItem();
                i.setAmount(amount);
                sender.sendMessage(ChatColor.GREEN+"Gave the player a Chunk Hopper");
                if(target.getInventory().firstEmpty()==-1){
                    target.getLocation().getWorld().dropItemNaturally(target.getLocation(),i);
                    return true;
                }
                target.getInventory().addItem(i);
            }catch (IllegalArgumentException ex){
                sender.sendMessage("Wrong amount!");
                return true;
            }
            }
            sender.sendMessage(ChatColor.GRAY+"Usage: /ch give <player> <amount>");
        }
        return true;
    }
}
