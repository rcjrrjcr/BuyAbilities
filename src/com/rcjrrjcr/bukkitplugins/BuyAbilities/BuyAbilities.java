package com.rcjrrjcr.bukkitplugins.BuyAbilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface.EconFactory;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface.EconPlugin;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.EconomyInterface.IEconHandler;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.ChatHelper.ChatHelper;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.PermissionsInterface.IPermHandler;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.PermissionsInterface.PermFactory;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.PermissionsInterface.PermPlugin;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.Settings.Settings;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.Storage.IStorage;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.Storage.StorageFactory;
import com.rcjrrjcr.bukkitplugins.BuyAbilities.Storage.Storage;
/**
 * BuyAbilities for Bukkit
 *
 * @author rcjrrjcr
 */
//TODO: Javadoc
public class BuyAbilities extends JavaPlugin
{
	
	private BuyAbilitiesServerListener serverListener;
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	IEconHandler eHandler;
	IStorage storage;
	IPermHandler pHandler;
	Logger log;
	PluginManager pm;
	public Settings settings;
	AbilityManager abManager;
	BukkitScheduler scheduler;
	BuyAbilitiesChecker checker;
	private final Integer checkDelay = 5;
	private final Integer checkInterval = 10;
	public PluginStruct active;
	/**
	 Bukkit-called method. Creates appropriate instances of economy, permissions and storage plugins. Also starts thread to check for permission expiry.
	 
	  @author rcjrrjcr
	 */
	@Override
	public void onEnable()
	{
		
		//log = getServer().getLogger();
		pm = getServer().getPluginManager();
		scheduler = getServer().getScheduler();
		try {
			active = hook();
		} catch (Exception e) {
			e.printStackTrace();
			pm.disablePlugin(this);
		}
		
		if(!(active.getStatus()))
		{
			PluginDescriptionFile pdfFile = this.getDescription();
			System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is inactive." );
		}
		else
		{
			PluginDescriptionFile pdfFile = this.getDescription();
			System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		}
		serverListener  = new BuyAbilitiesServerListener(this,active);
		
		
		try {
			settings = new Settings(this,"plugins"+File.separator+"BuyAbilities"+File.separator+"costs.yml");
		} catch (Exception e) {
			e.printStackTrace();
			pm.disablePlugin(this);
		}
		//Load data from the database
		abManager = new AbilityManager(this);
		try {
			storage.init(this);
		} catch (Exception e) {
			System.out.println("BuyAbilities: Stored data not detected. If this is your first run, it's fine.");
		}
		try {
			abManager.load(storage.getData());
		} catch (Exception e) {
			System.out.println("Malformed data.yml.");
			e.printStackTrace();
		}
		//Start the checker thread
		checker = new BuyAbilitiesChecker(this,checkInterval);
		scheduler.scheduleAsyncRepeatingTask(this, checker, checkDelay, checkInterval);
		
		//Register our events
		getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
	}
	 /**
	  Bukkit-called method. Prints notification of the disabling of the plugin to console.
	  @author rcjrrjcr
	  */
	@Override
	public void onDisable() 
	{
		scheduler.cancelTasks(this);
		try {
			storage.writeData(abManager.save());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isDebugging(final Player player) {
		if (debugees.containsKey(player)) {
			return debugees.get(player);
		} else {
			return false;
		}
	}

	public void setDebugging(final Player player, final boolean value) {
		debugees.put(player, value);
	}
	boolean hookPerm() throws Exception
	{
		boolean perm = true;
		Plugin permPlugin = null;
		permPlugin = pm.getPlugin("Permissions");
		if(permPlugin == null)
		{
			permPlugin = pm.getPlugin("GroupManager");
			if(permPlugin == null)
			{
				throw new Exception("No Permission Plugin found!");
			}
			else if(!(permPlugin.isEnabled()))
			{
				perm = false;
				pHandler = PermFactory.getInstance(PermPlugin.CACHE, null, this);
			}
			else
			{
				pHandler = PermFactory.getInstance(PermPlugin.GroupManager,permPlugin,this);
			}
		}
		else if(!(permPlugin.isEnabled()))
		{
			perm = false;
			pHandler = PermFactory.getInstance(PermPlugin.CACHE, null, this);
		}
		else
		{
			pHandler = PermFactory.getInstance(PermPlugin.PermYeti,permPlugin,this);
		}
		return perm;
	}
	boolean hookEcon() throws Exception
	{
		boolean econ = true;
		Plugin econPlugin = null;
		econPlugin = pm.getPlugin("iConomy");
		if(econPlugin==null)
		{
			econPlugin = pm.getPlugin("Essentials");
			if(econPlugin == null)
			{
				throw new Exception("No Economy Plugin found!");
			}
			else if(!(econPlugin.isEnabled()))
			{
				econ = false;
				eHandler = null;
			}
			else
			{
				eHandler = EconFactory.getInstance(EconPlugin.ESSECO, econPlugin);
			}
		}
		else if(!(econPlugin.isEnabled()))
		{
			econ = false;
		}
		else
		{
			eHandler = EconFactory.getInstance(EconPlugin.IC4, econPlugin);
		}
		return econ;
	}
	PluginStruct hook() throws Exception
	{
		
		storage = StorageFactory.getInstance(Storage.YAML);
		boolean perm = hookPerm();
		boolean econ = hookEcon();
		return new PluginStruct(perm,econ);
	}
	
	
	
	/**
	 * Bukkit-called method. Processes commands that start with /bperm or /buyperm
	 * @return true
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
	    String commandName = command.getName().toLowerCase();
	    if (sender instanceof Player)
	    {
	    	Player player = (Player) sender;
	    	if (commandName.equals("bperm")||commandName.equals("buyperms")) {
	    		return commandHandler(player,args);
	    	}
	    }
	    else
	    {
	    	System.out.println("Testing onCommand");
	    }
	    return true;
	}
	
	boolean commandHandler(Player player, String[] args)
	{
		//TODO: Write command handlers here
		return true;
	}
	/**
	 * Print all available categories to player's chat
	 * @param player
	 */
	
	void setPermissions(Plugin permPlugin, PermPlugin type)
	{
		if(permPlugin != null&&type!=PermPlugin.NONE)
		{
			try {
				pHandler = PermFactory.getInstance(type, permPlugin,this);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			System.out.println("BuyAbilities: Permissions hooked!");
			active.perm = true;
			if(active.getStatus()) System.out.println("BuyAbilities: BuyAbilities active.");
		}
		if(!active.getStatus()) System.out.println("BuyAbilities: BuyAbilities inactive.");
	}
	
	void setEconomy(Plugin econPlugin, EconPlugin type)
	{
		if((econPlugin != null) && (type != EconPlugin.NONE))
		{
			try {
				eHandler = EconFactory.getInstance(type, econPlugin);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			System.out.println("BuyAbilities: Economy hooked!");
			active.econ = true;
			if(active.getStatus()) System.out.println("BuyAbilities: BuyAbilities active.");
		}
		if(!active.getStatus()) System.out.println("BuyAbilities: BuyAbilities inactive.");
	}
	
	public boolean hasPermission(String world, Player player, String perm)
	{
		return pHandler.hasPerm(world, player.getName(), perm);
	}
}

