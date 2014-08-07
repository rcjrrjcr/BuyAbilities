package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import java.util.logging.Logger;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.rcjrrjcr.bukkitplugins.util.PluginStruct;
import com.rcjrrjcr.bukkitplugins.util.permissionsinterface.PermPlugin;

//import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.EconomicPlugins.EconPlugin;
/**
 * Bukkit server listener. Used to detect plugin enable/disable events and hook
 * into plugins
 * 
 * @author rcjrrjcr
 * 
 */
public class BuyAbilitiesServerListener extends ServerListener {
    private static final Logger log = BuyAbilities.log;

    private final BuyAbilities plugin;
    private boolean permActive;
    private boolean econActive;

    public BuyAbilitiesServerListener(BuyAbilities instance, PluginStruct hooks) {
        plugin = instance;
        permActive = hooks.isPermActive();
        econActive = hooks.isEconActive();
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        log.fine("BuyAbilities: PluginEnabled event detected!");
        Plugin permPlugin = null;
//        Plugin econPlugin = null;

        if (!permActive) {
            // permPlugin =
            // plugin.getServer().getPluginManager().getPlugin("GroupManager");
            // if(permPlugin != null)
            // {
            // if(permPlugin.isEnabled())
            // {
            // System.out.println("BuyAbilities: Hooking into GroupManager.");
            // plugin.setPermissions(permPlugin, PermPlugin.GroupManager);
            // permActive = true;
            // return;
            // }
            // }
            // else
            // {
            permPlugin = plugin.getServer().getPluginManager()
                    .getPlugin("Permissions");
            if (permPlugin != null && permPlugin.isEnabled()) {
                log.info("BuyAbilities: Hooking into Permissions.");
                plugin.setPermissions(permPlugin, PermPlugin.PermYeti);
                permActive = true;
                return;
            }
            // }
        }

        if (!econActive) {
            plugin.active.setEcon(plugin.hookEcon(event));
            
            /*
            econPlugin = plugin.getServer().getPluginManager()
                    .getPlugin("iConomy");
            if (econPlugin != null) {
                if (econPlugin.isEnabled()) {
                    System.out.println("BuyAbilities: Hooking into iConomy.");
                    plugin.setEconomy(econPlugin, EconPlugin.IC4);
                    econActive = true;
                    return;
                }
            } else {
                econPlugin = plugin.getServer().getPluginManager()
                        .getPlugin("Essentials");
                if (econPlugin != null && econPlugin.isEnabled()) {
                    System.out
                            .println("BuyAbilities: Hooking into EssentialsEco.");
                    plugin.setEconomy(econPlugin, EconPlugin.ESSECO);
                    econActive = true;
                    return;
                } else {
                    econPlugin = plugin.getServer().getPluginManager()
                            .getPlugin("BOSEconomy");
                    if (econPlugin != null && econPlugin.isEnabled()) {
                        System.out
                                .println("BuyAbilities: Hooking into BOSEconomy.");
                        plugin.setEconomy(econPlugin, EconPlugin.BOS);
                        econActive = true;
                        return;
                    }
                }
            }
            */
        }

        return;
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (permActive) {
            String name = event.getPlugin().getDescription().getName();
            if (name.equals("Permissions")) {
                try {
                    plugin.active.setPerm(plugin.hookPerm());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (econActive) {
            String name = event.getPlugin().getDescription().getName();
            if (name.equals("iConomy") || name.equals("Essentials")) {
                try {
                    plugin.active.setEcon(plugin.hookEcon(event));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
