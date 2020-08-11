package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.api.APIException;

/**
 * Represents errors that occur during interactions with the Master Patient Index.
 */
public class MpiException extends APIException {
	
	/**
	 * General constructor to give the end user a helpful message that relates to why this error
	 * occurred.
	 * 
	 * @param s helpful message string for the end user
	 */
	public MpiException(String s) {
		super(s);
	}
	
	/**
	 * General constructor to give the end user a helpful message and to also propagate the parent
	 * error exception message.
	 * 
	 * @param s helpful message string for the end user
	 * @param throwable the parent exception cause that this MpiException is wrapping around
	 */
	public MpiException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
