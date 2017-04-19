package wa.was.blastradius.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import wa.was.blastradius.BlastRadius;
import wa.was.blastradius.commands.OnCommand;

 /*************************
 * 
 *	Copyright (c) 2017 Jordan Thompson (WASasquatch)
 *	
 *	Permission is hereby granted, free of charge, to any person obtaining a copy
 *	of this software and associated documentation files (the "Software"), to deal
 *	in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *	
 *	The above copyright notice and this permission notice shall be included in all
 *	copies or substantial portions of the Software.
 *	
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *	SOFTWARE.
 *	
 *************************/

public class TNTLocationManager {
	
	private JavaPlugin plugin;
	private static TNTLocationManager instance = new TNTLocationManager();
	
	private Map<UUID, Map<Location, String>> placedTNT;
	
	private TNTLocationManager() {
		placedTNT = new HashMap<UUID, Map<Location, String>>();
		plugin = BlastRadius.getBlastRadiusInstance();
	}
	
	public void addTNT(Player player, String type, Location location) {
		if ( placedTNT.containsKey(player.getUniqueId()) ) {
			Map<Location, String> tnt = placedTNT.get(player.getUniqueId());
			tnt.put(location, type);
			placedTNT.put(player.getUniqueId(), tnt);
		} else {
			placedTNT.put(player.getUniqueId(), new HashMap<Location, String>(){
				private static final long serialVersionUID = 1L; {
				put(location, type);
			}});
		}
	}
	
	// Code snippet from SethBling
	
    public static Vector calculateVelocity(Vector from, Vector to, int heightGain) {
        // Gravity of a potion | 115
        double gravity = 0.115;
 
        // Block locations
        int endGain = to.getBlockY() - from.getBlockY();
        double horizDist = Math.sqrt(distanceSquared(from, to));
 
        // Height gain
        int gain = heightGain;
 
        double maxGain = gain > (endGain + gain) ? gain : (endGain + gain);
 
        // Solve quadratic equation for velocity
        double a = -horizDist * horizDist / (4 * maxGain);
        double b = horizDist;
        double c = -endGain;
 
        double slope = -b / (2 * a) - Math.sqrt(b * b - 4 * a * c) / (2 * a);
 
        // Vertical velocity
        double vy = Math.sqrt(maxGain * gravity);
 
        // Horizontal velocity
        double vh = vy / slope;
 
        // Calculate horizontal direction
        int dx = to.getBlockX() - from.getBlockX();
        int dz = to.getBlockZ() - from.getBlockZ();
        double mag = Math.sqrt(dx * dx + dz * dz);
        double dirx = dx / mag;
        double dirz = dz / mag;
 
        // Horizontal velocity components
        double vx = vh * dirx;
        double vz = vh * dirz;
 
        return new Vector(vx, vy, vz);
    }
 
    private static double distanceSquared(Vector from, Vector to) {
        double dx = to.getBlockX() - from.getBlockX();
        double dz = to.getBlockZ() - from.getBlockZ();
        return dx * dx + dz * dz;
    }
    
    // End code snippet from SethBling
	
	public void clear() {
		placedTNT.clear();
	}
	
