package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.IOException;
//import java.util.List;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

public interface IStorage
{
	public void init(BuyAbilities plugin) throws Exception;

	public Iterable<PurchasedAbility> getData() throws Exception;
	public void writeData(Iterable<PurchasedAbility> data) throws IOException;
}