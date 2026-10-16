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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines credentials class which holds required credentials for authentication to MPi server.
 */
@XmlRootElement(name = "authenticationRequest")
public class MpiCredentials {
	
	private String username;
	
	private String password;
	
	/**
	 * Default constructor
	 */
	public MpiCredentials() {
		//Is used for XML marshalling.
	}
	
	/**
	 * Constructor with username & password
	 * 
	 * @param username
	 * @param password
	 */
	public MpiCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		MpiCredentials passedCredentials = (MpiCredentials) obj;
		return passedCredentials.getUsername() != null && passedCredentials.getUsername().equals(this.username)
		        && passedCredentials.getPassword() != null && passedCredentials.getPassword().equals(this.password);
	}
}
