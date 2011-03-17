package com.rcjrrjcr.bukkitplugins.BuyAbilities.Settings;


public class Cost {
	public int cost;
	public int duration;
	public int uses;
	
	public Cost(Integer cost, Integer duration, Integer uses) {
		this.cost = cost;
		this.duration = duration;
		this.uses = uses;
	}
	
	public Cost()
	{
		cost = 0;
		duration = 0;
		uses = 0;
	}
	public void set(Integer cost, Integer duration, Integer uses)
	{
		this.cost = cost;
		this.duration = duration;
		this.uses = uses;
	}
	
}