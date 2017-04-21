package wa.was.blastradius.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

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

public class TNTProjectileImpactEvent implements Listener {
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	private static List<EntityType> ignitionTypes;
	
	public TNTProjectileImpactEvent() {
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
		ignitionTypes = new ArrayList<EntityType>(){
			private static final long serialVersionUID = 4318379400467504164L;
			{
				add(EntityType.FIREBALL);
				add(EntityType.FIREWORK);
				add(EntityType.DRAGON_FIREBALL);
				add(EntityType.SHULKER_BULLET);
				add(EntityType.SMALL_FIREBALL);
			}};
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onProjectileHit(ProjectileHitEvent e) {
		
		Projectile projectile = e.getEntity();
		Block target = e.getHitBlock();
				
		if ( target != null && ignitionTypes.contains(projectile.getType()) 
				&& TNTManager.containsLocation(target.getLocation()) ) {
					
			String type = TNTManager.getType(target.getLocation());
					
			if ( TNTEffects.hasEffect(type) ) {
						
				Map<String, Object> effect = TNTEffects.getEffect(type);
						
				target.setType(Material.AIR);
				
				TNTEffects.createPrimedTNT(effect, 
						target.getLocation(), 
						(float) effect.get("yieldMultiplier"), 
						(int) effect.get("fuseTicks"), 
						(Sound) effect.get("fuseEffect"), 
						(float) effect.get("fuseEffectPitch"),
						(float) effect.get("fuseEffectPitch"));
						
				TNTManager.removeTNT(target.getLocation());
						
			}
					
		}	
		
	}
	
	public static boolean addIgnitionType(EntityType type) {
		return ignitionTypes.add(type);
	}
	
	public static boolean hasIgnitionType(EntityType type) {
		return ignitionTypes.contains(type);
	}
	
	public static boolean removeIgnitionType(EntityType type) {
		if ( ignitionTypes.contains(type) ) {
			return ignitionTypes.remove(type);
		}
		return false;
	}
	
	public static void clearIgnitionTypes() {
		ignitionTypes.clear();
	}

}
