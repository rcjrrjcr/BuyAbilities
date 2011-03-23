package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMonitor implements CommandExecutor{

	private CommandMonitor chain;
	private String name;
	private final BuyAbilities origin;
	protected CommandMonitor(String name, CommandMonitor chain, BuyAbilities origin) {
		this.chain = chain;
		this.name = name;
		this.origin = origin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(sender instanceof Player)
		{
			Player player = (Player) sender;
			origin.decrement(player, name);
		}
		return chain.onCommand(sender, command, label, args);
	}



}
