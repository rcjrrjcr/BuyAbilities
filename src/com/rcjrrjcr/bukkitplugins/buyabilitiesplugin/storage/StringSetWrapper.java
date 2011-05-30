/**
 * 
 */
package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.storage;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.logging.Logger;

import com.rcjrrjcr.bukkitplugins.buyabilitiesplugin.BuyAbilities;

/** An object which can wrap and convert from Set<String> to a String and back again.
 * @author morganm
 *
 */
public class StringSetWrapper {
    private static final Logger log = BuyAbilities.log;
    
	private Set<String> set;
	
	public StringSetWrapper(Set<String> set) {
		this.set = set;
	}
	public StringSetWrapper(String str) {
		this.set = deSerializeStringToSet(str);
	}
	
	public Set<String> getSet() { return set; }
	public String toString() { return serializeStringSet(set); }

    private String serializeStringSet(Set<String> set) {
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	XMLEncoder encoder = new XMLEncoder(os);
    	encoder.writeObject(set);
    	encoder.close();
    	
    	// check log level to avoid overhead when not in debug mode
    	if( log.isLoggable(java.util.logging.Level.FINE) )
    		log.fine("serialized XML String = "+os.toString());
    	
    	return os.toString();
    }
    
    @SuppressWarnings("unchecked")
	private Set<String> deSerializeStringToSet(String serialized) {
    	ByteArrayInputStream is = new ByteArrayInputStream(serialized.getBytes());
    	XMLDecoder decoder = new XMLDecoder(is);
    	Set<String> set = (Set<String>) decoder.readObject();
    	decoder.close();

    	// check log level to avoid overhead when not in debug mode
    	if( log.isLoggable(java.util.logging.Level.FINE) )
    		log.fine("set.size() = "+set.size()+" for deSerialized XML String = "+serialized);
    	
    	return set;
    }
}
