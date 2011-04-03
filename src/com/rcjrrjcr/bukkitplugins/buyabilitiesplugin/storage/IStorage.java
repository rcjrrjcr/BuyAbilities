package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.IOException;
import java.util.Collection;
//import java.util.List;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

public interface IStorage
{
	public void init(BuyAbilities plugin) throws Exception;

	public Collection<PurchasedAbility> getData();
	public void writeData(Collection<PurchasedAbility> data) throws IOException;
	
	public Collection<PurchasedAbility> getPlayerData(String playerName);
	public void writePlayerData(Collection<PurchasedAbility> data, String playerName) throws IOException;
}
