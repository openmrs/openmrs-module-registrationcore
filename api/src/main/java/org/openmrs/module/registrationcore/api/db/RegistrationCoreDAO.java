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
package org.openmrs.module.registrationcore.api.db;

import java.util.Set;

import org.openmrs.Person;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;

/**
 *  Database methods for {@link RegistrationCoreService}.
 */
public interface RegistrationCoreDAO {


	/**
     * Auto generated method comment
     * 
     * @param person
	 * @param maxResults TODO
     * @return
     */
    Set<Person> searchForSimilarPeople(Person person, Integer maxResults);
}