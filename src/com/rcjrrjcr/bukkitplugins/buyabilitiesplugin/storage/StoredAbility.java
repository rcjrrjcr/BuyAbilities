/**
 * 
 */
package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.PurchasedAbility;

/**
 * 
 * @author morganm
 *
 */
@Entity()
@Table(name="bab")
public class StoredAbility {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    @NotEmpty
    @Length(max=32)
	private String abilityName;
    @NotEmpty
    @Length(max=32)
	private String extName;
    @NotEmpty
    @Length(max=4096)
    private String perms;
    @NotEmpty
    @Length(max=32)
	private String playerName;
    @NotEmpty
    @Length(max=32)
	private String world;
    @NotNull
    @Enumerated(EnumType.STRING)
	private PurchasedAbilityType type;
    @NotNull
    private int duration;
    
    public StoredAbility(PurchasedAbility pa) {
    	abilityName = pa.getAbilityName();
    	extName = pa.getExtName();
    	perms = new StringSetWrapper(pa.getPerms()).toString();
    	playerName = pa.getPlayerName();
    	world = pa.getWorld();
    	type = pa.getType();
    	duration = pa.getDuration();
    }
    
    public StoredAbility() {}
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAbilityName() {
		return abilityName;
	}

	public String getExtName() {
		return extName;
	}

	public String getPerms() {
		return perms;
	}
	
	public void setPerms(String perms) {
		this.perms = perms;
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
