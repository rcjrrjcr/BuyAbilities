package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings;

import java.util.HashSet;
import java.util.Set;

public class Ability {
	public String name;
	public Info info;
	public Set<String> perms;
	public Set<String> categories;
	public Costs costs;
	public Set<String> commands;
	

	public Ability() {
		perms = new HashSet<String>();
		categories = new HashSet<String>();
		info = new Info();
		costs = new Costs();
		commands = new HashSet<String>();
	}
}