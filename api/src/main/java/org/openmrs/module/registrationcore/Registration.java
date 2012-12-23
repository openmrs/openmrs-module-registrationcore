/**
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationcore;

import java.util.List;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;

/**
 * An instance of this class represents a patient registration, it is merely a wrapper for a
 * {@link Patient} and their a list of {@link Relationship}s
 */
public class Registration {
	
	private Person person;
	
	private List<Relationship> relationships;
	
	private Location identifierLocation;
	
	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return the relationships
	 */
	public List<Relationship> getRelationships() {
		return relationships;
	}
	
	/**
	 * @param relationships the relationships to set
	 */
	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}
	
	/**
	 * @return the identifierLocation
	 */
	public Location getIdentifierLocation() {
		return identifierLocation;
	}
	
	/**
	 * @param identifierLocation the identifierLocation to set
	 */
	public void setIdentifierLocation(Location identifierLocation) {
		this.identifierLocation = identifierLocation;
	}
}
