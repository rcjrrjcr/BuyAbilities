/**
 * 
 */
package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;

/** NOTUSED
 * 
 * @author morganm
 *
 */
@Entity()
@Table(name="bab_permission_nodes")
public class StoredPermissionNode {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    @NotEmpty
    @Length(max=256)
	private String permission;

    /** Convert a Set<String> into a Set<StoredPermissionNode> - this is used since PurchasedAbility
     * stores permissions as Set<String>.
     * 
     * @param permission
     * @return
     */
//    static Set<StoredPermissionNode> convertStringSet(Set<String> permission) {
//    	;
//    }
    
    public StoredPermissionNode() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}
    
    
}
