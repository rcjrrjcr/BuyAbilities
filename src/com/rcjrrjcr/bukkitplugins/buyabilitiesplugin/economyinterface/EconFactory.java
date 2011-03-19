package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.economyinterface;

import org.bukkit.plugin.Plugin;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;


public final class EconFactory
{
	public static IEconHandler getInstance(EconPlugin pluginType,Plugin plugin,BuyAbilities origin) throws Exception
	{
		if (pluginType == EconPlugin.IC4)
		{
			return new EconIC4Handler(origin);
		}
		else if (pluginType == EconPlugin.ESSECO)
		{
			return new EconEssentials(origin);
		}
		else if (pluginType == EconPlugin.NONE)
		{
			return new EconNone();
		}
		else
		{
			throw new Exception("Economy Interface was unable to be created!");
		}	
	}
}
