package wa.was.blastradius.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;

/*************************
 * 
	Copyright (c) 2017 Jordan Thompson (WASasquatch)
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
	
	POTION ARCHETYPE
	
	potion-effects:
	  Effect_Name:
	    type: PotionEffectType
	    duration: TICKS
	    amplifier: DOUBLE
	    ambient: BOOLEAN
	    particles: BOOLEAN
	    color: Color
	
	For PotionEffectType's see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html
	For Color's see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Color.html
	
*
**************************/

public class PotionEffectsManager {

	private static PotionEffectsManager instance = new PotionEffectsManager();
	
    private HashMap<String, List<Player>> playerSets = new HashMap<String, List<Player>>();
    private HashMap<String, List<PotionEffect>> potionSets = new HashMap<String, List<PotionEffect>>();
    private Map<String, List<String>> messageMap = new HashMap<String, List<String>>();    

    
    private long chatCooldown;
    private boolean isActive = false;
    private boolean isMsgActive = false;
    private boolean hasParsedConfig = false;

    private PotionEffectsManager() {}

    public boolean addAllEffects(String set, List<PotionEffect> effects) {
    	if ( ! ( playerSets.containsKey(set) ) ) {
    		createPotionSet(set);
    	}
    	List<PotionEffect> potionEffects = potionSets.get(set); 
    	for ( PotionEffect effect : effects ) {
    		potionEffects.add(effect);
    	}
        potionSets.put(set, potionEffects);
        if ( potionSets.get(set).containsAll(effects) ) {
        	return true;
        }
        return false;
    }

    public boolean addEffect(String set, PotionEffect effect) {
    	if ( ! ( potionSets.containsKey(set) ) ) {
    		createPotionSet(set);
    	}
    	List<PotionEffect> potionEffects = potionSets.get(set); 
        return potionEffects.add(effect);
    }

    public boolean addAllPlayers(String set, List<Player> players) {
    	if ( ! ( playerSets.containsKey(set) ) ) {
    		createPlayerSet(set);
    	}
    	List<Player> playerSet = playerSets.get(set); 
    	for ( Player player : players ) {
    		playerSet.add(player);
    	}
        playerSets.put(set, playerSet);
        if ( playerSets.get(set).containsAll(players) ) {
        	return true;
        }
        return false;
    }

    public boolean addPlayer(String set, Player player) {
    	if ( ! ( playerSets.containsKey(set) ) ) {
    		createPlayerSet(set);
    	}
        return playerSets.get(set).add(player);
    }

    public boolean addPlayersInRadius(String set, Location center, int radius, boolean ellipsis) {
        List<Player> ep = new ArrayList<Player>();
        List<Player> players = center.getWorld().getPlayers();
        if ( ellipsis ) {
        	radius = ( radius * radius );
        }
        for ( Player player : players ) {
	        if ( center.distanceSquared(player.getLocation()) <= radius ) {
	            ep.add(player);
	        }
        }
        if ( ep.size() > 0 ) {
            addAllPlayers(set, ep);
            return true;
        }
        return false;
    }
    
    public void addMessages(String set, List<String> messages) {
    	messageMap.put(set, messages);
    }

    public List<Player> applySetToPlayers(String potionSet, String playerSet) {
    	List<Player> rogues = new ArrayList<Player>();
    	FileConfiguration config = getConfig(potionSet);
    	if ( config == null ) {
    		return new ArrayList<Player>();
    	}

    	if ( config.getBoolean("consecutive-effects") ) {
	    	if ( ! isActive ) {
	    		isActive = true;
		        for ( Player player: playerSets.get(playerSet) ) {
		            if ( ! ( player.addPotionEffects(potionSets.get(potionSet)) ) ) {
		            	rogues.add(player);
		            } else {
		            	if ( messageMap.size() > 0 && config.getBoolean("show-potion-messages", true) ) {
		            		if ( ! isMsgActive ) {
		            	    	isMsgActive = true;
			            		boolean doMessage = false;
			            		if ( chatCooldown <= System.currentTimeMillis()) {
			            			doMessage = true;
			            			chatCooldown = 0;
			            		} 
			            		if ( doMessage ) {
			                    	for ( String msg : messageMap.get(potionSet) ) {
			                    		player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			                    		chatCooldown = System.currentTimeMillis() + ( 10 * 1000 );
			                    	}
			            		}
		            		}
		            	}
		            }
		        }
	            isMsgActive = false;
	            isActive = false;
	        }
    	} else {
	        for ( Player player: playerSets.get(playerSet) ) {
	            if ( ! ( player.addPotionEffects(potionSets.get(potionSet)) ) ) {
	            	rogues.add(player);
	            } else {
	            	if ( messageMap.size() > 0 && config.getBoolean("show-potion-messages", true) ) {
	            		if ( ! isMsgActive ) {
	            	    	isMsgActive = true;
		            		boolean doMessage = false;
		            		if ( chatCooldown <= System.currentTimeMillis()) {
		            			doMessage = true;
		            			chatCooldown = 0;
		            		} 
		            		if ( doMessage ) {
		                    	for ( String msg : messageMap.get(potionSet) ) {
		                    		player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		                    		chatCooldown = System.currentTimeMillis() + ( 10 * 1000 );
		                    	}
		            		}
	            		}
	            	}
	            }
	        }
            isMsgActive = false;
    	}
        if ( rogues.size() > 0 ) {
        	// Something has gone wrong with applying effects to these "rogues"
        	return rogues;
        }
        return null;
    }
    
