package com.rcjrrjcr.bukkitplugins.BuyAbilities.PermissionsInterface;

import org.bukkit.plugin.Plugin;

import com.rcjrrjcr.bukkitplugins.BuyAbilities.BuyAbilities;


public final class PermFactory
{
	private static IPermHandler pHandler;
	public static IPermHandler getInstance(PermPlugin pluginType,Plugin plugin,BuyAbilities origin) throws Exception
	{
		IPermHandler newHandler;
		if (pluginType == PermPlugin.PermYeti)
		{
			newHandler = new PermYetiHandler(plugin,origin);
		}
		else if (pluginType == PermPlugin.GroupManager)
		{
			newHandler = new GroupManagerHandler(plugin,origin);
		}
		else if (pluginType == PermPlugin.CACHE)
		{
			newHandler = new PermCacheOnly(plugin,origin);
		}
		else
		{
			throw new Exception("Permission Interface was unable to be created!");
		}
		if(pHandler != null)
		{
			newHandler.setCache(pHandler.getAddCache(), pHandler.getRemCache());
			newHandler.flushCache();
		}
		pHandler = newHandler;
		return newHandler;
	}
}
