package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings;

import java.util.ArrayList;
import java.util.LinkedList;
//import java.util.LinkedList;
import java.util.List;

public class Ability {
	public String name;
	public Info info;
	public List<String> perms;
	public List<String> categories;
	public Costs costs;
	public List<String> commands;

	public Ability() {
		perms = new ArrayList<String>();
		categories = new ArrayList<String>();
		info = new Info();
		costs = new Costs();
		commands = new LinkedList<String>();
	}
}