package wa.was.blastradius.events;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.scheduler.BukkitRunnable;

import wa.was.blastradius.BlastRadius;
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
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *	SOFTWARE.
 *	
 *************************/

public class BlockPhysEvent implements Listener {
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	private static Location iloc;
	
	public BlockPhysEvent() {
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPhysics(BlockPhysicsEvent e) {
		if ( e.isCancelled() ) return;
		Block block = e.getBlock();
		if ( ! ( block.getType().equals(Material.TNT) ) ) return;
		if ( ! ( TNTManager.containsLocation(block.getLocation()) ) ) return;
		String type = TNTManager.getType(block.getLocation());
		if ( ! ( TNTEffects.hasEffect(type) ) ) return;
		Map<String, Object> effect =  TNTEffects.getEffect(type);
		if ( effect == null ) return;
		if ( ! ( block.isBlockPowered() ) || ! ( block.isBlockIndirectlyPowered() ) ) return;
		block.setType(Material.AIR);
	   	TNTEffects.createPrimedTNT(effect, 
	   								block.getLocation(), 
	   								(float) effect.get("yieldMultiplier"), 
	   								(int) effect.get("fuseTicks"), 
	   								(Sound) effect.get("fuseEffect"), 
	   								(float) effect.get("fuseEffectVolume"),
	   								(float) effect.get("fuseEffectPitch"));
		iloc = block.getLocation();
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
