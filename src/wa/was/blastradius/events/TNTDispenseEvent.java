package wa.was.blastradius.events;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Dispenser;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wa.was.blastradius.BlastRadius;
import wa.was.blastradius.managers.TNTEffectsManager;

/*************************
 * 
 * Copyright (c) 2017 Jordan Thompson (WASasquatch)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 *************************/

public class TNTDispenseEvent implements Listener {

	private TNTEffectsManager TNTEffects;
	private static Dispenser rdisp;

	public TNTDispenseEvent() {
		TNTEffects = BlastRadius.getBlastRadiusInstance().getTNTEffectsManager();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDispense(BlockDispenseEvent e) {
		if (e.isCancelled())
			return;

		Block block = e.getBlock();
		BlockFace face = ((DirectionalContainer)e.getBlock().getState().getData()).getFacing();

		if (!(e.getBlock().getState() instanceof org.bukkit.block.Dispenser)) {
			return;
		}

		rdisp = (Dispenser) e.getBlock().getState();
		Inventory inv = rdisp.getInventory();
		ItemStack item = null;
		ItemMeta meta = null;

		boolean isEmpty = true;
		for (ItemStack fi : inv.getStorageContents()) {
			if (fi != null) {
				if (fi.hasItemMeta()) {
					ItemMeta fim = fi.getItemMeta();
					isEmpty = false;
					if (TNTEffects.hasDisplayName(fim.getDisplayName())) {
						item = fi;
						meta = fim;
						break;
					}
				}
			}
		}

		Boolean doClear = false;
		if (!isEmpty && item == null) {
			return;
		} else if (isEmpty && item == null) {
			if (e.getItem() != null && e.getItem().hasItemMeta()) {
				meta = e.getItem().getItemMeta();
				if (!(TNTEffects.hasDisplayName(meta.getDisplayName()))) {
					return;
				}
				item = e.getItem();
				doClear = true;
			}
		}

		if (item != null && meta != null) {

			e.setCancelled(true);

			String type = TNTEffects.displayNameToType(meta.getDisplayName());
			Map<String, Object> effect = TNTEffects.getEffect(type);

			Location location = block.getRelative(face, 1).getLocation();
			Vector direction = new Vector(face.getModX(), face.getModY(), face.getModZ());

			if (!doClear) {
				rdisp.getInventory().removeItem(item);
			} else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if( rdisp instanceof Dispenser ) {
                            rdisp.getInventory().clear();
                        }
                    }
                }.runTaskLater(BlastRadius.getBlastRadiusPluginInstance(), 1);
			}

			TNTEffects.createPrimedTNT(effect, location, (float) effect.get("yieldMultiplier"),
					(int) effect.get("fuseTicks"), (Sound) effect.get("fuseEffect"),
					(float) effect.get("fuseEffectPitch"), (float) effect.get("fuseEffectPitch"),
					direction.normalize().multiply(0.1), true);

		}

	}

	public ItemStack removeItem(ItemStack item) {
		if (item.getAmount() > 1) {
			item.setAmount(item.getAmount() - 1);
		} else {
			item = null;
		}
		return item;
	}

}
