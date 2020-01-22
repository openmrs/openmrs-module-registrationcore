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
