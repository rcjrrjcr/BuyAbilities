package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BABException;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

public class StorageYaml implements IStorage {
    private static final Logger log = BuyAbilities.log;

	private File yamlFile;
	private Configuration yamlConfig;
	private final BuyAbilities origin;
	private final String path = "plugins"+File.separator+"BuyAbilities"+File.separator+"data.yml";
	
	public StorageYaml(BuyAbilities plugin) throws IOException {

		origin = plugin;
		yamlFile = new File(path);
		if(!(yamlFile.exists()))
		{
			log.fine(path + " not found.");
			log.fine("Creating " + path + "...");
			yamlFile.createNewFile();
		}
		if(!(yamlFile.isFile()))
		{
			throw new IOException(path + " not a file.");
		}
		if(!(yamlFile.canRead()))
		{
			throw new IOException(path + "not readable.");
		}

		yamlConfig = new Configuration(yamlFile);
		yamlConfig.load();

	}

	

	@Override
	public Set<PurchasedAbility> getData() {
		Map<String, ConfigurationNode> data = yamlConfig.getNodes("Data");
		Set<PurchasedAbility> result = new HashSet<PurchasedAbility>();
		if(data == null||data.isEmpty())
		{
			log.fine("BuyAbilities: Node \"Data:\" empty!");
			return result;
		}
		for(String playerName : data.keySet())
		{
			Set<PurchasedAbility> playerData = getPlayerData(playerName);
			result.addAll(playerData);
		}
		return result;
	}

	@Override
	public void writeData(Set<PurchasedAbility> data) throws IOException
	{
	    Map<String,Set<PurchasedAbility>> playerData = new HashMap<String,Set<PurchasedAbility>>();
	    for(PurchasedAbility ab : data)
	    {
	        if(!playerData.keySet().contains(ab.getPlayerName())) playerData.put(ab.getPlayerName(), new HashSet<PurchasedAbility>());
	        playerData.get(ab.getPlayerName()).add(ab);
	    }
	    for(String playerName : playerData.keySet())
	    {
	        writePlayerData(playerData.get(playerName),playerName);
	    }
	}



	@Override
	public Set<PurchasedAbility> getPlayerData(String playerName){
		
		Map<String, ConfigurationNode> playerData = yamlConfig.getNodes("Data."+playerName);
		Set<PurchasedAbility> result = new HashSet<PurchasedAbility>();
		if(playerData==null||	playerData.isEmpty()) return result;
		for(String world : playerData.keySet())
		{
			Map<String, ConfigurationNode> playerWorldData = yamlConfig.getNodes("Data."+playerName+"."+world);
			log.fine("BuyAbilities: Loading \""+playerName+"\"'s data...");
			
			for(String ability : playerWorldData.keySet())
			{
				String yamlBase = "Data."+playerName+"."+world+"."+ability;
				
				try {
					PurchasedAbility pAb = new PurchasedAbility();
					pAb.setAbilityName(ability); 
					pAb.setDuration(yamlConfig.getInt(yamlBase+".duration",0));
					pAb.setExtName(origin.settings.getInfo(ability).extName);
//					pAb.setPerms(new HashSet<String>(yamlConfig.getStringList(yamlBase+".nodes", null)));
					pAb.setPlayerName(playerName);
					String typeString = yamlConfig.getString(yamlBase+".type",PurchasedAbilityType.RENT.toString());
	
					pAb.setType(typeString);
					pAb.setWorld(world);
					result.add(pAb);
				} catch( BABException e ) {
					log.warning("Exception caught processing node "+yamlBase+" in datafile");
					e.printStackTrace();
				}
			}
		}
		return result;
	}



	@Override
	public void writePlayerData(Set<PurchasedAbility> data, String playerName)
			throws IOException {
		if(yamlConfig.getProperty("Data"+playerName)!=null)yamlConfig.removeProperty("Data."+playerName);
		for(PurchasedAbility pAb : data)
		{
			if(!pAb.getPlayerName().equalsIgnoreCase(playerName)) continue;
			String path = "Data."+pAb.getPlayerName()+"."+pAb.getWorld()+"."+pAb.getAbilityName();
			yamlConfig.setProperty(path+".duration", pAb.getDuration());
			yamlConfig.setProperty(path+".type", pAb.getType().toString());
			yamlConfig.setProperty(path+".nodes", pAb.getPerms());
		}
		yamlConfig.save();
        log.fine("BuyAbilities: Player \"" + playerName + "\"'s data saved.");
    }

}
