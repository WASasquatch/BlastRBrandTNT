package wa.was.blastradius.managers;

import wa.was.blastradius.BlastRadius;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class BlastEffectManager {
	
    private volatile static BlastEffectManager instance = null;
	
	private JavaPlugin plugin;
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	private BlastEffectManager(BlastRadius plugin) {
		this.plugin = (JavaPlugin) plugin;
		TNTManager = plugin.getTNTLocationManager();
		TNTEffects = plugin.getTNTEffectsManager();
	}
	
	public void createBlastRadius(Location center, List<Material> imats, List<Material> omats, List<Material> pmats, List<Material> obmats, boolean obliterate, boolean fires, boolean smoke, int sc, double so, int radius, int fradius, boolean ellipsis) {
		
		try {
			
			obliterate(center, obmats, obliterate, radius, ellipsis);
			render(center, omats, pmats, radius, true, ellipsis);
			render(center, omats, pmats, (int)(radius/1.25), false, ellipsis);
			render(center, imats, pmats, (int)(radius/1.75), false, ellipsis);
			if ( fires ) {
				fires(center, pmats, fradius, ellipsis);
			}
			if ( smoke ) {
				smoke(center, sc, so, radius, ellipsis);
			}
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void smoke(Location center, int sc, double so, int radius, boolean ellipsis) {
		
        int cX = center.getBlockX();
        int cY = center.getBlockY();
        int cZ = center.getBlockZ();
        int radiusSquared = radius * radius;
        for ( int x = cX - radius; x <= cX + radius; x++ ) {
            for ( int y = cY - radius; y <= cY + radius; y++ ) {
                for ( int z = cZ - radius; z <= cZ + radius; z++ ) {
                    if ( ellipsis && (cX - x) * (cX - x) + (cY - y) * (cY - y) + (cZ - z) * (cZ - z) <= radiusSquared ) { 
                    	
                    	Block block = center.getWorld().getBlockAt(x, y, z);
                    	Location smokeCenter = block.getLocation();
                    	int threshold = 20;
                    	
                    	if ( ! ( block.getType().equals(Material.AIR) ) ) {

                    		if ( randomInteger(0, threshold, new Random()) > 15 ) {
                    			createSmoke(smokeCenter, Particle.CLOUD, sc, so);
                    			createSmoke(smokeCenter, Particle.SMOKE_LARGE, sc, so);
                    		}
                    		
                    	}
                    	
                    } else {
                    	
                    	Block block = center.getWorld().getBlockAt(x, y, z);
                    	Location smokeCenter = block.getLocation();
                    	int threshold = 20;
                    	
                    	if ( ! ( block.getType().equals(Material.AIR) ) ) {

                    		if ( randomInteger(0, threshold, new Random()) > 15 ) {
                    			createSmoke(smokeCenter, Particle.CLOUD, sc, so);
                    			createSmoke(smokeCenter, Particle.SMOKE_LARGE, sc, so);
                    		}
                    		
                    	}
                    	
                    }
                }
            }
        }

	}
	
	public void fires(Location center, List<Material> pmats, int radius, boolean ellipsis) {
		
        int cX = center.getBlockX();
        int cY = center.getBlockY();
        int cZ = center.getBlockZ();
        int radiusSquared;
        if ( ellipsis ) {
        	radiusSquared = radius * radius;
        } else {
        	radiusSquared = radius;
        }
        for ( int x = cX - radius; x <= cX + radius; x++ ) {
            for ( int y = cY - radius; y <= cY + radius; y++ ) {
                for ( int z = cZ - radius; z <= cZ + radius; z++ ) {
                    if ( (cX - x) * (cX - x) + (cY - y) * (cY - y) + (cZ - z) * (cZ - z) <= radiusSquared ) { 
                    	
                    	Block block = center.getWorld().getBlockAt(x, y, z);
                    	Block blockAbove = block.getRelative(BlockFace.UP, 1);
                    	int threshold = 10;
                    	
                    	if ( ! ( block.getType().equals(Material.AIR) ) ) {
                    		if ( ! ( pmats.contains(block.getType())) 
                    				&&  block.getType().isFlammable() 
                    					|| block.getType().equals(Material.NETHERRACK) ) {
                    				if ( blockAbove.getType().equals(Material.AIR) && randomInteger(0, threshold, new Random()) > 5 )
                    					blockAbove.setType(Material.FIRE);
                    			
                    		}
                    	}
                    	
                    }
                }
            }
        }

	}
	
	public void obliterate(Location center, List<Material> obmats, boolean obliterate, int radius, boolean ellipsis) {
		
        int cX = center.getBlockX();
        int cY = center.getBlockY();
        int cZ = center.getBlockZ();
        int radiusSquared;
        if ( ellipsis ) {
        	radiusSquared = radius * radius;
        } else {
        	radiusSquared = radius;
        }
        for ( int x = cX - radius; x <= cX + radius; x++ ) {
            for ( int y = cY - radius; y <= cY + radius; y++ ) {
                for ( int z = cZ - radius; z <= cZ + radius; z++ ) {
                    if ( (cX - x) * (cX - x) + (cY - y) * (cY - y) + (cZ - z) * (cZ - z) <= radiusSquared ) { 
                    	
                    	Block block = center.getWorld().getBlockAt(x, y, z);
                    	
                    	if ( ! ( block.getType().equals(Material.AIR) ) ) {
                    			
                    		if ( obmats.contains(block.getType()) ) {
                    			if ( ! ( obliterate ) ) {
                    				block.breakNaturally();
                    			}
                    			block.setType(Material.AIR);
                    		}
                    			
                    	}
                    	
                    }
                }
            }
        }

	}
	
	public void render(Location center, List<Material> mats, List<Material> pmats, int radius, boolean doRandom, boolean ellipsis) {
		
        int cX = center.getBlockX();
        int cY = center.getBlockY();
        int cZ = center.getBlockZ();
        int radiusSquared;
        if ( ellipsis ) {
        	radiusSquared = radius * radius;
        } else {
        	radiusSquared = radius;
        }
        for ( int x = cX - radius; x <= cX + radius; x++ ) {
            for ( int y = cY - radius; y <= cY + radius; y++ ) {
                for ( int z = cZ - radius; z <= cZ + radius; z++ ) {
                    if ( (cX - x) * (cX - x) + (cY - y) * (cY - y) + (cZ - z) * (cZ - z) <= radiusSquared ) { 
                    	
                    	Block block = center.getWorld().getBlockAt(x, y, z);
                    	int mc = mats.size()-1;
                    	int threshold = 10;

                    	// Trigger TNT in deadzone radius... or spawn a bunch of spam TNT ugh.
                    	/*
                    	if ( block.getLocation().distance(center) > 1 && TNTManager.containsLocation(block.getLocation()) ) {
                    		
                    		String type = TNTManager.getType(block.getLocation());
                    		Map<String, Object> effect = TNTEffects.getEffect(type);
                    		
                    		block.setType(Material.AIR);
        					TNTManager.createPrimedTNT(type, 
        												block.getLocation(), 
        												(double)(int) effect.get("blastRadius"),
        												(int) effect.get("fuseTicks"), 
        												(Sound) effect.get("soundEffect"), 
        												(float) effect.get("soundEffectPitch"),
        												plugin);
        					
                    	}
                    	*/
                    	
                    	if ( ! ( block.getType().equals(Material.AIR) ) ) {
                    		if ( ! ( pmats.contains(block.getType())) ) {
                    			
                    			if ( doRandom ) {
                    			
                    				if ( randomInteger(0, threshold, new Random()) > 5 ) {
		                    			Material newMaterial = mats.get(randomInteger(0, mc, new Random()));
		                    			block.setType(newMaterial);
                    				}
                    			
                    			} else {
	                    			Material newMaterial = mats.get(randomInteger(0, mc, new Random()));
	                    			block.setType(newMaterial);
                    			}
                    			
                    		}
                    	}
                    	
                    }
                }
            }
        }

	}
	
	public static List<Material> getBlocksInRadius(Location center, int radius, boolean ellipsis) {
		
		List<Material> list = new ArrayList<Material>();
        int cX = center.getBlockX();
        int cY = center.getBlockY();
        int cZ = center.getBlockZ();
        int radiusSquared;
        if ( ellipsis ) {
        	radiusSquared = radius * radius;
        } else {
        	radiusSquared = radius;
        }
        for ( int x = cX - radius; x <= cX + radius; x++ ) {
            for ( int y = cY - radius; y <= cY + radius; y++ ) {
                for ( int z = cZ - radius; z <= cZ + radius; z++ ) {
                    if ( (cX - x) * (cX - x) + (cY - y) * (cY - y) + (cZ - z) * (cZ - z) <= radiusSquared ) { 
                    	list.add(center.getWorld().getBlockAt(x, y, z).getType());
                    }
                }
            }
        }
        
        return list;

	}
	
    public static BlastEffectManager getinstance(BlastRadius plugin) {
        if(instance == null) {
            synchronized (BlastEffectManager.class) {
                instance = new BlastEffectManager(plugin);
            }
        }
        return instance;
    }
	
	public void createSmoke(Location center, Particle type, int count, double offset) {
		if ( count > 15 || count <= 2 ) {
			count = 5;
		}
		if ( offset > 1.5 ) {
			offset = 0.5;
		}
		center.getWorld().spawnParticle(type, center, count, offset, 0, offset);
	}

	public int randomInteger(int aStart, int aEnd, Random aRandom) {
		long range = (long)aEnd - (long)aStart + 1;
		long fraction = (long)(range * aRandom.nextDouble());
		int random = (int)(fraction + aStart);    
		return random;
	}	

}
