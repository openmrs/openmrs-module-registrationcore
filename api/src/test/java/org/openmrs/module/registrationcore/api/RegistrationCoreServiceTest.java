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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.RegistrationData;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricData;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.biometrics.model.Fingerprint;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.NotTransactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Tests {@link RegistrationCoreService} .
 */
public class RegistrationCoreServiceTest extends RegistrationCoreSensitiveTestBase {
	
	private RegistrationCoreService service;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Autowired
	@Qualifier("personService")
	private PersonService personService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	@Autowired
	@Qualifier("locationService")
	private LocationService locationService;

	private PatientIdentifierType biometricsIdentifierType;
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/module/idgen/include/TestData.xml");
		service = Context.getService(RegistrationCoreService.class);
		adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID, "1"));

        biometricsIdentifierType = new PatientIdentifierType();
        biometricsIdentifierType.setName("Biometrics Reference Code");
        biometricsIdentifierType.setDescription("Biometrics Reference Code");
        biometricsIdentifierType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
        biometricsIdentifierType = patientService.savePatientIdentifierType(biometricsIdentifierType);
	}
	
	private Patient createBasicPatient() {
		Patient patient = new Patient();
		PersonName pName = new PersonName();
		pName.setGivenName("Demo");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		
		patient.setBirthdate(new Date());
		patient.setDeathDate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setGender("M");
		
		return patient;
	}
	
	/**
	 * @see {@link RegistrationCoreService#registerPatient(Patient, java.util.List, Location)}
	 */
	@Test
	@Verifies(value = "should create a patient from record with relationships", method = "registerPatient(Patient,List<Relationship>, Location)")
	public void registerPatient_shouldCreateAPatientFromRecordWithRelationships() throws Exception {
		Relationship r1 = new Relationship(null, personService.getPerson(2), personService.getRelationshipType(2));
		Relationship r2 = new Relationship(personService.getPerson(7), null, personService.getRelationshipType(2));
		Location location = locationService.getLocation(2);
		Patient createdPatient = service.registerPatient(createBasicPatient(), Arrays.asList(r1, r2), location);
		assertNotNull(createdPatient.getPatientId());
		assertNotNull(createdPatient.getPatientIdentifier());
		assertEquals(location, createdPatient.getPatientIdentifier().getLocation());
		assertNotNull(patientService.getPatient(createdPatient.getPatientId()));
		assertNotNull(createdPatient.getPersonId());
		assertNotNull(personService.getPerson(createdPatient.getPersonId()));
		assertEquals(2, personService.getRelationshipsByPerson(createdPatient).size());
	}

    @Test
    @Verifies(value = "should set identifier locaton to primary identifier location if tagged", method = "registerPatient(Patient,List<Relationship>, Location)")
    public void registerPatient_shouldSetProperIdentifierLocationBasedOnTag() throws Exception {

        LocationTag primaryIdentifierLocationTag = new LocationTag();
        primaryIdentifierLocationTag.setName(RegistrationCoreConstants.LOCATION_TAG_IDENTIFIER_ASSIGNMENT_LOCATION);
        locationService.saveLocationTag(primaryIdentifierLocationTag);

        Location parentLocation = locationService.getLocation(2);
        parentLocation.addTag(locationService.getLocationTagByName(RegistrationCoreConstants.LOCATION_TAG_IDENTIFIER_ASSIGNMENT_LOCATION));
        locationService.saveLocation(parentLocation);

        Location location = new Location();
        location.setName("My house");
        location.setParentLocation(parentLocation);
        locationService.saveLocation(location);

        Patient createdPatient = service.registerPatient(createBasicPatient(), null, location);
        assertEquals(parentLocation, createdPatient.getPatientIdentifier().getLocation());;
    }

    @Test
    @Verifies(value = "should create a patient with manually entered identifier", method = "registerPatient(Patient,List<Relationship>, String identifier, Location)")
    public void registerPatient_shouldCreateAPatientWithManuallyEnteredIdentifier() throws Exception {
        Location location = locationService.getLocation(2);
        Patient createdPatient = service.registerPatient(createBasicPatient(), null, "202-2", location);
        assertNotNull(createdPatient.getPatientId());
        assertNotNull(createdPatient.getPatientIdentifier());
        assertEquals("202-2", createdPatient.getPatientIdentifier().getIdentifier());
        assertEquals(location, createdPatient.getPatientIdentifier().getLocation());
        assertNotNull(patientService.getPatient(createdPatient.getPatientId()));
        assertNotNull(createdPatient.getPersonId());
        assertNotNull(personService.getPerson(createdPatient.getPersonId()));
    }

    @Test(expected = InvalidCheckDigitException.class)
    @Verifies(value = "should throw exception when invalid identifier", method = "registerPatient(Patient,List<Relationship>, String identifier, Location)")
    public void registerPatient_shouldThrowExceptionWhenInvalidIdentifier() throws Exception {
        Location location = locationService.getLocation(2);
        service.registerPatient(createBasicPatient(), null, "bah", location);
    }

	/**
	 * @see {@link RegistrationCoreService#registerPatient(Patient, List, Location)}
	 */
	@Test
	@Verifies(value = "should fire an event when a patient is registered", method = "registerPatient(Patient,List<Relationship>)")
	public void registerPatient_shouldFireAnEventWhenAPatientIsRegistered() throws Exception {
		MockRegistrationEventListener listener = new MockRegistrationEventListener(1);
		Event.subscribe(RegistrationCoreConstants.PATIENT_REGISTRATION_EVENT_TOPIC_NAME, listener);
		
		Relationship r1 = new Relationship(null, personService.getPerson(2), personService.getRelationshipType(2));
		Relationship r2 = new Relationship(personService.getPerson(7), null, personService.getRelationshipType(2));
		Patient createdPatient = service.registerPatient(createBasicPatient(), Arrays.asList(r1, r2), null);
		
		listener.waitForEvents(15, TimeUnit.SECONDS);
		
		assertEquals(createdPatient.getUuid(), listener.getPatientUuid());
		assertEquals(createdPatient.getCreator().getUuid(), listener.getRegistererUuid());
		assertEquals(
		    new SimpleDateFormat(RegistrationCoreConstants.DATE_FORMAT_STRING).format(createdPatient.getDateCreated()),
		    listener.getDateRegistered());
		assertFalse(listener.getWasAPerson());
		List<String> relationshipUuids = new ArrayList<String>();
		for (Relationship r : Arrays.asList(r1, r2)) {
			relationshipUuids.add(r.getUuid());
		}
		assertEquals(relationshipUuids, listener.getRelationshipUuids());
	}
	
	/**
	 * @see {@link RegistrationCoreService#registerPatient(Patient, List, Location)}
     * This needs to be fixed after fixing https://tickets.openmrs.org/browse/RC-9
	 */
	@Test
	@Ignore
	@Verifies(value = "should set wasPerson field to true for an existing person on the registration event", method = "registerPatient(Patient,List<Relationship>)")
	public void registerPatient_shouldSetWasPersonFieldToTrueForAnExistingPersonOnTheRegistrationEvent() throws Exception {
		MockRegistrationEventListener listener = new MockRegistrationEventListener(1);
		Event.subscribe(RegistrationCoreConstants.PATIENT_REGISTRATION_EVENT_TOPIC_NAME, listener);
		
		Relationship r1 = new Relationship(null, personService.getPerson(2), personService.getRelationshipType(2));
		Relationship r2 = new Relationship(personService.getPerson(7), null, personService.getRelationshipType(2));
		final Integer personId = 9;
		assertNull(patientService.getPatient(personId));
		Person person = personService.getPerson(personId);
		assertNotNull(person);
		service.registerPatient(createBasicPatient(), Arrays.asList(r1, r2), null);
		
		listener.waitForEvents(10, TimeUnit.SECONDS);
		assertTrue(listener.getWasAPerson());
	}

    /**
     * @see {@link RegistrationCoreService#registerPatient(org.openmrs.module.registrationcore.RegistrationData)}
     */
    @Test
    public void registerPatient_shouldNotRequireBiometrics() throws Exception {
        RegistrationData data = new RegistrationData();
        data.setPatient(createBasicPatient());
        int startPatients = getNumPatients();
        Patient p = service.registerPatient(data);
        assertEquals(0, p.getPatientIdentifiers(biometricsIdentifierType).size());
        assertEquals(startPatients+1, getNumPatients());
    }

    /**
     * @see {@link RegistrationCoreService#registerPatient(org.openmrs.module.registrationcore.RegistrationData)}
     */
    @Test
    @NotTransactional
    @Ignore // TODO: this passes when run on its own, but not in a suite
    public void registerPatient_shouldFailIfBiometricsSuppliedButNoEngineEnabled() throws Exception {
        RegistrationData data = getSampleRegistrationDataWithBiometrics();
        int numPatients = getNumPatients();
        boolean exceptionThrown = false;
        try {
            Patient p = service.registerPatient(data);
        }
        catch (IllegalStateException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertEquals(numPatients, getNumPatients()); // Make sure that transaction rolled back and no patient was created
    }

    /**
     * @see {@link RegistrationCoreService#registerPatient(org.openmrs.module.registrationcore.RegistrationData)}
     */
    @Test
    public void registerPatient_shouldSucceedIfBiometricsSuppliedAndEngineEnabled() throws Exception {
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_BIOMETRICS_IMPLEMENTATION, "testBiometricEngine"));
        RegistrationData data = getSampleRegistrationDataWithBiometrics();
        int startPatients = getNumPatients();
        Patient p = service.registerPatient(data);
        List<PatientIdentifier> identifiers = p.getPatientIdentifiers(biometricsIdentifierType);
        assertEquals(1, identifiers.size());
        assertEquals(startPatients+1, getNumPatients());
        PatientIdentifier identifier = identifiers.get(0);
        BiometricSubject subject = service.getBiometricEngine().lookup(identifier.getIdentifier());
        assertNotNull(subject);
        assertEquals(1, subject.getFingerprints().size());
        Fingerprint fingerprint = subject.getFingerprints().get(0);
        assertEquals("LEFT-INDEX", fingerprint.getType());
        assertEquals("ISO", fingerprint.getFormat());
        assertEquals("xxxyyyzzz", fingerprint.getTemplate());
    }

    private RegistrationData getSampleRegistrationDataWithBiometrics() {
        RegistrationData data = new RegistrationData();
        data.setPatient(createBasicPatient());

        BiometricSubject subject = new BiometricSubject();
        subject.addFingerprint(new Fingerprint("LEFT-INDEX", "ISO", "xxxyyyzzz"));
        data.addBiometricData(new BiometricData(subject, biometricsIdentifierType));
        return data;
    }

    private int getNumPatients() {
        List<List<Object>> results = adminService.executeSQL("select count(*) from patient", true);
        return ((Number)results.get(0).get(0)).intValue();
    }
}
