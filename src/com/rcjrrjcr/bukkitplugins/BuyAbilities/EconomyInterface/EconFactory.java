package com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface;

import org.bukkit.plugin.Plugin;


public final class EconFactory
{
	public static IEconHandler getInstance(EconPlugin pluginType,Plugin plugin) throws Exception
	{
		if (pluginType == EconPlugin.IC4)
		{
			return new EconIC4Handler();
		}
		else if (pluginType == EconPlugin.ESSECO)
		{
			return new EconEssentials();
		}
		else
		{
			throw new Exception("Economy Interface was unable to be created!");
		}	
	}
}
