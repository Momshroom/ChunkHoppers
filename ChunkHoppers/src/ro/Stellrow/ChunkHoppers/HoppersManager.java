package ro.Stellrow.ChunkHoppers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HoppersManager implements Listener {
    private final ChunkHoppers pl;

    private HashMap<Chunk,Block> hoppers = new HashMap<>();

    private List<Material> materialList = new ArrayList<>();

    public HoppersManager(ChunkHoppers pl) {
        this.pl = pl;
    }
    public void registerEvents(){
        pl.getServer().getPluginManager().registerEvents(this,pl);
    }
    public void loadHoppers(){
        hoppers.clear();
        for(Block b : pl.getSqLiteHandler().getAllHoppers()){
            hoppers.put(b.getChunk(),b);
        }
    }
    public void loadMaterials(){
        materialList.clear();
        for(String s : pl.getConfig().getStringList("General.materials")){
            try{
                materialList.add(Material.valueOf(s));
            }catch (IllegalArgumentException ex){
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"[ChunkHoppers] Found bad material name at "+ChatColor.GOLD+s);
            }
        }
    }


    //*Handling of breaking/placing
    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        if(event.getItemInHand().getType()== Material.HOPPER){
            ItemStack i = event.getItemInHand();
            Block b = event.getBlockPlaced();
            if(i.hasItemMeta()){
                if(i.getItemMeta().getPersistentDataContainer().has(pl.hopperKey, PersistentDataType.STRING)){
                    if(canPlace(b.getChunk())) {
                        pl.getSqLiteHandler().addHopper(b);
                        hoppers.put(b.getChunk(), b);
                        setPDC(b);
                        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',pl.getConfig().getString("Messages.placed-hopper")));
                    }else{
                        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',pl.getConfig().getString("Messages.already-placed-chunk")));
                        event.setCancelled(true);
                    }

                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        if(event.getBlock().getType()==Material.HOPPER){
            Hopper hopper = (Hopper) event.getBlock().getState();
            if(hopper.getPersistentDataContainer().has(pl.hopperKey,PersistentDataType.STRING)){
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',pl.getConfig().getString("Messages.destroyed-hopper")));
                pl.getSqLiteHandler().removeHopper(event.getBlock());
                hoppers.remove(event.getBlock().getChunk());
                spawnHopper(event.getBlock().getLocation());
                event.setDropItems(false);
            }
        }

    }

    private void setPDC(Block b){
        Hopper hopper = (Hopper) b.getState();
        hopper.getPersistentDataContainer().set(pl.hopperKey,PersistentDataType.STRING,"chunkHopper");
        hopper.update();
    }
    private void spawnHopper(Location location){
        location.getWorld().dropItemNaturally(location,pl.createItem());
    }
    private boolean canPlace(Chunk involved){
        if(hoppers.containsKey(involved)){
            return false;
        }
        return true;
    }

    //*Handling of breaking/placing


    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event){
        if(materialList.contains(event.getEntity().getItemStack().getType())){
            if(hoppers.containsKey(event.getLocation().getChunk())){
                Block hopperBlock = hoppers.get(event.getLocation().getChunk());
                Hopper hopper = (Hopper) hopperBlock.getState();
                if(hopper.getInventory().firstEmpty()==-1){
                    return;
                }
                hopper.getInventory().addItem(event.getEntity().getItemStack());
                event.setCancelled(true);
            }
        }

    }



}
