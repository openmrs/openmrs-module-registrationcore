/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * It is a default implementation of {@link RegistrationCoreDAO}.
 */
public class HibernateRegistrationCoreDAO implements RegistrationCoreDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private DbSessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the sessionFactory
	 */
	public DbSessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<String> findExistingSimilarGivenNames(String searchPhrase) {
		List<String> results = new ArrayList<String>();
		
		// don't search until we have at least three characters
		if (searchPhrase == null || searchPhrase.length() < 3) {
			return results;
		}
		
		Query query = sessionFactory.getCurrentSession().createQuery("select givenName from PersonName where voided = 0 and "
		        + "givenName like :query group by givenName having count(*) > 3 order by count(*) desc");
		query.setString("query", "%" + searchPhrase + "%");
		
		List<Object> rows = query.list();
		for (Object row : rows) {
			results.add((String) row);
		}
		
		return results;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<String> findExistingSimilarFamilyNames(String searchPhrase) {
		List<String> results = new ArrayList<String>();
		
		// don't search until we have at least three characters
		if (searchPhrase == null || searchPhrase.length() < 3) {
			return results;
		}
		
		Query query = sessionFactory.getCurrentSession()
		        .createQuery("select familyName from PersonName where voided = 0 and "
		                + "familyName like :query group by familyName having count(*) > 3 order by count(*) desc");
		query.setString("query", "%" + searchPhrase + "%");
		
		List<Object> rows = query.list();
		for (Object row : rows) {
			results.add((String) row);
		}
		
		return results;
	}
}
