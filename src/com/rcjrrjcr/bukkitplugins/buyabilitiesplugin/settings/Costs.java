package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings;

public class Costs {
	public Cost buy;
	public Cost rent;
	//public Cost use;

	public Costs() {
		buy = new Cost();
		rent = new Cost();
	//	use = new Cost();
	}
	@Override
	public String toString()
	{
		String val =  "Buy: $"+buy.cost+"\nRent: $"+rent.cost;
		if(rent.duration != 0)
		{
			val = val +" "+ rent.duration+"s";
		}
		if(rent.uses != 0)
		{
			val = val +" "+ rent.uses+"uses";
		}
		return val;
	}
	
	public String shortString()
	{
		String val =  "|B: $"+buy.cost+"| |R: $"+rent.cost;
		if(rent.duration != 0)
		{
			val = val +" "+ rent.duration+"s";
		}
		if(rent.uses != 0)
		{
			val = val +" "+ rent.uses+"uses";
		}
		val = val + "|";
		return val;
	}
}