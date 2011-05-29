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
import java.util.logging.Logger;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.PurchasedAbilityType;

public class AbilityManager
{
    private static final Logger log = BuyAbilities.log;
    
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
			if(p.getAbilityName().equalsIgnoreCase(abilityName) && p.getWorld().equalsIgnoreCase(worldName))
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
		Set<String> abPerms = p.getPerms(); 
		Set<String> newPerms = new HashSet<String>();
		for(String node : abPerms)
		{
			log.fine("BuyAbilities.addPlayerAbility(): "+p.getWorld()+","+p.getPlayerName()+","+node);
			
			if(!origin.pHandler.hasPerm(p.getWorld(), p.getPlayerName(),node))
			{
				origin.pHandler.addPerm(p.getWorld(), p.getPlayerName(),node);
                newPerms.add(node);
			}
			else if(load)
			{
                newPerms.add(node);
			}
		}
//		p.setPerms(newPerms);
		if(p.getType() == PurchasedAbilityType.RENT) rentedAbilities.add(p);
		if(p.getType() == PurchasedAbilityType.USE) useCountAbilities.add(p);
		if(currentAbilities.get(p.getPlayerName()) == null) currentAbilities.put(p.getPlayerName(), new HashSet<PurchasedAbility>());
		currentAbilities.get(p.getPlayerName()).add(p);
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
		Set<String> abPerms = p.getPerms();

		for(String node : abPerms)
		{
			if(origin.pHandler.hasPerm(p.getWorld(), p.getPlayerName(),node))
			{
				origin.pHandler.removePerm(p.getWorld(), p.getPlayerName(), node);
			}
		}
		if(p.getType() == PurchasedAbilityType.RENT) rentedAbilities.remove(p);
		if(p.getType() == PurchasedAbilityType.USE) useCountAbilities.remove(p);
		currentAbilities.get(p.getPlayerName()).remove(p);
	}
	
	synchronized void load(Set<PurchasedAbility> data)
	{
		if(data==null) return;
		rentedAbilities.clear();
		currentAbilities.clear();
		for(PurchasedAbility p : data)
		{
			if(p.getPerms()==null) continue;
			if(p.getPerms().isEmpty()) continue;
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
		log.fine("[BuyAbilities] Loading player \""+playerName+"\"'s data!");
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
			if(nAb.getPlayerName().equalsIgnoreCase(playerName)) addPlayerAbility(nAb,true);
		}
		return;
	}
	
	synchronized Set<PurchasedAbility> saveAndUnloadPlayer(String playerName)
	{
		log.fine("[BuyAbilities] Unloading player \""+playerName+"\"'s data!");
		Set<PurchasedAbility> pAbilities = getPlayer(playerName);
		Set<PurchasedAbility> pSaved = new HashSet<PurchasedAbility>(pAbilities.size());
//		log.fine(pAbilities);
		if(!pAbilities.isEmpty())
		{
			for(PurchasedAbility pAb : pAbilities)
			{
				pSaved.add((PurchasedAbility) pAb.clone());
				removePlayerAbility(pAb);
			}
		}
//		log.fine(pSaved);
		return pSaved;
	}
	public synchronized void decrement(String worldName, String playerName, String abilityName)
	{
//		log.fine("World: "+ worldName+" Player: "+playerName+" Ability: "+abilityName);
		PurchasedAbility p = getPlayerAbility(worldName,playerName,abilityName);
		if(p==null) return;
//		log.fine(p);
		p.setDuration( p.getDuration() - 1 );
		log.fine("Uses left: " + p.getDuration());
		return;
	}
	
	synchronized void update(final int interval)
	{
		
		
        
		for(ListIterator<PurchasedAbility> rent = rentedAbilities.listIterator();rent.hasNext();)
		{
		    PurchasedAbility p = rent.next();
			if(origin.getServer().getPlayer(p.getPlayerName())!=null&&origin.getServer().getPlayer(p.getPlayerName()).isOnline())
			{
				int duration = p.getDuration();
				duration -= interval;
				p.setDuration(duration);
				if(duration <= 0)
				{
			        Set<String> abPerms = p.getPerms();

			        for(String node : abPerms)
			        {
			            if(origin.pHandler.hasPerm(p.getWorld(), p.getPlayerName(),node))
			            {
			                origin.pHandler.removePerm(p.getWorld(), p.getPlayerName(), node);
			            }
			        }
			        rent.remove();
			        currentAbilities.get(p.getPlayerName()).remove(p);
				}
			}
		}
		
		for(ListIterator<PurchasedAbility> used = useCountAbilities.listIterator();used.hasNext();)
		{
		    PurchasedAbility u = used.next();
			if(origin.getServer().getPlayer(u.getPlayerName())!=null&&origin.getServer().getPlayer(u.getPlayerName()).isOnline())
			{
				int duration = u.getDuration();
				
				// is this a bug? we are double-checking if duration <= 0, seems one of these should be
				// looking for >= 0    -morganm 5/26/11
				if(duration <= 0)
				{
				    duration -= interval;
				    u.setDuration(duration);
	                if(duration <= 0)
	                {
	                    Set<String> abPerms = u.getPerms();

	                    for(String node : abPerms)
	                    {
	                        if(origin.pHandler.hasPerm(u.getWorld(), u.getPlayerName(),node))
	                        {
	                            origin.pHandler.removePerm(u.getWorld(), u.getPlayerName(), node);
	                        }
	                    }
	                    used.remove();
	                    currentAbilities.get(u.getPlayerName()).remove(u);
	                }
				}
			}
		}
	}
}
