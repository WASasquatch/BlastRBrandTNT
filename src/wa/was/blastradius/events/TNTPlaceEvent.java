package wa.was.blastradius.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

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

public class TNTPlaceEvent implements Listener {
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	public TNTPlaceEvent(JavaPlugin plugin) {
		TNTManager = ((BlastRadius)plugin).getTNTLocationManager();
		TNTEffects = ((BlastRadius)plugin).getTNTEffectsManager();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlaceTNT(BlockPlaceEvent e) {
		
		if ( e.isCancelled() || ! ( e.getBlockPlaced().getType().equals(Material.TNT) ) ) return;
		
		Player player = e.getPlayer();
		
		if ( e.getItemInHand().hasItemMeta() ) {
			
			ItemMeta meta = e.getItemInHand().getItemMeta();
			if ( TNTEffects.hasDisplayName(meta.getDisplayName()) ) {
				
				String type = TNTEffects.displayNameToType(meta.getDisplayName());
				Location loc = e.getBlockPlaced().getLocation();
				
				if ( ! ( player.hasPermission("blastradius.place."+type) ) )  {
					e.setCancelled(true);
					return;
				}
				
				TNTManager.addTNT(player, type, loc);
				
				if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
					
					Bukkit.getLogger().info("TNT Type: "+type+" Placed By: "+player.getUniqueId().toString()+" Location: "+loc.getBlockX()+", "+loc.getY()+", "+loc.getZ());
					
				}
				
			}
			
		}
		
	}

}