	public boolean containsLocation(Location location) {
		for ( Map.Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet() ) {
			if ( entry.getValue().containsKey(location) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsRelativeLocation(Location location) {
		for ( Map.Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet() ) {
			for ( Entry<Location, String> locEntry : entry.getValue().entrySet() ) {
				if ( Double.compare(location.distanceSquared(locEntry.getKey()), 1.0) < 0 ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public TNTPrimed createPrimedTNT(Map<String, Object> effect, Location location, Float multiplier, int ticks, Sound sound, float pitch, Vector velocity) {
		TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
		location.getWorld().playSound(location, sound, 1, pitch);
		String type = (String) effect.get("type");
		if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
			Bukkit.getLogger().info("Creating TNTPrimed Entity at: "+location.getX()+", "+location.getY()+", "+location.getZ()+" Initial Yield: "+tnt.getYield()+" Initial FuseTicks: "+tnt.getFuseTicks()+" Effect: "+type);
		}
		tnt.setYield(tnt.getYield() * multiplier);
		tnt.setMetadata("tntType", new FixedMetadataValue(plugin, type));
		if ( velocity != null ) {
			tnt.setVelocity(velocity);
		}
		tnt.setFuseTicks(ticks);
		if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
			Bukkit.getLogger().info("Created TNTPrimed Entity at: "+location.getX()+", "+location.getY()+", "+location.getZ()+" Yield: "+tnt.getYield()+" FuseTicks: "+tnt.getFuseTicks()+" Effect: "+type);
		}
		return tnt;
	}
	
	public TNTPrimed createPrimedTNT(Map<String, Object> effect, Location location, Float multiplier, int ticks, Sound sound, float pitch) {
		return createPrimedTNT(effect, location, multiplier, ticks, sound, pitch, null);
	}
	
	public static TNTLocationManager getInstance( ) {
		return instance;
	}
	
	public UUID getRelativeOwner(Location location) {
		for ( Map.Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet() ) {
			for ( Entry<Location, String> locEntry : entry.getValue().entrySet() ) {
				if ( Double.compare(location.distanceSquared(locEntry.getKey()), 1.0) < 0 ) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	public UUID getOwner(Location location) {
		for ( Map.Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet() ) {
			if ( entry.getValue().containsKey(location) ) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public String getType(Location location) {
		for ( Map.Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet() ) {
			if ( entry.getValue().containsKey(location) ) {
				return entry.getValue().get(location);
			}
		}
		return null;
	}
	
	public String getRelativeType(Location location) {
		for ( Map.Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet() ) {
			for ( Entry<Location, String> locEntry : entry.getValue().entrySet() ) {
				if ( Double.compare(location.distanceSquared(locEntry.getKey()), 1.0) < 0 ) {
					return locEntry.getValue();
				}
			}
		}
		return null;
	}
	
	public Collection<Location> getLocations(UUID uuid) {
		Collection<Location> locs = Collections.emptySet();
		if ( placedTNT.containsKey(uuid) ) {
			for ( Location loc : placedTNT.get(uuid).keySet() ) {
				locs.add(loc);
			}
		}
		return locs;
	}
	
	public String getPlayersType(UUID uuid, Location location) {
		if ( placedTNT.containsKey(uuid) ) {
			return placedTNT.get(uuid).get(location);
		}
		return null;
	}
	
	private Location getTargetBlock(Location location, int range) {
		BlockIterator iter = new BlockIterator(location, range);
		Block lastBlock = iter.next();
		while (iter.hasNext()) {
			lastBlock = iter.next();
			if (lastBlock.getType() != Material.AIR) {
				break;
			}
		}
		return lastBlock.getLocation();
    }

	@SuppressWarnings("unchecked")
	public void loadPlacedTNT() {
		
		String cachePath = Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").getDataFolder() + File.separator + "cache";
		File cache = new File(cachePath);
		File cacheMapFile =  new File(cachePath + File.separator + "placedTNT.ser");
		
		if ( ! ( cache.exists() ) ) {
			cache.mkdirs();
		}
		
		Map<UUID, Map<Map<String, Object>, String>> cacheMap = null;
		FileInputStream is;
		ObjectInputStream ois;
		
		if ( cacheMapFile.exists() ) {
			try {
				is = new FileInputStream(cacheMapFile);
				ois = new ObjectInputStream(is);
				cacheMap = (Map<UUID, Map<Map<String, Object>, String>>) ois.readObject();
				ois.close();
				is.close();
			} catch ( IOException | ClassNotFoundException e ) {
				e.printStackTrace();
			}
		}
		
		Map<Location, String> locationMap = new HashMap<Location, String>();
		
		if ( cacheMap != null && cacheMap.size() > 0 ) {
			for (Map.Entry<UUID, Map<Map<String, Object>, String>> entry : cacheMap.entrySet()) {
			    UUID uuid = entry.getKey();
			    Map<Map<String, Object>, String> map = entry.getValue();
			    for ( Map<String, Object> serialized : map.keySet() ) {
			    	String type = map.get(serialized);
			    	locationMap.put(Location.deserialize(serialized), type);
			    }
			    placedTNT.put(uuid, locationMap);
			}
		}
		
	}
	
	public void removeAllPlayersTNT(UUID uuid, Location loc) {
		if ( placedTNT.containsKey(uuid) ) {
			placedTNT.put(uuid, new HashMap<Location, String>());
		}
	}
	
	public void removeAllTNT() {
		for(Iterator<Map.Entry<UUID, Map<Location, String>>> it = placedTNT.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<UUID, Map<Location, String>> entry = it.next();
			for(Iterator<Map.Entry<Location, String>> ite = entry.getValue().entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<Location, String> locEntry = ite.next();
				if ( locEntry.getKey().getBlock().getType().equals(Material.TNT) ) {
					locEntry.getKey().getBlock().setType(Material.AIR);
				}
			}
		}
		clear();
	}
	
	public void removeRelativePlayersTNT(UUID uuid, Location location) {
		for(Iterator<Map.Entry<UUID, Map<Location, String>>> it = placedTNT.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<UUID, Map<Location, String>> entry = it.next();
			for(Iterator<Map.Entry<Location, String>> ite = entry.getValue().entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<Location, String> locEntry = ite.next();
				if ( Double.compare(location.distanceSquared(locEntry.getKey()), 1.0) < 0 ) {
					ite.remove();
				}
			}
		}
	}
	
	public void removePlayersTNT(UUID uuid, Location location, String type) {
		if ( placedTNT.containsKey(uuid) ) {
			if ( type != null ) {
				placedTNT.get(uuid).remove(location, type);
			} else {
				placedTNT.get(uuid).remove(location);
			}
		}
	}
	
	public void removePlayersTNT(UUID uuid, Location location) {
		removePlayersTNT(uuid, location, null);
	}
	
	public void removeTNT(Location location) {
		for(Iterator<Map.Entry<UUID, Map<Location, String>>> it = placedTNT.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<UUID, Map<Location, String>> entry = it.next();
			for(Iterator<Map.Entry<Location, String>> it2 = placedTNT.get(entry.getKey()).entrySet().iterator(); it2.hasNext(); ) {
				Map.Entry<Location, String> entry2 = it2.next();
				if ( entry2.equals(location) ) {
					it2.remove();
				}
			}
		}
	}
	
	public void savePlacedTNT() {
		
		String cachePath = Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").getDataFolder() + File.separator + "cache";
		File cache = new File(cachePath);
		File cacheMapFile =  new File(cachePath + File.separator + "placedTNT.ser");
		
		if ( ! ( cache.exists() ) ) {
			cache.mkdirs();
		}
		
		try {
			
			Map<UUID, Map<Map<String, Object>, String>> cacheMap = new HashMap<UUID, Map<Map<String, Object>, String>>();
	    	  
			FileOutputStream fileOut = new FileOutputStream(cacheMapFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			
			for (Map.Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet()) {
			    UUID uuid = entry.getKey();
			    Map<Map<String, Object>, String> userCacheMap = new HashMap<Map<String, Object>, String>();
			    Map<Location, String> userMap = entry.getValue();
			    for ( Location location : userMap.keySet() ) {
			    	String type = userMap.get(location);
			    	userCacheMap.put(location.serialize(), type);
			    }
			    cacheMap.put(uuid, userCacheMap);
			}
					         
			out.writeObject(cacheMap);
			out.close();
			fileOut.close();
					         					         
		} catch( IOException e ) {		    	  
			e.printStackTrace();		         
		}
		
	}
	
	public void sendDebugMessages() {
		for ( Entry<UUID, Map<Location, String>> entry : placedTNT.entrySet() ) {
			UUID uuid = entry.getKey();
			for ( Map.Entry<Location, String> loc : entry.getValue().entrySet() ) {
				Bukkit.getLogger().info("TNT Type: "+loc.getValue()+" Placed By: "+uuid.toString()+" Location: "+loc.getKey().getBlockX()+", "+loc.getKey().getY()+", "+loc.getKey().getZ());
			}
		}
	}
	
	public TNTPrimed playerTossTNT(Map<String, Object> effect, Player player, int range) {
	    if ( effect == null ) {
	    	return null;
	    }
		Vector d = player.getLocation().getDirection();
		Location el = player.getEyeLocation().add(d);
		Vector from = el.getDirection();
	    Location to = getTargetBlock(el, range);
	    Vector tossed = calculateVelocity(from, to.getDirection(), (int) effect.get("tossHeightGain"));
	    TNTPrimed tnt = createPrimedTNT(effect, 
										el, 
										(float) effect.get("yieldMultiplier"), 
										(int) effect.get("fuseTicks"), 
										(Sound) effect.get("soundEffect"), 
										(float) effect.get("soundEffectPitch"),
										tossed);
	    return tnt;
	    
	}

}
