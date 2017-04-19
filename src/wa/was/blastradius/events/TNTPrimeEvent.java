package wa.was.blastradius.events;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;

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

public class TNTPrimeEvent implements Listener {
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	public TNTPrimeEvent() {
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	public void onExplosionPrime(ExplosionPrimeEvent e) {

	    if ( ! ( e.getEntity() instanceof TNTPrimed ) 
	    		|| ( e.getEntity() instanceof TNTPrimed 
	    				&& e.getEntity().hasMetadata("tntType") ) )
	    	return;
		
	    TNTPrimed tnt = (TNTPrimed) e.getEntity();
	    Location location = tnt.getLocation();
			    
	    if ( TNTManager.containsRelativeLocation(location) ) {
	    	
	    	String type = TNTManager.getRelativeType(location);
	    	
	    	if ( type != null ) {
	    		
	    		Map<String, Object> effect = TNTEffects.getEffect(type);
	    		UUID owner = TNTManager.getRelativeOwner(location);
	    		OfflinePlayer player = Bukkit.getServer().getPlayer(owner);
	    		
	    		if ( (int) effect.get("fuseTicks") > 40 ) {
		    		e.setCancelled(true);
		    		tnt.remove();
	    		}
	    		
	    		// Calculate new ticks
	    		int ticks = ( ( effect.get("fuseTicks") != null ) ? 
	    					( ( (int) effect.get("fuseTicks") > 40 ) ? 
	    					(int) effect.get("fuseTicks") - 40 : 1 ) : 1 );
	    		
		    	TNTPrimed blastRTNT = TNTManager.createPrimedTNT(effect, 
		    													location, 
		    													(float) effect.get("yieldMultiplier"), 
		    													ticks, 
		    													(Sound) effect.get("soundEffect"), 
		    													(float) effect.get("soundEffectPitch"));
		    	blastRTNT.setVelocity(new Vector(0, 0, 0));
		    	
				if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
					if ( player != null ) {
						Bukkit.getLogger().info("Created BlastR Brand ("+type.toUpperCase()+") at: "+blastRTNT.getLocation().toString()+" By Player: ("+player.getName()+") "+owner);
					} else {
						Bukkit.getLogger().info("Created BlastR Brand ("+type.toUpperCase()+") at: "+blastRTNT.getLocation().toString()+" By Player: "+owner);
					}
				}
		    	
	    	}
	    	
	    }
	    
		if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
			Bukkit.getLogger().info("ExplosionPrimeEvent: "+location.toString());
		}
	    
	}

}
