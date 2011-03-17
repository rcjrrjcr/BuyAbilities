package com.rcjrrjcr.bukkitplugins.BuyAbilities.Storage;

import java.io.IOException;
//import java.util.List;

import com.rcjrrjcr.bukkitplugins.BuyAbilities.BuyAbilities;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.PurchasedAbility;

public interface IStorage
{
	public void init(BuyAbilities plugin) throws Exception;

	public Iterable<PurchasedAbility> getData() throws Exception;
	public void writeData(Iterable<PurchasedAbility> data) throws IOException;
}
