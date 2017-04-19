package wa.was.blastradius.interfaces;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import wa.was.blastradius.utils.ConsoleColor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

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

public class VaultInterface {
	
    private static Economy econ = null;
    
    public VaultInterface() {
    	implementVault();
    }
    
    public Double getBalance(UUID uuid) {
    	OfflinePlayer player = (OfflinePlayer) Bukkit.getServer().getPlayer(uuid);
    	Double balance = null;
    	if ( player != null ) {
    		if ( econ != null ) {
    			balance = econ.getBalance(player);
    		}
    	}
    	return balance;		
    }
    
    public boolean add(UUID uuid, double amount) {
    	OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
    	if ( player != null ) {
    		if ( econ != null ) {
    			EconomyResponse response = econ.depositPlayer(player, amount);
    			if ( response.transactionSuccess() ) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public boolean has(UUID uuid, double amount) {
    	OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
    	if ( player != null ) {
    		return econ.has(player, amount);
    	}
    	return false;
    }
    
    public boolean withdraw(UUID uuid, double amount) {
    	OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
    	if ( econ.hasAccount(player) ) {
    		EconomyResponse response = econ.withdrawPlayer(player, amount);
    		if ( response.transactionSuccess() ) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public String format(double amount) {
    	return econ.format(amount);
    }
    
    public boolean implementVault() {
        if ( Bukkit.getServer().getPluginManager().getPlugin("Vault") == null ) {
        	Bukkit.getServer().getLogger().severe(ConsoleColor.RED + ConsoleColor.BOLD + "There was a error implementing with Vault" + ConsoleColor.RESET);
        	return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return ( econ != null );
    }
    
}