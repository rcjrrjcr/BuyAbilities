package com.rcjrrjcr.bukkitplugins.BuyAbilities.Settings;

import java.util.ArrayList;
//import java.util.LinkedList;
import java.util.List;

public class Ability {
	public String name;
	public Info info;
	public List<String> perms;
	public String category;
	public Costs costs;

	public Ability() {
		perms = new ArrayList<String>();
		info = new Info();
		costs = new Costs();
	}
}