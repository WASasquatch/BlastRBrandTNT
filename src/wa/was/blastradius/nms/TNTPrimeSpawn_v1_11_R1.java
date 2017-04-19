package wa.was.blastradius.nms;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.BlockStateBoolean;
import net.minecraft.server.v1_11_R1.BlockTNT;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EntityTNTPrimed;
import net.minecraft.server.v1_11_R1.Explosion;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.World;
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
 *
 *	This is apparently a useless endeavor as well...
 *
 *	
 *************************/

public class TNTPrimeSpawn_v1_11_R1 extends BlockTNT {
	
	private BlastRadius plugin;
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	private Logger logger;
	
    public static final BlockStateBoolean EXPLODE = BlockStateBoolean.of("explode");
	
	public TNTPrimeSpawn_v1_11_R1() {
		super();
		plugin = BlastRadius.getBlastRadiusInstance();
		TNTManager = plugin.getTNTLocationManager();
		TNTEffects = plugin.getTNTEffectsManager();
		logger = plugin.getLogger();
	}
	
	@Override
	public void wasExploded(World world, BlockPosition blockposition, Explosion explosion) {
		if ( ! ( world.isClientSide ) ) {
			Location location = new Location((org.bukkit.World) world, blockposition.getX(), blockposition.getY(), blockposition.getZ());
			if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
				logger.info("Creating Primed TNT at: "+location.getX()+", "+location.getY()+", "+location.getZ()+ " ...");
			}
			EntityTNTPrimed tnt = new EntityTNTPrimed(world, (double) ((float) blockposition.getX() + 0.5F), (double) blockposition.getY(), (double) ((float) blockposition.getZ() + 0.5F), explosion.getSource());
			tnt.setFuseTicks(world.random.nextInt(tnt.getFuseTicks() / 4) + tnt.getFuseTicks() / 8);
			if ( TNTManager.containsRelativeLocation(location) ) {
				String type = TNTManager.getRelativeType(location);
				Map<String, Object> effect = TNTEffects.getEffect(type);
				tnt.setFuseTicks((int) effect.get("fuseTicks"));
				((TNTPrimed)tnt).setYield(((TNTPrimed)tnt).getYield() * (float) effect.get("yieldMultiplier"));
				((TNTPrimed)tnt).setMetadata("tntType", new FixedMetadataValue(((JavaPlugin)plugin), type));
				if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
					logger.info("Created Primed TNT with Effect Type: "+type);
				}
			}
			world.addEntity(tnt);
		}
		super.wasExploded(world, blockposition, explosion);
	}
	
	@Override
	public void a(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving) {
		if ( ! ( world.isClientSide ) ) {
			if ( ((Boolean)iblockdata.get(BlockTNT.EXPLODE)).booleanValue() ) {
				Location location = new Location((org.bukkit.World) world, blockposition.getX(), blockposition.getY(), blockposition.getZ());
				if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
					logger.info("Creating Primed TNT at: "+location.getX()+", "+location.getY()+", "+location.getZ()+ " ...");
				}
				EntityTNTPrimed tnt = new EntityTNTPrimed(world, (double) ((float) blockposition.getX() + 0.5F), (double) blockposition.getY(), (double) ((float) blockposition.getZ() + 0.5F), entityliving);
				tnt.setFuseTicks(world.random.nextInt(tnt.getFuseTicks() / 4) + tnt.getFuseTicks() / 8);
				if ( TNTManager.containsRelativeLocation(location) ) {
					String type = TNTManager.getRelativeType(location);
					Map<String, Object> effect = TNTEffects.getEffect(type);
					tnt.setFuseTicks((int) effect.get("fuseTicks"));
					((TNTPrimed)tnt).setYield(((TNTPrimed)tnt).getYield() * (float) effect.get("yieldMultiplier"));
					((TNTPrimed)tnt).setMetadata("tntType", new FixedMetadataValue(((JavaPlugin)plugin), type));
					if ( OnCommand.toggleDebug != null && OnCommand.toggleDebug ) {
						logger.info("Created Primed TNT with Effect Type: "+type);
					}
				}
				world.addEntity(tnt);
			}
		}
		super.a(world, blockposition, iblockdata, entityliving);
	}

}