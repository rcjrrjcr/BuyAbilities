package com.rcjrrjcr.bukkitplugins.BuyAbilities.PermissionsInterface;

import java.util.LinkedHashSet;

import org.bukkit.plugin.Plugin;



public interface IPermHandler
{
	public void setPlugin(Plugin plugin);
	
	public boolean hasPerm(String world, String playerName, String perm);
	public void addPerm(String world,String playerName, String perm);
	public void removePerm(String world,String playerName, String perm);
	public void setPerm(String world,String playerName, String perm, boolean hasPerm);
	
	public boolean isInGroup(String world,String playerName, String group);

	public String[] listGroups(String world, String playerName);
	
	public LinkedHashSet<PermissionData> getAddCache();
	public LinkedHashSet<PermissionData> getRemCache();
	public void setCache(LinkedHashSet<PermissionData> addCache,LinkedHashSet<PermissionData> remCache);
	public void flushCache();
}