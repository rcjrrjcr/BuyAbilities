/**
 * 
 */
package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.util.LinkedList;
import java.util.List;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;

/** Code shamelessly stolen from @Acrobot's iConomyChestShop plugin.
 * 
 * @author morganm
 *
 */
public class DBQueue implements Runnable {
    private static List<StoredAbility> queue = new LinkedList<StoredAbility>();
    
    public static void addToQueue(StoredAbility t){
        queue.add(t);
    }
    
    public static void saveQueue(){
        BuyAbilities.getBukkitServer().getScheduler().scheduleAsyncDelayedTask(BuyAbilities.getPlugin(), new DBQueue());
    }

    public static void saveQueueOnExit(){
    	BuyAbilities.getPlugin().getDatabase().save(queue);
        queue.clear();
    }
    
    public void run(){
    	BuyAbilities.getPlugin().getDatabase().save(queue);
        queue.clear();
    }
}
