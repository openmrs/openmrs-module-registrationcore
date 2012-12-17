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
package org.openmrs.module.registrationcore.api.db.hibernate;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;

/**
 * It is a default implementation of {@link RegistrationCoreDAO}.
 */
public class HibernateRegistrationCoreDAO implements RegistrationCoreDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO#searchForSimilarPeople(org.openmrs.Person,
	 *      Integer)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<Person> searchForSimilarPeople(Person person, Integer maxResults) {
		Criteria criteria = sessionFactory
		        .getCurrentSession()
		        .createCriteria(Person.class)
		        .add(
		            Example.create(person).excludeNone().excludeZeroes().excludeProperty("isPatient")
		                    .excludeProperty("uuid").excludeProperty("personId"));
		
		if (person.getNames() != null && !person.getNames().isEmpty()) {
			Criteria namesCriteria = criteria.createCriteria("names");
			for (PersonName name : person.getNames()) {
				namesCriteria.add(Example.create(name).excludeNone().excludeZeroes().excludeProperty("uuid")
				        .excludeProperty("personNameId").excludeProperty("preferred"));
			}
		}
		
		if (person.getAddresses() != null && !person.getAddresses().isEmpty()) {
			Criteria addressesCriteria = criteria.createCriteria("addresses");
			for (PersonAddress address : person.getAddresses()) {
				addressesCriteria.add(Restrictions.eq("country", address.getCountry()));
				//addressesCriteria.add(Example.create(address).excludeNone().excludeZeroes().excludeProperty("uuid")
				//        .excludeProperty("personAddressId").excludeProperty("preferred"));
			}
		}
		
		if (maxResults != null) {
			criteria.setMaxResults(maxResults);
		}
		
		return new HashSet<Person>(criteria.list());
	}
}
