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

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.api.LocationService;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
	 * relationships, the method always attempts to set the other side of each relationship
	 * therefore callers of this method are required to set exactly one side
	 * 
	 * @param patient the patient to save
	 * @param relationships the relationships to save along with the patient
	 * @param identifierLocation the location to set for the patient identifier, if not specified,
	 *            it defaults to the system default locale see
	 *            {@link LocationService#getDefaultLocation()}
	 * @return the created patient
	 * @should create a patient from record with relationships
	 * @should fire an event when a patient is registered
	 * @should set wasPerson field to true for an existing person on the registration event
	 */
	public Patient registerPatient(Patient patient, List<Relationship> relationships, Location identifierLocation);

    /**
     * Assigns an identifier to the patient and saves them in the database including the specified
     * relationships, the method always attempts to set the other side of each relationship
     * therefore callers of this method are required to set exactly one side
     *
     * @param patient the patient to save
     * @param relationships the relationships to save along with the patient
     * @param identifierLocation the location to set for the patient identifier, if not specified,
     *            it defaults to the system default locale see
     *            {@link LocationService#getDefaultLocation()}
     * @param registrationEncounterType the encounter type of the registration encounter to create
     * @param registrationDatetime the datetime of the registration encounter
     * @param registrationLocation the location of registration
     * @return the created patient
     * @should create a patient from record with relationships
     * @should fire an event when a patient is registered
     * @should set wasPerson field to true for an existing person on the registration event
     * @should create a registration encounter of the specified type at the specified location
     * @should not create an encounter if registration encounter type is null
     * @should set encounter location to system default if null (and registration encountertype not null)
     * @should set encounter datetime of registration encounter to current datetime if null (and registration encountertype not null)
     */
    public Patient registerPatient(Patient patient, List<Relationship> relationships, Location identifierLocation,
                                   EncounterType registrationEncounterType, Date registrationDatetime,
                                   Location registrationLocation);
	
	/**
	 * Returns a list of matching patients using the fast algorithm.
	 * <p>
	 * You can change the underlying implementation by calling
	 * {@link #setFastSimilarPatientSearchAlgorithm(org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm)}.
	 * 
	 * @param patient
	 * @param otherDataPoints
	 * @param cutoff
	 * @param maxResults
	 * @return the list
	 */
	List<PatientAndMatchQuality> findFastSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                     Double cutoff, Integer maxResults);
	
	/**
	 * Returns a list of matching patients using the precise algorithm.
	 * <p>
	 * You can change the underlying implementation by calling
	 * {@link #setPreciseSimilarPatientSearchAlgorithm(org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm)}.
	 * 
	 * @param patient
	 * @param otherDataPoints
	 * @param cutoff
	 * @param maxResults
	 * @return the list
	 */
	List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                        Double cutoff, Integer maxResults);
	
	/**
	 * Searches for given names that are similar to a search phrase.
	 * <p>
	 * You can change the underlying implementation by setting a global property.
	 * 
	 * @param searchPhrase the search phrase.
	 * @return list of given names that match the search phrase.
	 */
	List<String> findSimilarGivenNames(String searchPhrase);
	
	/**
	 * Searches for family names that are similar to a search phrase.
	 * <p>
	 * You can change the underlying implementation by setting a global property.
	 * 
	 * @param searchPhrase the search phrase.
	 * @return list of family names that match the search phrase.
	 */
	List<String> findSimilarFamilyNames(String searchPhrase);
}
