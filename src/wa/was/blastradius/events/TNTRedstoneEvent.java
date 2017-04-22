package wa.was.blastradius.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.scheduler.BukkitRunnable;

import wa.was.blastradius.BlastRadius;
import wa.was.blastradius.commands.OnCommand;
import wa.was.blastradius.managers.TNTEffectsManager;
import wa.was.blastradius.managers.TNTLocationManager;

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
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO e SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *	SOFTWARE.
 *	
 *************************/

public class TNTRedstoneEvent implements Listener {
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	private static Location iloc;
	
	public TNTRedstoneEvent() {
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockRedstoneChange(BlockRedstoneEvent e) {

		// Currents
		int newCurrent = e.getNewCurrent();
		int oldCurrent = e.getOldCurrent();
		
		// Direct blocks...
		List<BlockFace> faces = new ArrayList<BlockFace>(){
			private static final long serialVersionUID = -6535364022695998798L; {
			add(BlockFace.UP);
			add(BlockFace.DOWN);
			add(BlockFace.WEST);
			add(BlockFace.EAST);
			add(BlockFace.NORTH);
			add(BlockFace.SOUTH);
		}};
		
		// Indirect extensions...
		List<BlockFace> ifaces = new ArrayList<BlockFace>(){
			private static final long serialVersionUID = -6535364022695998798L; {
			add(BlockFace.UP);
			add(BlockFace.DOWN);
		}};
		
		List<Material> invalid =  new ArrayList<Material>(){
			private static final long serialVersionUID = -8217516299686143670L; {
			add(Material.LEVER);
			add(Material.STONE_BUTTON);
			add(Material.WOOD_BUTTON);
		}};

		// Loop direct faces and their direct faces...
		for ( BlockFace face : faces ) {
			
			Block block = e.getBlock().getRelative(face, 1);
			
			// Loop indirect extensions first
			for ( BlockFace iface : ifaces ) {
				
				Block iblock = block.getRelative(iface, 1);
				
				// is powered block TNT
				if ( iblock.getType().equals(Material.TNT) 
						&& oldCurrent == 0 && newCurrent >= 1 
							&& ! ( invalid.contains(iblock.getType()) ) ) {
					
					Block below = iblock.getRelative(BlockFace.DOWN, 1);
					Block above = iblock.getRelative(BlockFace.UP, 1);
					
					if ( ( below.equals(Material.REDSTONE) && below.getBlockPower() >= 1 )
							|| ( above.equals(Material.REDSTONE) && above.getBlockPower() >= 1 ) ) {
						break;
					}
				
					if ( TNTManager.containsLocation(iblock.getLocation()) 
							&& TNTEffects.hasEffect(TNTManager.getType(iblock.getLocation())) ) {
						
						String type = TNTManager.getType(iblock.getLocation());
						Map<String, Object> effect = TNTEffects.getEffect(type);
						
						if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
							Bukkit.getLogger().info("BlockRedstoneEvent: "+face+" indirectly powering "+iface+" | TNT triggered at location: "+iblock.getLocation()+" Effect Type: "+type);
						}
						
						if ( effect != null ) {
							
							iloc = iblock.getLocation();
							block.setType(Material.AIR);
							primeBlock(effect, iblock.getLocation());
					        
						}
					}
					
				}
				
			}
			
			// Is powered block TNT
			if ( block.getType().equals(Material.TNT) 
					&& oldCurrent == 0 && newCurrent >= 1 ) {
				
				Block below = block.getRelative(BlockFace.DOWN, 1);
				Block above = block.getRelative(BlockFace.UP, 1);
				
				if ( ( below.equals(Material.REDSTONE) && below.getBlockPower() >= 1 )
						|| ( above.equals(Material.REDSTONE) && above.getBlockPower() >= 1 ) ) {
					break;
				}

				if ( TNTManager.containsLocation(block.getLocation()) 
						&& TNTEffects.hasEffect(TNTManager.getType(block.getLocation())) ) {
					
					String type = TNTManager.getType(block.getLocation());
					Map<String, Object> effect = TNTEffects.getEffect(type);
					
					if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
						Bukkit.getLogger().info("BlockRedstoneEvent: "+face+" powering | TNT triggered at location: "+block.getLocation()+" Effect Type: "+type);
					}
		
					if ( effect != null ) {
						
						iloc = block.getLocation();
						block.setType(Material.AIR);
						primeBlock(effect, block.getLocation());
				        
					}
				}
			}
		}
		
		iloc = null;
		
	}
	
	public void primeBlock(Map<String, Object> effect, Location location) {
		
	   	TNTEffects.createPrimedTNT(effect, 
	   								location, 
	   								(float) effect.get("yieldMultiplier"), 
	   								(int) effect.get("fuseTicks"), 
	   								(Sound) effect.get("fuseEffect"), 
	   								(float) effect.get("fuseEffectVolume"),
	   								(float) effect.get("fuseEffectPitch"));
	   	
		
        new BukkitRunnable() {
            @Override
            public void run() {
                if( iloc != null && TNTEffects.getEntitiesInChunks(iloc, 1).size() > 0 ) {
                	for ( Entity entity : TNTEffects.getEntitiesInChunks(iloc, 1) ) {
                		if ( entity instanceof TNTPrimed ) {
                			if ( ! ( ((TNTPrimed)entity).hasMetadata("tntType") ) 
                					&& entity.getLocation().distanceSquared(iloc) < 1 ) {
                				entity.remove();
                			}
                		}
                	}
                	iloc = null;
                }
            }
        }.runTaskLater(BlastRadius.getBlastRadiusPluginInstance(), 1);
        
	}
	
}
