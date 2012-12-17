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

import java.util.Set;

import org.openmrs.Person;
import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(RegistrationCoreService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface RegistrationCoreService extends OpenmrsService {
     

	/**
	 * 
	 * 
	 * @param person
	 * @param maxResults TODO
	 * @return
	 * @should find by given and family name
	 * @should find by given, middle and family name
	 * @should find by country
	 * @should find by country and city
	 * @should find by gender
	 */
	public Set<Person> searchForSimilarPeople(Person person, Integer maxResults);
}