package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import com.nijikokun.register.payment.Methods;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Ability;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Settings;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.IStorage;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.Storage;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.StorageFactory;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.StoredAbility;
import com.rcjrrjcr.bukkitplugins.util.PluginStruct;
import com.rcjrrjcr.bukkitplugins.util.RcjrPlugin;
import com.rcjrrjcr.bukkitplugins.util.SearchHelper;
import com.rcjrrjcr.bukkitplugins.util.chathelper.ChatHelper;
import com.rcjrrjcr.bukkitplugins.util.economyinterface.EconFactory;
import com.rcjrrjcr.bukkitplugins.util.economyinterface.EconPlugin;
import com.rcjrrjcr.bukkitplugins.util.economyinterface.IEconHandler;
import com.rcjrrjcr.bukkitplugins.util.permissionsinterface.IPermHandler;
import com.rcjrrjcr.bukkitplugins.util.permissionsinterface.PermFactory;
import com.rcjrrjcr.bukkitplugins.util.permissionsinterface.PermPlugin;

/**
 * BuyAbilities for Bukkit
 * 
 * @author rcjrrjcr
 */
public class BuyAbilities extends RcjrPlugin {
    public static final Logger log = Logger.getLogger(BuyAbilities.class.toString());
    
    /* for local debugging
    static {
    	log.setLevel(java.util.logging.Level.FINE);
    }
    */

    private static final int MISS_COUNT_THRESHOLD = 5;
    private static final int DL_THRESHOLD = 5;
    private static final ChatColor COLOR_CHAT = ChatColor.GOLD;
    
    // not fond of static variables and methods, they make the (perhaps false?) assumption
    // that there's only one copy, instead of enforcing it explicitly via singleton pattern.
    // However I don't know enough about Bukkit yet to know if multiple instances of a plugin
    // might be running at any time, and since this is what I've seen other plugins do, I'll
    // stick with this for now. -morganm
    private static Server server = null;
    private static BuyAbilities plugin = null;
    
    private BuyAbilitiesServerListener serverListener;
    private BuyAbilitiesPlayerListener playerListener;
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    IEconHandler eHandler;
    IStorage storage;
    IPermHandler pHandler;
    PluginManager pm;
    public Settings settings;
    AbilityManager abManager;
    BukkitScheduler scheduler;
    BuyAbilitiesChecker checker;
    private final Integer checkDelay = 5;
    private final Integer checkInterval = 10;
    
    private Methods Methods = new Methods();

    /**
     * {@inheritDoc}
     * 
     * @author rcjrrjcr
     */
    @Override
    public void onEnable() {
    	server = getServer();
    	plugin = this;

        pm = getServer().getPluginManager();
        scheduler = getServer().getScheduler();
        try {
            active = hook();
        } catch (Exception e) {
            e.printStackTrace();
            pm.disablePlugin(this);
        }

        PluginDescriptionFile pdfFile = this.getDescription();
        if (!(active.getStatus())) {
            log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is inactive.");
        } else {
        	log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
        }
        serverListener = new BuyAbilitiesServerListener(this, active);
        playerListener = new BuyAbilitiesPlayerListener(this);

        loadSettings();
        
        // Load data from the database
        abManager = new AbilityManager(this);
        try {
            abManager.load(storage.getData());
        } catch (Exception e) {
            System.out.println("Error reading Abilities data.");
            e.printStackTrace();
        }
        
        // Start the checker thread
        checker = new BuyAbilitiesChecker(this, checkInterval);
        scheduler.scheduleAsyncRepeatingTask(this, checker, checkDelay, checkInterval);

        // Register our events
        getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);

        getServer().getPluginManager().registerEvent(Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
    }
    
    private void loadSettings()
    {
    	if( settings != null )
    		log.info("[BuyAbilities] Reloading settings");
        try {
            settings = new Settings(this, "plugins" + File.separator + "BuyAbilities" + File.separator + "costs.yml");
        } catch (Exception e) {
            e.printStackTrace();
            pm.disablePlugin(this);
        }
    }

