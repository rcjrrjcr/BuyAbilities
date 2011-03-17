package com.rcjrrjcr.bukkitplugins.BuyAbilities;

public class PluginStruct {
	boolean perm;
	boolean econ;
	PluginStruct(boolean perm, boolean econ)
	{
		this.perm = perm;
		this.econ = econ;
	}
	public boolean getStatus()
	{
		return perm&econ;
	}
	public boolean isPermActive() {
		return perm;
	}
	void setPerm(boolean perm) {
		this.perm = perm;
	}
	public boolean isEconActive() {
		return econ;
	}
	void setEcon(boolean econ) {
		this.econ = econ;
	}
}
