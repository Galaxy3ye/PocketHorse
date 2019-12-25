package net.pockethorse;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryAbstractHorse;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Mule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Events implements Listener{
	
	/* Prueft ob das Inventar eines Reittiers leer ist
	 * im Allgemeinen werden damit nur Esel und Maultiere
	 * geprueft */
	private boolean emptyHorseInv(AbstractHorseInventory inv){
		for(int i=2;i<inv.getSize();i++){
			if(inv.getItem(i)!=null){
				return false;
			}
		}
		return true;
	}
	
	
	/* Verhindert dass ein bereits "belegter" Sattel
	 * einem anderen Reittier aufgesetzt werden kann
	 * Als key wird der String "POCKETHORSE_SADDLE"
	 * benutzt der durch das setzen vom Paragraphen
	 * vor jedem char unsichtbar gemacht wird */
	@EventHandler
	public void onTryPocketSaddleSet(InventoryClickEvent event){
		if(event.getInventory() instanceof CraftInventoryAbstractHorse){
			if(!event.isShiftClick() && event.getClickedInventory() instanceof CraftInventoryPlayer){
				return;
			}
			
			ItemStack saddle = event.isShiftClick() ? event.getCurrentItem() : event.getCursor();
			
			if(saddle!=null && saddle.getType()==Material.SADDLE && saddle.hasItemMeta()){
				List<String> lore = saddle.getItemMeta().getLore();
				if(lore!=null){
					String infoData = lore.get(0).replace("\u00A7", "");
					
					if(infoData.substring(0, 18).equals("POCKETHORSE_SADDLE")){
						event.getWhoClicked().sendMessage(ChatColor.RED+"Der Sattel ist nicht leer!");
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	
	/* Wird der Sattel eines Reittiers entfernt wird dieser Sattel geloescht
	 * und das Reittier entfernt
	 * Dem Spieler wird dann ein Sattel gegeben der die Daten des Reittiers
	 * enthaelt */
	@EventHandler
	public void onSaddleRemove(InventoryClickEvent event){
		if(event.getClickedInventory() instanceof CraftInventoryAbstractHorse){
			if(event.getSlot()==0 && event.getCurrentItem().getType()==Material.SADDLE){
				AbstractHorse horse = (AbstractHorse)event.getInventory().getHolder();
				
				if(horse!=null){
					//Man kann nur den Sattel seines eigenen Reittiers entfernen
					if(horse.getOwner()!=null && !horse.getOwner().getUniqueId().equals(event.getWhoClicked().getUniqueId())){
						event.setCancelled(true);
						event.getWhoClicked().sendMessage(ChatColor.RED+"Das ist nicht dein Pferd!");
						return;
					}
					
					//Reittiere mit nicht-leerem Inventar werden nicht gespeichert
					if(!emptyHorseInv(horse.getInventory())){
						event.setCancelled(true);
						event.getWhoClicked().sendMessage(ChatColor.RED+"Zu voll beladen!");
						return;
					}
					
					//Um Faelle zu bedenken in denen jemand bspw sein Pferd "Pferd" genannt hat werden bei
					//einem unbeannten Pferd 2 nicht-darstellende Chars hinzugefuegt um es wiederzuerkennen
					String name = horse.getCustomName()==null ? PocketHorse.engDe.get(horse.getType().toString())+ChatColor.MAGIC : horse.getCustomName();
					
					double sStrength = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
					double speed = sStrength/0.1 * 4.3;
					
					double jStrength = horse.getJumpStrength();
					double jump = -0.1817574952*jStrength*jStrength*jStrength
								  +3.689713992*jStrength*jStrength
								  +2.128599134*jStrength
								  -0.343930367;
					
					speed = ((int)(speed*100))/100.0;
					jump = ((int)(jump*100))/100.0;
					
					//Die erste Zeile der Lore ist ein unsichtbarer String der aus dem Key "POCKETHORSE_SADDLE"
					//und den doubles fuer die Geschwindigkeit und Sprungkraft besteht
					//Fuegt man Paragraph vor jedem char ein wird der char danach nicht dargestellt
					String tmp = "POCKETHORSE_SADDLE"+sStrength+"Z"+jStrength;
					String speedAndJump = "";
					for(char c : tmp.toCharArray()){
						speedAndJump += "\u00A7"+c;
					}
					
					
					String color = (horse instanceof Horse) ? PocketHorse.engDe.get(((Horse)horse).getColor().toString()) : "standard";
					String style = (horse instanceof Horse) ? ", "+PocketHorse.engDe.get(((Horse)horse).getStyle().toString()) : "";
					if(horse instanceof Donkey && ((Donkey)horse).isCarryingChest()){
						style = ", mit Truhe";
					}
					if(horse instanceof Mule && ((Mule)horse).isCarryingChest()){
						style = ", mit Truhe";
					}
					
					//Erstellt die Lore fuer den zuerzeugenden Sattel
					List<String> lore = new LinkedList<String>();
					lore.add(speedAndJump);
					lore.add(ChatColor.GRAY+"Name: "+name);
					lore.add(ChatColor.GRAY+"Typ: "+PocketHorse.engDe.get(horse.getType().toString()));
					lore.add(ChatColor.GRAY+"Farbe: "+color+style);
					lore.add(ChatColor.GRAY+"Tempo: "+speed);
					lore.add(ChatColor.GRAY+"Sprung: "+jump);
					lore.add(ChatColor.GRAY+"HP: "+(int)horse.getHealth()+" / "+(int)horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
					lore.add(ChatColor.GRAY+"");
					lore.add(ChatColor.GRAY+"Rüstung:");
					
					//Sollte das Pferd eine Reustung tragen wird
					//dieses ebenfalls gespeichert
					if(horse instanceof Horse){
						ItemStack armor = ((Horse)horse).getInventory().getArmor();
						if(armor!=null){
							//Fuer den Armornamen wird das Gleiche gemacht wie bei dem Reittiernamen
							String armorName = PocketHorse.engDe.get(armor.getType().toString())+ChatColor.MAGIC;
							if(armor.hasItemMeta() && armor.getItemMeta().getDisplayName().length()>0){
								armorName = armor.getItemMeta().getDisplayName();
							}
							
							String armorColor = "FFFFFF";
							if(armor.getItemMeta()!=null && armor.getItemMeta() instanceof LeatherArmorMeta){
								armorColor = Integer.toHexString(((LeatherArmorMeta)armor.getItemMeta()).getColor().asRGB()).toUpperCase();
							}
							
							lore.add(ChatColor.GRAY+"Name: "+armorName);
							lore.add(ChatColor.GRAY+"Material: "+PocketHorse.engDe.get(armor.getType().toString()));
							lore.add(ChatColor.GRAY+"Farbe: #"+armorColor);
							lore.add(ChatColor.GRAY+"Schaden: "+armor.getDurability());
							for(Enchantment e : armor.getEnchantments().keySet()){
								lore.add(ChatColor.GRAY+PocketHorse.engDe.get(e.getKey().toString().substring(10))+": "+armor.getEnchantments().get(e));
							}
							
							if(armor.hasItemMeta() && armor.getItemMeta().getLore()!=null){
								lore.add(ChatColor.GRAY+"Lore:");
								List<String> armorLore = armor.getItemMeta().getLore();
								for(int i=0;i<armorLore.size();i++){
									lore.add(armorLore.get(i));
								}
							}
						}
					}
					
					
					//Das Item das evtl mit dem Sattel ausgetauscht werden sollte
					//wird wieder dem Inventar hinzugefuegt
					if(!event.isShiftClick()){
						event.getWhoClicked().getInventory().addItem(event.getCursor());
						event.setCursor(null);
					}
					event.setCancelled(true);
					
					ItemStack saddle = new ItemStack(Material.SADDLE, 1);
					
					saddle.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
					
					
					ItemMeta meta = saddle.getItemMeta();
					
					//Enchantments des Sattels werden in der Lore
					//nicht angezeigt aber das schimmern bleibt
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					meta.setLore(lore);
					meta.setDisplayName(ChatColor.BLUE+event.getWhoClicked().getName()+"'s "+name);
					
					saddle.setItemMeta(meta);
					
					//Sollte das Inventar des Spielers voll sein wird der Sattel
					//vor ihm gedroppt
					if(!event.getWhoClicked().getInventory().addItem(saddle).isEmpty()){
						horse.getWorld().dropItem(horse.getLocation(), saddle);
					}
					
					horse.remove();
				}
			}
		}
	}

	
	/* Macht man Rechtsklick mit dem Sattel in der Hand auf den Boden
	 * wird das gespiecherte Reittier gespawnt
	 * Dabei wrid der neue Owner der Player der den Sattel benutzt
	 * Das Reittier spawnt nur wenn man auf die obere Seite eines Blocks klickt und
	 * dieser und die 2 oberen(bzw 3 wenn nicht) durchlaessig sind */
	@EventHandler
	public void onRightClick(PlayerInteractEvent event){
		if(event.getAction()==Action.RIGHT_CLICK_BLOCK && event.getHand().equals(EquipmentSlot.HAND)){
			World world = event.getPlayer().getWorld();
			ItemStack saddle = event.getPlayer().getInventory().getItemInMainHand();
			
			
			if(saddle.getType()!=Material.SADDLE || !saddle.hasItemMeta()){
				return;
			}
			
			
			List<String> lore = saddle.getItemMeta().getLore();
			
			if(lore==null){
				return;
			}
			
			String infoData = lore.get(0).replace("\u00A7", "");
			
			//Prueft ob der Sattel ein Reittier enthaelt
			if(!infoData.substring(0, 18).equals("POCKETHORSE_SADDLE")){
				return;
			}
			
			//Entfernt die Farbcodes aus der Lore
			for(int i=1;i<lore.size();i++){
				if(lore.get(i).equals(ChatColor.GRAY+"Lore:")){
					break;
				}
				lore.set(i, lore.get(i).replace(""+ChatColor.GRAY, ""));
			}
			
			String type = lore.get(2).substring(5);
			
			Location loc = event.getClickedBlock().getLocation();
			
			//Prueft ob genug Platz zum spawnen ist
			if(event.getClickedBlock().isPassable()){
				if(!(world.getBlockAt(loc.add(0, 1, 0)).isPassable() && world.getBlockAt(loc.add(0, 1, 0)).isPassable())){
					event.getPlayer().sendMessage(ChatColor.RED+"Nicht genug Platz!");
					return;
				}
			}else if(event.getBlockFace()!=BlockFace.UP || !(world.getBlockAt(loc.add(0, 1, 0)).isPassable() && world.getBlockAt(loc.add(0, 1, 0)).isPassable() && world.getBlockAt(loc.add(0, 1, 0)).isPassable())){
				event.getPlayer().sendMessage(ChatColor.RED+"Nicht genug Platz!");
				return;
			}
			
			loc.add(0.5, -2, 0.5);
			
			AbstractHorse horse = (AbstractHorse)world.spawnEntity(loc, EntityType.valueOf(PocketHorse.deEng.get(type)));
			if(horse!=null){
				horse.setOwner(event.getPlayer());
				horse.setAdult();
				
				String name = lore.get(1).substring(6);
				
				//Prueft ob es einen Customnamen gibt
				if(!name.contains(""+ChatColor.MAGIC)){
					horse.setCustomName(name);
				}
				
				if(type.equals("Pferd")){
					String color = "";
					String style = "";
					for(int i=7;i<lore.get(3).length();i++){
						if(lore.get(3).charAt(i)==','){
							color = lore.get(3).substring(7, i);
							style = lore.get(3).substring(i+2);
							break;
						}
					}
					
					((Horse)horse).setColor(org.bukkit.entity.Horse.Color.valueOf(PocketHorse.deEng.get(color)));
					((Horse)horse).setStyle(Style.valueOf(PocketHorse.deEng.get(style)));
				}
				
				if(lore.get(3).equals("Farbe: standard, mit Truhe")){
					if(type.equals("Esel")){
						((Donkey)horse).setCarryingChest(true);
					}else{
						((Mule)horse).setCarryingChest(true);
					}
				}
				
				
				//Hole die Geschwindigkeit und Sprunkraft aus
				//der versteckten ersten Zeile
				double sStrength = 0;
				double jStrength = 0;
				for(int i=18;i<infoData.length();i++){
					if(infoData.charAt(i)=='Z'){
						sStrength = Double.parseDouble(infoData.substring(18, i));
						jStrength = Double.parseDouble(infoData.substring(i+1));
						break;
					}
				}
				
				horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(sStrength);
				horse.setJumpStrength(jStrength);
				
				
				int maxHp = 0;
				int hp = 0;
				
				for(int i=4;i<lore.get(6).length();i++){
					if(lore.get(6).charAt(i)=='/'){
						hp = Integer.parseInt(lore.get(6).substring(4, i-1));
						maxHp = Integer.parseInt(lore.get(6).substring(i+2));
						break;
					}
				}
				
				horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHp);
				horse.setHealth(hp);
				
				
				//Rekonstruiere falls vorhanden die Armor des Reittiers
				ItemStack armor = null;
				if(lore.size()>9){
					name = lore.get(9).substring(6);
					//Prueft ob die Armor benannt ist
					if(name.substring(name.length()-2).equals(""+ChatColor.MAGIC)){
						name = "";
					}
					armor = new ItemStack(Material.valueOf(PocketHorse.deEng.get(lore.get(10).substring(10))));
					
					int k = lore.get(11).contains("Farbe") ? 0 : 1;
					
					ItemMeta meta = null;
					List<String> armorLore = null;
					
					armor.setDurability(Short.parseShort(lore.get(12-k).substring(9)));
					if(lore.size()>13-k){
						boolean foundLore = false;
						for(int i=13-k;i<lore.size();i++){
							if(lore.get(i).equals(ChatColor.GRAY+"Lore:")){
								meta = armor.getItemMeta();
								armorLore = new LinkedList<String>();
								foundLore = true;
								continue;
							}
							
							if(foundLore){
								armorLore.add(lore.get(i));
							}else{
								int cut = 0;
								for(int j=0;j<lore.get(i).length();j++){
									if(lore.get(i).charAt(j)==':'){
										cut = j;
										break;
									}
								}
								
								armor.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(PocketHorse.deEng.get(lore.get(i).substring(0, cut)))), Integer.parseInt(lore.get(i).substring(cut+2)));
							}
						}
					}
					
					if(meta==null){
						meta = armor.getItemMeta();
					}
					
					meta.setLore(armorLore);
					meta.setDisplayName(name);
					armor.setItemMeta(meta);
					
					if(armor.getItemMeta() instanceof LeatherArmorMeta){
						String color = lore.get(11).substring(8);
						if(!color.equals("FFFFFF")){
							meta = armor.getItemMeta();
							((LeatherArmorMeta)meta).setColor(Color.fromRGB(Integer.parseInt(color, 16)));
						}
						armor.setItemMeta(meta);
					}
					
					((Horse)horse).getInventory().setArmor(armor);
				}
				
				horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
				
				saddle.setAmount(0);
			}
		}
	}
	
}
