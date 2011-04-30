package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

public class StorageYaml implements IStorage {

	private File yamlFile;
	private Configuration yamlConfig;
	private final BuyAbilities origin;
	private final String path = "plugins"+File.separator+"BuyAbilities"+File.separator+"data.yml";
	
	public StorageYaml(BuyAbilities plugin) throws IOException {

		origin = plugin;
		yamlFile = new File(path);
		if(!(yamlFile.exists()))
		{
			System.out.println(path + " not found.");
			System.out.println("Creating " + path + "...");
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
			System.out.println("BuyAbilities: Node \"Data:\" empty!");
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
	        if(!playerData.keySet().contains(ab.playerName)) playerData.put(ab.playerName, new HashSet<PurchasedAbility>());
	        playerData.get(ab.playerName).add(ab);
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
			for(String ability : playerWorldData.keySet())
			{
				PurchasedAbility pAb = new PurchasedAbility();
				pAb.abilityName = ability; 
				pAb.duration = yamlConfig.getInt("Data."+playerName+"."+world+"."+ability+".duration",0);
				pAb.extName = origin.settings.getInfo(ability).extName;
				pAb.perms = new HashSet<String>(yamlConfig.getStringList("Data."+playerName+"."+world+"."+ability+".nodes", null));
				pAb.playerName = playerName;
				String typeString = yamlConfig.getString("Data."+playerName+"."+world+"."+ability+".type",PurchasedAbilityType.RENT.toString());
				
				
				if(typeString.equalsIgnoreCase(PurchasedAbilityType.BUY.toString()))
				{
					pAb.type = PurchasedAbilityType.BUY;
				}
				else if (typeString.equalsIgnoreCase(PurchasedAbilityType.RENT.toString()))
				{
					pAb.type = PurchasedAbilityType.RENT;
				}
				else if (typeString.equalsIgnoreCase(PurchasedAbilityType.USE.toString()))
				{
					pAb.type = PurchasedAbilityType.USE;
				}
				
				
				pAb.world = world;
				result.add(pAb);
			}
		}
		System.out.println("BuyAbilities: Loading \""+playerName+"\"'s data...");
		return result;
	}



	@Override
	public void writePlayerData(Set<PurchasedAbility> data, String playerName)
			throws IOException {
		if(yamlConfig.getProperty("Data"+playerName)!=null)yamlConfig.removeProperty("Data."+playerName);
		for(PurchasedAbility pAb : data)
		{
			if(!pAb.playerName.equalsIgnoreCase(playerName)) continue;
			String path = "Data."+pAb.playerName+"."+pAb.world+"."+pAb.abilityName;
			yamlConfig.setProperty(path+".duration", pAb.duration);
			yamlConfig.setProperty(path+".type", pAb.type.toString());
			yamlConfig.setProperty("Data."+pAb.playerName+"."+pAb.world+"."+pAb.abilityName+".nodes", pAb.perms);
		}
		yamlConfig.save();
        System.out.println("BuyAbilities: Player \"" + playerName + "\"'s data saved.");
    }

}
