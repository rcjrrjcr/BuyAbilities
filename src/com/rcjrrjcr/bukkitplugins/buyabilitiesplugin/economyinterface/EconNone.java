package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.economyinterface;

import org.bukkit.entity.Player;

public class EconNone implements IEconHandler {

	@Override
	public double getBalance(Player player) {
		return 0;
	}

	@Override
	public boolean deduct(Player player, Integer cost) {
		return false;
	}

	@Override
	public double getBalance(String playerName) {
		return 0;
	}

	@Override
	public boolean deduct(String playerName, Integer cost) {
		return false;
	}

}
