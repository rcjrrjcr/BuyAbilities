package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
//import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
//import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import com.rcjrrjcr.bukkitplugins.util.RcjrPlugin;
import com.rcjrrjcr.bukkitplugins.util.economyinterface.EconFactory;
import com.rcjrrjcr.bukkitplugins.util.economyinterface.EconPlugin;
import com.rcjrrjcr.bukkitplugins.util.economyinterface.IEconHandler;
import com.rcjrrjcr.bukkitplugins.util.chathelper.ChatHelper;
import com.rcjrrjcr.bukkitplugins.util.permissionsinterface.IPermHandler;
import com.rcjrrjcr.bukkitplugins.util.permissionsinterface.PermFactory;
import com.rcjrrjcr.bukkitplugins.util.permissionsinterface.PermPlugin;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Ability;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Settings;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.IStorage;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.StorageFactory;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.Storage;
/**
 * BuyAbilities for Bukkit
 *
 * @author rcjrrjcr
 */
//TODO: Javadoc
public class BuyAbilities extends RcjrPlugin
{
	
	private static final ChatColor COLOR_CHAT = ChatColor.GOLD;
	private BuyAbilitiesServerListener serverListener;
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	IEconHandler eHandler;
	IStorage storage;
	IPermHandler pHandler;
//	Logger log;
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
				eHandler = EconFactory.getInstance(EconPlugin.ESSECO, econPlugin,this);
			}
		}
		else if(!(econPlugin.isEnabled()))
		{
			econ = false;
		}
		else
		{
			eHandler = EconFactory.getInstance(EconPlugin.IC4, econPlugin,this);
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
	 * Bukkit-called method. Processes commands that start with /bab or /buyab
	 * @return true
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
	    String commandName = command.getName().toLowerCase();
	    if (sender instanceof Player)
	    {
	    	Player player = (Player) sender;
	    	if (commandName.equals("buyab")||commandName.equals("bab")) {
	    		return commandHandler(player,args);
	    	}
	    }
	    else
	    {
	    	if(commandName.equalsIgnoreCase("bab")||commandName.equalsIgnoreCase("buyab"))
	    	{
	    		if(args.length==0)
	    		{
	    			ChatHelper.sendMsgWrap("Incorrect syntax. Syntax /bab [hasperm|balance|listall].",sender);
	    			return true;
	    		}
	    		if(args[0].equalsIgnoreCase("listall"))
	    		{
	    			ChatHelper.sendMsgWrap(abManager.currentAbilities.toString(), sender);
	    			return true;
	    		}
	    		if(args[0].equalsIgnoreCase("balance"))
	    		{
	    			if(args.length != 2)
	    			{
		    			ChatHelper.sendMsgWrap("Incorrect syntax. Syntax /bab balance <playername>", sender);
		    			return true;
	    			}
	    			Integer bal = balance(args[1]);
	    			if(bal==null)
	    			{
		    			ChatHelper.sendMsgWrap("Player does not exist.", sender);
		    			return true;
	    			}
	    			ChatHelper.sendMsgWrap("Player's balance:"+bal.toString(), sender);
	    			return true;
	    		}
	    		if(args[0].equalsIgnoreCase("hasperm"))
	    		{
	    			if(args.length != 4)
	    			{
		    			ChatHelper.sendMsgWrap("Incorrect syntax. Syntax /bab hasperm <worldname> <playername> <nodename>", sender);
		    			return true;
	    			}
	    			Boolean msg = (hasPermission(args[1],args[2],args[3]));
	    			if(msg==null)
	    			{
		    			ChatHelper.sendMsgWrap("Player or world does not exist.", sender);
		    			return true;
	    			}
	    			ChatHelper.sendMsgWrap("Does player have permission:"+msg.toString(), sender);
	    			return true;
	    		}
	    		if(args[0].equalsIgnoreCase("commandtest"))
	    		{
	    			String cmd = commandName + " ";
	    			for(int i = 0; i < args.length;i++)
	    			{
	    				cmd = cmd + " " + args[i];
	    			}
	    			ChatHelper.sendMsgWrap(cmd, sender);
	    			return true;
	    		}
	    	}
	    }
	    return false;
	}
	
	boolean commandHandler(Player player, String[] args)
	{
		if(!pHandler.hasPerm(player.getWorld().getName(), player.getName(), "buyabilities.use")) 
		{
			ChatHelper.sendMsgWrap(COLOR_CHAT,"You do not have permissions to use this command.",player);
			return true;		
		}
		if(args.length == 0)
		{
			ChatHelper.sendMsgWrap(COLOR_CHAT,"Help: /bab [categories|category|page|current|buy|rent|info].",player);
			return true;			
		}
		else if(args[0].equalsIgnoreCase("categories"))
		{
			if(args.length > 2)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab categories <pageno>", player);
				return true;
			}
			String msg = "BuyAbilities: Category list: ";
			List<String> catList = settings.getCategories(player.getWorld().getName(), player);
			if(catList.isEmpty())
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT, "No categories accessible", player);
				return true;
			}
			msg = msg + catList.get(0);
			if(catList.size() > 1)
			{
				for(int i = 1; i < catList.size();i++)
				{
					msg = msg + ", " + catList.get(i);
				}
			}
			ChatHelper.sendMsgWrap(COLOR_CHAT, msg, player);
			return true;
		}
		else if(args[0].equalsIgnoreCase("category"))
		{
			if(args.length > 3)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab category <categoryname> <pageno>", player);
				return true;
			}
			int pageNo = 1;
			if(args.length == 3)
			{
				try
				{
					pageNo = Integer.parseInt(args[2]);
				}
				catch(NumberFormatException e)
				{
					pageNo = 1;
				}
			}
			List<String> abList = settings.getAbilites(args[1]);
			if(abList==null||abList.isEmpty())
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT, "No abilities found in that category.", player);
				return true;
			}
