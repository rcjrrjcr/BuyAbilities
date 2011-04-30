package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.io.IOException;
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
            origin.getDatabase().find(PurchasedAbility.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing database for "
                    + origin.getDescription().getName()
                    + " due to first time usage");
            origin.initDB();
        }
    }

    @Override
    public Set<PurchasedAbility> getData() {
        return origin.getDatabase().find(PurchasedAbility.class).findSet();
    }

    @Override
    public void writeData(Set<PurchasedAbility> data) throws IOException {
        EbeanServer server = origin.getDatabase();
        server.beginTransaction();
        server.delete(origin.getDatabase().find(PurchasedAbility.class).findSet());
        server.save(data);
        server.commitTransaction();
    }

    @Override
    public Set<PurchasedAbility> getPlayerData(String playerName) {
        return origin.getDatabase().find(PurchasedAbility.class).where().ieq("playerName", playerName).findSet();
    }

    @Override
    public void writePlayerData(Set<PurchasedAbility> data, String playerName)
            throws IOException {
        EbeanServer server = origin.getDatabase();
        server.beginTransaction();
        server.delete(origin.getDatabase().find(PurchasedAbility.class).where()
                .ieq("playername", playerName).findSet());
        server.save(data);
        server.commitTransaction();
    }

}
