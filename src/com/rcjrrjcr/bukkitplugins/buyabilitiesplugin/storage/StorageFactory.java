package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

public final class StorageFactory
{
	public static IStorage getInstance(Storage pluginType) throws Exception
	{
		if (pluginType == Storage.YAML)
		{
			return new StorageYaml();
		}
		else
		{
			throw new Exception("Unable to create Storage interface.");
		}
	}
}
