package com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface;

import org.bukkit.entity.Player;

import com.earth2me.essentials.User;

public class EconEssentials implements IEconHandler {

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

}
