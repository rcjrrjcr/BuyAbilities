package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
//import java.util.List;
import java.util.Map;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

public class StorageYaml implements IStorage {

	private File yamlFile;
	private Configuration yamlConfig;
	private BuyAbilities origin;
	private final String path = "plugins"+File.separator+"BuyAbilities"+File.separator+"data.yml";
	@Override
	public void init(BuyAbilities plugin) throws Exception {

		origin = plugin;
		yamlFile = new File(path);
		if(!(yamlFile.exists()))
		{
			throw new Exception(path + " not found.");
		}
		if(!(yamlFile.isFile()))
		{
			throw new Exception(path + " not a file.");
		}
		if(!(yamlFile.canRead()))
		{
			throw new Exception(path + "not readable.");
		}

		yamlConfig = new Configuration(yamlFile);
		yamlConfig.load();

	}

	

	@Override
	public Iterable<PurchasedAbility> getData() {
		Map<String, ConfigurationNode> data = yamlConfig.getNodes("Data");
		if(data == null)
		{
			System.out.println("BuyAbilities: Node \"Data:\" empty!");
			return null;
		}
		ArrayList<PurchasedAbility> result = new ArrayList<PurchasedAbility>();
		for(String playerName : data.keySet())
		{
			Map<String, ConfigurationNode> playerData = yamlConfig.getNodes("Data."+playerName);
			for(String world : playerData.keySet())
			{
				Map<String, ConfigurationNode> playerWorldData = yamlConfig.getNodes("Data."+playerName+"."+world);
				for(String ability : playerWorldData.keySet())
				{
					PurchasedAbility pAb = new PurchasedAbility();
					pAb.abilityName = ability; 
					pAb.duration = yamlConfig.getInt("Data."+playerName+"."+world+"."+ability+".duration",0);
					pAb.extName = origin.settings.getInfo(ability).extName;
					pAb.perms = yamlConfig.getStringList("Data."+playerName+"."+world+"."+ability+".nodes", new LinkedList<String>());
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
					//System.out.println(pAb.type.toString());
					pAb.world = world;
					//System.out.println(pAb.playerName);
					//System.out.println(pAb.toString());
					result.add(pAb);
				}
			}
		}
		return result;
	}

	@Override
	public void writeData(Iterable<PurchasedAbility> data) throws IOException
	{
		yamlFile.delete();
		yamlFile.createNewFile();
		assert(yamlFile != null);
		yamlConfig = new Configuration(yamlFile);
		for(PurchasedAbility pAb : data)
		{
			//System.out.println(pAb.toString());
			String path = "Data."+pAb.playerName+"."+pAb.world+"."+pAb.abilityName;
			yamlConfig.setProperty(path+".duration", pAb.duration);
			yamlConfig.setProperty(path+".type", pAb.type.toString());
			yamlConfig.setProperty("Data."+pAb.playerName+"."+pAb.world+"."+pAb.abilityName+".nodes", pAb.perms);
		}
		yamlConfig.save();
		System.out.println("BuyAbilities: Data saved.");
	}

}
