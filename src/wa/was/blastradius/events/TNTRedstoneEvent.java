package wa.was.blastradius.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Comparator;
import org.bukkit.material.Diode;

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
	
	public TNTRedstoneEvent() {
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockRedstoneChange(BlockRedstoneEvent e) {

		int newCurrent = e.getNewCurrent();
		int oldCurrent = e.getOldCurrent();
		Block block = e.getBlock();
		
		List<BlockFace> faces = new ArrayList<BlockFace>(){
			private static final long serialVersionUID = -6535364022695998798L; {
			add(BlockFace.DOWN);
			add(BlockFace.UP);
			add(BlockFace.WEST);
			add(BlockFace.EAST);
			add(BlockFace.NORTH);
			add(BlockFace.SOUTH);
		}};

		for ( BlockFace face : faces ) {			

			// Relative is TNT and is placed TNT
			if ( block.getRelative(face, 1).getType().equals(Material.TNT)
					&& TNTManager.containsRelativeLocation(block.getRelative(face, 1).getLocation()) ) {
				
				// Is diode facing wrong direction
				if ( block.getType().equals(Material.DIODE) 
						&& ! ((Diode)block).getFacing().equals(face) ) {
					return;
				}
				
				// Is comparator facing wrong direction or powered off
				if ( block.getType().equals(Material.REDSTONE_COMPARATOR_ON) 
								&& ! ((Comparator)block).getFacing().equals(face) ) {
					return;
				} 

				// Is redstone torch OFF
				if ( block.getType().equals(Material.REDSTONE_TORCH_OFF) ) {
					return;
				}
				
				Block target = block.getRelative(face, 1);
				Location location = target.getLocation();
				String type = TNTManager.getRelativeType(location);
				UUID owner = TNTManager.getRelativeOwner(location);
				
				Map<String, Object> effect = TNTEffects.getEffect(type);
				
				// From low to high
				if ( oldCurrent == 0 && newCurrent >= 1 ) {
					
					target.setType(Material.AIR);
					TNTManager.removeRelativePlayersTNT(owner, location);
					TNTEffects.createPrimedTNT(effect, 
												location, 
												(float) effect.get("yieldMultiplier"), 
												(int) effect.get("fuseTicks"),
												(Sound) effect.get("soundEffect"), 
												(float) effect.get("soundEffectPitch"));
					
					if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
						Bukkit.getLogger().info("BlockRedstoneEvent Material: "+e.getBlock().getType()+" at Location: "+e.getBlock().getLocation());
					}
					
				}
				
			}
		
		}
		
	}

}
