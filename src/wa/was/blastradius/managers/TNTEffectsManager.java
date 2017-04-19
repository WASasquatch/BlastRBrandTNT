package wa.was.blastradius.managers;

import wa.was.blastradius.BlastRadius;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wa.was.blastradius.utils.ConsoleColor;

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

public class TNTEffectsManager {
	
	private static TNTEffectsManager instance = new TNTEffectsManager();
	
	private PotionEffectsManager potionEffectsManager;
	
	private Map<String, Map<String, Object>> effects;
	private Map<String, String> displayNames;
	
    private HashMap<String, Color> colors;
    private HashMap<String, PotionEffectType> effectTypes;
	
	private TNTEffectsManager() {
		effects = new HashMap<String, Map<String, Object>>();
		displayNames = new HashMap<String, String>();
		potionEffectsManager = BlastRadius.potionManager;
		colors = new HashMap<String, Color>() {
	        private static final long serialVersionUID = 323135985062054098L; {
	            put("AQUA", Color.AQUA);
	            put("BLACK", Color.BLACK);
	            put("BLUE", Color.BLUE);
	            put("FUCHSIA", Color.FUCHSIA);
	            put("GRAY", Color.GRAY);
	            put("GREEN", Color.GREEN);
	            put("LIME", Color.LIME);
	            put("MAROON", Color.MAROON);
	            put("NAVY", Color.NAVY);
	            put("OLIVE", Color.OLIVE);
	            put("ORANGE", Color.ORANGE);
	            put("PRUPLE", Color.PURPLE);
	            put("RED", Color.RED);
	            put("SILVER", Color.SILVER);
	            put("TEAL", Color.TEAL);
	            put("WHIE", Color.WHITE);
	            put("YELLOW", Color.YELLOW);
	        }
	    };
		effectTypes = new HashMap<String, PotionEffectType>() {
	        private static final long serialVersionUID = 7862567811034165045L; {
	            for ( PotionEffectType type: PotionEffectType.values() ) {
	            	if (type != null && type.getName() != null ) {
	            		put(type.getName(), type);
	            	}
	            }
	        }
	    };
		loadDefaultEffects();
	}
	
	public String displayNameToType(String name) {
		if ( hasDisplayName(name) ) {
			return displayNames.get(name);
		}
		return null;
	}
	
