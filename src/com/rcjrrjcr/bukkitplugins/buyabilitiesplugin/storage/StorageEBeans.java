package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebean.EbeanServer;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

public class StorageEBeans implements IStorage {
    private final BuyAbilities origin;

    public StorageEBeans(BuyAbilities plugin) {
        this.origin = plugin;
        try {
            EbeanServer db = origin.getDatabase();
            if( db == null ) {
            	throw new NullPointerException("origin.getDatabase() returned null EbeanServer!");
            }
            
            db.find(StoredAbility.class).findRowCount();
        } catch (PersistenceException ex) {
//        	System.out.println("Caught exception:");
//        	ex.printStackTrace();
        	
            System.out.println("Installing database for "
                    + origin.getDescription().getName()
                    + " due to first time usage");
            origin.initDB();
        }
    }

    /** Convert a StoredAbility set to a PurchasedAbility set.
     * 
     * @param sas
     * @return
     */
    private Set<PurchasedAbility> convertSetFromDB(Set<StoredAbility> sas) {
    	Set<PurchasedAbility> pas = new HashSet<PurchasedAbility>();
        for(StoredAbility sa : sas) {
        	pas.add(new PurchasedAbility(sa));
        }
        return pas;
    }
    
    /** Convert a PurchasdAbility set to a StoredAbility set.
     * 
     * @param sas
     * @return
     */
    private Set<StoredAbility> convertSetToDB(Set<PurchasedAbility> pas) {
    	Set<StoredAbility> sas = new HashSet<StoredAbility>();
        for(PurchasedAbility pa : pas) {
        	sas.add(new StoredAbility(pa));
        }
        return sas;
    }
    
    @Override
    public Set<PurchasedAbility> getData() {
        return convertSetFromDB(origin.getDatabase().find(StoredAbility.class).findSet());
    }

    @Override
    public void writeData(Set<PurchasedAbility> data) throws IOException {
    	Set<StoredAbility> sas = convertSetToDB(data);
    	
        EbeanServer server = origin.getDatabase();
        server.beginTransaction();
        server.delete(origin.getDatabase().find(StoredAbility.class).findSet());
        server.save(sas);
        server.commitTransaction();
    }

    @Override
    public Set<PurchasedAbility> getPlayerData(String playerName) {
    	return convertSetFromDB(
    			origin.getDatabase().find(StoredAbility.class).where().ieq("playerName", playerName).findSet()
    			);
        
    }

    @Override
    public void writePlayerData(Set<PurchasedAbility> data, String playerName)
            throws IOException {
    	Set<StoredAbility> sas = convertSetToDB(data);
    	
        EbeanServer server = origin.getDatabase();
        server.beginTransaction();
        server.delete(origin.getDatabase().find(StoredAbility.class).where()
                .ieq("playerName", playerName).findSet());
        server.save(sas);
        server.commitTransaction();
    }

}
