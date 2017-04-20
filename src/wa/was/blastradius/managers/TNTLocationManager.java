package wa.was.blastradius.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;

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
	
	private static TNTLocationManager instance = new TNTLocationManager();
	
	private Map<UUID, Map<Location, String>> placedTNT;
	
	private TNTLocationManager() {
		placedTNT = new HashMap<UUID, Map<Location, String>>();
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
	
	public List<Location> getPlayerLocations(UUID uuid) {
		List<Location> locs = new ArrayList<Location>();
		if ( placedTNT.containsKey(uuid) ) {
			for ( Location loc : placedTNT.get(uuid).keySet() ) {
				locs.add(loc);
			}
		}
		return locs;
	}
	
	public List<Location> getPlayerLocationsByType(UUID uuid, String type) {
		List<Location> locs = new ArrayList<Location>();
		if ( placedTNT.containsKey(uuid) ) {
			for ( Map.Entry<Location, String> entry : placedTNT.get(uuid).entrySet() ) {
				if ( entry.getValue().equals(type) ) {
					locs.add(entry.getKey());
				}
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

}
