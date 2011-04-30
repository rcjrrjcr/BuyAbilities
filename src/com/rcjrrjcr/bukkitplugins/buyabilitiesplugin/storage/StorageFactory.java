package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;

public final class StorageFactory
{
	public static IStorage getInstance(Storage pluginType, BuyAbilities origin) throws Exception
	{
		if (pluginType == Storage.YAML)
		{
			return new StorageYaml(origin);
		}
		else if (pluginType == Storage.EBEANS)
		{
			return new StorageEBeans(origin);
		}
		else
		{
			throw new Exception("Unable to create Storage interface.");
		}
	}
}
