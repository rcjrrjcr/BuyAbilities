package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.PurchasedAbilityType;

public class AbilityManager
{
	List<PurchasedAbility> rentedAbilities;
	List<PurchasedAbility> useCountAbilities;
	Map<String,Set<PurchasedAbility>> currentAbilities;
	BuyAbilities origin;
	
    public AbilityManager(BuyAbilities origin)
	{
		rentedAbilities = Collections.synchronizedList(new LinkedList<PurchasedAbility>());
		useCountAbilities = Collections.synchronizedList(new LinkedList<PurchasedAbility>());
		currentAbilities = Collections.synchronizedMap(new HashMap<String, Set<PurchasedAbility> >());
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
	
	private synchronized void addPlayerAbility(PurchasedAbility p, boolean load)
	{
		Set<String> abPerms = p.perms; 
		Set<String> newPerms = new HashSet<String>();
		for(String node : abPerms)
		{
			if(!origin.pHandler.hasPerm(p.world, p.playerName,node))
			{
				origin.pHandler.addPerm(p.world, p.playerName, node);
                newPerms.add(node);
			}
			else if(load)
			{
                newPerms.add(node);
			}
		}
		p.perms = newPerms;
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
		addPlayerAbility(p,false);
	}
	public synchronized void buyAbility(String worldName, String playerName, String abilityName)
	{
		PurchasedAbility p = new PurchasedAbility(origin.settings.getAbility(abilityName),playerName, worldName, PurchasedAbilityType.BUY);
		addPlayerAbility(p,false);
	}
	public synchronized void useCountAbility(String worldName, String playerName, String abilityName)
	{
		PurchasedAbility p = new PurchasedAbility(origin.settings.getAbility(abilityName),playerName, worldName, PurchasedAbilityType.USE);
		addPlayerAbility(p,false);
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
		Set<String> abPerms = p.perms;

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
	
	synchronized void load(Set<PurchasedAbility> data)
	{
		if(data==null) return;
		rentedAbilities.clear();
		currentAbilities.clear();
		for(PurchasedAbility p : data)
		{
			if(p.perms==null) continue;
			if(p.perms.isEmpty()) continue;
			addPlayerAbility(p,true);
		}
	}
	synchronized Set<PurchasedAbility> save()
	{
		Set<PurchasedAbility> abSet = new LinkedHashSet<PurchasedAbility>();
		for(Set<PurchasedAbility> pSet : currentAbilities.values())
		{
			if(pSet==null||pSet.isEmpty()) continue;
			abSet.addAll(pSet);
		}
		return abSet;
	}
	synchronized void loadPlayer(Set<PurchasedAbility> data, String playerName)
	{
		System.out.println("Loading player \""+playerName+"\"'s data!");
		if(data == null) return;
		Set<PurchasedAbility> pAbilities = getPlayer(playerName);
		if(!pAbilities.isEmpty())
		{
			for(PurchasedAbility pAb : pAbilities)
			{
				removePlayerAbility(pAb);
			}
		}
		
		for(PurchasedAbility nAb : data)
		{
			if(nAb.playerName.equalsIgnoreCase(playerName)) addPlayerAbility(nAb,true);
		}
		return;
	}
	synchronized Set<PurchasedAbility> saveAndUnloadPlayer(String playerName)
	{
		System.out.println("Unloading player \""+playerName+"\"'s data!");
		Set<PurchasedAbility> pAbilities = getPlayer(playerName);
		Set<PurchasedAbility> pSaved = new HashSet<PurchasedAbility>(pAbilities.size());
//		System.out.println(pAbilities);
		if(!pAbilities.isEmpty())
		{
			for(PurchasedAbility pAb : pAbilities)
			{
				pSaved.add((PurchasedAbility) pAb.clone());
				removePlayerAbility(pAb);
			}
		}
//		System.out.println(pSaved);
		return pSaved;
	}
	public synchronized void decrement(String worldName, String playerName, String abilityName)
	{
//		System.out.println("World: "+ worldName+" Player: "+playerName+" Ability: "+abilityName);
		PurchasedAbility p = getPlayerAbility(worldName,playerName,abilityName);
		if(p==null) return;
//		System.out.println(p);
		p.duration--;
		System.out.println("Uses left: " + p.duration);
		return;
	}
	synchronized void update(final int interval)
	{
		
		
        
		for(ListIterator<PurchasedAbility> rent = rentedAbilities.listIterator();rent.hasNext();)
		{
		    PurchasedAbility p = rent.next();
			if(origin.getServer().getPlayer(p.playerName)!=null&&origin.getServer().getPlayer(p.playerName).isOnline())
			{
				p.duration -= interval;
				if(p.duration <= 0)
				{
			        Set<String> abPerms = p.perms;

			        for(String node : abPerms)
			        {
			            if(origin.pHandler.hasPerm(p.world, p.playerName,node))
			            {
			                origin.pHandler.removePerm(p.world, p.playerName, node);
			            }
			        }
			        rent.remove();
			        currentAbilities.get(p.playerName).remove(p);
				}
			}
		}
		
		for(ListIterator<PurchasedAbility> used = useCountAbilities.listIterator();used.hasNext();)
		{
		    PurchasedAbility u = used.next();
			if(origin.getServer().getPlayer(u.playerName)!=null&&origin.getServer().getPlayer(u.playerName).isOnline())
			{
				if(u.duration <= 0)
				{
				    u.duration -= interval;
	                if(u.duration <= 0)
	                {
	                    Set<String> abPerms = u.perms;

	                    for(String node : abPerms)
	                    {
	                        if(origin.pHandler.hasPerm(u.world, u.playerName,node))
	                        {
	                            origin.pHandler.removePerm(u.world, u.playerName, node);
	                        }
	                    }
	                    used.remove();
	                    currentAbilities.get(u.playerName).remove(u);
	                }
				}
			}
		}
	}
}
