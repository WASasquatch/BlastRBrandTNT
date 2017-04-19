package wa.was.blastradius.events;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

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

public class TNTInteractEvent implements Listener {
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	public TNTInteractEvent() {
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onTNTInteract(PlayerInteractEvent e) {
		
		if ( e.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
				&& e.getClickedBlock().getType().equals(Material.TNT)
					&& e.getItem().getType().equals(Material.FLINT_AND_STEEL)
						&& TNTManager.containsLocation(e.getClickedBlock().getLocation()) ) {
			
			Location location = e.getClickedBlock().getLocation();
			String type = TNTManager.getType(location);
			
			Map<String, Object> effect = TNTEffects.getEffect(type);
			
			e.setCancelled(true);
			e.getClickedBlock().setType(Material.AIR);
			TNTManager.createPrimedTNT(effect, 
										location, 
										(float) effect.get("yieldMultiplier"), 
										(int) effect.get("fuseTicks"), 
										(Sound) effect.get("soundEffect"), 
										(float) effect.get("soundEffectPitch"));
			
		}
		
		if ( e.getAction().equals(Action.RIGHT_CLICK_AIR) ) {
			
			BlastRadius plugin = BlastRadius.getBlastRadiusInstance();
			
			if ( ! ( e.getPlayer().hasPermission("blastradius.toss") ) ) {
				e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', ((JavaPlugin)plugin).getConfig().getString("local.no-permission")));
				return;
			}
			
			ItemStack item = e.getItem();
			
			if ( item.hasItemMeta() ) {
				
				ItemMeta meta = e.getItem().getItemMeta();
				
				if ( TNTEffects.hasDisplayName(meta.getDisplayName()) ) {
					
					if ( ! ( e.getPlayer().hasPermission("blastradius.toss") ) ) {
						e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', ((JavaPlugin)plugin).getConfig().getString("local.no-permission")));
						return;
					}
					
					String type = TNTEffects.displayNameToType(meta.getDisplayName());
					Map<String, Object> effect = TNTEffects.getEffect(type);
					
					if ( effect != null && (boolean) effect.get("tntTossable") ) {
				
						TNTManager.playerTossTNT(effect, e.getPlayer(), (int) effect.get("tossRange"));
						
					}
				
				}
				
			}
			
		}
		
	}

}
