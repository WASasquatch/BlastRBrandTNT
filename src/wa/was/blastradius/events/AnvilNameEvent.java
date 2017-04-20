package wa.was.blastradius.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import wa.was.blastradius.BlastRadius;
import wa.was.blastradius.commands.OnCommand;
import wa.was.blastradius.managers.TNTEffectsManager;

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

public class AnvilNameEvent implements Listener {
	
	private TNTEffectsManager TNTEffects;
	private FileConfiguration config;
	
	public AnvilNameEvent() {
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
		config = BlastRadius.getBlastRadiusPluginInstance().getConfig();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onAnvilRename(PrepareAnvilEvent e) {
		
		if ( e.getResult() != null ) {
			
			ItemStack item = e.getResult();
			
			if ( ! ( item.hasItemMeta() ) ) return;
			
			ItemMeta meta = item.getItemMeta();
			
			if ( meta.getDisplayName() == null ) return;
			
			if ( TNTEffects.hasDisplayName(meta.getDisplayName()) ) {
				
				String type = TNTEffects.displayNameToType(meta.getDisplayName());
				item.setType(Material.AIR);
				meta.setDisplayName(null);
				meta.getLore().clear();
				item.setItemMeta(meta);
				
				for ( HumanEntity viewer : e.getViewers() ) {
					Player player =  (Player) viewer;
					if ( player.isValid() && player.isOnline() ) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
					}
				}
				
				if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
					BlastRadius.getBlastRadiusInstance().getLogger().info("PrepareAnvilEvent: Attempted to rename TNT to BlastR Brand type: "+type);
					BlastRadius.getBlastRadiusInstance().getLogger().info("PrepareAnvilEvent: Users involved in event: "+e.getViewers().toString());
				}
				
			}
			
		}
		
		
	}

}
