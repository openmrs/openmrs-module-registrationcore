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

import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests {@link $ RegistrationCoreService} .
 */
public class RegistrationCoreServiceTest extends BaseModuleContextSensitiveTest {
	
	RegistrationCoreService service;
	
	PersonService personService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-names.xml");
		
		service = Context.getService(RegistrationCoreService.class);
		personService = Context.getPersonService();
	}
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(RegistrationCoreService.class));
	}
	
	/**
	 * @see RegistrationCoreService#searchForSimilarPeople(Person, Integer)
	 * @verifies find by country
	 */
	@Test
	public void searchForSimilarPeople_shouldFindByCountry() throws Exception {
		Person person = newBlankPerson();
		
		PersonAddress address = newBlankAddress();
		address.setCountry("USA");
		
		person.addAddress(address);
		
		Set<Person> results = service.searchForSimilarPeople(person, 10);
		
		Assert.assertEquals(1, results.size());
		Person result = results.iterator().next();
		Assert.assertEquals("USA", result.getPersonAddress().getCountry());
	}

	private PersonAddress newBlankAddress() {
	    PersonAddress address = new PersonAddress();
		address.setVoided(null);
	    return address;
    }
	
	/**
	 * @see RegistrationCoreService#searchForSimilarPeople(Person, Integer)
	 * @verifies find by country and city
	 */
	@Test
	public void searchForSimilarPeople_shouldFindByCountryAndCity() throws Exception {
		PersonAddress personAddress = personService.getPersonAddressByUuid("7418fe01-e7e0-4f0c-ad1b-4b4d845577f9");
		personAddress.setCountry("USA");
		personService.savePerson(personAddress.getPerson());
		
		Context.flushSession();
		
		Person person = newBlankPerson();
		
		PersonAddress address = newBlankAddress();
		address.setCountry("USA");
		//address.setCityVillage("Indianapolis");
		
		person.addAddress(address);
		
		Set<Person> results = service.searchForSimilarPeople(person, 10);
		
		Assert.assertEquals(1, results.size());
		Person result = results.iterator().next();
		Assert.assertEquals("USA", result.getPersonAddress().getCountry());
		Assert.assertEquals("Indianapolis", result.getPersonAddress().getCityVillage());
	}
	
	/**
	 * @see RegistrationCoreService#searchForSimilarPeople(Person, Integer)
	 * @verifies find by gender
	 */
	@Test
	public void searchForSimilarPeople_shouldFindByGender() throws Exception {
		Person person = newBlankPerson();
		
		person.setGender("M");
		
		Set<Person> results = service.searchForSimilarPeople(person, 10);
		
		Assert.assertEquals(10, results.size());
		for (Person result : results) {
			Assert.assertEquals("M", result.getGender());
		}
	}
	
	private Person newBlankPerson() {
		Person person = new Person();
		
		person.setVoided(null);
		person.setPersonVoided(null);
		person.setDead(null);
		person.setBirthdateEstimated(null);
		return person;
	}
	
	public static Matcher<Person> matchesPersonById(final Integer personId) {
		
		return new BaseMatcher<Person>() {
			
			protected Integer theExpected = personId;
			
			public boolean matches(Object o) {
				if (!(o instanceof Person)) {
					return false;
				}
				Person person = (Person) o;
				return personId.equals(person.getId());
			}
			
			public void describeTo(Description description) {
				description.appendText(theExpected.toString());
			}
		};
	}
	
	/**
	 * @see RegistrationCoreService#searchForSimilarPeople(Person, Integer)
	 * @verifies find by given and family name
	 */
	@Test
	public void searchForSimilarPeople_shouldFindByGivenAndFamilyName() throws Exception {
		Person person = newBlankPerson();
		
		PersonName name = newBlankName();
		person.addName(name);
		name.setGivenName("Darius");
		
		Set<Person> results = service.searchForSimilarPeople(person, 10);
		
		Assert.assertEquals(4, results.size());
		for (Person result : results) {
			Assert.assertEquals("Darius", result.getPersonName().getGivenName());
		}
		
		name.setFamilyName("Jazayeri");
		
		Set<Person> finalResults = service.searchForSimilarPeople(person, 10);
		
		Assert.assertEquals(1, finalResults.size());
		Person darius = finalResults.iterator().next();
		Assert.assertEquals("Darius", darius.getPersonName().getGivenName());
		Assert.assertEquals("Jazayeri", darius.getPersonName().getFamilyName());
	}
	
	private PersonName newBlankName() {
		PersonName name = new PersonName();
		name.setPreferred(null);
		name.setVoided(null);
		return name;
	}
}
