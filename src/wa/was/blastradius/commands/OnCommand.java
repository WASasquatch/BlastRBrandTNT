package wa.was.blastradius.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import wa.was.blastradius.BlastRadius;
import wa.was.blastradius.interfaces.VaultInterface;
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

public class OnCommand implements CommandExecutor {
	
	private FileConfiguration config;
	private VaultInterface vault;
	private TNTLocationManager TNTManager;
	private TNTEffectsManager TNTEffects;
	
	private static Integer itemsRemoved;
	public static Boolean toggleDebug;
	
	public OnCommand(JavaPlugin plugin) {
		this.config = plugin.getConfig();
		vault = new VaultInterface();
		TNTManager = ((BlastRadius)plugin).getTNTLocationManager();
		TNTEffects = ((BlastRadius)plugin).getTNTEffectsManager();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String tag, String[] args) {
		
		Player player = null;
		if ( sender instanceof Player ) {
			player = (Player) sender;
		}
		
		if ( args.length > 0 && args[0] != null ) {
	
			switch(args[0].toLowerCase()) {
			
				case "buy":
					
					if ( ! ( sender instanceof Player ) ) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-console")));
						return true;
					}
					
					if ( args.length > 1 && args[1] != null ) {
						
						String type = args[1].toUpperCase();
						
						if ( ! ( player.hasPermission("blastradius.buy") ) ) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
							return true;
						}
						
						if ( TNTEffects.hasEffect(type) ) {
							
							Map<String, Object> effect = TNTEffects.getEffect(type);
							
							if ( ! ( (boolean) effect.get("tntReceivable") ) ) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
								return true;
							}
						
							int amount = 1;
							
							if ( args.length > 2 && args[2] != null ) {
								if ( args[2].matches("\\d+") ) {
									if ( Integer.parseInt(args[2]) >= 1 && Integer.parseInt(args[2]) <= 64 ) {
										amount = Integer.parseInt(args[2]);
									} else {
										sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.amount-to-large")));
										return true;
									}
								}
							}
							
							if ( vault.has(player.getUniqueId(), ( (double)effect.get("vaultCost")*amount) ) ) {
								
								String price = config.getString("local.free", "&2FREE&r");
								if ( (double)effect.get("vaultCost") > 0 ) {
									vault.withdraw(player.getUniqueId(), ( (double) effect.get("vaultCost")*amount ));
									price = config.getString("price-format", "&2")+vault.format((double)effect.get("vaultCost")*amount);
								}
								
								ItemStack tnt = new ItemStack(Material.TNT, amount);
								ItemMeta tntMeta = tnt.getItemMeta();
								tntMeta.setDisplayName((String) effect.get("displayName"));
								tntMeta.setLore((List<String>) effect.get("lore"));
								tnt.setItemMeta(tntMeta);
								
								if ( (boolean) effect.get("remoteDetonation") ) {
									ItemStack detonator = new ItemStack((Material) effect.get("remoteDetonator"), 1);
									ItemMeta detMeta = detonator.getItemMeta();
									detMeta.setDisplayName((String) effect.get("remoteDetonatorName"));
									detonator.setItemMeta(detMeta);
									player.getInventory().addItem(detonator);
								}
								
								player.getInventory().addItem(tnt);
								player.updateInventory();
								
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.purchase-success")
										.replace("{AMOUNT}", ""+amount)
										.replace("{TYPE}", (String) effect.get("displayName"))
										.replace("{PRICE}", price)));

							} else {
								
								String price = config.getString("price-format", "&2")+vault.format((double)effect.get("vaultCost")*amount);
								
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.purchase-fail")
										.replace("{AMOUNT}", ""+amount)
										.replace("{TYPE}", (String) effect.get("displayName"))
										.replace("{PRICE}", price)));
								
							}
							
						} else {
							
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.invalid-effect")
										.replace("{TYPE}", type)));
							
						}
						
					}
					
					return true;
					
				case "sell":
					
					if ( ! ( sender instanceof Player ) ) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-console")));
						return true;
					}
					
					if ( args.length > 1 && args[1] != null ) {
						
						String type = args[1].toUpperCase();
						
						if ( ! ( player.hasPermission("blastradius.sell") ) ) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
							return true;
						}
						
						int amount = 1;
						
						if ( args.length > 2 && args[2] != null ) {
							if ( args[2].matches("\\d+") ) {
								if ( Integer.parseInt(args[2]) >= 1 && Integer.parseInt(args[2]) <= 64 ) {
									amount = Integer.parseInt(args[2]);
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.amount-to-large-total")));
									return true;
								}
							}
						}
						
						if ( TNTEffects.hasEffect(type) ) {
							
							Map<String, Object> effect = TNTEffects.getEffect(type);
							
							if ( ! ( (boolean) effect.get("tntReceivable") ) ) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
								return true;
							}
							
							if ( ! ( hasEnoughOfType(player, (String) effect.get("displayName"), amount) ) ) {
								
								double worth = ((double) effect.get("vaultWorth") * amount );
								
								if ( removeType(player, (String) effect.get("displayName"), amount) ) {
									vault.add(player.getUniqueId(), worth);
								} else {
									worth = ((double) effect.get("vaultWorth") * itemsRemoved );
									amount = itemsRemoved;
									vault.add(player.getUniqueId(), worth);
									itemsRemoved = null;
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.problem-removing-items")));
								}
								
								String received = config.getString("price-format", "&2")+vault.format(worth);
								
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.sold-success")
										.replace("{AMOUNT}", ""+amount)
										.replace("{TYPE}", (String) effect.get("displayName"))
										.replace("{PRICE}", received)));
								
							} else {
								
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.sold-fail")
										.replace("{AMOUNT}", ""+amount)
										.replace("{TYPE}", (String) effect.get("displayName"))));
								
							}
							
						} else {
							
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.invalid-effect")
									.replace("{TYPE}", type)));
							
						}
					
					} else {
						
						return false;
						
					}
					
					return true;
					
				case "give":
					
					if ( args.length > 1 && args[1] != null ) {
						
						String type = args[2].toUpperCase();
						
						if ( player != null && ! ( player.hasPermission("blastradius.give") ) ) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
							return true;
						}
						
						Player target = Bukkit.getServer().getPlayer(args[1]);
						
						if ( target == null ) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.target-not-online")
									.replace("{PLAYER}", args[1])));
							return true;
						}
						
						if ( TNTEffects.hasEffect(type) ) {
							
							Map<String, Object> effect = TNTEffects.getEffect(type);
							
							if ( ! ( (boolean) effect.get("tntReceivable") ) ) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
								return true;
							}
						
							int amount = 1;
							
							if ( args.length > 3 && args[3] != null ) {
								if ( args[3].matches("\\d+") ) {
									if ( Integer.parseInt(args[3]) >= 1 && Integer.parseInt(args[3]) <= 64 ) {
										amount = Integer.parseInt(args[3]);
									} else {
										sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.amount-too-large")));
										return true;
									}
								}
							}
							
								
							ItemStack tnt = new ItemStack(Material.TNT, amount);
							ItemMeta tntMeta = tnt.getItemMeta();
							tntMeta.setDisplayName((String) effect.get("displayName"));
							tntMeta.setLore((List<String>) effect.get("lore"));
							tnt.setItemMeta(tntMeta);
							
							if ( (boolean) effect.get("remoteDetonation") ) {
								ItemStack detonator = new ItemStack((Material) effect.get("remoteDetonator"), 1);
								ItemMeta detMeta = detonator.getItemMeta();
								detMeta.setDisplayName((String) effect.get("remoteDetonatorName"));
								detonator.setItemMeta(detMeta);
								player.getInventory().addItem(detonator);
							}
								
							target.getInventory().addItem(tnt);
							target.updateInventory();
							
							if ( sender instanceof Player ) {
								if ( target.getUniqueId().equals(player.getUniqueId()) ) {
								
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.give-success")
											.replace("{AMOUNT}", ""+amount)
											.replace("{TYPE}", (String) effect.get("displayName"))
											.replace("{PLAYER}", args[1])));
								
								} else {
									
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.give-other-success")
											.replace("{AMOUNT}", ""+amount)
											.replace("{TYPE}", (String) effect.get("displayName"))
											.replace("{PLAYER}", args[1])));
									target.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.gave-success")
											.replace("{AMOUNT}", ""+amount)
											.replace("{TYPE}", (String) effect.get("displayName")))
											.replace("{PLAYER}", args[1]));
									
								}
							} else {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.give-other-success")
										.replace("{AMOUNT}", ""+amount)
										.replace("{TYPE}", (String) effect.get("displayName"))
										.replace("{PLAYER}", args[1])));
							}
							
						} else {
							
							return false;
							
						}
						
					}
					
					return true;
					
				case "debug":
				
					if ( args.length > 1 && args[1] != null ) {
					
						if ( sender instanceof Player && ! ( player.hasPermission("blastradius.admin") ) ) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.no-permission")));
							return true;
						}
						
						if ( args[1].equalsIgnoreCase("dumplocations") ) {
							
							if ( sender instanceof Player ) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.debug-command")));
							}
							
							TNTManager.sendDebugMessages();
							
						} else if ( args[1].equalsIgnoreCase("clearlocations") ) {
							
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.cleared-locations")));
							TNTManager.removeAllTNT();
							
						} else {
							return false;
							
						}
					
					} else {
						
						if ( sender instanceof Player ) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.debug-command")));
						}
					
						if ( toggleDebug != null && toggleDebug ) {
							toggleDebug = false;
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.debug-command-off", "&6Debug monitor &4Off.")));
							return true;
						} else {
							toggleDebug = true;
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("local.debug-command-on", "&6Debug monitor &2On.")));
						}
					}
					
					return true;
					
				default:
					
					return false;
			
			}
			
		}
		
		return false;
	}
	
	public boolean hasEnoughOfType(Player player, String displayName, int amount) {
		int sum = 0;
		for ( int i = 0; i < player.getInventory().getSize(); i++ ) {
			ItemStack item = player.getInventory().getItem(i);
			if ( item != null && item.getType().equals(Material.TNT) ) {
				ItemMeta meta = item.getItemMeta();
					if ( meta.getDisplayName().equals(displayName) ) {
				    sum = ( sum + item.getAmount() );
				    if ( sum >= amount ) {
				    	return true;
				    }
				}	
			}
			if ( i == player.getInventory().getSize() && sum < amount ) {
				return false;
			}
		}
		return false;
	}
	
	public boolean removeType(Player player, String displayName, int amount) {
		List<Integer> slots =  new ArrayList<Integer>();
		int sum = 0;
		for ( int i = 0; i < player.getInventory().getSize(); i++ ) {
			ItemStack item = player.getInventory().getItem(i);
			if ( item != null && item.getType().equals(Material.TNT) ) {
				ItemMeta meta = item.getItemMeta();
				if ( meta.getDisplayName().equals(displayName) ) {
					slots.add(i);
				    sum = ( sum + item.getAmount() );
				    if ( sum >= amount ) {
				    	break;
				    }
				}
			}
			if ( i == player.getInventory().getSize() && sum < amount ) {
				return false;
			}
		}
		int rem = 0;
		if ( sum > 0 ) {
			for ( int i : slots ) {
				ItemStack item = player.getInventory().getItem(i);
				if ( item.getAmount() > amount ) {
					item.setAmount(( item.getAmount() - amount ));
					rem = rem + amount;
					player.updateInventory();
					return true;
				} else if ( item.getAmount() == amount ) {
					player.getInventory().setItem(i, null);
					rem = rem + amount;
					player.updateInventory();
					return true;
				} else {
					amount = ( amount - item.getAmount() );
					player.getInventory().setItem(i, null);
					rem = rem + amount;
					player.updateInventory();
				}
			}
		}	
		itemsRemoved = rem;
		return false;
	}

}
