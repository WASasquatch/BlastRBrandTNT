package wa.was.blastradius.events;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import wa.was.blastradius.BlastRadius;
import wa.was.blastradius.commands.OnCommand;
import wa.was.blastradius.managers.BlastEffectManager;
import wa.was.blastradius.managers.PotionEffectsManager;
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

public class TNTExplosionEvent implements Listener {
	
	private static JavaPlugin plugin;
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	private PotionEffectsManager potionManager;
	private BlastEffectManager blastManager;
	
	public TNTExplosionEvent(JavaPlugin plug) {
		plugin = plug;
		TNTManager = ((BlastRadius)plugin).getTNTLocationManager();
		TNTEffects = ((BlastRadius)plugin).getTNTEffectsManager();
		potionManager = ((BlastRadius)plugin).getPotionEffectsManager();
		blastManager = ((BlastRadius)plugin).getBlastManager();
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void tntExplosion(EntityExplodeEvent e) {
		
		if ( ! ( e.getEntity() instanceof TNTPrimed ) || e.isCancelled() ) return;
		
		String type = "";
		Location location = e.getEntity().getLocation();
		TNTPrimed tnt = (TNTPrimed) e.getEntity();
		
		if ( tnt.hasMetadata("tntType") ) {
			
			MetadataValue meta = tnt.getMetadata("tntType").get(0);
			type = meta.asString();
			
			if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
				Bukkit.getLogger().info("EntityExplodeEvent: "+tnt.getName()+" found with MetaData Type: "+meta.asString());
			}
			
		} else if ( TNTManager.containsRelativeLocation(location) ) {
			
			type = TNTManager.getRelativeType(location);
			
			if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
				Bukkit.getLogger().info("EntityExplodeEvent: "+tnt.getName()+" found with Location based Type: "+type);
			}
			
		}
		
		if ( TNTEffects.hasEffect(type) ) {
			
			if ( plugin.getConfig().getBoolean("notify-owner") ) {
				Player owner = Bukkit.getServer().getPlayer(TNTManager.getOwner(location));
				if ( owner != null && owner.isOnline() ) {
					owner.sendMessage(ChatColor.translateAlternateColorCodes('&', 
							plugin.getConfig().getString("notify-message")
								.replace("{TYPE}", type)
								.replace("{LOCATION}", location.getX()+", "+location.getY()+", "+location.getZ())));

				}
			}
			
			Map<String, Object> effect = TNTEffects.getEffect(type);
			
			TNTManager.removeRelativePlayersTNT(TNTManager.getOwner(location), location);
			
			blastManager.createBlastRadius(location, 
											(List<Material>) effect.get("innerMaterials"), 
											(List<Material>) effect.get("outerMaterials"), 
											(List<Material>) effect.get("protectedMaterials"), 
											(List<Material>) effect.get("obliterateMaterials"), 
											(boolean) effect.get("obliterate"), 
											(boolean) effect.get("doFires"), 
											(boolean) effect.get("doSmoke"), 
											(int) effect.get("smokeCount"), 
											(double) effect.get("smokeOffset"),  
											Integer.parseInt(""+effect.get("blastRadius")), 
											(int) effect.get("fireRadius"),
											(boolean) effect.get("ellipsis"));
			
			
			potionManager.createPlayerSet("explosion-event");
			potionManager.addPlayersInRadius("explosion-event", location, (int) effect.get("blastRadius"), (boolean) effect.get("ellipsis"));
			potionManager.applySetToPlayers(type, "explosion-event");
			
		}
		
		return;
		
	}

}
