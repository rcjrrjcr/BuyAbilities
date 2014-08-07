/**
 * 
 */
package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.nijikokun.register.payment.Method;
import com.rcjrrjcr.bukkitplugins.util.economyinterface.IEconHandler;

/** Code originally copied from @Acrobot's great iConomyChestShop plugin.
 * Implements a bridge between @Rcjrrjcr's IEconHandler interface and
 * @Nijikokun's Register module.
 * 
 * @author morganm
 *
 */
public class EconomyManager implements IEconHandler {
    private static final Logger log = BuyAbilities.log;

    public Method economy;

    public void add(String name, Integer amount){
        economy.getAccount(name).add(amount);
    }
    
    public void add(Player player, Integer amount){
        add(player.getName(), amount);
    }
    
    public boolean deduct(String name, Integer amount){
    	if( !economy.getAccount(name).hasEnough(amount) )
    		return false;
    	
    	log.fine("economy.hasEnough for "+name+" is true (amount: "+amount+")");
        return economy.getAccount(name).subtract(amount);
    }
    public boolean deduct(Player player, Integer amount){
    	return deduct(player.getName(), amount);
    }
    
    public double getBalance(String name){
        return economy.getAccount(name).balance();
    }
    public double getBalance(Player player){
    	return getBalance(player.getName());
    }

}
