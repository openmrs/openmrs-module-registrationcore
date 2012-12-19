/**
 * The contents of this file are subject to the OpenMRS Public License
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.Relationship;

/**
 * It is a model class. It should extend either {@link BaseOpenmrsObject} or
 * {@link BaseOpenmrsMetadata}.
 */
public class RegistrationEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String patientUuid;
	
	private List<String> relationshipUuids;
	
	private String registererUuid;
	
	private Date dateRegistered;
	
	private Boolean wasAPerson = Boolean.FALSE;
	
	/**
	 * @param patient
	 */
	public RegistrationEvent(Patient patient) {
		patientUuid = patient.getUuid();
	}
	
	/**
	 * @param patient
	 * @param relationships
	 */
	public RegistrationEvent(Patient patient, List<Relationship> relationships) {
		patientUuid = patient.getUuid();
		relationshipUuids = new ArrayList<String>();
		for (Relationship relationship : relationships) {
			relationshipUuids.add(relationship.getUuid());
		}
	}
	
	/**
	 * @return the patientUuid
	 */
	public String getPatientUuid() {
		return patientUuid;
	}
	
	/**
	 * @param patientUuid the patientUuid to set
	 */
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	/**
	 * @return the relationshipUuids
	 */
	public List<String> getRelationshipUuids() {
		if (relationshipUuids == null)
			relationshipUuids = new ArrayList<String>();
		return relationshipUuids;
	}
	
	/**
	 * @param relationshipUuids the relationshipUuids to set
	 */
	public void setRelationshipUuids(List<String> relationshipUuids) {
		this.relationshipUuids = relationshipUuids;
	}
	
	/**
	 * @return the registererUuid
	 */
	public String getRegistererUuid() {
		return registererUuid;
	}
	
	/**
	 * @param registererUuid the registererUuid to set
	 */
	public void setRegistererUuid(String registererUuid) {
		this.registererUuid = registererUuid;
	}
	
	/**
	 * @return the dateRegistered
	 */
	public Date getDateRegistered() {
		return dateRegistered;
	}
	
	/**
	 * @param dateRegistered the dateRegistered to set
	 */
	public void setDateRegistered(Date dateRegistered) {
		this.dateRegistered = dateRegistered;
	}
	
	/**
	 * @return the wasAPerson
	 */
	public Boolean getWasAPerson() {
		return wasAPerson;
	}
	
	/**
	 * @param wasAPerson the wasAPerson to set
	 */
	public void setWasAPerson(Boolean wasAPerson) {
		this.wasAPerson = wasAPerson;
	}
}
