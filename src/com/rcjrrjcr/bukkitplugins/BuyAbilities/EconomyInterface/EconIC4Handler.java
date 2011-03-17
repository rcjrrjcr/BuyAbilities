package com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface;

import org.bukkit.entity.Player;
import com.nijiko.coelho.iConomy.iConomy;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.BuyAbilities;

public class EconIC4Handler implements IEconHandler {
	
	//private BuyAbilities origin;
	public EconIC4Handler(BuyAbilities origin) {
	//	this.origin = origin;
	}

	@Override
	public double getBalance(Player player) {
		return getBalance(player.getName());
	}

	@Override
	public boolean deduct(Player player, Integer cost) {
		return deduct(player.getName(),cost);
	}

	@Override
	public double getBalance(String playerName) {
		if(iConomy.getBank().hasAccount(playerName))
		{
			return iConomy.getBank().getAccount(playerName).getBalance();
		}
		return -1.0d;
	}

	@Override
	public boolean deduct(String playerName, Integer cost) {
		if(getBalance(playerName) >= cost)
		{
			iConomy.getBank().getAccount(playerName).subtract(cost);
			return true;
		}
		
		return false;
	}

}
