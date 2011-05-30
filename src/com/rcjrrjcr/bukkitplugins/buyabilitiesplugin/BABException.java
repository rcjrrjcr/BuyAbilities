/**
 * 
 */
package com.rcjrrjcr.bukkitplugins.buyabilitiesplugin;

/** For BuyAbilities Application exceptions.
 * 
 * @author morganm
 *
 */
public class BABException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3939263456675990713L;

	/**
	 * 
	 */
	public BABException() {
	}

	/**
	 * @param message
	 */
	public BABException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public BABException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BABException(String message, Throwable cause) {
		super(message, cause);
	}

}
