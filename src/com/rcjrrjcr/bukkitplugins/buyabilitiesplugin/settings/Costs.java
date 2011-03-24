package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings;

public class Costs {
	
	public boolean canBuy;
	public int buyCost;
	
	public boolean canRent;
	public int rentCost;
	public int rentDuration;
	
	public boolean canUse;
	public int useCost;
	public int useCount;

	public Costs() {
		canBuy = false;
		canRent = false;
		canUse = false;
	}
	@Override
	public String toString()
	{
		String val = "";
		if(canBuy)
		{
			val = val + "Buy: $"+buyCost+"\n";
		}
		if(canRent)
		{
			val = val + "Rent: $"+rentCost +" "+ rentDuration+"s\n";;
		}
		if(canUse)
		{
			val = val + "Rent: $"+useCost +" "+ useCount+"uses";
		}
		return val;
	}
	
	public String shortString()
	{
		String val = "";
		if(canBuy)
		{
			val = val + "|B: $"+buyCost+"|";
		}
		if(canRent)
		{
			val = val + "|R: $"+rentCost +" "+ rentDuration+"s|";;
		}
		if(canUse)
		{
			val = val + "|U: $"+useCost +" "+ useCount+"uses|";
		}
		return val;
	}
}