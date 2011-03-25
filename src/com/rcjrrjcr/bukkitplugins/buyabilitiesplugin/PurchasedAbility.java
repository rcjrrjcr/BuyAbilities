package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

//import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Ability;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.PurchasedAbilityType;

public class PurchasedAbility implements Comparable<PurchasedAbility> 
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
	
}
