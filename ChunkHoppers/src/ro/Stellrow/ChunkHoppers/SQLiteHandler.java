package ro.Stellrow.ChunkHoppers;

import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHandler {
    private File pluginDataFolder;
    private String fileName;
    private String sqlDefaultStatement;
    private String table;

    private Connection connection;

    //We ask for the plugins folder,how the file should be named,how the table should be named(for later uses),and a default statement allowing to specify columns
    public SQLiteHandler(@NotNull File pluginDataFolder, @NotNull String fileName,String table,@NotNull String sqlDefaultStatement){
        this.pluginDataFolder=pluginDataFolder;
        this.fileName=fileName;
        this.sqlDefaultStatement = sqlDefaultStatement;
        this.table=table;
    }


    public Connection getSQLConnection() {
        if(!pluginDataFolder.exists()){
            pluginDataFolder.mkdirs();
        }
        File dataFolder = new File(pluginDataFolder, fileName+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
           ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(sqlDefaultStatement);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }

    private void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE uuid = ?");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void close(PreparedStatement ps,Connection conn){
        try {
            if (ps != null)
                ps.close();
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<Block> getAllHoppers(){
        List<Block> toReturn = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try{
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM "+table);
            rs = ps.executeQuery();
            while(rs.next()){
                String world;
                int x;
                int y;
                int z;
                world = rs.getString("world");
                x = rs.getInt("x");
                y = rs.getInt("y");
                z = rs.getInt("z");
                Block b = new Location(Bukkit.getWorld(world),x,y,z).getBlock();
                if(b.getType()!= Material.HOPPER){
                    removeHopper(b);
                }else {
                    toReturn.add(b);
                }
            }

        } catch (SQLException e) {

        }finally {
            close(ps,conn);
        }
        return toReturn;
    }

    //Add
    public void addHopper(Block toAdd){
        Location loc = toAdd.getLocation();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO " + table + " (world,x,y,z) VALUES(?,?,?,?)");
            ps.setString(1,toAdd.getWorld().getName());
            ps.setInt(2,loc.getBlockX());
            ps.setInt(3,loc.getBlockY());
            ps.setInt(4,loc.getBlockZ());
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(ps,conn);
        }
    }
    //Remove
    public void removeHopper(Block toRemove){
        Location loc = toRemove.getLocation();
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM "+table +" WHERE world=? AND x=? AND y=? AND z=?");
            ps.setString(1,loc.getWorld().getName());
            ps.setInt(2,loc.getBlockX());
            ps.setInt(3,loc.getBlockY());
            ps.setInt(4,loc.getBlockZ());
            ps.executeUpdate();
            return;

        } catch (SQLException e) {
        }finally {
           close(ps,conn);
        }
    }
}
