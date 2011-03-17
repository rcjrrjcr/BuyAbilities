package com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface;

import org.bukkit.entity.Player;

import com.earth2me.essentials.User;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.BuyAbilities;

public class EconEssentials implements IEconHandler {

	private BuyAbilities origin;
	
	public EconEssentials(BuyAbilities origin) {
		this.origin = origin;
	}

	@Override
	public double getBalance(Player player) {
		return User.get(player).getMoney();
	}

	@Override
	public boolean deduct(Player player, Integer cost) {
		if(!User.get(player).canAfford(cost)) return false;
		User.get(player).takeMoney(cost);
		return true;		
	}

	@Override
	public double getBalance(String playerName) {
		return getBalance(origin.getServer().getPlayer(playerName));
	}

	@Override
	public boolean deduct(String playerName, Integer cost) {
		return deduct(origin.getServer().getPlayer(playerName),cost);
	}

}
