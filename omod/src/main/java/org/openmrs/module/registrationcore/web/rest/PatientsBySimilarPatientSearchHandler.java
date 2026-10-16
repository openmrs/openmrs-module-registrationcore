/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.web.rest;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Component
public class PatientsBySimilarPatientSearchHandler implements SearchHandler {
	
	private static final String MATCH_SIMILAR = "matchSimilar";
	
	private static final String GIVENNAME = "givenname";
	
	private static final String FAMILYNAME = "familyname";
	
	private static final String MIDDLENAME = "middlename";
	
	private static final String GENDER = "gender";
	
	private static final String BIRTHDATE = "birthdate";
	
	private static final String ADDRESS_1 = "address1";
	
	private static final String ADDRESS_2 = "address2";
	
	private static final String CITY = "city";
	
	private static final String POSTALCODE = "postalcode";
	
	private static final String COUNTRY = "country";
	
	private static final String STATE = "state";
	
	private final SearchConfig searchConfig = new SearchConfig("registrationCoreFindSimilar", RestConstants.VERSION_1
	        + "/patient", Arrays.asList("1.8.*", "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*"),
	        Arrays.asList(new SearchQuery.Builder("Allows you to find similar patients")
	                .withRequiredParameters(MATCH_SIMILAR).withOptionalParameters(GIVENNAME, FAMILYNAME, MIDDLENAME, GENDER,
	                    BIRTHDATE, ADDRESS_1, ADDRESS_2, CITY, POSTALCODE, COUNTRY, STATE)
	                .build()));
	
	@Override
	public SearchConfig getSearchConfig() {
		return this.searchConfig;
	}
	
	@Override
	public PageableResult search(RequestContext requestContext) throws ResponseException {
		String matchSimilar = requestContext.getParameter(MATCH_SIMILAR);
		
		if (Boolean.valueOf(matchSimilar)) {
			Patient patient = generatePatientFromParams(requestContext);
			RegistrationCoreService registrationCoreService = Context.getService(RegistrationCoreService.class);
			List<Patient> patientList = new LinkedList<Patient>();
			
			List<PatientAndMatchQuality> patientAndMatchQualityList = registrationCoreService
			        .findPreciseSimilarPatients(patient, null, 2d, 10);
			for (PatientAndMatchQuality matches : patientAndMatchQualityList) {
				patientList.add(matches.getPatient());
			}
			
			return new NeedsPaging<Patient>(patientList, requestContext);
		}
		
		return new EmptySearchResult();
	}
	
	private Patient generatePatientFromParams(RequestContext requestContext) {
		Person person = new Person();
		PersonName personName = new PersonName();
		personName.setGivenName(requestContext.getParameter(GIVENNAME));
		personName.setMiddleName(requestContext.getParameter(MIDDLENAME));
		personName.setFamilyName(requestContext.getParameter(FAMILYNAME));
		person.setNames(Collections.singleton(personName));
		PersonAddress address = new PersonAddress();
		address.setAddress1(requestContext.getParameter(ADDRESS_1));
		address.setAddress2(requestContext.getParameter(ADDRESS_2));
		address.setCityVillage(requestContext.getParameter(CITY));
		address.setCountry(requestContext.getParameter(COUNTRY));
		address.setPostalCode(requestContext.getParameter(POSTALCODE));
		address.setStateProvince(requestContext.getParameter(STATE));
		person.setAddresses(Collections.singleton(address));
		person.setGender(requestContext.getParameter(GENDER));
		person.setBirthdate(getDateFromString(requestContext.getParameter(BIRTHDATE)));
		
		return new Patient(person);
	}
	
	private Date getDateFromString(String birthdate) {
		if (StringUtils.isNotBlank(birthdate)) {
			IllegalArgumentException pex = null;
			String[] supportedFormats = { "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss.SSS",
			        "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss",
			        "yyyy-MM-dd" };
			for (int i = 0; i < supportedFormats.length; i++) {
				try {
					Date date = DateTime.parse(birthdate, DateTimeFormat.forPattern(supportedFormats[i])).toDate();
					return date;
				}
				catch (IllegalArgumentException ex) {
					pex = ex;
				}
			}
			
			throw new ConversionException(
			        "Error converting date - correct format (ISO8601 Long): yyyy-MM-dd'T'HH:mm:ss.SSSZ", pex);
		}
		return null;
	}
	
}
