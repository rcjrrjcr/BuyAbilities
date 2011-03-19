package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings;

public class Info {
	public String extName;
	public String desc;
	public String help;
	
	public Info(String name, String desc, String help) {
		this.extName = name;
		this.desc = desc;
		this.help = help;
	}

	public Info() {
		extName = new String();
		desc = new String();
		help = new String();
	}

	
}