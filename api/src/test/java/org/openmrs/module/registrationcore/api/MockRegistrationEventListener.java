/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api;

import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;

import junit.framework.Assert;

import org.openmrs.event.MockEventListener;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

/**
 * Subclass of {@link MockEventListener} that extracts the patient registration pay load for testing
 * purposes
 */
public class MockRegistrationEventListener extends MockEventListener {
	
	private volatile String patientUuid;
	
	private volatile List<String> relationshipUuids;
	
	private volatile String registererUuid;
	
	private volatile String dateRegistered;
	
	private volatile Boolean wasAPerson = Boolean.FALSE;
	
	MockRegistrationEventListener(int expectedEventsCount) {
		super(expectedEventsCount);
	}
	
	/**
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Message message) {
		MapMessage mapMessage = (MapMessage) message;
		try {
			patientUuid = mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID);
			if (patientUuid != null) {
				relationshipUuids = (List<String>) mapMessage.getObject(RegistrationCoreConstants.KEY_RELATIONSHIP_UUIDS);
				registererUuid = mapMessage.getString(RegistrationCoreConstants.KEY_REGISTERER_UUID);
				dateRegistered = mapMessage.getString(RegistrationCoreConstants.KEY_DATE_REGISTERED);
				wasAPerson = mapMessage.getBoolean(RegistrationCoreConstants.KEY_WAS_A_PERSON);
				
				super.onMessage(message);
			}
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
	}
	
	/**
	 * @return the patientUuid
	 */
	public String getPatientUuid() {
		return patientUuid;
	}
	
	/**
	 * @return the relationshipUuids
	 */
	public List<String> getRelationshipUuids() {
		return relationshipUuids;
	}
	
	/**
	 * @return the registererUuid
	 */
	public String getRegistererUuid() {
		return registererUuid;
	}
	
	/**
	 * @return the dateRegistered
	 */
	public String getDateRegistered() {
		return dateRegistered;
	}
	
	/**
	 * @return the wasAPerson
	 */
	public Boolean getWasAPerson() {
		return wasAPerson;
	}
	
}
