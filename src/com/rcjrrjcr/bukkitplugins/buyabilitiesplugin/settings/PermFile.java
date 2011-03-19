package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings;

import java.util.ArrayList;
import java.util.HashMap;

public class PermFile {
	public ArrayList<Ability> abilities;
	public HashMap<String, String> categories;

	public PermFile() {
		abilities = new ArrayList<Ability>();
		categories = new HashMap<String,String>();
	}
}