	public boolean hasDisplayName(String name) {
		return displayNames.containsKey(name);
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasLore(String effect) {
		if ( ((List<String>) effects.get(effect).get("lore")).size() > 0 ) {
			return true;
		}
		return false;
	}
	
	public boolean hasEffect(String effect) {
		return effects.containsKey(effect);
	}
	
	public void loadEffect(File effectFile) {
		
		if ( ! ( effectFile.exists() ) ) {
			return;
		}
		
		YamlConfiguration effect = new YamlConfiguration();
		Map<String, Object> effectInfo = new HashMap<String, Object>();
		
		try {
			
			effect.load(effectFile);
			
			String effectName = effect.getString("effect-name", "DEFAULT").toUpperCase();
			
			effectInfo.put("type", effectName);
			effectInfo.put("fuseTicks", effect.getInt("fuse-ticks", 80));
			effectInfo.put("vaultCost", effect.getDouble("cost", 10.0));
			if ( Double.compare(effect.getDouble("worth", 10), 0.1) > 0 ) {
				effectInfo.put("vaultWorth", effect.getDouble("worth", 0.1));
			} else {
				Bukkit.getServer().getLogger().warning("Vault Worth cannot be less than 0.1. Vault cost entered: "+effect.getInt("fire-radius")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("vaultWorth", 0.1);
			}
			effectInfo.put("displayName", ChatColor.translateAlternateColorCodes('&', effect.getString("display-name", "TNT")));
			displayNames.put(ChatColor.translateAlternateColorCodes('&', (String)effectInfo.get("displayName")), effectName);
			List<String> effectLore = effect.getStringList("lore");
			List<String> lores = new ArrayList<String>();
			if ( effectLore.size() > 0 ) {
				for ( String line : effectLore ) {
					lores.add(ChatColor.translateAlternateColorCodes('&', line));
				}
				effectInfo.put("lore", lores);
			}
			if ( Sound.valueOf(effect.getString("sound-effect", "ENTITY_TNT_PRIMED")) != null ) {
				effectInfo.put("soundEffect", Sound.valueOf(effect.getString("sound-effect", "ENTITY_TNT_PRIMED")));
			} else {
				Bukkit.getServer().getLogger().warning("Sound Effect invalid: "+effect.getInt("sound-effect")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("soundEffect", Sound.ENTITY_TNT_PRIMED);
			}
			double effectPitch = effect.getDouble("sound-effect-pitch");
			if ( Double.compare(effectPitch, 2.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("Sound Effect pitch is too high: "+effectPitch+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("soundEffectPitch", (float)1.0);
			} else if ( Double.compare(effectPitch, 0.5) < 0 ) {
				Bukkit.getServer().getLogger().warning("Sound Effect pitch is too high: "+effectPitch+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("soundEffectPitch",  (float)1.0);
			} else {
				effectInfo.put("soundEffectPitch", (float)effectPitch);
			}
			double explosionVolume = effect.getDouble("sound-explosion-volume", 2);
			if ( Double.compare(explosionVolume, 12.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("Sound Explosion volume is too high: "+explosionVolume+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("explosionVolume", (float)2);
			} else {
				effectInfo.put("explosionVolume", (float)explosionVolume);
			}
			effectInfo.put("tamperProof", effect.getBoolean("tamper-proof", false));
			effectInfo.put("doFires", effect.getBoolean("blast-fires", true));
			effectInfo.put("doSmoke", effect.getBoolean("blast-smoke", false));
			effectInfo.put("obliterate", effect.getBoolean("obliterate-obliterables", false));
			effectInfo.put("ellipsis", effect.getBoolean("elliptical-radius", true));
			if ( effect.getInt("blast-yield-multiplier", 1) <= 20 ) {
				effectInfo.put("yieldMultiplier", (float) effect.getDouble("blast-yield-multiplier", 1));
			} else {
				Bukkit.getServer().getLogger().warning("Blast Multiplier out of Range: "+effect.getDouble("blast-yield-multiplier")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("yieldMultiplier", 1);
			}
			if ( effect.getInt("blast-radius", 10) <= 50 ) {
				effectInfo.put("blastRadius", effect.getInt("blast-radius", 10));
			} else {
				Bukkit.getServer().getLogger().warning("Dead Zone out of Range: "+effect.getInt("blast-radius")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("blastRadius", 10);
			}
			if ( effect.getInt("fire-radius", 9) <= 50 ) {
				effectInfo.put("fireRadius", effect.getInt("fire-radius", 9));
			} else {
				Bukkit.getServer().getLogger().warning("Fire Radius out of Range: "+effect.getInt("fire-radius")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("fireRadius", 9);
			}
			if ( effect.getInt("smoke-count", 10) <= 100 ) {
				effectInfo.put("smokeCount", effect.getInt("smoke-count", 10));
			} else {
				Bukkit.getServer().getLogger().warning("Smoke Count out of Range: "+effect.getInt("smoke-count")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("smokeCount",  10);
			}
			if ( Double.compare(effect.getDouble("smoke-offset", 0.25), 10) < 0 ) {
				effectInfo.put("smokeOffset", effect.getDouble("smoke-offset", 0.25));
			} else {
				Bukkit.getServer().getLogger().warning("Smoke Offset out of Range: "+effect.getInt("smoke-offset")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("smokeOffset", 0.25);
			}
			effectInfo.put("tntTossable", effect.getBoolean("tnt-tossable", false));
			if ( effect.getInt("tnt-tossable-height", 3) < 256 ) {
				effectInfo.put("tossHeightGain", effect.getInt("tnt-tossed-height", 3));
			} else {
				Bukkit.getServer().getLogger().warning("TNT Tossable Height gain out of Range: "+effect.getInt("tnt-tossable-height")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("tossHeightGain", 3);
			}
			if ( effect.getInt("tnt-tossable-range", 3) < 50 && effect.getInt("tnt-tossable-range", 3) > 1 ) {
				effectInfo.put("tossRange", effect.getInt("tnt-tossable-range", 7));
			} else {
				Bukkit.getServer().getLogger().warning("TNT Tossable Range out of Range: "+effect.getInt("tnt-tossable-range")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("tossRange", 7);
			}
			if ( effect.getInt("tnt-tossable-cooldown", 10) < 1 ) {
				Bukkit.getServer().getLogger().warning("TNT Tossable Cooldown must be above 1. Value found: "+effect.getInt("tnt-tossable-cooldown")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("tossCooldown", 10);
			} else {
				effectInfo.put("tossCooldown", effect.getInt("tnt-tossable-cooldown", 10));
			}
			effectInfo.put("doPotions", effect.getBoolean("potion-effect", true));
			effectInfo.put("consecPotions", effect.getBoolean("consecutive-potion-effects", false));
			effectInfo.put("showPotionMsg", effect.getBoolean("show-potion-message", true));
			effectInfo.put("potionMsg", effect.getString("potoin-message", "&1You have been &2effected &1with &c{TYPE} &1for &6{TIME} &rseconds"));
			
			List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
			List<String> messages = new ArrayList<String>();
			for ( String type : effect.getConfigurationSection("potion-effects").getKeys(false) ) {
	            if ( effectTypes.containsKey(effect.getString("potion-effects." + type + ".type").toUpperCase()) ) {
	                if ( effect.getString("potion-effects." + type + ".color") != null &&
	                    colors.containsKey(effect.getString("potion-effects." + type + ".color")) ) {
	                    potionEffects.add(
	                        new PotionEffect(effectTypes.get(effect.getString("potion-effects." + type + ".type").toUpperCase()),
	                            effect.getInt("potion-effects." + type + ".duration"),
	                            effect.getInt("potion-effects." + type + ".amplifier"),
	                            effect.getBoolean("potion-effects." + type + ".ambient"),
	                            effect.getBoolean("potion-effects." + type + ".particles"),
	                            colors.get(effect.getString("potion-effects." + type + ".color"))));
	                } else {
	                    potionEffects.add(
	                        new PotionEffect(effectTypes.get(effect.getString("potion-effects." + type + ".type").toUpperCase()),
	                            effect.getInt("potion-effects." + type + ".duration"),
	                            effect.getInt("potion-effects." + type + ".amplifier"),
	                            effect.getBoolean("potion-effects." + type + ".ambient"),
	                            effect.getBoolean("potion-effects." + type + ".particles")));
	                }
	                messages.add(effect.getString("potion-message", "&1You have been &2effected &1with &c{TYPE} &1for &6{TIME} &rseconds")
	                				.replace("{TYPE}", WordUtils.capitalize(effect.getString("potion-effects." + type + ".type").toLowerCase()))
	                				.replace("{TIME}", ""+( effect.getInt("potion-effects." + type + ".duration") / 20)));
	            }
			}
			potionEffectsManager.addMessages(effectName, messages);
	        potionEffectsManager.addAllEffects(effectName, potionEffects);
			
			List<Material> innerMaterials = new ArrayList<Material>();
			for ( String mat : effect.getStringList("inner-blast-materials") ) {
				if ( Material.valueOf(mat) != null ) {
					innerMaterials.add(Material.valueOf(mat));
				} else {
					Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Inner Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
				}
			}
			effectInfo.put("innerMaterials", innerMaterials);
			
			List<Material> outerMaterials = new ArrayList<Material>();
			for ( String mat : effect.getStringList("outer-blast-materials") ) {
				if ( Material.valueOf(mat) != null ) {
					outerMaterials.add(Material.valueOf(mat));
				} else {
					Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Outer Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
				}
			}
			effectInfo.put("outerMaterials", outerMaterials);
			
			List<Material> protectedMaterials = new ArrayList<Material>();
			for ( String mat : effect.getStringList("protected-materials") ) {
				if ( Material.valueOf(mat) != null ) {
					protectedMaterials.add(Material.valueOf(mat));
				} else {
					Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Protected Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
				}
			}
			effectInfo.put("protectedMaterials", protectedMaterials);
			
			List<Material> obliterateMaterials = new ArrayList<Material>();
			for ( String mat : effect.getStringList("obliterate-materials") ) {
				if ( Material.valueOf(mat) != null ) {
					obliterateMaterials.add(Material.valueOf(mat));
				} else {
					Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Obliterate Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
				}
			}
			effectInfo.put("obliterateMaterials", obliterateMaterials);
			
			Bukkit.getServer().getLogger().warning("[BlastRadius] Added BlastR Brand TNT Effect: "+ConsoleColor.YELLOW+ConsoleColor.BOLD+effectName+ConsoleColor.RESET);
			effects.put(effectName, effectInfo);
			
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private void loadDefaultEffects() {
		
		String effectsPath = Bukkit.getPluginManager().getPlugin("BlastRadius").getDataFolder().getAbsolutePath() + File.separator + "effects" + File.separator;
		File effectsDir = new File(effectsPath);
		
		File defaultEffect = new File(effectsPath + "default.yml");
		File defaultEffectNuke = new File(effectsPath + "nuke.yml");
		
		if ( ! (effectsDir.exists() ) ) {
			effectsDir.mkdirs();
		}
		
		if ( ! ( defaultEffect.exists() ) ) {
			Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/default.yml", false);
		}
		
		if ( ! ( defaultEffectNuke.exists() ) ) {
			Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/nuke.yml", false);
		}
		
		try(Stream<Path> paths = Files.walk(Paths.get(effectsPath))) {
		    paths.forEach(filePath -> {
		        if ( Files.isRegularFile(filePath) ) {
		        	
		        	String ext = "";
		        	
		        	int i = filePath.toString().lastIndexOf('.');
		        	if (i > 0) {
		        	    ext = filePath.toString().substring(i+1);
		        	}
		        	
		        	if ( ext.equalsIgnoreCase("yml") ) {
		        		
		        		File effectFile = new File(filePath.toString());
		        		
		        		loadEffect(effectFile);
		        	
		        	}
		        }
		    });
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Map<String, Object> getEffect(String effect) {
		if ( effects.containsKey(effect) ) {
			return effects.get(effect);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLore(String effect) {
		if ( hasEffect(effect) ) {
			return (List<String>) effects.get(effect).get("lore");
		}
		return new ArrayList<String>();
	}
	
	public static TNTEffectsManager getInstance() {
		return instance;
	}

}