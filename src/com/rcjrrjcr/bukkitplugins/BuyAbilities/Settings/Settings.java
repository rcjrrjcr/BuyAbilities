package com.rcjrrjcr.bukkitplugins.BuyAbilities.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.rcjrrjcr.bukkitplugins.BuyAbilities.BuyAbilities;


public class Settings {
	private BuyAbilities origin;
	private File yamlFile;
	private Configuration yamlConfig;
	private HashMap<String,Ability> nameToAbilityMap;
	private HashMap<String,Set<Ability>> categoryToAbilityMap;
	public Settings(BuyAbilities origin, String path) throws Exception
	{
		nameToAbilityMap = new HashMap<String,Ability>();
		categoryToAbilityMap = new HashMap<String,Set<Ability>>();
		this.origin = origin; 
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
		
		
		reload();
	}
	
	public void reload() throws Exception
	{
		Map<String, ConfigurationNode> abilityNodeList = yamlConfig.getNodes("Abilities");
		for(String abilityName : abilityNodeList.keySet())
		{
//			System.out.println(abilityName);
			Ability ab = new Ability();
			ab.name = abilityName;
//			System.out.println(yamlConfig.getString("Abilities."+abilityName+".info.name"));
			ab.info.extName = yamlConfig.getString("Abilities."+abilityName+".info.name");
			ab.info.desc = yamlConfig.getString("Abilities."+abilityName+".info.description");
			ab.info.help = yamlConfig.getString("Abilities."+abilityName+".info.help");
			ab.perms.addAll(yamlConfig.getStringList("Abilities."+abilityName+".permissions", new ArrayList<String>()));
			ab.categories = yamlConfig.getStringList("Abilities."+abilityName+".categories", new ArrayList<String>());
			ab.costs.buy.set(yamlConfig.getInt("Abilities."+abilityName+"costs.buy.cost", 0),0,0);
			ab.costs.rent.set(yamlConfig.getInt("Abilities."+abilityName+"costs.rent.cost", 0),yamlConfig.getInt("Abilities."+abilityName+"costs.rent.duration", 0),yamlConfig.getInt("Abilities."+abilityName+"costs.rent.uses", 0));
//			ab.costs.use.set(yamlConfig.getInt("Abilities."+abilityName+"costs.use.cost", 0),0,yamlConfig.getInt("Abilities."+abilityName+"costs.buy.stock", 0));
//			System.out.println(ab.name != null);
			nameToAbilityMap.put(ab.name, ab);
//			System.out.println(abilityNameToAbilityMap.toString());
//			System.out.println(ab.categories.size());
			for(String category : ab.categories)
			{
				if(!categoryToAbilityMap.containsKey(category)) categoryToAbilityMap.put(category, new HashSet<Ability>());
				categoryToAbilityMap.get(category).add(ab);
			}
		}
		
	}
	
	
	public Costs getCost(String abilityName)
	{
		return nameToAbilityMap.get(abilityName).costs;
	}
	public Info getInfo(String abilityName)
	{
		return nameToAbilityMap.get(abilityName).info;
	}
	public List<String> getPerms(String abilityName)
	{
		return nameToAbilityMap.get(abilityName).perms;
	}
	public List<String> getAbilityCategories(String abilityName)
	{
		return nameToAbilityMap.get(abilityName).categories;
	}
	public List<String> getCategories(String world, Player player)
	{
		List<String> categoryList = new ArrayList<String>();
		//System.out.println(categoryList.size());
		for(String category : categoryToAbilityMap.keySet())
		{
			if(origin.hasPermission(world, player.getName(), "buyabilities.abilities."+category.replace(' ', '.')))
			{
				categoryList.add(category);
				//System.out.println(category);
			}
		}
		return categoryList;
	}
	
	public List<String> getAbilites(String categoryName)
	{
		if(categoryToAbilityMap.get(categoryName)==null) return null;
		List<String> abList = new ArrayList<String>();
		Set<Ability> abSet = categoryToAbilityMap.get(categoryName);
		if(abSet==null||abSet.isEmpty()) return abList;
		for(Ability ab : abSet)
		{
			abList.add(ab.name);
		}
		return abList;
	}
	public boolean canPurchase(String abilityName,String world, Player player)
	{
		if(nameToAbilityMap.get(abilityName) == null) return false;
		Ability ab = nameToAbilityMap.get(abilityName);
		for(String categoryName : ab.categories)
		{
			 if(origin.hasPermission(world, player.getName(), "buyabilities.abilities."+categoryName.replace(' ', '.'))) return true;
		}
		return false;
	}

	public boolean canAccess(String categoryName,String world, Player player)
	{
		return origin.hasPermission(world, player.getName(), categoryName.replace(" ", ".")  );
	}
	
	public Ability getAbility(String abilityName)
	{
		return nameToAbilityMap.get(abilityName);
	}
	
	public List<String> getAllAbilities(String worldName, Player player)
	{
		List<String> categories = getCategories(worldName,player);
//		System.out.println(categories.size());
		Set<String> abSet = new HashSet<String>();
		for(String categoryName : categories)
		{
			Set<Ability> abList = categoryToAbilityMap.get(categoryName);
			for(Ability ab : abList)
			{
				abSet.add(ab.name);
			}
		}
		List<String> abList = new ArrayList<String>();
		abList.addAll(abSet);
		return abList;
	}
}


