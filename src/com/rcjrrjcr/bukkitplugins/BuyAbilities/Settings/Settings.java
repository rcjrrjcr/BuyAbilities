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
			ab.perms.addAll(yamlConfig.getStringList("Abilities."+abilityName+".permissions", null));
			ab.category = yamlConfig.getString("Abilities."+abilityName+".category");
			ab.costs.buy.set(yamlConfig.getInt("Abilities."+abilityName+"costs.buy.cost", 0),0,0);
			ab.costs.rent.set(yamlConfig.getInt("Abilities."+abilityName+"costs.rent.cost", 0),yamlConfig.getInt("Abilities."+abilityName+"costs.rent.duration", 0),yamlConfig.getInt("Abilities."+abilityName+"costs.rent.uses", 0));
//			ab.costs.use.set(yamlConfig.getInt("Abilities."+abilityName+"costs.use.cost", 0),0,yamlConfig.getInt("Abilities."+abilityName+"costs.buy.stock", 0));
//			System.out.println(ab.name != null);
			nameToAbilityMap.put(ab.name, ab);
//			System.out.println(abilityNameToAbilityMap.toString());
			if(!categoryToAbilityMap.containsKey(ab.category)) categoryToAbilityMap.put(ab.category, new HashSet<Ability>());
			categoryToAbilityMap.get(ab.category).add(ab);
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
	public String getCategory(String abilityName)
	{
		return nameToAbilityMap.get(abilityName).category;
	}
	public List<String> getCategories(String world, Player player)
	{
		List<String> catergoryList = new ArrayList<String>();
		for(String category : categoryToAbilityMap.keySet())
		{
			if(origin.hasPermission(world, player.getName(), "bperm."+category.replace(" ", "."))) catergoryList.add(category);
		}
		return catergoryList;
	}
	
	public List<String> getAbilites(String categoryName)
	{
		//Fix this
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
		return origin.hasPermission(world, player.getName(), "bperm."+nameToAbilityMap.get(abilityName).category.replace(" ", ".")  );
	}

	public boolean canAccess(String categoryName,String world, Player player)
	{
		return origin.hasPermission(world, player.getName(), categoryName.replace(" ", ".")  );
	}
	
	public Ability getAbility(String abilityName)
	{
		return nameToAbilityMap.get(abilityName);
	}
}


