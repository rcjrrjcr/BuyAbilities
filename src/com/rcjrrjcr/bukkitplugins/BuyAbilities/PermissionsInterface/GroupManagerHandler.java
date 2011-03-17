package com.rcjrrjcr.bukkitplugins.BuyAbilities.PermissionsInterface;

import java.util.LinkedHashSet;

import org.bukkit.plugin.Plugin;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;

import com.rcjrrjcr.bukkitplugins.BuyAbilities.BuyAbilities;



public class GroupManagerHandler implements IPermHandler {

	private GroupManager plugin;
	private WorldsHolder dataHolder;
	private BuyAbilities origin;
	private LinkedHashSet<PermissionData> permAddCache;
	private LinkedHashSet<PermissionData> permRemCache;

	public GroupManagerHandler(Plugin plugin, BuyAbilities origin)
	{
		setPlugin(plugin);
		this.origin = origin;
		permAddCache = new LinkedHashSet<PermissionData>();
		permRemCache = new LinkedHashSet<PermissionData>();
	}
	@Override
	public void setPlugin(Plugin plugin) {
		if(plugin instanceof GroupManager)
		{
			this.plugin = (GroupManager) plugin;
			dataHolder = this.plugin.getWorldsHolder();
		}
		
		
		return;		
	}


	@Override
	public boolean hasPerm(String world, String playerName, String perm) {
		if(!origin.active.isPermActive()) return false;
		return dataHolder.getWorldPermissions(world).has(origin.getServer().getPlayer(playerName), perm);
	}


	@Override
	public void addPerm(String world, String playerName, String perm)
	{	
		if(!origin.active.isPermActive())
		{
			PermissionData pData = new PermissionData();
			pData.setWorld(world);
			pData.setPlayerName(playerName);
			pData.setNode(perm);
			permAddCache.add(pData);
			return;
		}
		dataHolder.getWorldData(world).getUser(playerName).addPermission(perm);
	}


	@Override
	public void removePerm(String world, String playerName, String perm) {
		if(!origin.active.isPermActive())
		{
			PermissionData pData = new PermissionData();
			pData.setWorld(world);
			pData.setPlayerName(playerName);
			pData.setNode(perm);
			permRemCache.add(pData);
			return;
		}
		dataHolder.getWorldData(world).getUser(playerName).removePermission(perm);
	}


	@Override
	public void setPerm(String world, String playerName, String perm,
			boolean hasPerm)
	{
		if(hasPerm)
		{
			addPerm(world,playerName,perm);
		}
		else
		{
			removePerm(world,playerName,perm);
		}
	}


	@Override
	public boolean isInGroup(String world, String playerName, String group) {
		if(!origin.active.isPermActive()) return false;
		return dataHolder.getWorldPermissions(world).inGroup(playerName, group);
	}


	@Override
	public String[] listGroups(String world, String playerName) {
		if(!origin.active.isPermActive()) return null;
		String[] groups;
		Group[] groupCollection = (Group[])dataHolder.getWorldData(world).getGroupList().toArray();
		Integer size = 	groupCollection.length;
		groups =  new String[size];
		int ctr;
		for(ctr=0;ctr<size;ctr++)
		{
			groups[ctr] = groupCollection[ctr].getName();
		}
		return groups;
	}
	@Override
	public void flushCache() {
		if(!origin.active.isPermActive()) return;
		for(PermissionData pAdd : permAddCache)
		{
			addPerm(pAdd.getWorld(),pAdd.getPlayerName(),pAdd.getNode());
			permAddCache.remove(pAdd);
		}
		for(PermissionData pAdd : permRemCache)
		{
			removePerm(pAdd.getWorld(),pAdd.getPlayerName(),pAdd.getNode());
			permRemCache.remove(pAdd);
		}
		return;
	}
	@Override
	public LinkedHashSet<PermissionData> getAddCache() {
		return permAddCache;
	}
	@Override
	public LinkedHashSet<PermissionData> getRemCache() {
		return permRemCache;
	}
	@Override
	public void setCache(LinkedHashSet<PermissionData> addCache,
			LinkedHashSet<PermissionData> remCache) {
		permAddCache = addCache;
		permRemCache = remCache;
		
	}


	

}
