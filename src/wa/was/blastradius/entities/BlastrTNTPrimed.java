package wa.was.blastradius.entities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import net.minecraft.server.v1_11_R1.EntityInsentient;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EntityTNTPrimed;
import net.minecraft.server.v1_11_R1.EntityTypes;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagString;
import net.minecraft.server.v1_11_R1.World;
import wa.was.blastradius.BlastRadius;
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

public class BlastrTNTPrimed extends EntityTNTPrimed {

	private double x, y, z;
	private World world;
	
	private TNTLocationManager TNTManager;
	
	public BlastrTNTPrimed(World world, double d0, double d1, double d2, EntityLiving entityliving) {
		super(world, d0, d1, d2, entityliving);
		this.x = d0; this.y = d1; this.z = d2;
		this.world = world;
		TNTManager = BlastRadius.getBlastRadiusInstance().getTNTLocationManager();
		System.out.print("Created BlastrTNTPrimed! You did it man!");
	}

	@Override
	protected void b(NBTTagCompound nbttagcompound) {
		nbttagcompound.setByte("Fuse", (byte) this.getFuseTicks());
		Location loc = new Location(world.getWorld(), x, y, z);
		if ( TNTManager.containsRelativeLocation(loc) ) {
			String type = TNTManager.getType(loc);
			System.out.print("Added NBT Tag to Entity!");
			nbttagcompound.set("tntType", new NBTTagString(type));
		}
	}

	@Override
	protected void a(NBTTagCompound nbttagcompound) {
		this.setFuseTicks(nbttagcompound.getByte("Fuse"));
	}
	
	public static void registerEntity(String tname, int tid, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass){
        try {
     
            List<Map<?, ?>> dataMap = new ArrayList<Map<?, ?>>();
            for (Field f : EntityTypes.class.getDeclaredFields()){
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())){
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }
     
            if (dataMap.get(2).containsKey(tid)){
                dataMap.get(0).remove(tname);
                dataMap.get(2).remove(tid);
            }
     
            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, tname, tid);
     
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
}