//			for(String ab : abList)
//			{
//				System.out.println(ab);
//			}
			ChatHelper.paging("BuyAbilities: Abilities in "+args[1], COLOR_CHAT, abList , 6, pageNo, player);
			return true;
		}
		else if(args[0].equalsIgnoreCase("page"))
		{
			if(args.length > 2)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab page <pageno>", player);
				return true;
			}
			int pageNo = 1;
			if(args.length == 2)
			{
				try
				{
					pageNo = Integer.parseInt(args[1]);
				}
				catch(NumberFormatException e)
				{
					pageNo = 1;
				}
			}
		
			List<String> abList = settings.getAllAbilities(player.getWorld().getName(), player);
			if(abList == null||abList.isEmpty())
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"No abilities found.", player);
				return true;
			}
			ChatHelper.paging("BuyAbilities: All abilities", COLOR_CHAT, abList , 6, pageNo, player);
			return true;
		}
		else if(args[0].equalsIgnoreCase("current"))
		{
			if(args.length > 2)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab current <pageno>", player);
				return true;
			}
			int pageNo = 1;
			if(args.length == 2)
			{
				try
				{
					pageNo = Integer.parseInt(args[1]);
				}
				catch(NumberFormatException e)
				{
					pageNo = 1;
				}
			}
			Set<PurchasedAbility> currentAb = abManager.getPlayer(player.getName());
			if(currentAb==null||currentAb.isEmpty())
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT, "No abilities are currently active.", player);
				return true;
			}
			List<String> curAb = new LinkedList<String>();
			for(PurchasedAbility p : currentAb)
			{
				curAb.add(p.toString());
			}
			ChatHelper.paging("BuyAbilities: All abilities", COLOR_CHAT, curAb , 6, pageNo, player);
			return true;
		}
		else if(args[0].equalsIgnoreCase("buy"))
		{
			if(args.length != 2)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab buy <abilityname>", player);
				return true;
			}
			String abilityName = args[1];
			Ability ab = settings.getAbility(abilityName);
			if(ab==null)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Ability not found.", player);
				return true;
			}
			if(!eHandler.deduct(player, ab.costs.buy.cost))
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Insufficient funds.", player);
				return true;
			}
			abManager.buyAbility(player.getWorld().getName(), player.getName(), abilityName);
			ChatHelper.sendMsgWrap(COLOR_CHAT,"Ability bought.", player);
			return true;
		}
		else if(args[0].equalsIgnoreCase("rent"))
		{
			if(args.length != 2)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab rent <abilityname>", player);
				return true;
			}
			String abilityName = args[1];
			Ability ab = settings.getAbility(abilityName);
			if(ab==null)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Ability not found.", player);
				return true;
			}
			if(!eHandler.deduct(player, ab.costs.buy.cost))
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Insufficient funds.", player);
				return true;
			}
			abManager.rentAbility(player.getWorld().getName(), player.getName(), abilityName);
			ChatHelper.sendMsgWrap(COLOR_CHAT,"Ability rented.", player);
			return true;
		}
		else if(args[0].equalsIgnoreCase("info"))
		{
			if(args.length != 2)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab info <abilityname>", player);
				return true;
			}
			String abilityName = args[1];
			Ability ab = settings.getAbility(abilityName);
			if(ab==null)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Ability not found.", player);
				return true;
			}
			String catString = "";
			List<String> catList = ab.categories;
			for(String cat : catList)
			{
				catString = catString + cat + "/";
			}
			if(catString.length() > 0)catString = catString.substring(0, catString.length() - 2);
			ChatHelper.sendMsgWrap(COLOR_CHAT,ab.name+" from category/categories "+catString, player);
			ChatHelper.sendMsgWrap(COLOR_CHAT,ab.info.desc, player);
			ChatHelper.sendMsgWrap(COLOR_CHAT,ab.costs.toString(), player);
			return true;
		}
		else if(args[0].equalsIgnoreCase("help"))
		{
			if(args.length != 2)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab help <abilityname>", player);
				return true;
			}
			String abilityName = args[1];
			Ability ab = settings.getAbility(abilityName);
			if(ab==null)
			{
				ChatHelper.sendMsgWrap(COLOR_CHAT,"Ability not found.", player);
				return true;
			}
			ChatHelper.sendMsgWrap(COLOR_CHAT,ab.info.help, player);
			return true;
		}
		else
		{
			ChatHelper.sendMsgWrap(COLOR_CHAT,"Incorrect syntax. Syntax /bab [categories|category|page|current|buy|rent|info|help].",player);
			return true;
		}
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
				eHandler = EconFactory.getInstance(type, econPlugin,this);
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
	
	public Boolean hasPermission(String world, String playerName, String perm)
	{
		if(world==null||playerName==null||perm==null) return null;
		if(getServer().getWorld(world)==null) return null;
		if(getServer().getPlayer(playerName)==null) return null;
		return pHandler.hasPerm(world, playerName, perm);
	}
	public Integer balance(String playerName)
	{
		if(playerName==null) return null;
		if(getServer().getPlayer(playerName)==null) return null;
		return (int) eHandler.getBalance(getServer().getPlayer(playerName));
	}
	
	
	void decrement(Player player, String name) {
		//TODO: Decrement usage counter using separate thread
		
	}
	
}

