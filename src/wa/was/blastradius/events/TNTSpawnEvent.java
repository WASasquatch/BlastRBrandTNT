package wa.was.blastradius.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import wa.was.blastradius.commands.OnCommand;

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

public class TNTSpawnEvent implements Listener {
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onTNTSpawn(EntitySpawnEvent e) {
		if ( e.getEntityType().equals(EntityType.PRIMED_TNT) 
				|| e.getEntity() instanceof TNTPrimed ) {
			
            if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
            	
            	Bukkit.getLogger().info("Detecting TNT Spawn Event: "+e.getEntity().getLocation());
            	
            }
			
		}
		
		if ( e.getEntity().getClass().equals(TNTPrimed.class) ) { 
			
            if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
            	
            	Bukkit.getLogger().info("Detecting TNT Spawn Even via assignable classt: "+e.getEntity().getLocation());
            	
            }
			
		}
		
	}

}
