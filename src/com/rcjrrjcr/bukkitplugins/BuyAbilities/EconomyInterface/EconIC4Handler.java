package com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface;

import org.bukkit.entity.Player;
import com.nijiko.coelho.iConomy.iConomy;

public class EconIC4Handler implements IEconHandler {
	
	@Override
	public double getBalance(Player player) {
		if(iConomy.getBank().hasAccount(player.getName()))
		{
			return iConomy.getBank().getAccount(player.getName()).getBalance();
		}
		return -1.0d;
	}

	@Override
	public boolean deduct(Player player, Integer cost) {
		if(getBalance(player) > cost)
		{
			iConomy.getBank().getAccount(player.getName()).subtract(cost);
		}
		
		return false;
	}

}