    public List<Player> applySetToAllPlayers(String potionSet) {
    	List<Player> rogues = new ArrayList<Player>();
    	FileConfiguration config = getConfig(potionSet);
    	if ( config == null ) {
    		return new ArrayList<Player>();
    	}
    	if ( config.getBoolean("consecutive-effects") ) {
	    	if ( ! isActive ) {
	    		isActive = true;
		    	for ( List<Player> players : playerSets.values() ) {
			        for ( Player player : players ) {
			            if ( ! ( player.addPotionEffects(potionSets.get(potionSet)) ) ) {
			            	rogues.add(player);
			            } else {
			            	if ( messageMap.size() > 0 && config.getBoolean("show-potion-messages", true) ) {
			            		if ( ! isMsgActive ) {
			            	    	isMsgActive = true;
				            		boolean doMessage = false;
				            		if ( chatCooldown <= System.currentTimeMillis()) {
				            			doMessage = true;
				            			chatCooldown = 0;
				            		} 
				            		if ( doMessage ) {
				                    	for ( String msg : messageMap.get(potionSet) ) {
				                    		player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				                    		chatCooldown = System.currentTimeMillis() + ( 10 * 1000 );
				                    	}
				            		}
			            		}
			            	}
			            }
			        }
		    	}
		    	isMsgActive = false;
		        isActive = false;
	    	}
    	} else {
	    	for ( List<Player> players : playerSets.values() ) {
		        for ( Player player : players ) {
		            if ( ! ( player.addPotionEffects(potionSets.get(potionSet)) ) ) {
		            	rogues.add(player);
		            } else {
		            	if ( messageMap.size() > 0 && config.getBoolean("show-potion-messages", true) ) {
		            		if ( ! isMsgActive ) {
		            	    	isMsgActive = true;
			            		boolean doMessage = false;
			            		if ( chatCooldown <= System.currentTimeMillis()) {
			            			doMessage = true;
			            			chatCooldown = 0;
			            		} 
			            		if ( doMessage ) {
			                    	for ( String msg : messageMap.get(potionSet) ) {
			                    		player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			                    		chatCooldown = System.currentTimeMillis() + ( 10 * 1000 );
			                    	}
			            		}
		            		}
		            	}
		            }
		        }
	    	}
	    	isMsgActive = false;
    	}
        if ( rogues.size() > 0 ) {
        	// Something has gone wrong with applying effects to these "rogues"
        	return rogues;
        }
        return null;
    }
    
    // Create or reset a player set
    public void createPlayerSet(String set) {
    	playerSets.put(set, new ArrayList<Player>());
    }
    
    // Create or reset a potion set
    public void createPotionSet(String set) {
    	potionSets.put(set, new ArrayList<PotionEffect>());
    }
    
    public static PotionEffectsManager getInstance() {
    	return instance;
    }
    
    public List<PotionEffect> getPotionSet(String set) {
    	return potionSets.get(set);
    }
    
    public List<Player> getPlayerSet(String set) {
    	return playerSets.get(set);
    }
    
    public boolean hasParsedConfig() {
    	return hasParsedConfig;
    }

    public boolean removeAllEffects(String set, List<PotionEffect> effects) {
        return potionSets.get(set).removeAll(effects);
    }

    public boolean removeEffect(String set, PotionEffect effect) {
        return potionSets.get(set).remove(effect);
    }

    public boolean removeAllPlayers(String set, List<Player> players) {
        return playerSets.get(set).removeAll(players);
    }

    public boolean removePlayer(String set, Player player) {
        return playerSets.get(set).remove(player);
    }
    
    public void reset() {
    	resetPotionSets();
    	resetPlayerSets();
    	messageMap = new HashMap<String, List<String>>();
    }

    public void resetPotionSets() {
        potionSets = new HashMap<String, List<PotionEffect>>();
    }
    
    public void resetMessages() {
    	messageMap = new HashMap<String, List<String>>();
    }
    
    public void resetPlayerSets() {
        playerSets = new HashMap<String, List<Player>>();
    }
    
    public FileConfiguration getConfig(String type) {
    	File effectFile = new File(Bukkit.getPluginManager().getPlugin("BlastRadius").getDataFolder().getAbsolutePath() + File.separator + "effects" + File.separator + type.toLowerCase() + ".yml");
    	YamlConfiguration effect = new YamlConfiguration();
    	if ( effectFile.exists() ) {
    		boolean loaded = false;
    		try {
    			loaded = true;
				effect.load(effectFile);
			} catch (IOException | InvalidConfigurationException e) {
				loaded = false;
				e.printStackTrace();
			}
    		if ( loaded )  {
    			return effect;
    		}
    	}
    	return null;
    }

}