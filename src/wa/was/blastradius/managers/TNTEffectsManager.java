package wa.was.blastradius.managers;

import wa.was.blastradius.BlastRadius;
import wa.was.blastradius.commands.OnCommand;
import wa.was.blastradius.interfaces.VaultInterface;

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
import java.util.Random;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

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
	
	private JavaPlugin plugin;
	private PotionEffectsManager potionEffectsManager;
	
	private Map<String, String> displayNames;
	private Map<String, Map<String, Object>> effects;
	private Map<String, String> remoteDetonators;
	
    private HashMap<String, Color> colors;
    private HashMap<String, PotionEffectType> effectTypes;
	
	private TNTEffectsManager() {
		effects = new HashMap<String, Map<String, Object>>();
		displayNames = new HashMap<String, String>();
		remoteDetonators = new HashMap<String, String>();
		potionEffectsManager = BlastRadius.potionManager;
		plugin = BlastRadius.getBlastRadiusInstance();
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
	
	// Code snippet from SethBling
	
    private Vector calculateVelocity(Vector from, Vector to, int heightGain) {
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
 
    private double distanceSquared(Vector from, Vector to) {
        double dx = to.getBlockX() - from.getBlockX();
        double dz = to.getBlockZ() - from.getBlockZ();
        return dx * dx + dz * dz;
    }
    
    // End code snippet from SethBling
	
	public TNTPrimed createPrimedTNT(Map<String, Object> effect, Location location, Float multiplier, int ticks, Sound sound, float volume, float pitch, Vector velocity, boolean doSound) {
		Location center = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		TNTPrimed tnt = location.getWorld().spawn(center, TNTPrimed.class);
		if ( doSound ) {
			location.getWorld().playSound(location, sound, volume, pitch);
		}
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
	
	public TNTPrimed createPrimedTNT(Map<String, Object> effect, Location location, Float multiplier, int ticks, Sound sound, float volume, float pitch, boolean doSound) {
		return createPrimedTNT(effect, location, multiplier, ticks, sound, volume, pitch, null, doSound);
	}
	
	public TNTPrimed createPrimedTNT(Map<String, Object> effect, Location location, Float multiplier, int ticks, Sound sound, float volume, float pitch) {
		return createPrimedTNT(effect, location, multiplier, ticks, sound, volume, pitch, null, true);
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
		return (effect == null) ? false : effects.containsKey(effect);
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
				Bukkit.getServer().getLogger().warning("Vault Worth cannot be less than 0.1. Vault cost entered: "+effect.get("worth")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("vaultWorth", 0.1);
			}
			
			effectInfo.put("remoteDetonation", effect.getBoolean("remote-detonation", false));
			
			if ( Material.valueOf(effect.getString("remote-detonator-material", "STONE_BUTTON")) != null ) {
				effectInfo.put("remoteDetonator", Material.valueOf(effect.getString("remote-detonator-material", "STONE_BUTTON")));
			} else {
				Bukkit.getServer().getLogger().warning("Remote Detonator Material Invalid: "+effect.get("remote-detonator-material")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("remoteDetonator", Material.STONE_BUTTON);
			}
			
			effectInfo.put("displayName", ChatColor.translateAlternateColorCodes('&', effect.getString("display-name", "TNT")));
			String detonatorName = effect.getString("display-name", "TNT") +" "+ effect.getString("remote-detonator-tag", "&6Detonator");
			effectInfo.put("remoteDetonatorName", ChatColor.translateAlternateColorCodes('&', detonatorName));
			
			remoteDetonators.put((String) effectInfo.get("remoteDetonatorName"), effectName);
			
			if ( Sound.valueOf(effect.getString("detonator-effect", "BLOCK_NOTE_PLING")) != null ) {
				effectInfo.put("detonatorEffect", Sound.valueOf(effect.getString("detonator-effect", "BLOCK_NOTE_PLING")));
			} else {
				Bukkit.getServer().getLogger().warning("Detonator Effect Invalid: "+effect.get("detonator-effect")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("detonatorEffect", Sound.BLOCK_NOTE_PLING);
			}
			
			double effectDetonatorPitch = effect.getDouble("detonaor-effect-pitch", 1.5);
			
			if ( Double.compare(effectDetonatorPitch, 2.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("Detonator Effect Pitch is too high: "+effectDetonatorPitch+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("detonatorEffectPitch", (float)1.0);
			} else if ( Double.compare(effectDetonatorPitch, 0.5) < 0 ) {
				Bukkit.getServer().getLogger().warning("Detonator Effect Pitch is too low: "+effectDetonatorPitch+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("detonatorEffectPitch",  (float)1.5);
			} else {
				effectInfo.put("detonatorEffectPitch", (float)effectDetonatorPitch);
			}
			
			double effectDetonatorVolume = effect.getDouble("detonator-effect-volume", 1.0);
			
			if ( Double.compare(effectDetonatorVolume, 12.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("Detonator Effect Volume is too high: "+effectDetonatorVolume+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("detontorEffectVolume", (float)1.0);
			} else if ( Double.compare(effectDetonatorVolume, 0.5) < 0 ) {
				Bukkit.getServer().getLogger().warning("Detonator Effect Volume is too high: "+effectDetonatorVolume+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("detonatorEffectVolume",  (float)1.0);
			} else {
				effectInfo.put("detonatorEffectVolume", (float)effectDetonatorVolume);
			}
			
			effectInfo.put("tntReceivable", effect.getBoolean("tnt-receivable", true));
			
			displayNames.put(ChatColor.translateAlternateColorCodes('&', (String) effectInfo.get("displayName")), effectName);
			List<String> effectLore = effect.getStringList("lore");
			List<String> lores = new ArrayList<String>();
			
			if ( effectLore.size() > 0 ) {
				for ( String line : effectLore ) {
					lores.add(ChatColor.translateAlternateColorCodes('&', line));
				}
				effectInfo.put("lore", lores);
			}
			
			if ( Sound.valueOf(effect.getString("fuse-effect", "ENTITY_TNT_PRIMED")) != null ) {
				effectInfo.put("fuseEffect", Sound.valueOf(effect.getString("fuse-effect", "ENTITY_TNT_PRIMED")));
			} else {
				Bukkit.getServer().getLogger().warning("Fuse Effect Invalid: "+effect.get("fuse-effect")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("fuseEffect", Sound.ENTITY_TNT_PRIMED);
			}
			
			double effectPitch = effect.getDouble("fuse-effect-pitch", 1.0);
			
			if ( Double.compare(effectPitch, 2.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("Fuse Effect Pitch is too high: "+effectPitch+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("fuseEffectPitch", (float)1.0);
			} else if ( Double.compare(effectPitch, 0.5) < 0 ) {
				Bukkit.getServer().getLogger().warning("Fuse Effect Pitch is too low: "+effectPitch+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("fuseEffectPitch",  (float)1.0);
			} else {
				effectInfo.put("fuseEffectPitch", (float)effectPitch);
			}
			
			double effectVolume = effect.getDouble("fuse-effect-volume", 1.0);
			
			if ( Double.compare(effectVolume, 12.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("Fuse Effect Volume is too high: "+effectVolume+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("fuseEffectVolume", (float)1.0);
			} else if ( Double.compare(effectVolume, 0.5) < 0 ) {
				Bukkit.getServer().getLogger().warning("Fuse Effect Volume is too high: "+effectVolume+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("fuseEffectVolume",  (float)1.0);
			} else {
				effectInfo.put("fuseEffectVolume", (float)effectVolume);
			}
			
			double explosionVolume = effect.getDouble("sound-explosion-volume", 2);
			
			if ( Double.compare(explosionVolume, 12.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("Sound Explosion volume is too high: "+explosionVolume+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("explosionVolume", (float)2);
			} else {
				effectInfo.put("explosionVolume", (float)explosionVolume);
			}
			
			effectInfo.put("tamperProof", effect.getBoolean("tamper-proof", false));
			effectInfo.put("doWaterDamage", effect.getBoolean("tnt-water-damage", false));
			effectInfo.put("doFires", effect.getBoolean("blast-fires", true));
			effectInfo.put("doSmoke", effect.getBoolean("blast-smoke", false));
			effectInfo.put("obliterate", effect.getBoolean("obliterate-obliterables", false));
			effectInfo.put("ellipsis", effect.getBoolean("elliptical-radius", true));
			
			if ( Double.compare(effect.getDouble("blast-yield-multiplier", 1F), 20.0) < 0 ) {
				effectInfo.put("yieldMultiplier", (float) effect.getDouble("blast-yield-multiplier", 1F));
			} else {
				Bukkit.getServer().getLogger().warning("Blast Multiplier out of Range: "+effect.get("blast-yield-multiplier")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("yieldMultiplier", (float) 1F);
			}
			
			if ( effect.getInt("blast-radius", 10) <= 50 ) {
				effectInfo.put("blastRadius", effect.getInt("blast-radius", 10));
			} else {
				Bukkit.getServer().getLogger().warning("Dead Zone out of Range: "+effect.get("blast-radius")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("blastRadius", (int)10);
			}
			
			if ( effect.getInt("fire-radius", 9) <= 50 ) {
				effectInfo.put("fireRadius", effect.getInt("fire-radius", 9));
			} else {
				Bukkit.getServer().getLogger().warning("Fire Radius out of Range: "+effect.get("fire-radius")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("fireRadius", (int)9);
			}
			
			if ( effect.getInt("smoke-count", 10) <= 100 ) {
				effectInfo.put("smokeCount", effect.getInt("smoke-count", 10));
			} else {
				Bukkit.getServer().getLogger().warning("Smoke Count out of Range: "+effect.get("smoke-count")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("smokeCount",  (int)10);
			}
			
			if ( Double.compare(effect.getDouble("smoke-offset", 0.25), 10) < 0 ) {
				effectInfo.put("smokeOffset", effect.getDouble("smoke-offset", 0.25));
			} else {
				Bukkit.getServer().getLogger().warning("Smoke Offset out of Range: "+effect.get("smoke-offset")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("smokeOffset", (double)0.25);
			}
			
			effectInfo.put("tntTossable", effect.getBoolean("tnt-tossable", false));
			
			if ( effect.getInt("tnt-tossable-height", 3) < 256 ) {
				effectInfo.put("tossHeightGain", effect.getInt("tnt-tossed-height", 3));
			} else {
				Bukkit.getServer().getLogger().warning("TNT Tossable Height gain out of Range: "+effect.get("tnt-tossable-height")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("tossHeightGain", (int)3);
			}
			
			if ( Double.compare(effect.getDouble("tnt-tossable-force", 1.5), 5.0) < 0 ) {
				effectInfo.put("tossForce", effect.getDouble("tnt-tossable-force", 1.5));
			} else {
				Bukkit.getServer().getLogger().warning("TNT Force to Strong. Forced used: "+effect.get("tnt-tossable-force")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("tossForce", (double)1.5);
			}
			
			if ( effect.getInt("tnt-tossable-cooldown", 10) < 1 ) {
				Bukkit.getServer().getLogger().warning("TNT Tossable Cooldown must be above 1. Value found: "+effect.get("tnt-tossable-cooldown")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("tossCooldown", (int)10);
			} else {
				effectInfo.put("tossCooldown", effect.getInt("tnt-tossable-cooldown", 10));
			}
			
			effectInfo.put("doCluster", effect.getBoolean("tnt-cluster-effect", false))
			;
			if ( effect.getInt("tnt-cluster-effect-amount", 3) > 10 ) {
				Bukkit.getServer().getLogger().warning("TNT Cluster Effect Amount must be between 1 - 10: "+effect.get("tnt-cluster-effect-amount")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("clusterAmount", (int)3);
			} else {
				effectInfo.put("clusterAmount", effect.getInt("tnt-cluster-effect-amount", 3));
			}
			
			effectInfo.put("clusterType", effect.getString("tnt-cluster-effect-type", "DEFAULT"));
			
			if ( Double.compare(effect.getDouble("tnt-cluster-effect-height-offset", 10.0), 30.0) > 0 ) {
				Bukkit.getServer().getLogger().warning("TNT Cluster Effect Amount must be between 1 - 10: "+effect.get("tnt-cluster-effect-amount")+" for TNT Effect: "+effectName+". Defaulting...");
				effectInfo.put("clusterOffset", (double)10.0);
			} else { 
				effectInfo.put("clusterOffset", effect.getDouble("tnt-cluster-effect-height-offset", 10.0));
			}
			
			effectInfo.put("doPotions", effect.getBoolean("potion-effect", true));
			effectInfo.put("consecPotions", effect.getBoolean("consecutive-potion-effects", false));
			effectInfo.put("showPotionMsg", effect.getBoolean("show-potion-message", true));
			effectInfo.put("potionMsg", effect.getString("potoin-message", "&1You have been &2effected &1with &c{TYPE} &1for &6{TIME} &rseconds"));
			
			List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
			List<String> messages = new ArrayList<String>();
			
			if ( effect.getConfigurationSection("potion-effects").getKeys(false).size() > 0 ) {
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
			}
			
			potionEffectsManager.addMessages(effectName, messages);
	        potionEffectsManager.addAllEffects(effectName, potionEffects);
			
			List<Material> innerMaterials = new ArrayList<Material>();
			if ( effect.getStringList("inner-blast-materials").size() > 0 ) {
				for ( String mat : effect.getStringList("inner-blast-materials") ) {
					if ( Material.valueOf(mat) != null ) {
						innerMaterials.add(Material.valueOf(mat));
					} else {
						Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Inner Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
					}
				}
			}
			
			effectInfo.put("innerMaterials", innerMaterials);
			
			List<Material> outerMaterials = new ArrayList<Material>();
			if ( effect.getStringList("outer-blast-materials").size() > 0 ) {
				for ( String mat : effect.getStringList("outer-blast-materials") ) {
					if ( Material.valueOf(mat) != null ) {
						outerMaterials.add(Material.valueOf(mat));
					} else {
						Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Outer Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
					}
				}
			}
			
			effectInfo.put("outerMaterials", outerMaterials);
			
			List<Material> protectedMaterials = new ArrayList<Material>();
			if ( effect.getStringList("protected-materials").size() > 0 ) {
				for ( String mat : effect.getStringList("protected-materials") ) {
					if ( Material.valueOf(mat) != null ) {
						protectedMaterials.add(Material.valueOf(mat));
					} else {
						Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Protected Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
					}
				}
			}
			
			effectInfo.put("protectedMaterials", protectedMaterials);
			
			List<Material> obliterateMaterials = new ArrayList<Material>();
			if ( effect.getStringList("obliterate-materials").size() > 0 ) {
				for ( String mat : effect.getStringList("obliterate-materials") ) {
					if ( Material.valueOf(mat) != null ) {
						obliterateMaterials.add(Material.valueOf(mat));
					} else {
						Bukkit.getServer().getLogger().warning("[BlastRadius] Invalid Obliterate Material: "+mat+" for TNT Effect: "+effectName+". Skipping...");
					}
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
		File defaultEffectCluster = new File(effectsPath + "cluster.yml");
		File defaultEffectClusterDrop = new File(effectsPath + "cluster_drop.yml");
		File defaultEffectCryobomb = new File(effectsPath + "cryobomb.yml");
		File defaultEffectC4 = new File(effectsPath + "c4.yml");
		
		if ( ! (effectsDir.exists() ) ) {
			effectsDir.mkdirs();
		}
		
		if ( ! ( plugin.getConfig().getBoolean("no-default-effects") ) ) {
		
			if ( ! ( defaultEffect.exists() ) ) {
				Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/default.yml", false);
			}
			
			if ( ! ( defaultEffectNuke.exists() ) ) {
				Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/nuke.yml", false);
			}
			
			if ( ! ( defaultEffectCluster.exists() ) ) {
				Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/cluster.yml", false);
			}
			
			if ( ! ( defaultEffectClusterDrop.exists() ) ) {
				Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/cluster_drop.yml", false);
			}
			
			if ( ! ( defaultEffectC4.exists() ) ) {
				Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/c4.yml", false);
			}
			
			if ( ! ( defaultEffectCryobomb.exists() ) ) {
				Bukkit.getServer().getPluginManager().getPlugin("BlastRadius").saveResource("effects/cryobomb.yml", false);
			}

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
	
	public List<String> getCatalog() {
		List<String> catalog = new ArrayList<String>();
		VaultInterface vault = new VaultInterface();
		catalog.add(ChatColor.translateAlternateColorCodes('&', "&6|-----------------------{ "+plugin.getConfig().getString("local.catalog-title", "&4BlastR Brand TNT &r- &7Catalog")+"&r &6}-----------------------|"));
		boolean found = false;
		if ( displayNames.size() > 0 ) {
			for ( Map.Entry<String, String> entry : displayNames.entrySet() ) {
				if ( ! ( hasEffect(entry.getValue()) ) ) continue;
				found = true;
				Map<String, Object> effect = getEffect(entry.getValue());
				String listing = ChatColor.translateAlternateColorCodes('&', "&a"+vault.format((double)effect.get("vaultCost"))+" &6| &r"+entry.getKey()+" &6- &r"+entry.getValue());
				catalog.add(listing);
			}
		}
		if ( ! found ) {
			catalog.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("local.catalog-empty", "&7Catalog is empty...")));
		}
		StringBuilder catEnd = new StringBuilder();
		catEnd.append("&6|");
		for ( int i = 1; i <= ChatColor.stripColor(catalog.get(0)).length()+13; i++ ) {
			catEnd.append("-");
		}
		catEnd.append("|");
		catalog.add(ChatColor.translateAlternateColorCodes('&', catEnd.toString()));
		return catalog;
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
	
	public List<Location> getRandomLocation(Location center, int radius, int amount) {
		Random rand = new Random();
		List<Location> locations = new ArrayList<Location>();
		for ( int i = 1; i <= amount; i++ ) {
			double angle = rand.nextDouble()*360;
			double x = center.getX() + (rand.nextDouble()*radius*Math.cos(Math.toRadians(angle)));
			double y = center.getY() + (rand.nextDouble()*radius*Math.cos(Math.toRadians(angle)));
			double z = center.getZ() + (rand.nextDouble()*radius*Math.sin(Math.toRadians(angle)));
			Location relLoc = new Location(center.getWorld(), x, y, z);
			Location location = center.getWorld().getHighestBlockAt(relLoc).getLocation();
			locations.add(location);
		}
		return locations;
	}
	
	public Location getTargetBlock(Location location, int range) {
		BlockIterator iter = new BlockIterator(location, range);
		Block lastBlock = iter.next();
		while (iter.hasNext()) {
			lastBlock = iter.next();
			if (lastBlock.getType() != Material.AIR) {
				continue;
			}
			break;
		}
		return lastBlock.getLocation();
    }
	
	public boolean isRemoteDetonator(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if ( ! ( meta.hasDisplayName() ) ) return false;
		if ( ! ( remoteDetonators.containsKey(meta.getDisplayName()) ) ) return false;
		return true;
	}
	
	public TNTPrimed playerTossTNT(Map<String, Object> effect, Player player) {
	    if ( effect == null ) return null;
		Vector d = player.getLocation().getDirection();
		Location el = player.getEyeLocation().add(d);
		Vector from = el.getDirection();
	    Location to = getTargetBlock(el, 5);
	    Vector tossed = calculateVelocity(from, to.getDirection(), (int) effect.get("tossHeightGain"));
	    TNTPrimed tnt = createPrimedTNT(effect, 
										el, 
										(float) effect.get("yieldMultiplier"), 
										(int) effect.get("fuseTicks"), 
										(Sound) effect.get("fuseEffect"), 
										(float) effect.get("fuseEffectVolume"),
										(float) effect.get("fuseEffectPitch"),
										tossed,
										true);
	    tnt.setVelocity(tnt.getVelocity().add(d).multiply((double) effect.get("tossForce")));
	    return tnt;
	    
	}
	
	public String remoteDetonatorsType(String name) {
		if ( ! ( remoteDetonators.containsKey(name) ) ) return null;
		return remoteDetonators.get(name);
	}
	
	public void tossClusterTNT(String type, Location center, int radius, int amount, boolean ellipsis) {
		if ( ! ( hasEffect(type) ) ) return;
		Map<String, Object> effect = getEffect(type);
		if ( ! ( hasEffect((String) effect.get("clusterType")) ) ) return;
		Map<String, Object> ceffect = getEffect((String) effect.get("clusterType"));
		List<Location> locations = getRandomLocation(center, radius, amount);
		if ( locations.size() > 0 ) {
			for ( Location location : locations ) {
				float ac = 0.2F;
				Location nc = location.add(0.0, (double) effect.get("clusterOffset"), 1.0);	
				Vector velocity = nc.toVector().subtract(center.toVector());
				velocity.add(new Vector(Math.random()*(ac - (ac/2)),Math.random()*(ac - (ac/2)),Math.random()*(ac - (ac/2)))).multiply(0.125);
				tossTNT(ceffect, center, velocity);
			}
		}
	}
	
	public void tossTNT(Map<String, Object> effect, Location from, Vector velocity) {
	    if ( effect == null ) return;
		effect.put("doCluster", false);
	    createPrimedTNT(effect, 
						from, 
						(float) effect.get("yieldMultiplier"), 
						(int) effect.get("fuseTicks"), 
						(Sound) effect.get("fuseEffect"), 
						(float) effect.get("fuseEffectVolume"),
						(float) effect.get("fuseEffectPitch"),
						velocity,
						true);
	}

}