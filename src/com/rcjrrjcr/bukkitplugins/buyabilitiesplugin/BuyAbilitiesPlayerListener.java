package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class BuyAbilitiesPlayerListener extends PlayerListener {


    public void onPlayerCommandPreprocess(PlayerChatEvent event) {
    	System.out.println(event.getMessage());
    	//TODO: Write decrement()
    }
}
