package ro.Stellrow.ChunkHoppers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ChunkHoppers extends JavaPlugin {

    private HoppersManager hoppersManager;
    public NamespacedKey hopperKey = new NamespacedKey(this,"chunkhopper");

    private SQLiteHandler sqLiteHandler = new SQLiteHandler(this.getDataFolder(),"chunkHoppers","chunkHoppers",
            "CREATE TABLE IF NOT EXISTS chunkHoppers (" +
                    "`uuid` INTEGER PRIMARY KEY," +
                    "`world` varchar(50) NOT NULL," +
                    "`x` INT NOT NULL," +
                    "`y` INT NOT NULL," +
                    "`z` INT NOT NULL" +
                    ");"
            );

    public void onEnable(){
        loadConfig();
        hoppersManager = new HoppersManager(this);
        hoppersManager.registerEvents();
        hoppersManager.loadMaterials();
        sqLiteHandler.load();
        hoppersManager.loadHoppers();
        getCommand("ch").setExecutor(new CHCommands(this));
    }
    public ItemStack createItem(){
        ItemStack i = new ItemStack(Material.HOPPER);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&',getConfig().getString("ItemConfig.name")));
        List<String> lore = new ArrayList<>();
        for(String s : getConfig().getStringList("ItemConfig.lore")){
            lore.add(ChatColor.translateAlternateColorCodes('&',s));
        }
        im.setLore(lore);
        im.getPersistentDataContainer().set(hopperKey, PersistentDataType.STRING,"chunkhopper");
        i.setItemMeta(im);
        return i;
    }

    public SQLiteHandler getSqLiteHandler(){return sqLiteHandler;}

    private void loadConfig(){
        getConfig().options().copyDefaults(true);
        saveConfig();
    }


}
