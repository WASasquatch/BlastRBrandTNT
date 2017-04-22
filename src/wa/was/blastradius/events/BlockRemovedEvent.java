package wa.was.blastradius.events;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

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
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *	SOFTWARE.
 *	
 *************************/

public class BlockRemovedEvent implements Listener {
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	public BlockRemovedEvent() {
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockRemove(BlockBreakEvent e) {
		if ( e.isCancelled() ) return;
		
		Block block = e.getBlock();
		Location location = block.getLocation();
		Player player = e.getPlayer();
		
		if ( e.getBlock().getType().equals(Material.TNT)
					&& TNTManager.containsLocation(location) ) {
			
			UUID uuid = TNTManager.getOwner(location);
			String type = TNTManager.getType(location);
			Map<String, Object> effect = TNTEffects.getEffect(type);
				
			if ( (boolean) effect.get("tamperProof") 
					&& player instanceof Player 
						&& ! ( player.getUniqueId().equals(uuid) ) ) {
					
				block.setType(Material.AIR);
				TNTManager.removePlayersTNT(uuid, location, type);
				TNTEffects.createPrimedTNT(effect, 
											location, 
											(float) effect.get("yieldMultiplier"), 
											(int) effect.get("fuseTicks"), 
											(Sound) effect.get("fuseEffect"), 
											(float) effect.get("fuseEffectPitch"),
											(float) effect.get("fuseEffectPitch"));
				
				e.setCancelled(true);
					
			} else {
					
				TNTManager.removePlayersTNT(uuid, location, type);
				
				ItemStack tnt = TNTEffects.createTNT(effect, 1);
				
				location.getWorld().dropItemNaturally(location, tnt);
				block.setType(Material.AIR);
				
				e.setCancelled(true);
					
			}
			
			if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
				
				Bukkit.getLogger().info("TNT Type: "+type+" Removed By: "+uuid.toString()+" Location: "+location.getBlockX()+", "+location.getY()+", "+location.getZ());
				
			}
			
		}
		
	}

}
