package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.IOException;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BABException;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;

public final class StorageFactory
{
	public static IStorage getInstance(Storage pluginType, BuyAbilities origin) throws BABException, IOException
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
			throw new BABException("Unable to create Storage interface.");
		}
	}
}
