package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.IOException;
import java.util.Set;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

public interface IStorage
{

	public Set<PurchasedAbility> getData();
	public void writeData(Set<PurchasedAbility> data) throws IOException;
	
	public Set<PurchasedAbility> getPlayerData(String playerName);
	public void writePlayerData(Set<PurchasedAbility> data, String playerName) throws IOException;
}