    /**
     * Bukkit-called method. Prints notification of the disabling of the plugin
     * to console.
     * 
     * @author rcjrrjcr
     */
    @Override
    public void onDisable() {
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

    boolean hookPerm() throws Exception {
        boolean perm = true;
        Plugin permPlugin = null;
        
        // look for GroupManager first, since it can emulate Permissions, but it's emulated mode
        // doesn't work with this mod.
        permPlugin = pm.getPlugin("GroupManager");
        if (permPlugin != null) {
        	if( !permPlugin.isEnabled() ) {
                perm = false;
                pHandler = PermFactory.getInstance(PermPlugin.CACHE, null, this);
        	}
        	else {
        		pHandler = PermFactory.getInstance(PermPlugin.GroupManager, permPlugin, this);
        	}
        }

        // if we didn't find GroupManager, then look for Permissions
        if( permPlugin == null || pHandler == null ) {
        	permPlugin = pm.getPlugin("Permissions");

        	if (permPlugin == null) {
        		throw new Exception("No Permission Plugin found!");
        	} else if (!(permPlugin.isEnabled())) {
        		perm = false;
        		pHandler = PermFactory.getInstance(PermPlugin.CACHE, null, this);
        	} else {
        		pHandler = PermFactory.getInstance(PermPlugin.PermYeti, permPlugin, this);
        	}
        }
        
        return perm;
    }

    boolean hookEcon(PluginEvent event) {
        boolean econ = false;

        //Economy plugin
        if(!this.Methods.hasMethod()){
            if(this.Methods.setMethod(event.getPlugin())){
            	EconomyManager em = new EconomyManager();
            	eHandler = em;
                em.economy = this.Methods.getMethod();
                log.info("[BuyAbilities] " + em.economy.getName() + " version " + em.economy.getVersion() + " loaded.");
                econ = true;
            }
        }
        
        /*
        Plugin econPlugin = null;
        econPlugin = pm.getPlugin("iConomy");
        if (econPlugin == null) {
            econPlugin = pm.getPlugin("Essentials");
            if (econPlugin == null) {
                econPlugin = pm.getPlugin("BOSEconomy");
                if (econPlugin == null) {
                    throw new Exception("No Economy Plugin found!");
                } else if (!(econPlugin.isEnabled())) {
                    econ = false;
                    eHandler = null;
                } else {
                    eHandler = EconFactory.getInstance(EconPlugin.BOS, econPlugin, this);
                }
            } else if (!(econPlugin.isEnabled())) {
                econ = false;
                eHandler = null;
            } else {
                eHandler = EconFactory.getInstance(EconPlugin.ESSECO, econPlugin, this);
            }
        } else if (!(econPlugin.isEnabled())) {
            econ = false;
        } else {
            eHandler = EconFactory.getInstance(EconPlugin.IC4, econPlugin, this);
        }
        */
        
        return econ;
    }

    PluginStruct hook() throws Exception {

        storage = StorageFactory.getInstance(Storage.EBEANS, this);
        boolean perm = hookPerm();
        // Econ is enabled in ENABLE_PLUGIN event now
//        boolean econ = hookEcon();
        return new PluginStruct(perm, false);
    }

    /**
     * Bukkit-called method. Processes commands that start with /bab or /buyab
     * 
     * @return true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
    	
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ("buyabilities".equals(commandName) || commandName.equals("buyab") || commandName.equals("bab")) {
                return commandHandler(player, args);
            }
        } else {
            if ("buyabilities".equals(commandName) || commandName.equalsIgnoreCase("bab") || commandName.equalsIgnoreCase("buyab")) {
                if (args.length == 0) {
                    ChatHelper.sendMsgWrap("Incorrect syntax. Syntax /bab [hasperm|balance|listall].", sender);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("listall")) {
                    ChatHelper.sendMsgWrap(abManager.currentAbilities.toString(), sender);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("balance")) {
                    if (args.length != 2) {
                        ChatHelper.sendMsgWrap("Incorrect syntax. Syntax /bab balance <playername>", sender);
                        return true;
                    }
                    Integer bal = balance(args[1]);
                    if (bal == null) {
                        ChatHelper.sendMsgWrap("Player does not exist.", sender);
                        return true;
                    }
                    ChatHelper.sendMsgWrap("Player's balance:" + bal.toString(), sender);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("hasperm")) {
                    if (args.length != 4) {
                        ChatHelper.sendMsgWrap("Incorrect syntax. Syntax /bab hasperm <worldname> <playername> <nodename>", sender);
                        return true;
                    }
                    Boolean msg = (hasPermission(args[1], args[2], args[3]));
                    if (msg == null) {
                        ChatHelper.sendMsgWrap("Player or world does not exist.", sender);
                        return true;
                    }
                    ChatHelper.sendMsgWrap("Does player have permission:" + msg.toString(), sender);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("commandtest")) {
                    String cmd = commandName + " ";
                    for (int i = 0; i < args.length; i++) {
                        cmd = cmd + " " + args[i];
                    }
                    ChatHelper.sendMsgWrap(cmd, sender);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("dldist")) {
                    if (args.length != 3) {
                        ChatHelper.sendMsgWrap("Incorrect syntax. Syntax /bab dldist <word1> <word2>", sender);
                        return true;
                    }
                    ChatHelper.sendMsgWrap(String.valueOf(SearchHelper.damlev(args[1], args[2])), sender);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("status")) {
                    log.info("Econ hooked: " + String.valueOf(active.isEconActive()));
                    log.info("Perm hooked: " + String.valueOf(active.isPermActive()));
                    return true;
                }
                else if (args[0].equalsIgnoreCase("reload")) {
                    loadSettings();
                    return true;
                }
            }
        }
        
        return false;
    }

    boolean commandHandler(Player player, String[] args) {
        if (!pHandler.hasPerm(player.getWorld().getName(), player.getName(), "buyabilities.use")) {
            ChatHelper.sendMsgWrap(COLOR_CHAT, "You do not have permission to use this command.", player);
            return true;
        }
        if (args.length == 0) {
            ChatHelper.sendMsgWrap(COLOR_CHAT, "Help: /bab [categories|category|page|current|buy|rent|rentuse|info].", player);
            return true;
        } else if (args[0].equalsIgnoreCase("categories")) {
            if (args.length > 1) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab categories", player);
                return true;
            }
            String msg = "BuyAbilities: Category list: ";
            List<String> catList = settings.getCategories(player.getWorld().getName(), player);
            if (catList.isEmpty()) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "No categories accessible", player);
                return true;
            }
            msg = msg + catList.get(0);
            if (catList.size() > 1) {
                for (int i = 1; i < catList.size(); i++) {
                    msg = msg + ", " + catList.get(i);
                }
            }
            ChatHelper.sendMsgWrap(COLOR_CHAT, msg, player);
            return true;
        } else if (args[0].equalsIgnoreCase("category")) {
            if (args.length == 1) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab category <categoryname> <pageno>", player);
                return true;
            }
            int pageNo = 1;
            boolean defaultPage = false;
            try {
                pageNo = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException e) {
                defaultPage = true;
            }
            String categoryName = args[1];
            int catNameLength = defaultPage ? args.length : args.length - 1;
            for (int i = 2; i < catNameLength; i++) {
                categoryName = categoryName + " " + args[i];
            }

            if (!(settings.canAccess(categoryName, player.getWorld().getName(), player))) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Unable to access category.", player);
                return true;
            }
            List<String> abList = settings.getAbilites(categoryName);
            if (abList == null || abList.isEmpty()) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "No abilities found in that category.", player);
                List<String> categoryMisses = settings.getCategoryMisses(categoryName, DL_THRESHOLD);
                if (categoryMisses.isEmpty())
                    return true;
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Did you mean any of these?", player);
                int max = Math.min(MISS_COUNT_THRESHOLD, categoryMisses.size());
                for (int i = 0; i < max; i++) {
                    ChatHelper.sendMsgWrap(COLOR_CHAT, categoryMisses.get(i), player);
                }
                return true;
            }
            // for(String ab : abList)
            // {
            // System.out.println(ab);
            // }
            ChatHelper.paging("BuyAbilities: Abilities in " + categoryName, COLOR_CHAT, abList, 6, pageNo, player);
            return true;
        } else if (args[0].equalsIgnoreCase("page")) {
            if (args.length > 2) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab page <pageno>", player);
                return true;
            }
            int pageNo = 1;
            if (args.length == 2) {
                try {
                    pageNo = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    pageNo = 1;
                }
            }

            List<String> abList = settings.getAllAbilities(player.getWorld().getName(), player);
            if (abList == null || abList.isEmpty()) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "No abilities found.", player);
                return true;
            }
            ChatHelper.paging("BuyAbilities: All abilities", COLOR_CHAT, abList, 6, pageNo, player);
            return true;
        } else if (args[0].equalsIgnoreCase("current")) {
            if (args.length > 2) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab current <pageno>", player);
                return true;
            }
            int pageNo = 1;
            if (args.length == 2) {
                try {
                    pageNo = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    pageNo = 1;
                }
            }
            Set<PurchasedAbility> currentAb = abManager.getPlayer(player.getName());
            if (currentAb == null || currentAb.isEmpty()) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "No abilities are currently active.", player);
                return true;
            }
            List<String> curAb = new LinkedList<String>();
            for (PurchasedAbility p : currentAb) {
                curAb.add(p.toString());
            }
            ChatHelper.paging("BuyAbilities: All abilities", COLOR_CHAT, curAb, 6, pageNo, player);
            return true;
        } else if (args[0].equalsIgnoreCase("buy")) {
            if (args.length < 2) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab buy <abilityname>", player);
                return true;
            }
            String abilityName = args[1];
            int catNameLength = args.length;
            for (int i = 2; i < catNameLength; i++) {
                abilityName = abilityName + " " + args[i];
            }
            Ability ab = settings.getAbility(abilityName);
            if (ab == null) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability not found.", player);
                List<String> abilityMisses = settings.getAbilityMisses(abilityName, DL_THRESHOLD);
                if (abilityMisses.isEmpty())
                    return true;
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Did you mean any of these?", player);
                int max = Math.min(MISS_COUNT_THRESHOLD, abilityMisses.size());
                for (int i = 0; i < max; i++) {
                    ChatHelper.sendMsgWrap(COLOR_CHAT, abilityMisses.get(i), player);
                }
                return true;
            }
            if (abManager.hasPlayerAbility(player.getWorld().getName(), player.getName(), abilityName)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "You already have this ability.", player);
                return true;
            }
            if (!settings.canPurchase(abilityName, player.getWorld().getName(), player)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "You cannot access this ability.", player);
                return true;
            }
            if (!ab.costs.canBuy) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Unable to buy this ability.", player);
                return true;
            }
            if (!eHandler.deduct(player, ab.costs.buyCost)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Insufficient funds.", player);
                return true;
            }
            abManager.buyAbility(player.getWorld().getName(), player.getName(), abilityName);
            ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability bought.", player);
            return true;
        } else if (args[0].equalsIgnoreCase("rent")) {
            if (args.length < 2) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab rent <abilityname>", player);
                return true;
            }
            String abilityName = args[1];
            int catNameLength = args.length;
            for (int i = 2; i < catNameLength; i++) {
                abilityName = abilityName + " " + args[i];
            }
            Ability ab = settings.getAbility(abilityName);
            if (ab == null) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability not found.", player);
                List<String> abilityMisses = settings.getAbilityMisses(abilityName, DL_THRESHOLD);
                if (abilityMisses.isEmpty())
                    return true;
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Did you mean any of these?", player);
                int max = Math.min(MISS_COUNT_THRESHOLD, abilityMisses.size());
                for (int i = 0; i < max; i++) {
                    ChatHelper.sendMsgWrap(COLOR_CHAT, abilityMisses.get(i), player);
                }
                return true;
            }
            if (abManager.hasPlayerAbility(player.getWorld().getName(), player.getName(), abilityName)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "You already have this ability.", player);
                return true;
            }
            if (!settings.canPurchase(abilityName, player.getWorld().getName(), player)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "You cannot access this ability.", player);
                return true;
            }
            if (!ab.costs.canRent) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Unable to rent this ability.", player);
                return true;
            }
            if (!eHandler.deduct(player, ab.costs.rentCost)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Insufficient funds.", player);
                return true;
            }
            abManager.rentAbility(player.getWorld().getName(), player.getName(), abilityName);
            ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability rented.", player);
            return true;
        } else if (args[0].equalsIgnoreCase("rentuse")) {
            if (args.length < 2) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab rentuse <abilityname>", player);
                return true;
            }
            String abilityName = args[1];
            int catNameLength = args.length;
            for (int i = 2; i < catNameLength; i++) {
                abilityName = abilityName + " " + args[i];
            }
            Ability ab = settings.getAbility(abilityName);
            if (ab == null) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability not found.", player);
                List<String> abilityMisses = settings.getAbilityMisses(abilityName, DL_THRESHOLD);
                if (abilityMisses.isEmpty())
                    return true;
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Did you mean any of these?", player);
                int max = Math.min(MISS_COUNT_THRESHOLD, abilityMisses.size());
                for (int i = 0; i < max; i++) {
                    ChatHelper.sendMsgWrap(COLOR_CHAT, abilityMisses.get(i), player);
                }
                return true;
            }
            if (abManager.hasPlayerAbility(player.getWorld().getName(), player.getName(), abilityName)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "You already have this ability.", player);
                return true;
            }
            if (!settings.canPurchase(abilityName, player.getWorld().getName(), player)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "You cannot access this ability.", player);
                return true;
            }
            if (!ab.costs.canUse) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Unable to buy uses of this ability.", player);
                return true;
            }
            if (!eHandler.deduct(player, ab.costs.useCost)) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Insufficient funds.", player);
                return true;
            }
            abManager.useCountAbility(player.getWorld().getName(), player.getName(), abilityName);
            ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability rented.", player);
            return true;
        } else if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab info <abilityname>", player);
                return true;
            }
            String abilityName = args[1];
            int catNameLength = args.length;
            for (int i = 2; i < catNameLength; i++) {
                abilityName = abilityName + " " + args[i];
            }
            Ability ab = settings.getAbility(abilityName);
            if (ab == null) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability not found.", player);
                List<String> abilityMisses = settings.getAbilityMisses(abilityName, DL_THRESHOLD);
                if (abilityMisses.isEmpty())
                    return true;
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Did you mean any of these?", player);
                int max = Math.min(MISS_COUNT_THRESHOLD, abilityMisses.size());
                for (int i = 0; i < max; i++) {
                    ChatHelper.sendMsgWrap(COLOR_CHAT, abilityMisses.get(i), player);
                }
                return true;
            }

            // changed to use StringBuffer, much more efficient. Also fixed bug with dangling "/"
            // on end of list. -morganm 6/3/11
            String catString = "";
            StringBuffer sb = new StringBuffer();
            Set<String> catSet = ab.categories;
            if (catSet != null && !catSet.isEmpty()) {
                catString = ": Categories ";
                for (String category : catSet) {
                	if(sb.length() > 0 ) {
                		sb.append("/");
                	}
                	sb.append(category);
                }
                catString = ": Categories " + sb.toString();
            }
            ChatHelper.sendMsgWrap(COLOR_CHAT, ab.name + " categories " + catString, player);
            ChatHelper.sendMsgWrap(COLOR_CHAT, ab.info.desc, player);
            ChatHelper.sendMsgWrap(COLOR_CHAT, ab.costs.shortString(), player);
            return true;
        } else if (args[0].equalsIgnoreCase("help")) {
            if (args.length < 2) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab help <abilityname>", player);
                return true;
            }
            String abilityName = args[1];
            int catNameLength = args.length;
            for (int i = 2; i < catNameLength; i++) {
                abilityName = abilityName + " " + args[i];
            }
            Ability ab = settings.getAbility(abilityName);
            if (ab == null) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Ability not found.", player);
                List<String> abilityMisses = settings.getAbilityMisses(abilityName, DL_THRESHOLD);
                if (abilityMisses.isEmpty())
                    return true;
                ChatHelper.sendMsgWrap(COLOR_CHAT, "Did you mean any of these?", player);
                int max = Math.min(MISS_COUNT_THRESHOLD, abilityMisses.size());
                for (int i = 0; i < max; i++) {
                    ChatHelper.sendMsgWrap(COLOR_CHAT, abilityMisses.get(i), player);
                }
                return true;
            }
            ChatHelper.sendMsgWrap(COLOR_CHAT, ab.info.help, player);
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!pHandler.hasPerm(player.getWorld().getName(), player.getName(), "buyabilities.admin.reload")) {
                ChatHelper.sendMsgWrap(COLOR_CHAT, "You do not have permission to use this command.", player);
            }
            else {
            	loadSettings();
                ChatHelper.sendMsgWrap(COLOR_CHAT, "BuyAbilities settings reloaded.", player);
            }
        	return true;
        } else {
            ChatHelper.sendMsgWrap(COLOR_CHAT, "Incorrect syntax. Syntax /bab [categories|category|page|current|buy|rent|rentuse|info|help].", player);
            return true;
        }
    }

    /**
     * Print all available categories to player's chat
     * 
     * @param player
     */

    void setPermissions(Plugin permPlugin, PermPlugin type) {
        if (permPlugin != null && type != PermPlugin.NONE) {
            try {
                pHandler = PermFactory.getInstance(type, permPlugin, this);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            log.info("BuyAbilities: Permissions hooked!");
            active.setPerm(true);
            if (active.getStatus())
                log.info("BuyAbilities: BuyAbilities active.");
        }
        if (!active.getStatus())
            log.info("BuyAbilities: BuyAbilities inactive.");
    }

    void setEconomy(Plugin econPlugin, EconPlugin type) {
        if ((econPlugin != null) && (type != EconPlugin.NONE)) {
            try {
                eHandler = EconFactory.getInstance(type, econPlugin, this);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            log.info("BuyAbilities: Economy hooked!");
            active.setEcon(true);
            if (active.getStatus())
                log.info("BuyAbilities: BuyAbilities active.");
        }
        if (!active.getStatus())
            log.info("BuyAbilities: BuyAbilities inactive.");
    }

    public Boolean hasPermission(String world, String playerName, String perm) {
        if (world == null || playerName == null || perm == null) {
            log.warning("Arguments are null!");
            return false;
        }
        if (getServer().getWorld(world) == null) {

            log.warning("World does not exist!");
            return false;
        }
        if (getServer().getPlayer(playerName) == null) {
            log.warning("Player not online!");
            return false;
        }
        return pHandler.hasPerm(world, playerName, perm);
    }

    public Integer balance(String playerName) {
        if (playerName == null)
            return null;
        if (getServer().getPlayer(playerName) == null)
            return null;
        return (int) eHandler.getBalance(getServer().getPlayer(playerName));
    }

    /** This is called asynchronously to decrement on-use abilities.  It matches regex's against
     * the declares regex for those commands and decrements the use count if it finds any matches. 
     * 
     * @param cmdLine
     * @param playerName
     * @param worldName
     */
    void commandPreprocess(String cmdLine, String playerName, String worldName) {
//    	Level logLevel = log.getLevel();
//    	log.setLevel(Level.FINE);
    	
        log.fine("commandPreprocess called! cmdLine = "+cmdLine);
        Set<Pattern> rgxs = settings.getAllCmds();
        log.fine(rgxs.toString());
        Set<Ability> abSet = new HashSet<Ability>();
        for (Pattern rgx : rgxs) {
        	log.fine("Checking: "+rgx);
            if (rgx.matcher(cmdLine).lookingAt()) {
            	log.fine("Matched!");
                abSet.addAll(settings.getCmdAbility(rgx));
            }
        }
        
        log.fine(abSet.toString());
        for (Ability ab : abSet) {
            // System.out.println("World: "+
            // worldName+"Player: "+playerName+"Ability: "+ab.name);
            abManager.decrement(worldName, playerName, ab.name);
        }
        
//		log.setLevel(logLevel);
    }

    void processLogonLogoff(PlayerEvent event) throws Exception {
        String playerName = event.getPlayer().getName();

        switch (event.getType()) {
        case PLAYER_JOIN:
            abManager.loadPlayer(storage.getPlayerData(playerName), playerName);
            break;
        case PLAYER_KICK:
            // Don't save
            abManager.saveAndUnloadPlayer(playerName);
            break;
        case PLAYER_QUIT:
            storage.writePlayerData(abManager.saveAndUnloadPlayer(playerName), playerName);
            break;
        default:
            throw new Exception("BuyAbilities: Non-Player(Join|Kick|Quit)Event passed to processLogonLogoff!");
        }
        return;
    }

    public void initDB() {
        installDDL();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> classList = new LinkedList<Class<?>>();
        classList.add(StoredAbility.class);
        return classList;
    }
    
    public static Server getBukkitServer() {
        return server;
    }
    
    public static BuyAbilities getPlugin() {
        return plugin;
    }
    
    public Settings getSettings() {
    	return settings;
    }
}

class BuyAbilitiesChecker implements Runnable {

    private BuyAbilities origin;
    private final int interval;

    public BuyAbilitiesChecker(BuyAbilities origin, final int interval) {
        this.origin = origin;
        this.interval = interval;
    }

    @Override
    public void run() {
        origin.abManager.update(interval);
    }

}
