package wa.was.blastradius.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

public class TNTInteractOrTossEvent implements Listener {
	
	private JavaPlugin plugin;
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	private Map<UUID, Long> cooldowns;
	
	public TNTInteractOrTossEvent(JavaPlugin plugin) {
		this.plugin = plugin;
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
		cooldowns = new HashMap<UUID, Long>();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onTNTInteract(PlayerInteractEvent e) {
		
		if ( e.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
				&& e.getClickedBlock().getType().equals(Material.TNT)
					&& ( e.getItem() != null && e.getItem().getType().equals(Material.FLINT_AND_STEEL) )
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
			
			plugin.getLogger().info("Here 1");
			
			if ( ! ( e.getPlayer().hasPermission("blastradius.toss") ) ) {
				e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("local.no-permission")));
				return;
			}
			
			plugin.getLogger().info("Here 2");
			
			ItemStack item = e.getItem();
			
			if ( item.hasItemMeta() ) {
				
				plugin.getLogger().info("Here 3 has Meta");
				
				ItemMeta meta = e.getItem().getItemMeta();
				
				if ( TNTEffects.hasDisplayName(meta.getDisplayName()) ) {
					
					plugin.getLogger().info("Here 4 Has DisplayName");
					
					String type = TNTEffects.displayNameToType(meta.getDisplayName());
					Map<String, Object> effect = TNTEffects.getEffect(type);
					
					if ( effect != null && (boolean) effect.get("tntTossable") ) {
						
						plugin.getLogger().info("Here 5 Tossable");
						
						if ( cooldowns.containsKey(e.getPlayer().getUniqueId()) ) {
							if ( cooldowns.get(e.getPlayer().getUniqueId()) > System.currentTimeMillis() ) {
								plugin.getLogger().info("Here Waiting for Cooldown");
								long timeDiff = TimeUnit.MILLISECONDS.toMinutes(cooldowns.get(e.getPlayer().getUniqueId()) - System.currentTimeMillis());
								e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', 
											plugin.getConfig().getString("local.toss-cooldown")
											.replace("{COUNT}", ""+timeDiff)
											.replace("{TYPE}", (String) effect.get("displayName"))));
								return;
							} else if ( cooldowns.get(e.getPlayer().getUniqueId()) < System.currentTimeMillis() ) {
								plugin.getLogger().info("Here Removing Cooldown");
								cooldowns.remove(e.getPlayer().getUniqueId());
							}
						}
						plugin.getLogger().info("Here Tossing TNT");
				
						TNTManager.playerTossTNT(effect, e.getPlayer(), (int) effect.get("tossRange"));
						cooldowns.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (int) effect.get("tossCooldown") * 1000);
						
					}
				
				}
				
			}
			
		}
		
	}

}
