package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.permissionsinterface;

public class PermissionData {
	private String playerName;
	private String world;
	private String node;
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public String getWorld() {
		return world;
	}
	public void setWorld(String world) {
		this.world = world;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public PermissionData(String playerName, String world, String node) {
		this.playerName = playerName;
		this.world = world;
		this.node = node;
	}
	public PermissionData()
	{
		
	}
}
