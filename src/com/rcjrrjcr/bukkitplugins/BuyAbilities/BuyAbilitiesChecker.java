package com.rcjrrjcr.bukkitplugins.BuyAbilities;

public class BuyAbilitiesChecker implements Runnable {

	private BuyAbilities origin;
	private final int interval;
	public BuyAbilitiesChecker(BuyAbilities origin, final int interval)
	{
		this.origin = origin;
		this.interval = interval;
	}
	
	
	@Override
	public void run() {
		origin.abManager.update(interval);
	}

}
