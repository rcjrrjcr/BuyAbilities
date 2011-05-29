package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

//import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Ability;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.PurchasedAbilityType;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.StoredAbility;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.StringSetWrapper;

//TODO: Shift Ebeans annotations to StoredAbility, create String wrapper for ebeans

public class PurchasedAbility implements Comparable<PurchasedAbility>, Cloneable {

    private String abilityName;

    private String extName;

    private Set<String> perms;

    private String playerName;

    private String world;

    private PurchasedAbilityType type;

    private int duration;

    @Override
    public int compareTo(PurchasedAbility o) {
        return new Integer(duration).compareTo(o.duration);
    }

    @Override
    public String toString() {
        if (type == PurchasedAbilityType.RENT)
            return abilityName + ": " + extName + " with " + new Integer(duration / 20).toString() + "s left in world " + world;
        if (type == PurchasedAbilityType.BUY)
            return abilityName + ": " + extName + " in world " + world;
        if (type == PurchasedAbilityType.USE)
            return abilityName + ": " + extName + " with " + duration + " uses left in world " + world;
        return "Error: " + abilityName + "type not specified.";
    }

    public PurchasedAbility() {
        abilityName = new String();
        extName = new String();
        perms = new HashSet<String>();
        playerName = new String();
        world = new String();
        duration = 0;
    }

    public PurchasedAbility(Ability a, String playerName, String worldName, PurchasedAbilityType type) {
        abilityName = a.name;
        extName = a.info.extName;
        perms = a.perms;
        this.playerName = playerName;
        this.world = worldName;
        this.type = type;
        duration = 0;
        if (type == PurchasedAbilityType.RENT)
            duration = a.costs.rentDuration;
        if (type == PurchasedAbilityType.USE)
            duration = a.costs.useCount;
    }

    private PurchasedAbility(PurchasedAbility p) {
        this.abilityName = p.abilityName;
        this.extName = p.extName;
        this.perms = new HashSet<String>();
        this.perms.addAll(p.perms);
        this.playerName = p.playerName;
        this.world = p.world;
        this.duration = p.duration;
        this.type = p.type;
    }

    public PurchasedAbility(StoredAbility p) {
        this.abilityName = p.getAbilityName();
        this.extName = p.getExtName();
        this.perms = new HashSet<String>();
        this.perms = new StringSetWrapper(p.getPerms()).getSet();
//        this.perms = p.getPerms();
//        for (StringWrapper wrapped : p.getPerms()) {
//            this.perms.add(wrapped.getString());
//        }
        this.playerName = p.getPlayerName();
        this.world = p.getWorld();
        this.duration = p.getDuration();
        this.type = p.getType();
    }

    public StoredAbility toStore() {
        StoredAbility s = new StoredAbility(this);
        
//        s.setAbilityName(abilityName);
//        s.setDuration(duration);
//        s.setExtName(extName);
//        List<StringWrapper> permList = new ArrayList<StringWrapper>(perms.size());
//        for (String perm : perms)
//            permList.add(new StringWrapper(perm, s));
//        s.setPerms(permList);
//        s.setPlayerName(playerName);
//        s.setType(type);
//        s.setWorld(world);
        
        return s;
    }

    @Override
    public Object clone() {
        return new PurchasedAbility(this);
    }

    public boolean valueEquals(Object obj) {
        if (!(obj instanceof PurchasedAbility))
            return false;
        PurchasedAbility p = (PurchasedAbility) obj;
        if (!abilityName.equals(p.abilityName))
            return false;
        if (!extName.equals(p.extName))
            return false;
        if (!playerName.equals(p.playerName))
            return false;
        if (!world.equals(p.world))
            return false;
        if (duration != p.duration)
            return false;
        if (type != p.type)
            return false;
        if (!(perms.containsAll(p.perms) && p.perms.containsAll(perms)))
            return false;
        return true;
    }

    /** Given a type string, set our internal type.
     * 
     * @param typeString
     */
    public void setType(String typeString) throws BABException {
		if(typeString.equalsIgnoreCase(PurchasedAbilityType.BUY.toString()))
		{
			setType(PurchasedAbilityType.BUY);
		}
		else if (typeString.equalsIgnoreCase(PurchasedAbilityType.RENT.toString()))
		{
			setType(PurchasedAbilityType.RENT);
		}
		else if (typeString.equalsIgnoreCase(PurchasedAbilityType.USE.toString()))
		{
			setType(PurchasedAbilityType.USE);
		}
		else {
			throw new BABException("Invalid type string: "+typeString);
		}
    }
    
    /** Return the Ability object this PurchasedAbility node is associated with.
     * 
     * @return
     */
    public Ability getAssociatedAbility() {
    	return BuyAbilities.getPlugin().getSettings().getAbility(abilityName);
    }
    
	public String getAbilityName() {
		return abilityName;
	}

	public String getExtName() {
		return extName;
	}

	public Set<String> getPerms() {
		return perms;
		
//		Ability ability = getAssociatedAbility();
//		
//		if( ability == null ) {
//			throw new NullPointerException("ability is null");
//		}
//		
//		return ability.perms;
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getWorld() {
		return world;
	}

	public PurchasedAbilityType getType() {
		return type;
	}

	public int getDuration() {
		return duration;
	}

	public void setAbilityName(String abilityName) {
		this.abilityName = abilityName;
	}

	public void setExtName(String extName) {
		this.extName = extName;
	}

	public void setPerms(Set<String> perms) {
		this.perms = perms;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public void setType(PurchasedAbilityType type) {
		this.type = type;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
