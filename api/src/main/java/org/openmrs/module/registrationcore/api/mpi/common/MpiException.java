/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
	 * General constructor to give the end user a helpful message and to also propagate the parent error
	 * exception message.
	 * 
	 * @param s helpful message string for the end user
	 * @param throwable the parent exception cause that this MpiException is wrapping around
	 */
	public MpiException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
