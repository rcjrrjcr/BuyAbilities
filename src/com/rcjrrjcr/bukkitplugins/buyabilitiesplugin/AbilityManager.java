package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.PurchasedAbilityType;

public class AbilityManager
{
	List<PurchasedAbility> rentedAbilities;
	List<PurchasedAbility> useCountAbilities;
	HashMap<String,Set<PurchasedAbility>> currentAbilities;
	BuyAbilities origin;
	
	public AbilityManager(BuyAbilities origin)
	{
		rentedAbilities = new LinkedList<PurchasedAbility>();
		useCountAbilities = new LinkedList<PurchasedAbility>();
		currentAbilities = new HashMap<String, Set<PurchasedAbility> >();
		this.origin = origin;
		return;
	}
	
	public synchronized Set<PurchasedAbility> getPlayer(String playerName)
	{
		if(currentAbilities.get(playerName)==null) currentAbilities.put(playerName, new HashSet<PurchasedAbility>());
		return currentAbilities.get(playerName);
	}
	
	public synchronized PurchasedAbility getPlayerAbility(String worldName, String playerName, String abilityName)
	{
		Set<PurchasedAbility> playerCurrent = getPlayer(playerName);
		for(PurchasedAbility p : playerCurrent)
		{
			if(p.abilityName.equalsIgnoreCase(abilityName)&&p.world.equalsIgnoreCase(worldName))
			{
				return p;
			}
		}
		return null;
	}
	
	public synchronized boolean hasPlayerAbility(String worldName, String playerName, String abilityName)
	{
		if(getPlayerAbility(worldName,playerName,abilityName)==null) return false;
		return true;
	}
	
	private synchronized void addPlayerAbility(PurchasedAbility p)
	{
		List<String> abPerms = p.perms;
		for(String node : abPerms)
		{
			if(origin.pHandler.hasPerm(p.world, p.playerName,node))
			{
				abPerms.remove(node);
			}
			else
			{
				origin.pHandler.addPerm(p.world, p.playerName, node);
			}
		}
		if(p.type == PurchasedAbilityType.RENT) rentedAbilities.add(p);
		if(p.type == PurchasedAbilityType.USE) useCountAbilities.add(p);
		if(currentAbilities.get(p.playerName) == null) currentAbilities.put(p.playerName, new HashSet<PurchasedAbility>());
		currentAbilities.get(p.playerName).add(p);
	}
	
	public boolean hasPermission(String worldName, String playerName, String nodeName)
	{
		if(worldName==null||playerName==null||nodeName==null) return false;
		if(origin.getServer().getWorld(worldName)==null) return false;
		if(origin.getServer().getPlayer(playerName)==null) return false;
		return origin.pHandler.hasPerm(worldName, playerName, nodeName);
	}
	
	public synchronized void rentAbility(String worldName, String playerName, String abilityName)
	{
		PurchasedAbility p = new PurchasedAbility(origin.settings.getAbility(abilityName),playerName, worldName, PurchasedAbilityType.RENT);
		//TODO: Add ways to prevent existing permissions from being associated with the ability
		addPlayerAbility(p);
	}
	public synchronized void buyAbility(String worldName, String playerName, String abilityName)
	{
		PurchasedAbility p = new PurchasedAbility(origin.settings.getAbility(abilityName),playerName, worldName, PurchasedAbilityType.BUY);
		//TODO: Add ways to prevent existing permissions from being associated with the ability
		addPlayerAbility(p);
	}
	public synchronized void useCountAbility(String worldName, String playerName, String abilityName)
	{
		PurchasedAbility p = new PurchasedAbility(origin.settings.getAbility(abilityName),playerName, worldName, PurchasedAbilityType.USE);
		//TODO: Add ways to prevent existing permissions from being associated with the ability
		addPlayerAbility(p);
	}
	public synchronized void removePlayerAbility(String worldName, String playerName, String abilityName)
	{
		PurchasedAbility p = getPlayerAbility(worldName,playerName,abilityName);
		if(p == null) return;
		removePlayerAbility(p);
		
	}
	private synchronized void removePlayerAbility(PurchasedAbility p)
	{
		if(p == null) return;
		List<String> abPerms = p.perms;

		for(String node : abPerms)
		{
			if(origin.pHandler.hasPerm(p.world, p.playerName,node))
			{
				origin.pHandler.removePerm(p.world, p.playerName, node);
			}
		}
		if(p.type == PurchasedAbilityType.RENT) rentedAbilities.remove(p);
		if(p.type == PurchasedAbilityType.USE) useCountAbilities.remove(p);
		currentAbilities.get(p.playerName).remove(p);
	}
	
	synchronized void load(Iterable<PurchasedAbility> data)
	{
		if(data==null) return;
		rentedAbilities.clear();
		currentAbilities.clear();
		for(PurchasedAbility p : data)
		{
			if(p.perms==null) continue;
			if(p.perms.isEmpty()) continue;
			addPlayerAbility(p);
		}
	}
	synchronized Iterable<PurchasedAbility> save()
	{
		List<PurchasedAbility> abList = new LinkedList<PurchasedAbility>();
		for(Set<PurchasedAbility> pSet : currentAbilities.values())
		{
			if(pSet.isEmpty()) continue;
			abList.addAll(pSet);
		}
		return abList;
	}
	public synchronized void decrement(String worldName, String playerName, String abilityName)
	{
//		System.out.println("World: "+ worldName+" Player: "+playerName+" Ability: "+abilityName);
		PurchasedAbility p = getPlayerAbility(worldName,playerName,abilityName);
		if(p==null) return;
//		System.out.println(p);
		p.duration--;
		return;
	}
	synchronized void update(final int interval)
	{
		for(PurchasedAbility p : rentedAbilities)
		{
			if(origin.getServer().getPlayer(p.playerName)==null||!origin.getServer().getPlayer(p.playerName).isOnline()) continue;
			p.duration -= interval;
			if(p.duration <= 0)
			{
				currentAbilities.get(p.playerName).remove(p);
				rentedAbilities.remove(p);
			}
		}
		for(PurchasedAbility u : useCountAbilities)
		{
			if(origin.getServer().getPlayer(u.playerName)==null||!origin.getServer().getPlayer(u.playerName).isOnline()) continue;
			if(u.duration <= 0)
			{
				currentAbilities.get(u.playerName).remove(u);
				rentedAbilities.remove(u);
			}
		}
	}
}
