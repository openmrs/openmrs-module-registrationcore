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
	@Override
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

        Query query = sessionFactory.getCurrentSession().createQuery("select givenName from PersonName where voided = 0 and " +
                "givenName like :query group by givenName having count(*) > 3 order by count(*) desc");
        query.setString("query", "%" + searchPhrase + "%");

        List<Object> rows = query.list();
        for (Object row: rows) {
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

        Query query = sessionFactory.getCurrentSession().createQuery("select familyName from PersonName where voided = 0 and " +
                "familyName like :query group by familyName having count(*) > 3 order by count(*) desc");
        query.setString("query", "%" + searchPhrase + "%");

        List<Object> rows = query.list();
        for (Object row: rows) {
            results.add((String) row);
        }

        return results;
    }
}
