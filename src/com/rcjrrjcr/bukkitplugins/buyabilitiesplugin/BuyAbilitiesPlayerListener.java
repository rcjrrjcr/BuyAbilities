package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class BuyAbilitiesPlayerListener extends PlayerListener {

	// use Thread-safe LinkedList Queue for processing commands on a separate thread
	// TODO: maybe implement this later as a more efficient way to process commands, instead of
	// 		 creating a new Decrementer() object every time like currently happens.  -morganm
//	private Queue<PlayerCommandPreprocessEvent> decrementerQueue = new ConcurrentLinkedQueuePlayerCommandPreprocessEvent>();
	
	public BuyAbilities origin;
	
	public BuyAbilitiesPlayerListener(BuyAbilities origin)
	{
		this.origin = origin;
	}

	@Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    	origin.getServer().getScheduler().scheduleAsyncDelayedTask(origin, new Decrementer(origin, event));
    }
    
	@Override
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		System.out.println("Player \""+event.getPlayer().getName()+"\" joined!");
    	origin.getServer().getScheduler().scheduleAsyncDelayedTask(origin, new PlayerLoaderSaver(origin,event));		
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		System.out.println("Player \""+event.getPlayer().getName()+"\" quitted!");
    	origin.getServer().getScheduler().scheduleAsyncDelayedTask(origin, new PlayerLoaderSaver(origin,event));	
	}
	
	@Override
	public void onPlayerKick(PlayerKickEvent event)
	{
		System.out.println("Player \""+event.getPlayer().getName()+"\" was kicked!");
    	origin.getServer().getScheduler().scheduleAsyncDelayedTask(origin, new PlayerLoaderSaver(origin,event));	
	}
}

class Decrementer implements Runnable
{
	private final BuyAbilities origin;
	private final PlayerCommandPreprocessEvent event;
	
	public Decrementer(BuyAbilities origin, PlayerCommandPreprocessEvent event)
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

class PlayerLoaderSaver implements Runnable
{
	private final BuyAbilities origin;
	private final PlayerEvent event;
	
	public PlayerLoaderSaver(BuyAbilities origin, PlayerQuitEvent event)
	{
		this.origin = origin;
		this.event = event;
	}
	
	public PlayerLoaderSaver(BuyAbilities origin, PlayerJoinEvent event)
	{
		this.origin = origin;
		this.event = event;
	}
	public PlayerLoaderSaver(BuyAbilities origin, PlayerKickEvent event)
	{
		this.origin = origin;
		this.event = event;
	}
	@Override
	public void run() {
    	try {
			origin.processLogonLogoff(event);
		} catch (Exception e) {
//			System.err.println("BuyAbilities: Wrong event passed! This shouldn't happen under normal circumstances.");
			e.printStackTrace();
		}
	}
}