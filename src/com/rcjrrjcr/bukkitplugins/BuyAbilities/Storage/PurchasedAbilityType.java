package com.rcjrrjcr.bukkitplugins.BuyAbilities.Storage;


/**
 * Type of purchased ability. Either BUY or RENT
 * @author rcjrrjcr
 */
public enum PurchasedAbilityType {
	RENT,
	BUY;
	
	/**
	 * Utility function used for YAML serialization
	 * @return
	 */
	@Deprecated
	public int toInt()
	{
		if(this == BUY)
		{
			return 1;
		}
		else if(this == RENT)
		{
			return 2;
		}
		return 0;
	}
	
	public String shortString()
	{
		if(this == BUY) return "B";
		return "R";
	}
}
