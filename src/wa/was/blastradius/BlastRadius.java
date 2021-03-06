package wa.was.blastradius;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import wa.was.blastradius.events.TNTSpreadEvent;
import wa.was.blastradius.commands.OnCommand;
import wa.was.blastradius.events.AnvilNameEvent;
import wa.was.blastradius.events.TNTExplosionEvent;
import wa.was.blastradius.events.InteractionEvent;
import wa.was.blastradius.events.TNTPrimeEvent;
import wa.was.blastradius.events.TNTProjectileImpactEvent;
// import wa.was.blastradius.events.TNTRedstoneEvent;
import wa.was.blastradius.events.BlockPlacedEvent;
import wa.was.blastradius.events.BlockRemovedEvent;
import wa.was.blastradius.events.TNTDispenseEvent;
import wa.was.blastradius.managers.BlastEffectManager;
import wa.was.blastradius.managers.PotionEffectsManager;
import wa.was.blastradius.managers.TNTEffectsManager;
import wa.was.blastradius.managers.TNTLocationManager;
import wa.was.blastradius.utils.ConsoleColor;

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

public class BlastRadius extends JavaPlugin {
	
	public static boolean doVault = false;
	public static boolean doMessages = false;
	
	private static BlastRadius instance;
	
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	private BlastEffectManager blastManager;
	
	// Ugh
	public static PotionEffectsManager potionManager;
	
	public BlastRadius() {
		
		instance = (BlastRadius) this;
	}
	
	@Override
	public void onDisable() {
		TNTManager.savePlacedTNT();
	}
	
	@Override
	public void onEnable() {
		
		createConfig();
		
		// Setup Managers
		potionManager = PotionEffectsManager.getInstance();
		TNTManager = TNTLocationManager.getInstance();
		TNTEffects = TNTEffectsManager.getInstance();
		blastManager = BlastEffectManager.getinstance(this);	
		
		if ( getConfig().getBoolean("show-player-messages") ) {
			doMessages = true;
		}
		
		if ( getServer().getPluginManager().getPlugin("Vault") != null ) {
			doVault = true;
			getLogger().info(ConsoleColor.YELLOW+ConsoleColor.BOLD+"Found Vault"+ConsoleColor.WHITE+" - "+ConsoleColor.GREEN+ConsoleColor.BOLD+"Implementing with Vault"+ConsoleColor.RESET);
		}
		
		TNTManager.loadPlacedTNT();

		getServer().getPluginManager().registerEvents(new AnvilNameEvent(), this);
		getServer().getPluginManager().registerEvents(new InteractionEvent(this), this);
		getServer().getPluginManager().registerEvents(new BlockPlacedEvent(), this);
		getServer().getPluginManager().registerEvents(new BlockRemovedEvent(), this);
		getServer().getPluginManager().registerEvents(new TNTExplosionEvent(), this);
		getServer().getPluginManager().registerEvents(new TNTSpreadEvent(), this);
		getServer().getPluginManager().registerEvents(new TNTPrimeEvent(), this); // Would love to have a actual prime event
		getServer().getPluginManager().registerEvents(new TNTProjectileImpactEvent(), this);
		getServer().getPluginManager().registerEvents(new TNTDispenseEvent(), this);
		// getServer().getPluginManager().registerEvents(new TNTRedstoneEvent(), this); // Terrible Logic... relying on location manager
		
		getCommand("blastr").setExecutor(new OnCommand(this));
		
	}
	
	public static BlastRadius getBlastRadiusInstance() {
		return (BlastRadius) instance;
	}
	
	public static JavaPlugin getBlastRadiusPluginInstance() {
		return instance;
	}
	
	public TNTLocationManager getTNTLocationManager() {
		return TNTManager;
	}
	
	public TNTEffectsManager getTNTEffectsManager() {
		return TNTEffects;
	}
	
	public PotionEffectsManager getPotionEffectsManager() {
		return potionManager;
	}
	
	public BlastEffectManager getBlastManager() {
		return blastManager;
	}
	
	public static List<String> getVersion() {
		PluginDescriptionFile desc = BlastRadius.getBlastRadiusPluginInstance().getDescription();
		List<String> response = new ArrayList<String>();
		response.add(ChatColor.translateAlternateColorCodes('&', "&6"+desc.getName()+" &r- &3version &r"+desc.getVersion() +" by "+desc.getAuthors()));
		response.add("&6Website: &4"+desc.getWebsite());
		return response;
	}
	
    private void createConfig() {
    	boolean success = false;
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info(ConsoleColor.GREEN+ConsoleColor.BOLD+"Creating configuration..."+ConsoleColor.RESET);
                saveDefaultConfig();
            } else {
                getLogger().info(ConsoleColor.GREEN+ConsoleColor.BOLD+"Loading configuration..."+ConsoleColor.RESET);
            }
            success = true;
        } catch (Exception e) {
            getLogger().severe(ConsoleColor.RED+ConsoleColor.BOLD+"Error creating configuration!"+ConsoleColor.RESET);
            e.printStackTrace();
            success = false;
        }
        if ( success ) {
            getLogger().info(ConsoleColor.GREEN+ConsoleColor.BOLD+"Configuration succesfully loaded."+ConsoleColor.RESET);
        }
    }

}
