package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

//import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Ability;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.PurchasedAbilityType;

public class PurchasedAbility implements Comparable<PurchasedAbility>, Cloneable
{
	public String abilityName;
	public String extName;
	public List<String> perms;
	public String playerName;
	public String world;
	public PurchasedAbilityType type;
	public int duration;
	
	@Override
	public int compareTo(PurchasedAbility o) {
		return new Integer(duration).compareTo(o.duration);
	}
	
	@Override
	public String toString()
	{
		if(type== PurchasedAbilityType.RENT) return abilityName + ": " + extName+" with "+ new Integer(duration/20).toString() +"s left in world " + world +".";
		if(type== PurchasedAbilityType.BUY) return abilityName + ": " + extName+" in world " + world +".";
		if(type== PurchasedAbilityType.USE) return abilityName + ": " + extName+" with "+ duration +" uses left in world " + world +".";
		return "Error: " + abilityName + "type not specified.";
	}
	public PurchasedAbility()
	{
		abilityName = new String();
		extName = new String();
		perms = new LinkedList<String>();
		playerName = new String();
		world = new String();
		duration = 0;
	}
	
	public PurchasedAbility(Ability a, String playerName, String worldName, PurchasedAbilityType type)
	{
		abilityName = a.name;
		extName = a.info.extName;
		perms = a.perms;
		this.playerName = playerName;
		this.world = worldName;
		this.type = type;
		duration = 0;
		//if(type == PurchasedAbilityType.BUY) duration = 0;
		if(type == PurchasedAbilityType.RENT) duration = a.costs.rentDuration;
		if(type == PurchasedAbilityType.USE) duration = a.costs.useCount;
	}
	private PurchasedAbility(PurchasedAbility p)
	{
		this.abilityName = new String(p.abilityName);
		this.extName = new String(p.extName);
		this.perms = new LinkedList<String>();
		for(String perm : p.perms)
		{
			this.perms.add(new String(perm));
		}
		this.playerName = new String(p.playerName);
		this.world = new String(p.world);
		this.duration = p.duration;
		this.type = p.type;
	}
	@Override
	public Object clone()
	{
		return new PurchasedAbility(this);
	}
	
	public boolean valueEquals(Object obj)
	{
		if(!(obj instanceof PurchasedAbility)) return false;
		PurchasedAbility p = (PurchasedAbility) obj;
		if(!abilityName.equals(p.abilityName)) return false;
		if(!extName.equals(p.extName)) return false;
		if(!playerName.equals(p.playerName)) return false;
		if(!world.equals(p.world)) return false;
		if(duration != p.duration) return false;
		if(type != p.type) return false;
		if(!(perms.containsAll(p.perms)&&p.perms.containsAll(perms)) ) return false;
		return true;
	}
}
