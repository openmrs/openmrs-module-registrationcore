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
package org.openmrs.module.registrationcore.api;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.api.OpenmrsService;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(RegistrationCoreService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
public interface RegistrationCoreService extends OpenmrsService {
	
	/**
	 * Assigns an identifier to the patient and saves them in the database including the specified
	 * relationships
	 * 
	 * @param patient the patient to save
	 * @param relationships the relationships to save along with the patient
	 * @return the created patient
	 * @should create a patient from record with relationships
	 * @should fire an event when a patient is registered
	 * @should set wasPerson field to true for an existing person on the registration event
	 */
	public Patient registerPatient(Patient patient, List<Relationship> relationships);
}
