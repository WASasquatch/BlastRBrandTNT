package wa.was.blastradius.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
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

public class InteractionEvent implements Listener {
	
	private JavaPlugin plugin;
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	private Map<UUID, Long> cooldowns;
	
	private static Location iloc;
	
	public InteractionEvent(JavaPlugin plugin) {
		this.plugin = plugin;
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
		cooldowns = new HashMap<UUID, Long>();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onTNTInteract(PlayerInteractEvent e) {
		
		List<Material> ignitionTypes = new ArrayList<Material>(){
			private static final long serialVersionUID = 1304111693270154131L; {
			add(Material.REDSTONE_BLOCK);
			add(Material.REDSTONE_TORCH_ON);
		}};
		
		Player player = (Player) e.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		
		if ( e.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
				&& e.getClickedBlock().getType().equals(Material.TNT)
					&& ( item != null && ( item.getType().equals(Material.FLINT_AND_STEEL)
							|| item.getType().equals(Material.FIREBALL) ) )
						&& TNTManager.containsLocation(e.getClickedBlock().getLocation()) ) {
			
			Location location = e.getClickedBlock().getLocation();
			String type = TNTManager.getType(location);
			
			Map<String, Object> effect = TNTEffects.getEffect(type);
			
			e.setCancelled(true);
			e.getClickedBlock().setType(Material.AIR);
			TNTEffects.createPrimedTNT(effect, 
										location, 
										(float) effect.get("yieldMultiplier"), 
										(int) effect.get("fuseTicks"), 
										(Sound) effect.get("fuseEffect"), 
										(float) effect.get("fuseEffectPitch"),
										(float) effect.get("fuseEffectPitch"));
			TNTManager.removeTNT(location);
			
		} else if ( e.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
					&& e.getClickedBlock() != null 
						&& ignitionTypes.contains(e.getClickedBlock().getType()) ) {
			
			if ( e.getItem() != null && e.getItem().hasItemMeta() ) {
				
				ItemMeta meta = e.getItem().getItemMeta();
				
				if ( TNTEffects.hasDisplayName(meta.getDisplayName()) ) {
			
					Location location = e.getClickedBlock().getRelative(e.getBlockFace(), 1).getLocation();
					String type = TNTEffects.displayNameToType(meta.getDisplayName());
							
					Map<String, Object> effect = TNTEffects.getEffect(type);
					
					if ( effect != null ) {
							
						TNTEffects.createPrimedTNT(effect, 
													location, 
													(float) effect.get("yieldMultiplier"), 
													(int) effect.get("fuseTicks"),
													(Sound) effect.get("fuseEffect"), 
													(float) effect.get("fuseEffectPitch"),
													(float) effect.get("fuseEffectPitch"));
						
						if ( ! ( e.getPlayer().getGameMode().equals(GameMode.CREATIVE) ) ) {
							removeMainHand(e.getPlayer());
						}
						
						iloc = location;
						
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
						
						e.setCancelled(true);
						return;
					
					}
				
				}
			
			}
			
		} else if ( e.getAction().equals(Action.RIGHT_CLICK_AIR) 
				|| e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ) {
			
			if ( item != null && item.hasItemMeta() ) {
				
				ItemMeta meta = item.getItemMeta();
					
				if ( TNTEffects.isRemoteDetonator(item) && meta.getDisplayName() != null ) {
						
					String type = TNTEffects.remoteDetonatorsType(meta.getDisplayName());
						
					if ( TNTEffects.hasEffect(type) ) {

						Map<String, Object> effect = TNTEffects.getEffect(type);
						
						if ( ! ( (boolean) effect.get("remoteDetonation") ) ) {
							e.setCancelled(true);
							return;
						}

						List<Location> locations = TNTManager.getPlayerLocationsByType(e.getPlayer().getUniqueId(), type);
							
						e.getPlayer().playSound(e.getPlayer().getLocation(), 
												(Sound) effect.get("detonatorEffect"),
												(float) effect.get("detonatorEffectVolume"), 
												(float) effect.get("detonatorEffectPitch"));
							
						if ( locations.size() > 0 ) {
							for ( Location location : locations ) {
									
								if ( ! ( location.getChunk().isLoaded() ) 
										|| ! ( location.getBlock().getType().equals(Material.TNT) ) ) continue;
									
								Block block = location.getBlock();
								block.setType(Material.AIR);
						    		
							   	TNTEffects.createPrimedTNT(effect, 
							   								location, 
							   								(float) effect.get("yieldMultiplier"), 
							   								(int) effect.get("fuseTicks"), 
							   								(Sound) effect.get("fuseEffect"), 
							   								(float) effect.get("fuseEffectVolume"),
							   								(float) effect.get("fuseEffectPitch"));
							   	
							}
						}
						
					}
						
					e.setCancelled(true);
					return;
					
				} else if ( TNTEffects.hasDisplayName(meta.getDisplayName()) ) {
					
					String type = TNTEffects.displayNameToType(meta.getDisplayName());
					Map<String, Object> effect = TNTEffects.getEffect(type);
					
					if ( effect != null && (boolean) effect.get("tntTossable") ) {
						
						if ( ! ( e.getPlayer().hasPermission("blastradius.toss") ) ) {
							e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("local.no-permission")));
							return;
						}
						
						if ( cooldowns.containsKey(e.getPlayer().getUniqueId()) ) {
							
							if ( cooldowns.get(e.getPlayer().getUniqueId()) >= System.currentTimeMillis() ) {
								
								long timeDiff = TimeUnit.MILLISECONDS.toSeconds(cooldowns.get(e.getPlayer().getUniqueId()) - System.currentTimeMillis());
								
								e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', 
											plugin.getConfig().getString("local.toss-cooldown")
											.replace("{COUNT}", ""+timeDiff)
											.replace("{TYPE}", (String) effect.get("displayName"))));
								return;
								
							} else {
								
								cooldowns.remove(e.getPlayer().getUniqueId());
								
							}
				
						}
				
						e.setCancelled(true);
						TNTEffects.playerTossTNT(effect, e.getPlayer());
						if ( ! ( e.getPlayer().getGameMode().equals(GameMode.CREATIVE) ) ) {
							removeMainHand(e.getPlayer());
						}
						cooldowns.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (int) effect.get("tossCooldown") * 1000);
						return;
						
					}
				
				}
				
			}
			
		}
		
	}
	
	public void removeMainHand(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if ( item.getAmount() > 1 ) {
			item.setAmount(item.getAmount() - 1);
		} else {
			item = null;
		}
		player.getInventory().setItemInMainHand(item);
		player.updateInventory();
	}

}
