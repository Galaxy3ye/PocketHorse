package net.pockethorse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PocketHorse extends JavaPlugin{
	
	private final String d = "Dictionary";
	
	private File dictionary;
	private YamlConfiguration dictionaryConfig = new YamlConfiguration();
	
	public static HashMap<String, String> engDe = new HashMap<String, String>();
	public static HashMap<String, String> deEng = new HashMap<String, String>();
	
	
	@Override
	public void onEnable(){
		checkDictionary();
		
		readDictionary();
		
		this.getServer().getPluginManager().registerEvents(new Events(), this);
	}
	
	/* Prueft ob das File mit Woerterbuch vorhanden ist
	 * und erschafft es noetigenfalls */
	private void checkDictionary(){
		if(!this.getDataFolder().exists()){
			this.getDataFolder().mkdirs();
		}
		
		dictionary = new File(this.getDataFolder()+"/dictionary.yml");
		if(!dictionary.exists()){
			try{
				System.out.print("Dictionary is missing!");
				System.out.print("Creating a new one...");
				
				dictionary.createNewFile();
				writeDictionary();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	/* Liest das Woerterbuch aus und laed die jeweiligen
	 * Uebersetzungen in eine DE->EN und EN->DE HashMap */
	private void readDictionary(){
		try{
			dictionaryConfig = new YamlConfiguration();
			dictionaryConfig.load(dictionary);
			
			ConfigurationSection section = dictionaryConfig.getConfigurationSection(d);
			if(section!=null){
				if(section.getKeys(false)!=null){
					for(String enchantment : section.getKeys(false)){
						engDe.put(enchantment, dictionaryConfig.getString(d+"."+enchantment));
						deEng.put(dictionaryConfig.getString(d+"."+enchantment), enchantment);
					}
				}
			}
			
		}catch(IOException | InvalidConfigurationException e){
			e.printStackTrace();
		}
	}
	
	/* Basis-Woerterbuch */
	private void writeDictionary(){
		dictionaryConfig.set(d, "");
		
		dictionaryConfig.set(d+".HORSE", "Pferd");
		dictionaryConfig.set(d+".SKELETON_HORSE", "Skelettpferd");
		dictionaryConfig.set(d+".ZOMBIE_HORSE", "Zombiepferd");
		dictionaryConfig.set(d+".DONKEY", "Esel");
		dictionaryConfig.set(d+".MULE", "Maultier");
		
		dictionaryConfig.set(d+".BLACK", "schwarz");
		dictionaryConfig.set(d+".BROWN", "braun");
		dictionaryConfig.set(d+".CHESTNUT", "kastanie");
		dictionaryConfig.set(d+".CREAMY", "cremefarben");
		dictionaryConfig.set(d+".DARK_BROWN", "dunkelbraun");
		dictionaryConfig.set(d+".GRAY", "grau");
		dictionaryConfig.set(d+".WHITE", "weiß");
		
		dictionaryConfig.set(d+".BLACK_DOTS", "schwarze Punkte");
		dictionaryConfig.set(d+".NONE", "rein");
		dictionaryConfig.set(d+".WHITE", "weiß");
		dictionaryConfig.set(d+".WHITE_DOTS", "weiße Punkte");
		dictionaryConfig.set(d+".WHITEFIELD", "weiße Flecken");
		
		dictionaryConfig.set(d+".DIAMOND_HORSE_ARMOR", "Diamantener Rossharnisch");
		dictionaryConfig.set(d+".GOLDEN_HORSE_ARMOR", "Goldener Rossharnisch");
		dictionaryConfig.set(d+".IRON_HORSE_ARMOR", "Eiserner Rossharnisch");
		dictionaryConfig.set(d+".LEATHER_HORSE_ARMOR", "Lederner Rossharnisch");
		
		dictionaryConfig.set(d+".protection", "Schutz");
		dictionaryConfig.set(d+".fire_protection", "Feuerschutz");
		dictionaryConfig.set(d+".blast_protection", "Explosionsschutz");
		dictionaryConfig.set(d+".projectile_protection", "Schusssicher");
		dictionaryConfig.set(d+".feather_falling", "Federfall");
		dictionaryConfig.set(d+".respiration", "Atmung");
		dictionaryConfig.set(d+".aqua_affinity", "Wasseraffinität");
		dictionaryConfig.set(d+".thorns", "Dornen");
		dictionaryConfig.set(d+".depth_strider", "Wasserläufer");
		dictionaryConfig.set(d+".frost_walker", "Eisläufer");
		dictionaryConfig.set(d+".sweeping", "Schwungkraft");
		dictionaryConfig.set(d+".loyalty", "Treue");
		dictionaryConfig.set(d+".impaling", "Harpune");
		dictionaryConfig.set(d+".riptide", "Sog");
		dictionaryConfig.set(d+".channeling", "Entladung");
		dictionaryConfig.set(d+".multishot", "Mehrfachschuss");
		dictionaryConfig.set(d+".quick_charge", "Schnellladen");
		dictionaryConfig.set(d+".piercing", "Durchsschuss");
		dictionaryConfig.set(d+".mending", "Reperatur");
		dictionaryConfig.set(d+".smite", "Bann");
		dictionaryConfig.set(d+".bane_of_arthropods", "Nemesis der Gliederfüßer");
		dictionaryConfig.set(d+".knockback", "Rückstoß");
		dictionaryConfig.set(d+".fire_aspect", "Verbrennung");
		dictionaryConfig.set(d+".looting", "Plünderung");
		dictionaryConfig.set(d+".efficiency", "Effizienz");
		dictionaryConfig.set(d+".silk_touch", "Behutsamkeit");
		dictionaryConfig.set(d+".unbreaking", "Haltbarkeit");
		dictionaryConfig.set(d+".fortune", "Glück");
		dictionaryConfig.set(d+".luck_of_the_sea", "Glück des Meeres");
		dictionaryConfig.set(d+".lure", "Köder");
		dictionaryConfig.set(d+".sharpness", "Schärfe");
		dictionaryConfig.set(d+".power", "Stärke");
		dictionaryConfig.set(d+".punch", "Schlag");
		dictionaryConfig.set(d+".flame", "Flamme");
		dictionaryConfig.set(d+".infinity", "Unendlichkeit");
		dictionaryConfig.set(d+".vanishing_curse", "Fluch des Verschwindens");
		dictionaryConfig.set(d+".binding_curse", "Fluch der Bindung");
		
		try{
			dictionaryConfig.save(dictionary);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
}
