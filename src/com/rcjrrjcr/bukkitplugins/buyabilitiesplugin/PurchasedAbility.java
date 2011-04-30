package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

import java.util.HashSet;
import java.util.Set;


import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.settings.Ability;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage.PurchasedAbilityType;


@Entity
@Table(name="bab")
public class PurchasedAbility implements Comparable<PurchasedAbility>, Cloneable
{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    @NotEmpty
    @Length(max=32)
	public String abilityName;
    @NotEmpty
    @Length(max=32)
	public String extName;
    @NotNull
    @OneToMany
	public Set<String> perms;
    @NotEmpty
    @Length(max=32)
	public String playerName;
    @NotEmpty
    @Length(max=32)
	public String world;
    @NotNull
    @Enumerated(EnumType.STRING)
	public PurchasedAbilityType type;
    @NotNull
	public int duration;
	
	@Override
	public int compareTo(PurchasedAbility o) {
		return new Integer(duration).compareTo(o.duration);
	}
	
	@Override
	public String toString()
	{
		if(type== PurchasedAbilityType.RENT) return abilityName + ": " + extName+ " with " + new Integer(duration/20).toString() +"s left in world " + world;
		if(type== PurchasedAbilityType.BUY)  return abilityName + ": " + extName+ " in world " + world;
		if(type== PurchasedAbilityType.USE)  return abilityName + ": " + extName+ " with " + duration +" uses left in world " + world;
		return "Error: " + abilityName + "type not specified.";
	}
	public PurchasedAbility()
	{
		abilityName = new String();
		extName = new String();
		perms = new HashSet<String>();
		playerName = new String();
		world = new String();
		duration = 0;
	}
	
	public PurchasedAbility(Ability a, String playerName, String worldName, PurchasedAbilityType type)
	{
		abilityName = a.name;
		extName = a.info.extName;
		perms = a.perms;
		this.playerName = playerName;
		this.world = worldName;
		this.type = type;
		duration = 0;
		if(type == PurchasedAbilityType.RENT) duration = a.costs.rentDuration;
		if(type == PurchasedAbilityType.USE) duration = a.costs.useCount;
	}
	private PurchasedAbility(PurchasedAbility p)
	{
		this.abilityName = p.abilityName;
		this.extName = p.extName;
		this.perms = new HashSet<String>();
		this.perms.addAll(p.perms);
		this.playerName = p.playerName;
		this.world = p.world;
		this.duration = p.duration;
		this.type = p.type;
	}
	
	@Override
	public Object clone()
	{
		return new PurchasedAbility(this);
	}
	
	public boolean valueEquals(Object obj)
	{
		if(!(obj instanceof PurchasedAbility)) return false;
		PurchasedAbility p = (PurchasedAbility) obj;
		if(!abilityName.equals(p.abilityName)) return false;
		if(!extName.equals(p.extName)) return false;
		if(!playerName.equals(p.playerName)) return false;
		if(!world.equals(p.world)) return false;
		if(duration != p.duration) return false;
		if(type != p.type) return false;
		if(!(perms.containsAll(p.perms)&&p.perms.containsAll(perms)) ) return false;
		return true;
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public void setAbilityName(String abilityName) {
        this.abilityName = abilityName;
    }

    public String getExtName() {
        return extName;
    }

    public void setExtName(String extName) {
        this.extName = extName;
    }

    public Set<String> getPerms() {
        return perms;
    }

    public void setPerms(Set<String> perms) {
        this.perms = perms;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public PurchasedAbilityType getType() {
        return type;
    }

    public void setType(PurchasedAbilityType type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
	
}
