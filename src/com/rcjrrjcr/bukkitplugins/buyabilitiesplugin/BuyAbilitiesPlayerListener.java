package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class BuyAbilitiesPlayerListener extends PlayerListener {

	public BuyAbilities origin;
	
	public BuyAbilitiesPlayerListener(BuyAbilities origin)
	{
		this.origin = origin;
	}

    public void onPlayerCommandPreprocess(PlayerChatEvent event) {
//    	System.out.println("PlayerCommandPreprocess event detected!");
//    	System.out.println(event.getMessage());
    	origin.getServer().getScheduler().scheduleAsyncDelayedTask(origin, new Decrementer(origin, event));
    	return;
    }
}

class Decrementer implements Runnable
{
	private final BuyAbilities origin;
	private final PlayerChatEvent event;
	
	public Decrementer(BuyAbilities origin, PlayerChatEvent event)
	{
		this.origin = origin;
		this.event = event;
	}
	@Override
	public void run() {
//    	System.out.println("Decrementing!");
    	origin.commandPreprocess(event.getMessage(),event.getPlayer().getName(),event.getPlayer().getWorld().getName());
	}
}